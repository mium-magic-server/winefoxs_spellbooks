package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import java.util.List;
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
     * 追踪弹体的构造器签名。
     */
    static final Class<?>[] CTOR_SIGNATURE = {net.minecraft.world.level.Level.class, LivingEntity.class};

    /**
     * 一个单参 setter 的反射坐标：登记自检与实际调用共用同一份，改名时不会只改到一边。
     */
    private record Setter(String name, Class<?> paramType) {
        Class<?>[] signature() {
            return new Class<?>[] {paramType};
        }
    }

    private static final Setter SHOOT = new Setter("shoot", Vec3.class);

    private static final Setter SET_DAMAGE = new Setter("setDamage", float.class);

    private static final Setter SET_HOMING_TARGET = new Setter("setHomingTarget", LivingEntity.class);

    private static final Setter SET_DURATION = new Setter("setDuration", int.class);

    private static final List<Setter> HOMING_SETTERS = List.of(SHOOT, SET_DAMAGE, SET_HOMING_TARGET, SET_DURATION);

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
            AddonReflection.declareMethod(modId, className, setter.name(), setter.signature());
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

        Vec3 vec = target.getBoundingBox().getCenter().subtract(caster.getEyePosition()).normalize();
        invoke(projectile, SHOOT, vec.scale(0.75));
        invoke(projectile, SET_DAMAGE, damage);
        invoke(projectile, SET_HOMING_TARGET, target);
        invoke(projectile, SET_DURATION, duration);

        level.addFreshEntity(entity);
    }

    private static void invoke(Object projectile, Setter setter, Object value) {
        AddonReflection.invoke(projectile, setter.name(), setter.signature(), value);
    }
}
