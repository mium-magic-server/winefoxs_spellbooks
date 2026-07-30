package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import java.util.List;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 残月披风:给自己和附近自家女仆同时套上吸收护盾与短暂隐身,用于战斗中紧急脱险。
 * <p>
 * <b>全程不产生任何粒子</b>:施法不放学派爆发粒子,两个效果也都以 {@code visible=false} 挂上
 * （{@code showIcon} 保留,HUD 图标照常显示）。隐身脱险的法术若冒粒子等于自曝方位,与法术目的相悖。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-05
 */
public class CrescentMantleSpell extends AbstractSpell {

    /**
     * 女仆搜索半径(格);数据生成的女仆 AI 施法距离引用同一常量。
     */
    public static final double MAID_RADIUS = 6.0;

    /**
     * 吸收基础持续 4s（3 级）；每级 +0.5s。
     */
    private static final int ABSORPTION_TICKS = 80;

    /**
     * 隐身基础持续 3s（3 级）；每级 +0.5s。
     */
    private static final int INVISIBILITY_TICKS = 60;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "crescent_mantle");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.UNCOMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
        .setMaxLevel(8)
        .setCooldownSeconds(30.0)
        .build();

    public CrescentMantleSpell() {
        this.baseManaCost = 40;
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    /**
     * 施法动画与 Iron's 橡肤(oakskin)一致:瞬发自体施法。
     */
    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
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
    public int getMinLevel() {
        return 3;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
            Component.translatable("ui.irons_spellbooks.effect_length",
                Utils.timeFromTicks(ABSORPTION_TICKS + levelBonusTicks(spellLevel), 1)),
            Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(MAID_RADIUS, 1)));
    }

    /**
     * 时长成长：自起始等级（3 级）起每级 +0.5s，吸收与隐身共用。
     */
    private int levelBonusTicks(int spellLevel) {
        return 10 * (spellLevel - getMinLevel());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        // 吸收等级随法术等级缓慢成长:Lv3-5 → II,Lv6-8 → III;时长每级 +0.5s
        int absorptionAmp = spellLevel >= 6 ? 2 : 1;
        int bonusTicks = levelBonusTicks(spellLevel);
        applyMantle(entity, absorptionAmp, bonusTicks);
        // 附近自家女仆一并披上
        level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(MAID_RADIUS),
                e -> e != entity && WinefoxHexHelper.isMaidOwnedBy(e, entity))
            .forEach(maid -> applyMantle(maid, absorptionAmp, bonusTicks));
        // 刻意不调 spawnCastBurst:这是脱险法术,施法粒子会当场暴露位置
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /**
     * 两个效果统一以 {@code (ambient=false, visible=false, showIcon=true)} 挂上。 {@code visible=false} 是关粒子的开关——原版按 {@code MobEffectInstance#isVisible} 过滤要同步给 客户端的效果粒子；{@code showIcon=true} 让 HUD 图标照常显示，玩家仍看得到剩余时间。
     */
    private static void applyMantle(LivingEntity target, int absorptionAmp, int bonusTicks) {
        target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
            ABSORPTION_TICKS + bonusTicks, absorptionAmp, false, false, true));
        // Iron's 真实隐身:装备一并隐去且脱离仇恨,标志位与 Iron's InvisibilitySpell 一致
        target.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY,
            INVISIBILITY_TICKS + bonusTicks, 0, false, false, true));
    }
}
