package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import java.util.Optional;
import net.magicterra.winefoxsspellbooks.magic.recast.AddonReflection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
     * 追踪弹体的构造器签名，供各模组在自己的静态块里向自检登记。
     */
    static final Class<?>[] CTOR_SIGNATURE = {net.minecraft.world.level.Level.class, LivingEntity.class};

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

        Vec3 vec = target.getBoundingBox().getCenter().subtract(caster.getEyePosition()).normalize();
        AddonReflection.invoke(projectile, "shoot", new Class<?>[] {Vec3.class}, vec.scale(0.75));
        AddonReflection.invoke(projectile, "setDamage", new Class<?>[] {float.class}, damage);
        AddonReflection.invoke(projectile, "setHomingTarget", new Class<?>[] {LivingEntity.class}, target);
        AddonReflection.invoke(projectile, "setDuration", new Class<?>[] {int.class}, duration);

        level.addFreshEntity(entity);
    }
}
