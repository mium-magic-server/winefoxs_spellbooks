package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.magic.recast.AddonReflection;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastContext;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastRegistrar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Cataclysm Spellbooks 的 recast 收尾。
 * <p>
 * 两个法术都只在<b>用尽全部 recast 次数</b>时才放大招，中途结束（超时/被反制）不触发。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public final class CataclysmRecasts {
    private CataclysmRecasts() {
    }

    public static final String MOD_ID = "cataclysm_spellbooks";

    /**
     * 骨刺穿透的骨头弹体，来自 Cataclysm 本体（不是 spellbooks 附属），故 mod id 不同。
     */
    private static final String CATACLYSM_MOD_ID = "cataclysm";

    private static final String BLAZING_BONE = "com.github.L_Ender.cataclysm.entity.projectile.Blazing_Bone_Entity";

    private static final String ABYSS_FIREBALL = "com.github.L_Ender.cataclysm.entity.projectile.Ignis_Abyss_Fireball_Entity";

    private static final Class<?>[] BONE_CTOR =
        {Level.class, float.class, LivingEntity.class};

    private static final Class<?>[] FIREBALL_CTOR =
        {Level.class, LivingEntity.class};

    private static final AddonProjectiles.Setter SET_NO_GRAVITY =
        new AddonProjectiles.Setter("setNoGravity", boolean.class);

    private static final AddonProjectiles.Setter BONE_SHOOT =
        new AddonProjectiles.Setter("shoot", double.class, double.class, double.class, float.class, float.class);

    private static final AddonProjectiles.Setter SHOOT_FROM_ROTATION = new AddonProjectiles.Setter(
        "shootFromRotation", Entity.class, float.class, float.class, float.class, float.class, float.class);

    /**
     * 向外发射的骨头数量，均分一整圈。
     */
    private static final int BONE_COUNT = 8;

    /**
     * 登记本模组的收尾动作与反射目标。
     */
    public static void contribute(RecastRegistrar registrar) {
        AddonReflection.declareClass(CATACLYSM_MOD_ID, BLAZING_BONE);
        AddonReflection.declareMethod(CATACLYSM_MOD_ID, BLAZING_BONE, "<init>", BONE_CTOR);
        AddonProjectiles.declare(CATACLYSM_MOD_ID, BLAZING_BONE, SET_NO_GRAVITY);
        AddonProjectiles.declare(CATACLYSM_MOD_ID, BLAZING_BONE, BONE_SHOOT);

        AddonReflection.declareClass(CATACLYSM_MOD_ID, ABYSS_FIREBALL);
        AddonReflection.declareMethod(CATACLYSM_MOD_ID, ABYSS_FIREBALL, "<init>", FIREBALL_CTOR);
        AddonProjectiles.declare(CATACLYSM_MOD_ID, ABYSS_FIREBALL, SHOOT_FROM_ROTATION);

        registrar.register("piercing_bone", CataclysmRecasts::bonePierce);
        registrar.register("abyss_fireball", CataclysmRecasts::abyssFireball);
    }

    /**
     * BONE_PIERCE（骨刺穿透）：用尽 recast 次数时向八个方向撒一圈骨头。
     */
    public static void bonePierce(RecastContext ctx) {
        if (ctx.result() == RecastResult.USED_ALL_RECASTS) {
            spreadBoneShoot(ctx.level(), ctx.caster());
        }
        ctx.addCooldown();
    }

    /**
     * ABYSS_FIREBALL（深渊火球）：用尽 recast 次数时朝视线方向丢一发深渊火球。
     */
    public static void abyssFireball(RecastContext ctx) {
        if (ctx.result() == RecastResult.USED_ALL_RECASTS) {
            shootAbyssFireball(ctx.level(), ctx.caster());
        }
        ctx.addCooldown();
    }

    private static void spreadBoneShoot(ServerLevel level, LivingEntity caster) {
        caster.playSound(SoundEvents.DROWNED_SHOOT, 1.0f, 0.75f);

        for (int i = 0; i < BONE_COUNT; i++) {
            float throwAngle = (float) ((i * Math.PI) / (BONE_COUNT / 2.0));
            double casterX = caster.getX() + Mth.cos(throwAngle);
            double casterY = caster.getY() + (caster.getBbHeight() * 0.62);
            double casterZ = caster.getZ() + Mth.sin(throwAngle);
            double angleX = Mth.cos(throwAngle);
            double angleZ = Mth.sin(throwAngle);

            Optional<Object> created = AddonReflection.construct(BLAZING_BONE, BONE_CTOR, level, 3.0f, caster);
            if (created.isEmpty()) {
                // 类都拿不到，后面几发也一样，直接收工（启动自检已经报过 WARN）
                return;
            }
            Object bone = created.get();
            Entity entity = (Entity) bone;
            entity.moveTo(casterX, casterY, casterZ, i * (360.0f / BONE_COUNT), caster.getXRot());

            AddonProjectiles.invoke(bone, SET_NO_GRAVITY, true);
            AddonProjectiles.invoke(bone, BONE_SHOOT, angleX, 0.2, angleZ, 0.5f, 1.0f);

            level.addFreshEntity(entity);
        }
    }

    private static void shootAbyssFireball(ServerLevel level, LivingEntity caster) {
        Optional<Object> created = AddonReflection.construct(ABYSS_FIREBALL, FIREBALL_CTOR, level, caster);
        if (created.isEmpty()) {
            return;
        }
        Object fireball = created.get();
        Entity entity = (Entity) fireball;
        entity.setPos(caster.position().add(0,
            caster.getEyeHeight() - (entity.getBoundingBox().getYsize() * 0.5), 0));

        AddonProjectiles.invoke(fireball, SHOOT_FROM_ROTATION,
            caster, caster.getXRot(), caster.getYHeadRot(), 0.0f, 1.0f, 1.0f);

        level.addFreshEntity(entity);
    }
}
