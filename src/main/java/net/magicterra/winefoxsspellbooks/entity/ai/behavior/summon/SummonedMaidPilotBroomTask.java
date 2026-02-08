package net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon;

import java.util.Map;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

/**
 * 扫帚女仆飞行控制行为
 * <p>
 * 负责将 Brain 的 WALK_TARGET 转换为扫帚的飞行目标。
 * 跟随和游荡行为由原版的 StayCloseToTarget 和 RandomStroll.fly() 处理，
 * 本行为仅负责驱动扫帚移动和战斗时的追击。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-11
 */
public class SummonedMaidPilotBroomTask extends Behavior<SummonedEntityMaid> {

    /** 最大转向角度（度/tick） */
    private static final float MAX_TURN_DEGREES = 30.0f;

    public SummonedMaidPilotBroomTask() {
        super(Map.of(
            MaidCastingMemoryModuleTypes.BROOM_ENTITY.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, SummonedEntityMaid maid) {
        return maid.isAirForce()
            && maid.isPassenger()
            && maid.getVehicle() instanceof SummonedMaidBroom;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        return checkExtraStartConditions(level, maid);
    }

    @Override
    protected void tick(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        if (!(maid.getVehicle() instanceof SummonedMaidBroom broom)) {
            return;
        }

        // 获取攻击目标
        Optional<LivingEntity> attackTarget = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);

        if (attackTarget.isPresent() && attackTarget.get().isAlive()) {
            // 有攻击目标：追击或悬停攻击，并看向目标
            handleCombatFlight(maid, broom, attackTarget.get());
        } else {
            // 无攻击目标：将 WALK_TARGET 转换为飞行目标
            handleWalkTargetFlight(maid, broom);
        }
    }

    /**
     * 处理战斗飞行（追击和悬停攻击）
     * <p>
     * 优先读取 {@link SummonedMaidFlyStrafingTask} 设置的 FLIGHT_TARGET memory 来驱动扫帚，
     * 仅在 FLIGHT_TARGET 不存在时使用后备追击逻辑（直线飞向目标上方）。
     */
    private void handleCombatFlight(SummonedEntityMaid maid, SummonedMaidBroom broom, LivingEntity target) {
        // 让女仆和扫帚看向攻击目标
        lookAtTarget(maid, broom, target.getEyePosition());

        // 检查目标是否在流体中
        if (target.isInFluidType()) {
            broom.setHovering(true);
            return;
        }

        // 检查 IS_HOVERING（由 FlyStrafingTask 管理）
        boolean isHovering = maid.getBrain()
            .getMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get())
            .orElse(false);

        // 优先使用 FlyStrafingTask 设置的 FLIGHT_TARGET
        Optional<PositionTracker> flightTargetMemory = maid.getBrain()
            .getMemory(MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get());

        if (flightTargetMemory.isPresent()) {
            // FlyStrafingTask 已计算走位目标，直接使用
            Vec3 flightTarget = flightTargetMemory.get().currentPosition();
            broom.setHovering(isHovering);
            broom.setFlightTarget(flightTarget);
        } else if (isHovering) {
            // FlyStrafingTask 要求悬停（例如目标在流体中）
            broom.setHovering(true);
        } else {
            // 后备逻辑：FlyStrafingTask 未运行时，直线追击目标
            Vec3 targetPos = target.position();
            Vec3 broomPos = broom.position();
            double distanceSq = broomPos.distanceToSqr(targetPos);

            float minAttackRange = Config.getMinAirAttackRange();
            double minAttackRangeSq = minAttackRange * minAttackRange;

            if (distanceSq <= minAttackRangeSq) {
                broom.setHovering(true);
                maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get(), true);
            } else {
                broom.setHovering(false);
                maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get(), false);
                Vec3 fallbackTarget = new Vec3(targetPos.x, targetPos.y + 2.0, targetPos.z);
                broom.setFlightTarget(fallbackTarget);
            }
        }
    }

    /**
     * 处理 WALK_TARGET 转换为飞行
     * <p>
     * 原版的 StayCloseToTarget 和 RandomStroll 会设置 WALK_TARGET，
     * 这里将其转换为扫帚的飞行目标。
     */
    private void handleWalkTargetFlight(SummonedEntityMaid maid, SummonedMaidBroom broom) {
        Optional<WalkTarget> walkTarget = maid.getBrain().getMemory(MemoryModuleType.WALK_TARGET);

        if (walkTarget.isPresent()) {
            // 有行走目标，转换为飞行目标
            Vec3 targetPos = walkTarget.get().getTarget().currentPosition();
            broom.setHovering(false);
            maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get(), false);
            broom.setFlightTarget(targetPos);
        } else {
            // 无目标，悬停
            broom.setHovering(true);
            maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get(), true);
        }
    }

    /**
     * 让女仆和扫帚平滑地看向目标位置
     *
     * @param maid      女仆实体
     * @param broom     扫帚实体
     * @param targetPos 目标位置
     */
    private void lookAtTarget(SummonedEntityMaid maid, SummonedMaidBroom broom, Vec3 targetPos) {
        Vec3 broomPos = broom.position();
        double dx = targetPos.x - broomPos.x;
        double dy = targetPos.y - broomPos.y;
        double dz = targetPos.z - broomPos.z;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // 计算目标水平角度
        float targetYRot = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        // 计算目标俯仰角度
        float targetXRot = (float) (-(Mth.atan2(dy, horizontalDist) * (180.0 / Math.PI)));

        // 平滑转向
        float newYRot = rotlerp(maid.getYRot(), targetYRot, MAX_TURN_DEGREES);
        float newXRot = rotlerp(maid.getXRot(), targetXRot, MAX_TURN_DEGREES / 2);

        // 设置扫帚朝向
        broom.setYRot(newYRot);
        broom.yRotO = broom.getYRot();

        // 设置女仆朝向
        maid.setYRot(newYRot);
        maid.yRotO = newYRot;
        maid.setYHeadRot(newYRot);
        maid.setYBodyRot(newYRot);
        maid.setXRot(newXRot);
        maid.xRotO = newXRot;
    }

    /**
     * 平滑转向
     */
    private float rotlerp(float current, float target, float maxDelta) {
        float delta = Mth.wrapDegrees(target - current);
        if (delta > maxDelta) {
            delta = maxDelta;
        }
        if (delta < -maxDelta) {
            delta = -maxDelta;
        }
        return current + delta;
    }
}
