package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import java.util.List;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.magic.recast.AddonReflection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 反射生成附属模组追踪弹体的公共套路。
 * <p>
 * 几个附属模组的追踪弹体形状一致：{@code (Level, LivingEntity)} 构造器 + {@code shoot/setDamage/setHomingTarget/setDuration} 四个 setter。这里收一份， 各模组的 handler 只提供类名。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
final class AddonProjectiles {
    private AddonProjectiles() {
    }

    /**
     * 追踪弹体的构造器签名。
     */
    static final Class<?>[] CTOR_SIGNATURE = {Level.class, LivingEntity.class};

    /**
     * 一个 setter 的反射坐标：登记自检与实际调用共用同一份，改名时不会只改到一边。 各附属的 handler 也用它，别把方法名再抄成裸字符串。
     */
    record Setter(String name, Class<?>... paramTypes) {
    }

    private static final Setter SHOOT = new Setter("shoot", Vec3.class);

    private static final Setter SET_DAMAGE = new Setter("setDamage", float.class);

    private static final Setter SET_HOMING_TARGET = new Setter("setHomingTarget", LivingEntity.class);

    private static final Setter SET_DURATION = new Setter("setDuration", int.class);

    private static final List<Setter> HOMING_SETTERS = List.of(SHOOT, SET_DAMAGE, SET_HOMING_TARGET, SET_DURATION);

    /**
     * 弹体初速的缩放系数，与 Iron's 本体的追踪弹保持一致。
     */
    private static final double SHOT_SPEED = 0.75;

    /**
     * 散布角的下限、上限，以及取到上限所需的距离平方（32 格）。
     */
    private static final float MIN_INACCURACY = 0.2f;

    private static final float MAX_INACCURACY = 1.4f;

    private static final double FULL_SPREAD_DISTANCE_SQR = 32.0 * 32.0;

    /**
     * 从施法者眼睛指向目标包围盒中心的初速度向量。瞄的是中心而非脚底，矮个目标才不会被打空。
     */
    static Vec3 aimVelocity(LivingEntity caster, LivingEntity target) {
        return target.getBoundingBox().getCenter().subtract(caster.getEyePosition()).normalize().scale(SHOT_SPEED);
    }

    /**
     * 目标越远散布越大，32 格开外取满。数值与 Iron's 本体的多重弹幕一致。
     */
    static float spreadInaccuracy(LivingEntity caster, LivingEntity target) {
        return (float) Mth.clampedLerp(MIN_INACCURACY, MAX_INACCURACY,
            caster.distanceToSqr(target) / FULL_SPREAD_DISTANCE_SQR);
    }

    /**
     * 把一个 setter 登记进自检。
     */
    static void declare(String modId, String className, Setter setter) {
        AddonReflection.declareMethod(modId, className, setter.name(), setter.paramTypes());
    }

    /**
     * 反射调用一个 setter。
     */
    static boolean invoke(Object target, Setter setter, Object... args) {
        return AddonReflection.invoke(target, setter.name(), setter.paramTypes(), args);
    }

    /**
     * 把一枚追踪弹体用到的全部反射成员登记进自检：构造器 + {@link #shootHoming} 会调的四个 setter。
     * <p>
     * 各模组的 {@code declareReflection} 对每个弹体类调一次。只登记类是不够的——
     * 类还在、setter 改了名的话自检照样报「通过」，而弹体会以零伤害、不追踪的样子飞出去。
     */
    static void declareHoming(String modId, String className) {
        AddonReflection.declareClass(modId, className);
        AddonReflection.declareMethod(modId, className, "<init>", CTOR_SIGNATURE);
        for (Setter setter : HOMING_SETTERS) {
            declare(modId, className, setter);
        }
    }

    /**
     * 造一枚追踪弹体并射向目标。
     * <p>
     * 四个 setter 缺哪个都不致命，故逐个调用、不因单个失败中止。
     *
     * @param className 弹体实体的全限定类名
     * @param origin    生成位置
     * @param target    追踪目标
     */
    static void shootHoming(String className, ServerLevel level, LivingEntity caster,
                            Vec3 origin, LivingEntity target, float damage, int duration) {
        Optional<Object> created = AddonReflection.construct(className, CTOR_SIGNATURE, level, caster);
        if (created.isEmpty()) {
            return;
        }
        Object projectile = created.get();
        Entity entity = (Entity) projectile;
        entity.setPos(origin);

        invoke(projectile, SHOOT, aimVelocity(caster, target));
        invoke(projectile, SET_DAMAGE, damage);
        invoke(projectile, SET_HOMING_TARGET, target);
        invoke(projectile, SET_DURATION, duration);

        level.addFreshEntity(entity);
    }
}
