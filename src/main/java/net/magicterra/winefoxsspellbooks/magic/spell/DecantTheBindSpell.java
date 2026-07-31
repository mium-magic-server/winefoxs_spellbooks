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
import io.redspace.ironsspellbooks.util.ModTags;
import java.util.List;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.magic.particle.TransferStreamOption;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
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

    /**
     * 高酒龄门槛:达到该等级(即满级)改为整列净除;满级即毕业,不设无成长的溢出等级。
     */
    private static final int PURGE_LEVEL = 5;

    /**
     * 视线选取女仆的距离(格);数据生成的女仆 AI 施法距离引用同一常量。
     */
    public static final float TARGET_RANGE = 16.0f;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "decant_the_bind");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
        .setMaxLevel(PURGE_LEVEL)
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
            .filter(WinefoxHexHelper.raycastFilter(candidate -> WinefoxHexHelper.isMaidOwnedBy(candidate, entity)))
            .build();
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity maid) {
            target = maid;
        }

        // 与 Iron's CleanseSpell 同口径：净化免疫 tag 里的负面效果不碰，减半也不行
        List<MobEffectInstance> harmful = target.getActiveEffects().stream()
            .filter(inst -> inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL
                && !inst.getEffect().is(ModTags.CLEANSE_IMMUNE))
            .toList();
        int cleansed = 0;
        if (spellLevel >= PURGE_LEVEL) {
            for (MobEffectInstance inst : harmful) {
                target.removeEffect(inst.getEffect());
                cleansed++;
            }
        } else {
            // 低酒龄:减半 = 移除后按剩余时长一半重挂(MobEffectInstance 无公开时长 setter);无限时长跳过,防止 -1 被除成负数
            for (MobEffectInstance inst : harmful) {
                if (inst.isInfiniteDuration()) {
                    continue;
                }
                target.removeEffect(inst.getEffect());
                cleansed++;
                int half = inst.getDuration() / 2;
                if (half > 0) {
                    target.addEffect(new MobEffectInstance(inst.getEffect(), half, inst.getAmplifier(),
                        inst.isAmbient(), inst.isVisible(), inst.showIcon()));
                }
            }
        }
        // 施法者侧保留学派施法爆发作反馈;净化全套视觉(浊滴/清醒环/金星/酒泡)由发射器在目标身上铺设
        WinefoxHexHelper.spawnCastBurst(serverLevel, entity);
        serverLevel.sendParticles(
            new TransferStreamOption(WsbParticles.DECANT_CLEANSE.get(),
                0, target.getId(), cleansed, spellLevel >= PURGE_LEVEL ? 2 : 1),
            target.getX(), target.getY(), target.getZ(), 1, 0, 0, 0, 0);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
