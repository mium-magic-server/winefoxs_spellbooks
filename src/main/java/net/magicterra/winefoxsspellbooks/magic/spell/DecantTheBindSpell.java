package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import java.util.List;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 醒酒之礼:低酒龄减半自身或视线所指女仆的负面效果剩余时长,高酒龄整列净除。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-05
 */
public class DecantTheBindSpell extends AbstractSpell {

    /** 高酒龄门槛:达到该等级改为整列净除。 */
    private static final int PURGE_LEVEL = 5;
    /** 视线选取女仆的距离(格);数据生成的女仆 AI 施法距离引用同一常量。 */
    public static final float TARGET_RANGE = 16.0f;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "decant_the_bind");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
        .setMaxLevel(7)
        .setCooldownSeconds(20.0)
        .build();

    public DecantTheBindSpell() {
        this.baseManaCost = 30;
        this.manaCostPerLevel = 6;
        this.baseSpellPower = 1;
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

    @Override
    public int getMinLevel() {
        return 2;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        // 视线指向自家女仆则以她为目标,否则作用于自己
        LivingEntity target = entity;
        HitResult hit = RaycastBuilder.begin(level, entity)
            .range(TARGET_RANGE)
            .checkForBlocks(true)
            .bbInflation(0.35f)
            .filter(e -> e instanceof LivingEntity && WinefoxHexHelper.isMaidOwnedBy(e, entity))
            .build();
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity maid) {
            target = maid;
        }

        List<MobEffectInstance> harmful = target.getActiveEffects().stream()
            .filter(inst -> inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
            .toList();
        if (spellLevel >= PURGE_LEVEL) {
            for (MobEffectInstance inst : harmful) {
                target.removeEffect(inst.getEffect());
            }
        } else {
            // 低酒龄:减半 = 移除后按剩余时长一半重挂(MobEffectInstance 无公开时长 setter);无限时长跳过,防止 -1 被除成负数
            for (MobEffectInstance inst : harmful) {
                if (inst.isInfiniteDuration()) {
                    continue;
                }
                target.removeEffect(inst.getEffect());
                int half = inst.getDuration() / 2;
                if (half > 0) {
                    target.addEffect(new MobEffectInstance(inst.getEffect(), half, inst.getAmplifier(),
                        inst.isAmbient(), inst.isVisible(), inst.showIcon()));
                }
            }
        }
        WinefoxHexHelper.spawnCastBurst(serverLevel, target);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
