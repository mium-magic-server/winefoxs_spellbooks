package net.magicterra.winefoxsspellbooks.magic;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 为已知的直线、无重力弹体计算释放瞬间的拦截点；运动不稳定时缩短速度外推的持续时间。
 *
 * @author Gardel
 * @since 2026-10-23
 */
public final class SpellAimPredictor {
    private static final int HISTORY_SIZE = 4;

    private static final double MAX_FLIGHT_TICKS = 20;

    @Nullable
    private LivingEntity trackedTarget;

    private Vec3 lastPosition = Vec3.ZERO;

    private long lastTick = Long.MIN_VALUE;

    private final Vec3[] velocities = new Vec3[HISTORY_SIZE];

    private int samples;

    private int nextSample;

    /**
     * 空闲时也采样，避免每次起手重新积累历史；不使用可能滞后的实体 deltaMovement。
     */
    public void observe(@Nullable LivingEntity target, long tick) {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            trackedTarget = null;
            samples = 0;
            lastTick = Long.MIN_VALUE;
            return;
        }
        Vec3 position = target.position();
        if (trackedTarget != target || tick < lastTick || tick - lastTick > 1) {
            trackedTarget = target;
            samples = 0;
        } else if (tick == lastTick) {
            return;
        } else {
            Vec3 velocity = position.subtract(lastPosition);
            if (velocity.lengthSqr() > 16 || !finite(velocity)) {
                samples = 0;
            } else {
                velocities[nextSample] = velocity;
                nextSample = (nextSample + 1) % HISTORY_SIZE;
                samples = Math.min(samples + 1, HISTORY_SIZE);
            }
        }
        lastPosition = position;
        lastTick = tick;
    }

    /**
     * 返回可用的提前瞄准点。只在真正 onCast 前调用，不把已经过去的读条时间再算进飞行时间。
     */
    @Nullable
    public Vec3 predict(LivingEntity caster, @Nullable LivingEntity target, AbstractSpell spell, long tick) {
        double speed;
        double spawnForward;
        // Iron's 3.16.2 的这两种弹体速度固定，shoot 不叠加施法者自身速度。
        // 未知弹道、光线、追踪弹等继续使用各自原来的瞄准方式。
        if (spell == SpellRegistry.MAGIC_MISSILE_SPELL.get()) {
            speed = 2.5;
            spawnForward = 0;
        } else if (spell == SpellRegistry.FIREBALL_SPELL.get()) {
            speed = 1.15;
            spawnForward = 1;
        } else {
            return null;
        }
        if (target == null || target != trackedTarget || !target.isAlive() || target.isRemoved()
            || tick != lastTick || samples < 3 || target.position().distanceToSqr(lastPosition) > 16) {
            return null;
        }
        Vec3 velocity = velocities[(nextSample + HISTORY_SIZE - 1) % HISTORY_SIZE];
        if (velocity.lengthSqr() < 1.0E-6) {
            return null;
        }
        Vec3 origin = caster.getEyePosition();
        Vec3 currentAim = target.getEyePosition();
        double flightTicks = interceptTicks(currentAim.subtract(origin), velocity, speed, spawnForward);
        if (!Double.isFinite(flightTicks) || flightTicks <= 0 || flightTicks > MAX_FLIGHT_TICKS) {
            return null;
        }
        double acceleration = 0;
        double accelerationPerTick = 0;
        for (int i = 2; i <= Math.min(samples, HISTORY_SIZE); i++) {
            double difference = velocity.distanceTo(velocities[(nextSample + HISTORY_SIZE - i) % HISTORY_SIZE]);
            acceleration = Math.max(acceleration, difference);
            accelerationPerTick = Math.max(accelerationPerTick, difference / (i - 1));
        }
        // 误差按预计飞行时间增长；急停、折返和快速摆动不能沿用旧速度猛加提前量。
        double errorBound = acceleration * (flightTicks + flightTicks * flightTicks * .5);
        if (errorBound > target.getBbWidth() * .5) {
            // 新模型目前只在稳定站位验证出收益；移动追击保留原来的置信门控。
            // 这是模型适用范围，不给弹体叠加或抵消施法者速度。
            if (caster.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) return null;
            Vec3 previous = velocities[(nextSample + HISTORY_SIZE - 2) % HISTORY_SIZE];
            if (velocity.dot(previous) < 0 || velocity.lengthSqr() + 1.0E-6 < previous.lengthSqr()) {
                return null;
            }
            Vec3 curvature = velocity.subtract(previous.scale(2))
                .add(velocities[(nextSample + HISTORY_SIZE - 3) % HISTORY_SIZE]);
            // 速度衰减模型只适用于已观察到向反方向弯曲的趋势；匀加速不提供这种证据。
            if (curvature.lengthSqr() <= 1.0E-12 || velocity.dot(curvature) >= 0) return null;
            double targetSpeed = velocity.length();
            double tolerance = target.getBbWidth() * .5;
            for (int i = 2; i <= Math.min(samples, HISTORY_SIZE); i++) {
                Vec3 past = velocities[(nextSample + HISTORY_SIZE - i) % HISTORY_SIZE];
                double forward = past.dot(velocity) / targetSpeed;
                double lateralSqr = Math.max(0, past.lengthSqr() - forward * forward);
                if (forward < 0 || lateralSqr * flightTicks * flightTicks > tolerance * tolerance) return null;
            }
            double jerk = 0;
            for (int i = 0; i < Math.min(samples, HISTORY_SIZE) - 2; i++) {
                Vec3 a = velocities[(nextSample + HISTORY_SIZE - 1 - i) % HISTORY_SIZE];
                Vec3 b = velocities[(nextSample + HISTORY_SIZE - 2 - i) % HISTORY_SIZE];
                Vec3 c = velocities[(nextSample + HISTORY_SIZE - 3 - i) % HISTORY_SIZE];
                jerk = Math.max(jerk, a.subtract(b.scale(2)).add(c).length());
            }
            // 将当前速度视作逐渐失效的信息：变化越快，沿当前方向外推的时间越短。
            // 不拟合周期或猜测下一次转弯；衰减后的提前时间始终位于 [0, flightTicks]。
            double persistence = velocity.length() / (accelerationPerTick + jerk * flightTicks);
            if (!Double.isFinite(persistence) || persistence <= 0) return null;
            flightTicks = -persistence * Math.expm1(-flightTicks / persistence);
        }
        Vec3 aim = currentAim.add(velocity.scale(flightTicks));
        if (!finite(aim) || caster.level().clip(new ClipContext(origin, aim, ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, caster)).getType() != HitResult.Type.MISS) {
            return null;
        }
        return aim;
    }

    /**
     * 解 |relative + velocity*t| = speed*t + spawnForward，取最早的正根。
     */
    private static double interceptTicks(Vec3 relative, Vec3 velocity, double speed, double spawnForward) {
        double a = velocity.lengthSqr() - speed * speed;
        double b = 2 * (relative.dot(velocity) - speed * spawnForward);
        double c = relative.lengthSqr() - spawnForward * spawnForward;
        if (Math.abs(a) < 1.0E-8) {
            return Math.abs(b) < 1.0E-8 ? Double.NaN : -c / b;
        }
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return Double.NaN;
        }
        double root = Math.sqrt(discriminant);
        double first = (-b - root) / (2 * a);
        double second = (-b + root) / (2 * a);
        return first > 0 ? second > 0 ? Math.min(first, second) : first : second;
    }

    private static boolean finite(Vec3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }
}
