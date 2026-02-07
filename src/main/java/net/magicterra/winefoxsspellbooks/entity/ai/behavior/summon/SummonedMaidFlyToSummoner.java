package net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon;

import java.util.Map;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

/**
 * 扫帚女仆跟随召唤者行为
 * <p>
 * 自定义实现，解决原版 StayCloseToTarget 每 tick 更新目标导致的飞行卡顿问题：
 * - 只在召唤者移动一定距离后才更新飞行目标
 * - 与召唤者保持适当距离（不会挤在头上）
 * - 距离太近时不移动，太远时才跟随
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-18
 */
public class SummonedMaidFlyToSummoner extends Behavior<SummonedEntityMaid> {

    /**
     * 停止接近的距离
     */
    private static final float CLOSE_ENOUGH_DISTANCE_RATIO = 2.0f / 3.0f;

    /**
     * 开始跟随的距离
     */
    private static final float TOO_FAR_DISTANCE_RATIO = 2.0f;

    /**
     * 触发传送的距离（距离过远时直接传送到召唤者身边）
     */
    private static final int TELEPORT_DISTANCE = 32;

    /**
     * 跟随位置在召唤者后方的水平偏移距离
     */
    private static final float FOLLOW_BEHIND_DISTANCE_RATIO = 0.5f;

    /**
     * 飞行速度倍率
     */
    private static final float FLY_SPEED = 1.0f;

    /**
     * 目标位置更新阈值（召唤者移动超过此距离才更新目标）
     * <p>
     * 设置为 2.0 格，避免频繁更新目标导致的抖动
     */
    private static final double TARGET_UPDATE_THRESHOLD_SQ = 4.0; // 2.0^2

    /**
     * 上次设置目标时召唤者的位置
     */
    @Nullable
    private Vec3 lastSummonerPos;

    /**
     * 当前飞行目标位置
     */
    @Nullable
    private Vec3 currentTargetPos;

    public SummonedMaidFlyToSummoner() {
        super(Map.of(
            MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
            MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, SummonedEntityMaid maid) {
        // 必须飞行且骑乘扫帚
        if (!maid.isAirForce() || !maid.isPassenger()) {
            return false;
        }
        if (!(maid.getVehicle() instanceof SummonedMaidBroom)) {
            return false;
        }
        // 没有攻击目标或支援目标时才跟随
        if (maid.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        if (maid.getBrain().hasMemoryValue(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get())) {
            return false;
        }

        // 检查召唤者是否存在且距离足够远
        Entity summonerEntity = maid.getSummoner();
        if (!(summonerEntity instanceof LivingEntity summoner) || !summoner.isAlive()) {
            return false;
        }

        double distanceSq = maid.distanceToSqr(summoner);
        int tooFarDistance = getTooFarDistance();
        return distanceSq > tooFarDistance * tooFarDistance;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        // 必须飞行且骑乘扫帚
        if (!maid.isAirForce() || !maid.isPassenger()) {
            return false;
        }
        if (!(maid.getVehicle() instanceof SummonedMaidBroom)) {
            return false;
        }
        // 有攻击目标或支援目标时停止跟随
        if (maid.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        if (maid.getBrain().hasMemoryValue(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get())) {
            return false;
        }

        // 检查召唤者
        Entity summonerEntity = maid.getSummoner();
        if (!(summonerEntity instanceof LivingEntity summoner) || !summoner.isAlive()) {
            return false;
        }

        // 到达足够近时停止
        double distanceSq = maid.distanceToSqr(summoner);
        int closeEnoughDistance = getCloseEnoughDistance();
        return distanceSq > closeEnoughDistance * closeEnoughDistance;
    }

    @Override
    protected void start(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        Entity summonerEntity = maid.getSummoner();
        if (summonerEntity instanceof LivingEntity summoner) {
            updateTarget(maid, summoner);
        }
    }

    @Override
    protected void tick(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        Entity summonerEntity = maid.getSummoner();
        if (!(summonerEntity instanceof LivingEntity summoner)) {
            return;
        }

        Vec3 summonerPos = summoner.position();
        double distanceSq = maid.distanceToSqr(summoner);

        // 距离过远时直接传送
        if (distanceSq > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
            teleportToSummoner(level, maid, summoner);
            return;
        }

        // 只在召唤者移动超过阈值时更新目标
        if (lastSummonerPos == null || summonerPos.distanceToSqr(lastSummonerPos) > TARGET_UPDATE_THRESHOLD_SQ) {
            updateTarget(maid, summoner);
        }
    }

    @Override
    protected void stop(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        // 清除状态
        lastSummonerPos = null;
        currentTargetPos = null;
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    /**
     * 更新飞行目标
     * <p>
     * 计算召唤者背后的位置，避免挡住召唤者视线
     */
    private void updateTarget(SummonedEntityMaid maid, LivingEntity summoner) {
        Vec3 summonerPos = summoner.position();

        // 获取召唤者的视线方向（水平面上的朝向）
        float yawRad = (float) Math.toRadians(summoner.getYRot());

        // 计算召唤者背后的位置（视线反方向）
        // 注意：Minecraft 中 -sin(yaw) 是 X 方向，-cos(yaw) 是 Z 方向（前进方向）
        // 所以背后方向是 +sin(yaw) 和 +cos(yaw)
        double followBehindDistance = getFollowBehindDistance();
        double behindX = summonerPos.x + Math.sin(yawRad) * followBehindDistance;
        double behindZ = summonerPos.z + Math.cos(yawRad) * followBehindDistance;
        double targetY = summonerPos.y + Config.getFollowHeight();

        currentTargetPos = new Vec3(behindX, targetY, behindZ);
        lastSummonerPos = summonerPos;

        // 设置 WALK_TARGET 和 LOOK_TARGET
        BlockPosTracker tracker = new BlockPosTracker(currentTargetPos);
        maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(tracker, FLY_SPEED, getCloseEnoughDistance()));
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, tracker);
    }

    /**
     * 传送女仆和扫帚到召唤者身边
     * <p>
     * 当距离过远时，直接传送到召唤者背后上方的位置
     */
    private void teleportToSummoner(ServerLevel level, SummonedEntityMaid maid, LivingEntity summoner) {
        Vec3 summonerPos = summoner.position();

        // 计算召唤者背后上方的位置
        float yawRad = (float) Math.toRadians(summoner.getYRot());
        double followBehindDistance = getFollowBehindDistance();
        double behindX = summonerPos.x + Math.sin(yawRad) * followBehindDistance;
        double behindZ = summonerPos.z + Math.cos(yawRad) * followBehindDistance;
        double targetY = summonerPos.y + Config.getFollowHeight();

        // 获取扫帚实体
        Entity vehicle = maid.getVehicle();
        if (vehicle instanceof SummonedMaidBroom broom) {
            // 传送扫帚（女仆会随之移动）
            broom.moveTo(behindX, targetY, behindZ, broom.getYRot(), broom.getXRot());
            broom.setFlightTarget(new Vec3(behindX, targetY, behindZ));

            // 更新扫帚记忆
            maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get(), new BlockPosTracker(new Vec3(behindX, targetY, behindZ)));
        } else {
            // 如果没有扫帚，直接传送女仆
            maid.moveTo(behindX, targetY, behindZ, maid.getYRot(), maid.getXRot());
        }

        // 生成传送粒子效果
        level.sendParticles(
            ParticleTypes.PORTAL,
            behindX, targetY + 1.0, behindZ,
            20, 0.5, 0.5, 0.5, 0.1
        );

        // 更新缓存位置
        lastSummonerPos = summonerPos;
        currentTargetPos = new Vec3(behindX, targetY, behindZ);

        // 清除导航目标，避免冲突
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getBrain().eraseMemory(MemoryModuleType.PATH);
    }

    private static int getCloseEnoughDistance() {
        float followRadius = Config.getFollowRadius();
        return Math.max(2, Math.round(followRadius * CLOSE_ENOUGH_DISTANCE_RATIO));
    }

    private static int getTooFarDistance() {
        float followRadius = Config.getFollowRadius();
        return Math.max(getCloseEnoughDistance() + 1, Math.round(followRadius * TOO_FAR_DISTANCE_RATIO));
    }

    private static double getFollowBehindDistance() {
        return Math.max(1.0, Config.getFollowRadius() * FOLLOW_BEHIND_DISTANCE_RATIO);
    }
}
