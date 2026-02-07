package net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon;

import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * 扫帚女仆随机飞行行为
 * <p>
 * 基于原版 {@link net.minecraft.world.entity.ai.behavior.RandomStroll#fly}，
 * 但避免选择召唤者头顶正上方和前方的位置。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-19
 */
public class SummonedMaidRandomFlight {

    /** 最大水平距离 */
    private static final int MAX_XZ_DIST = 10;

    /** 最大垂直距离 */
    private static final int MAX_Y_DIST = 7;

    /** 前方禁区角度（度） - 召唤者面朝方向左右各这么多度不选择 */
    private static final double FRONT_FORBIDDEN_ANGLE = 60.0;

    /** 头顶禁区水平半径 - 召唤者头顶这个水平范围内不选择 */
    private static final double TOP_FORBIDDEN_RADIUS = 3.0;

    /** 头顶禁区最大高度 - 召唤者上方这个高度范围内都是禁区 */
    private static final double TOP_FORBIDDEN_HEIGHT = 5.0;

    /** 玩家碰撞禁区半径 - 距离玩家太近不选择 */
    private static final double COLLISION_FORBIDDEN_RADIUS = 2.0;

    /**
     * 创建随机飞行行为
     *
     * @param speedModifier 速度倍率
     * @return 行为控制器
     */
    public static OneShot<PathfinderMob> create(float speedModifier) {
        return BehaviorBuilder.create(context -> context
            .group(context.absent(MemoryModuleType.WALK_TARGET))
            .apply(context, walkTarget -> (level, entity, gameTime) -> {
                // 只对骑乘扫帚的召唤女仆生效
                if (!(entity instanceof SummonedEntityMaid maid)) {
                    return false;
                }
                if (!maid.isAirForce() || !maid.isPassenger()) {
                    return false;
                }
                if (!(maid.getVehicle() instanceof SummonedMaidBroom broom)) {
                    return false;
                }

                // 获取召唤者
                Entity summonerEntity = maid.getSummoner();
                if (!(summonerEntity instanceof LivingEntity summoner)) {
                    return false;
                }

                // 尝试找到有效的飞行目标
                Vec3 targetPos = getValidFlyPos(maid, summoner);
                if (targetPos == null) {
                    return false;
                }

                walkTarget.set(new WalkTarget(targetPos, speedModifier, 0));
                return true;
            })
        );
    }

    /**
     * 获取有效的飞行位置（避开召唤者头顶和前方）
     *
     * @param maid     女仆实体
     * @param summoner 召唤者
     * @return 有效的飞行位置，如果找不到返回 null
     */
    @Nullable
    private static Vec3 getValidFlyPos(SummonedEntityMaid maid, LivingEntity summoner) {
        Vec3 summonerPos = summoner.position();
        float summonerYRot = summoner.getYRot();

        // 尝试最多 10 次找到有效位置
        for (int attempt = 0; attempt < 10; attempt++) {
            // 使用原版方法生成随机飞行位置
            Vec3 viewVector = maid.getViewVector(0.0f);
            Vec3 targetPos = AirAndWaterRandomPos.getPos(
                maid, MAX_XZ_DIST, MAX_Y_DIST, -2,
                viewVector.x, viewVector.z, Math.PI / 2
            );

            if (targetPos == null) {
                continue;
            }

            // 检查是否在禁区内
            if (isInForbiddenZone(targetPos, summonerPos, summonerYRot)) {
                continue;
            }

            return targetPos;
        }

        // 如果随机位置都在禁区，尝试选择召唤者侧后方的位置
        return getFallbackPosition(maid, summoner);
    }

    /**
     * 检查位置是否在禁区内
     * <p>
     * 禁区包括：
     * 1. 玩家碰撞区：距离玩家 2 格内
     * 2. 头顶禁区：玩家上方 5 格内且水平距离 3 格内
     * 3. 前方视线禁区：玩家面朝方向左右 60 度内
     *
     * @param targetPos    目标位置
     * @param summonerPos  召唤者位置
     * @param summonerYRot 召唤者朝向
     * @return 是否在禁区内
     */
    private static boolean isInForbiddenZone(Vec3 targetPos, Vec3 summonerPos, float summonerYRot) {
        double dx = targetPos.x - summonerPos.x;
        double dy = targetPos.y - summonerPos.y;
        double dz = targetPos.z - summonerPos.z;
        double horizontalDistSq = dx * dx + dz * dz;
        double totalDistSq = horizontalDistSq + dy * dy;

        // 1. 检查玩家碰撞禁区：距离玩家太近会碰撞
        if (totalDistSq < COLLISION_FORBIDDEN_RADIUS * COLLISION_FORBIDDEN_RADIUS) {
            return true;
        }

        // 2. 检查头顶禁区：在召唤者上方 5 格内且水平距离 3 格内
        if (dy > 0 && dy < TOP_FORBIDDEN_HEIGHT
            && horizontalDistSq < TOP_FORBIDDEN_RADIUS * TOP_FORBIDDEN_RADIUS) {
            return true;
        }

        // 3. 检查前方视线禁区
        if (horizontalDistSq < 0.01) {
            // 正上方/正下方，不在水平视线禁区内
            return false;
        }

        // 计算目标相对于召唤者的角度（度）
        double angleToTarget = Math.toDegrees(Math.atan2(-dx, dz));

        // 计算与召唤者面朝方向的角度差
        double angleDiff = normalizeAngle(angleToTarget - summonerYRot);

        // 如果在前方视线禁区角度范围内（左右各 60 度 = 总共 120 度扇形）
        if (Math.abs(angleDiff) < FRONT_FORBIDDEN_ANGLE) {
            return true;
        }

        return false;
    }

    /**
     * 获取后备位置（召唤者侧后方）
     *
     * @param maid     女仆实体
     * @param summoner 召唤者
     * @return 后备位置
     */
    @Nullable
    private static Vec3 getFallbackPosition(SummonedEntityMaid maid, LivingEntity summoner) {
        Vec3 summonerPos = summoner.position();
        float summonerYRot = summoner.getYRot();

        // 选择召唤者背后的位置
        double backAngle = Math.toRadians(summonerYRot + 180.0);

        // 添加一些随机偏移（-90 到 +90 度）
        double randomOffset = (maid.getRandom().nextDouble() - 0.5) * Math.PI;
        double finalAngle = backAngle + randomOffset;

        double distance = 5.0 + maid.getRandom().nextDouble() * 5.0; // 5-10 格
        double height = 2.0 + maid.getRandom().nextDouble() * 3.0;   // 2-5 格高

        double offsetX = -Math.sin(finalAngle) * distance;
        double offsetZ = Math.cos(finalAngle) * distance;

        Vec3 targetPos = new Vec3(
            summonerPos.x + offsetX,
            summonerPos.y + height,
            summonerPos.z + offsetZ
        );

        // 验证位置是否有效（无碰撞）
        if (maid.level().noCollision(maid, maid.getBoundingBox().move(targetPos.subtract(maid.position())))) {
            return targetPos;
        }

        return null;
    }

    /**
     * 将角度归一化到 -180 到 180 度范围
     */
    private static double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}
