package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.api.util.Utils;
import java.util.List;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.spells.TipsyMistCloud;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 醉狐之雾:在准星前方落地一片圆形紫粉雾({@link TipsyMistCloud}),
 * 雾内非自家女仆的实体持续被灌上微醺,自家女仆穿行其中不受影响。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-06
 */
public class TipsyMistSpell extends AbstractSpell {

    /** 落点选取射程(格);数据生成的女仆 AI 施法距离引用同一常量。 */
    public static final float CAST_RANGE = 24.0f;
    /** 雾团半径(格)。 */
    private static final float MIST_RADIUS = 5.0f;
    /** 雾团持续时间(6s)。 */
    private static final int MIST_DURATION_TICKS = 120;
    /** 高酒龄门槛:达到该等级微醺叠加至 II。 */
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
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
            Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(MIST_DURATION_TICKS, 1)),
            Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(MIST_RADIUS, 1)));
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        // 落点:视线命中生物则落在其脚下,否则落在视线终点(贴地面);借用 Iron's PoisonSplashSpell 的选点写法
        HitResult raycast = RaycastBuilder.begin(level, entity).range(CAST_RANGE).checkForBlocks(true).build();
        Vec3 spawn = raycast.getType() == HitResult.Type.ENTITY
            ? ((EntityHitResult) raycast).getEntity().position()
            : Utils.moveToRelativeGroundLevel(level,
                raycast.getLocation().subtract(entity.getForward().normalize()), 5);

        TipsyMistCloud mist = new TipsyMistCloud(level);
        mist.setOwner(entity);
        mist.setRadius(MIST_RADIUS);
        mist.setDuration(MIST_DURATION_TICKS);
        mist.setTipsyAmplifier(spellLevel >= AMPLIFIER_LEVEL ? 1 : 0);
        mist.moveTo(spawn);
        level.addFreshEntity(mist);

        WinefoxHexHelper.spawnCastBurst(serverLevel, entity);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
