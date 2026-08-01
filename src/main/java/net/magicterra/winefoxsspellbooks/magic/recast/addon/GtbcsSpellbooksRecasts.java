package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.magicterra.winefoxsspellbooks.magic.recast.AddonReflection;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastContext;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastRegistrar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * GameTechBC's Spellbooks 的 recast 收尾。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-08-01
 */
public final class GtbcsSpellbooksRecasts {
    private GtbcsSpellbooksRecasts() {
    }

    public static final String MOD_ID = "gametechbcs_spellbooks";

    private static final String PSYCHIC_BOLT =
        "com.gametechbc.gametechbcsspellbooks.entity.spells.psychic_bolt.PsychicBoltProjectile";

    /**
     * {@code shoot(Vec3, float)} 是该弹体自己加的重载，父类只有单参版；走单参版会丢掉散布。
     */
    private static final AddonProjectiles.Setter SHOOT =
        new AddonProjectiles.Setter("shoot", Vec3.class, float.class);

    private static final AddonProjectiles.Setter SET_EFFECT_DURATION =
        new AddonProjectiles.Setter("setEffectDuration", int.class);

    /**
     * 镜头震动的持续 tick 与生效半径。
     */
    private static final int SHAKE_DURATION = 20;

    private static final float SHAKE_RADIUS = 8.0f;

    /**
     * 登记本模组的收尾动作与反射目标。
     */
    public static void contribute(RecastRegistrar registrar) {
        // 不走 AddonProjectiles.declareHoming：那套登记的是 shoot(Vec3)/setDamage/setDuration，
        // 这枚弹体只有 shoot(Vec3, float) 与 setEffectDuration，套用会误报 setDuration 缺失
        AddonReflection.declareClass(MOD_ID, PSYCHIC_BOLT);
        AddonReflection.declareMethod(MOD_ID, PSYCHIC_BOLT, "<init>", AddonProjectiles.CTOR_SIGNATURE);
        AddonProjectiles.declare(MOD_ID, PSYCHIC_BOLT, SHOOT);
        AddonProjectiles.declare(MOD_ID, PSYCHIC_BOLT, SET_EFFECT_DURATION);

        registrar.register("psychic_bolt", GtbcsSpellbooksRecasts::psychicBolt);
    }

    /**
     * PSYCHIC_BOLT（精神冲击）：震屏咆哮一声，向咏唱期间标记的每个目标各射一发追踪精神弹。
     * <p>
     * 原实现不看结束原因，超时/被反制照样发射，这里保持一致。
     */
    public static void psychicBolt(RecastContext ctx) {
        ctx.addCooldown();
        if (!ctx.isMultiTargetCast()) {
            return;
        }

        LivingEntity caster = ctx.caster();
        ServerLevel level = ctx.level();
        Vec3 origin = ctx.muzzle();
        // 精神控制时长：5 秒打底，法术强度每 1 点再加 5 秒
        int effectDuration = (int) (100.0f + (ctx.spellPower() * 100.0f));

        // 用带维度的构造器：不带维度的那个已标记废弃，会把震动一律记到主世界并刷警告
        CameraShakeManager.addCameraShake(new CameraShakeData(level, SHAKE_DURATION, caster.position(), SHAKE_RADIUS));
        ctx.playAtMuzzle(SoundEvents.WARDEN_ROAR, 1.5f);

        for (LivingEntity target : ctx.markedTargets()) {
            if (!shootBolt(level, caster, origin, target, effectDuration)) {
                // 类都拿不到，后面几发也一样，直接收工（启动自检已经报过 WARN）
                return;
            }
        }
    }

    private static boolean shootBolt(ServerLevel level, LivingEntity caster, Vec3 origin, LivingEntity target,
                                     int effectDuration) {
        // 弹体继承 Iron's 的 AbstractMagicProjectile，位置与追踪目标是编译期可见的成员，直接调
        if (!(AddonReflection.construct(PSYCHIC_BOLT, AddonProjectiles.CTOR_SIGNATURE, level, caster)
            .orElse(null) instanceof AbstractMagicProjectile bolt)) {
            return false;
        }
        bolt.setPos(origin.subtract(0.0, bolt.getBbHeight(), 0.0));

        AddonProjectiles.invoke(bolt, SHOOT,
            AddonProjectiles.aimVelocity(caster, target), AddonProjectiles.spreadInaccuracy(caster, target));
        AddonProjectiles.invoke(bolt, SET_EFFECT_DURATION, effectDuration);
        bolt.setHomingTarget(target);

        level.addFreshEntity(bolt);
        return true;
    }
}
