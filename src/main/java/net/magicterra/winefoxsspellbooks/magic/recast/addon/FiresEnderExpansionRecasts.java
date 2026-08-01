package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.MultiTargetEntityCastData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.magic.recast.AddonReflection;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastContext;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastRegistrar;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Fires Ender Expansion 的 recast 收尾。三个法术都要生成该模组自己的实体，只能反射拿类。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public final class FiresEnderExpansionRecasts {
    private FiresEnderExpansionRecasts() {
    }

    public static final String MOD_ID = "firesenderexpansion";

    private static final String NOVA_STAR = "net.fireofpower.firesenderexpansion.entities.spells.BinaryStars.NovaStar.NovaStarEntity";

    private static final String OBSIDIAN_STAR = "net.fireofpower.firesenderexpansion.entities.spells.BinaryStars.ObsidianStar.ObsidianStarEntity";

    private static final String HOLLOW_CRYSTAL = "net.fireofpower.firesenderexpansion.entities.spells.HollowCrystal.HollowCrystal";

    /**
     * 闪烁步伐结束时要摘掉的效果 id。注册名带 {@code _effect} 后缀（见该模组的 {@code EffectRegistry}），漏掉的话查表恒为空。
     */
    private static final String STRIDING_EFFECT = MOD_ID + ":striding_effect";

    /**
     * 空心水晶的蓄力效果 id，没有它就不发射。层数也从施法者身上的这层效果读。同样带 {@code _effect} 后缀。
     */
    private static final String HOLLOW_CRYSTAL_EFFECT = MOD_ID + ":hollow_crystal_effect";

    private static final AddonProjectiles.Setter SET_DAMAGE =
        new AddonProjectiles.Setter("setDamage", float.class);

    private static final AddonProjectiles.Setter SHOOT =
        new AddonProjectiles.Setter("shoot", Vec3.class);

    /**
     * 登记本模组的收尾动作与反射目标。
     */
    public static void contribute(RecastRegistrar registrar) {
        // 双星走 AddonProjectiles.shootHoming，构造器与四个 setter 都要登记，只登记类查不出 setter 改名
        AddonProjectiles.declareHoming(MOD_ID, NOVA_STAR);
        AddonProjectiles.declareHoming(MOD_ID, OBSIDIAN_STAR);
        AddonReflection.declareClass(MOD_ID, HOLLOW_CRYSTAL);
        AddonReflection.declareMethod(MOD_ID, HOLLOW_CRYSTAL, "<init>", AddonProjectiles.CTOR_SIGNATURE);
        AddonProjectiles.declare(MOD_ID, HOLLOW_CRYSTAL, SET_DAMAGE);
        AddonProjectiles.declare(MOD_ID, HOLLOW_CRYSTAL, SHOOT);

        registrar.register("scintillating_stride", FiresEnderExpansionRecasts::scintillatingStride);
        registrar.register("binary_stars", FiresEnderExpansionRecasts::binaryStars);
        registrar.register("hollow_crystal", FiresEnderExpansionRecasts::hollowCrystal);
    }

    /**
     * SCINTILLATING_STRIDE（闪烁步伐）：冲刺结束时原地爆发一次范围伤害，并摘掉冲刺效果。
     */
    public static void scintillatingStride(RecastContext ctx) {
        ctx.addCooldown();
        if (!ctx.result().isSuccess()) {
            return;
        }

        LivingEntity caster = ctx.caster();
        ServerLevel level = ctx.level();
        float spellPower = ctx.spellPower();
        float damage = (spellPower * 0.5f) + 5.0f;
        float radius = 2.0f + (spellPower / 10.0f);

        for (int i = 0; i < 20; i++) {
            level.sendParticles(
                (SimpleParticleType) ParticleRegistry.UNSTABLE_ENDER_PARTICLE.get(),
                caster.getX(), caster.getY() + 1.0, caster.getZ(),
                20, 0.0, 0.0, 0.0, 0.3
            );
        }

        MagicManager.spawnParticles(
            level,
            new BlastwaveParticleOptions(WsbSchools.WINEFOX_HEX.get().getTargetingColor(), radius),
            caster.position().x, caster.position().y, caster.position().z,
            1, 0.0, 0.0, 0.0, 0.0, true
        );

        // AoE 波及走【阵营敌我】的统一谓词，勿手写
        DamageSource damageSource = ctx.spell().getDamageSource(caster);
        level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius),
                e -> WinefoxHexHelper.isFactionHostile(e, caster))
            .forEach(e -> DamageSources.applyDamage(e, damage, damageSource));

        AddonEffects.remove(caster, STRIDING_EFFECT);
    }

    /**
     * BINARY_STARS（双星）：向标记的目标各发射一颗追踪星体。
     */
    public static void binaryStars(RecastContext ctx) {
        ctx.addCooldown();
        if (!ctx.result().isSuccess()) {
            return;
        }
        if (!(ctx.castData() instanceof MultiTargetEntityCastData targetingData)) {
            return;
        }

        LivingEntity caster = ctx.caster();
        ServerLevel level = ctx.level();
        float spellPower = ctx.spellPower();
        float damage = (spellPower * 0.15f) + 5.0f;
        int duration = (int) spellPower + 100;

        List<UUID> targets = targetingData.getTargets();

        double cosPsi = Math.cos(Math.toRadians(caster.getYRot()));
        double sinPsi = Math.sin(Math.toRadians(caster.getYRot()));
        double cosTheta = Math.cos(Math.toRadians(caster.getXRot()));
        double sinTheta = Math.sin(Math.toRadians(caster.getXRot()));

        for (int i = 0; i < targets.size(); i++) {
            if (!(level.getEntity(targets.get(i)) instanceof LivingEntity target)) {
                continue;
            }

            // 计算发射位置：单目标或首个目标走右侧，第二个走左侧
            double offsetX = (i == 0 || targets.size() == 1) ? 1.0 : -1.0;
            Vec3 origin = caster.position().add(
                (offsetX * cosPsi) - ((2.0 * sinTheta) * sinPsi),
                2.0 * cosTheta,
                (offsetX * sinPsi) + (2.0 * sinTheta * cosPsi)
            );

            String entityClassName = (i == 0 || targets.size() == 1) ? NOVA_STAR : OBSIDIAN_STAR;
            AddonProjectiles.shootHoming(entityClassName, level, caster, origin, target, damage, duration);
        }
    }

    /**
     * HOLLOW_CRYSTAL（空心水晶）：把蓄好的水晶朝视线方向丢出去。
     * <p>
     * 原版用 Timer 延迟执行，这里简化为立即执行。
     */
    public static void hollowCrystal(RecastContext ctx) {
        ctx.addCooldown();
        if (!ctx.result().isSuccess()) {
            return;
        }
        LivingEntity caster = ctx.caster();
        // 没有蓄力效果就没有可发射的水晶
        if (!AddonEffects.has(caster, HOLLOW_CRYSTAL_EFFECT)) {
            return;
        }

        ServerLevel level = ctx.level();
        Vec3 lookDir = caster.getLookAngle();

        Optional<Object> created = AddonReflection.construct(HOLLOW_CRYSTAL,
            AddonProjectiles.CTOR_SIGNATURE, level, caster);
        if (created.isEmpty()) {
            return;
        }
        Object crystal = created.get();
        Entity entity = (Entity) crystal;

        double yOffset = caster.getEyeHeight() + (entity.getBoundingBox().getYsize() * 0.25) - 3.0;
        entity.setPos(caster.position().add(0, yOffset, 0).add(caster.getForward().multiply(3.0, 3.0, 3.0)));

        AddonProjectiles.invoke(crystal, SET_DAMAGE, hollowCrystalDamage(ctx));
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5, 0.5, 0.5));
        AddonProjectiles.invoke(crystal, SHOOT, lookDir);

        AddonEffects.remove(caster, HOLLOW_CRYSTAL_EFFECT);
        level.addFreshEntity(entity);
        level.playSound(null, caster, SoundRegistry.SONIC_BOOM.get(), SoundSource.PLAYERS, 3.0f, 1.0f);
    }

    /**
     * 空心水晶伤害：按蓄力效果的层数放大；效果不在身上时退回一层的伤害。
     */
    private static float hollowCrystalDamage(RecastContext ctx) {
        // 用真实施法等级，附属自己的 HollowCrystalSpell#getDamage 也是这么算的
        float base = ctx.spellPower();
        int amplifier = AddonEffects.amplifier(ctx.caster(), HOLLOW_CRYSTAL_EFFECT);
        float stacks = amplifier < 0 ? 1.0f : amplifier;
        return ((stacks * 15.0f) * base) / 50.0f;
    }
}
