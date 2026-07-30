package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.magicterra.winefoxsspellbooks.registry.WsbSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 狐影伏击 Foxshade Ambush 法术。
 * <p>
 * 瞬发自增益：给施法者挂上 {@link WsbEffects#FOXSHADE_AMBUSH} 标记。持有标记期间，施法者 （玩家或女仆）首次对敌人造成任意伤害时，由 {@link net.magicterra.winefoxsspellbooks.event.FoxshadeAmbushHandler} 在目标处召出一只狐影撞咬一口（酒狐巫法学派伤害），随即标记消失；标记内未出手则自然到期。
 * <p>
 * 咬击打的是真正的 WH 学派伤害，因此命中带「残月烙印」的敌人会触发引爆。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-23
 */
public class FoxshadeAmbushSpell extends AbstractSpell {

    /**
     * 标记持续时长(ticks)：10s 内未出手则自然到期消失。
     */
    public static final int EFFECT_DURATION = 200;

    /**
     * 咬击伤害系数：咬击伤害 = 挂效时法术强度 × 此系数。
     */
    public static final float BITE_DAMAGE_MULTIPLIER = 1.0f;

    /**
     * 女仆 AI 施法距离(格)：本法术自增益不需精确瞄准，取中距，令女仆进入交战范围即预挂伏击。
     */
    public static final float MAID_CAST_RANGE = 12.0f;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "foxshade_ambush");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
        .setMaxLevel(4)
        .setCooldownSeconds(8.0)
        .build();

    public FoxshadeAmbushSpell() {
        this.baseManaCost = 20;
        this.manaCostPerLevel = 8;
        // 法术强度即咬击伤害基数：经 amplifier 传入效果，触发时读出换算
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
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

    /**
     * 施法音：低沉的「上膛」感。
     * <p>
     * 本法术施法当下什么都不发生，效果要到下次命中敌人时才兑现， 因此刻意不用明亮的爆发音——真正的兑现时刻是扑咬狐现身。
     */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(WsbSounds.FOXSHADE_AMBUSH_CAST.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        entity.addEffect(new MobEffectInstance(
            WsbEffects.FOXSHADE_AMBUSH,
            EFFECT_DURATION,
            Math.max(0, Math.round(getSpellPower(spellLevel, entity)))
        ));
        WinefoxHexHelper.spawnCastBurst(serverLevel, entity);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
