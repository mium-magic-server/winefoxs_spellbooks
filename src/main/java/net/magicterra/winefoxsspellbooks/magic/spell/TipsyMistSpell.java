package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import java.util.List;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.cloud.TipsyMistCloud;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.magicterra.winefoxsspellbooks.registry.WsbSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 醉狐之雾:在准星前方落地一片圆形粉雾({@link TipsyMistCloud}), 雾内非自家女仆的实体持续被灌上微醺,自家女仆穿行其中不受影响。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-06
 */
public class TipsyMistSpell extends AbstractSpell {

    /**
     * 落点选取射程(格);数据生成的女仆 AI 施法距离引用同一常量。
     */
    public static final float CAST_RANGE = 24.0f;

    /**
     * 雾团半径(格)。
     */
    private static final float MIST_RADIUS = 5.0f;

    /**
     * 雾团基础持续时间(6s)；每级 +0.5s，见 {@link #getMistDuration}。
     */
    private static final int MIST_DURATION_TICKS = 120;

    /**
     * 高酒龄门槛:达到该等级微醺叠加至 II。
     */
    private static final int AMPLIFIER_LEVEL = 5;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "tipsy_mist");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
        .setMaxLevel(8)
        .setCooldownSeconds(12.0)
        .build();

    public TipsyMistSpell() {
        this.baseManaCost = 25;
        this.manaCostPerLevel = 7;
        // 与 Iron's 治愈之环(healing_circle)同款施法方式:1s 咏唱后落雾
        this.castTime = 20;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    /**
     * 施法动画与 Iron's 治愈之环一致:双手举顶咏唱。
     */
    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_OVERHEAD;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    /**
     * 咏唱音：借用 Iron's 治愈之环的 {@code loop.cloud_of_regen}，只在起手播一次。
     * <p>
     * 该素材两端带淡入淡出，直接循环会在接缝处掉到近乎静音，不要改成循环床。
     */
    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.CLOUD_OF_REGEN_LOOP.get());
    }

    /**
     * 落地音：低频一记「噗」，延后 0.16 s 接弥散气声散开。
     * <p>
     * 本法术是 LONG（20t），此钩子只在咏唱结束时触发一次。雾团存续期间的环境音是另一条 （{@code ambient.tipsy_mist}，由 {@code client.sound.TipsyMistCloudSound} 循环播放）。
     */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(WsbSounds.TIPSY_MIST_BURST.get());
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
            Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getMistDuration(spellLevel), 1)),
            Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(MIST_RADIUS, 1)));
    }

    /**
     * 雾团持续时间：基础 6s，每级 +0.5s（8 级 9.5s）。
     */
    private static int getMistDuration(int spellLevel) {
        return MIST_DURATION_TICKS + 10 * (spellLevel - 1);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        // 落点:视线命中目标则落在其脚下,否则落在视线终点(贴地面);借用 Iron's PoisonSplashSpell 的选点写法。
        // 射线必须过滤:不然挡在身前的自家女仆会把雾团吸到脚下,真正的敌人一点没沾上。
        // 玩家用准星判据(套住猪也照样落在它脚下),女仆没有准星,只认 AI 指派的交战对象(见 castRaycastFilter)
        HitResult raycast = RaycastBuilder.begin(level, entity)
            .range(CAST_RANGE)
            .checkForBlocks(true)
            .filter(WinefoxHexHelper.castRaycastFilter(entity))
            .build();
        Vec3 spawn = raycast.getType() == HitResult.Type.ENTITY
                     ? ((EntityHitResult) raycast).getEntity().position()
                     : Utils.moveToRelativeGroundLevel(level,
            raycast.getLocation().subtract(entity.getForward().normalize()), 5);

        TipsyMistCloud mist = new TipsyMistCloud(level);
        mist.setOwner(entity);
        mist.setRadius(MIST_RADIUS);
        mist.setDuration(getMistDuration(spellLevel));
        mist.setTipsyAmplifier(spellLevel >= AMPLIFIER_LEVEL ? 1 : 0);
        mist.moveTo(spawn);
        level.addFreshEntity(mist);

        WinefoxHexHelper.spawnCastBurst(serverLevel, entity);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
