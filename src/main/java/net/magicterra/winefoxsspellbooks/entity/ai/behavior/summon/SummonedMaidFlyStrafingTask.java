package net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.Map;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

/**
 * 飞行女仆扫射/走位行为
 * <p>
 * 模仿 {@link net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellStrafingTask}，
 * 让骑乘扫帚的女仆在战斗时围绕目标进行扫射走位。
 * <p>
 * 只负责设置 FLIGHT_TARGET，扫帚会在 travel() 中读取并飞向目标。
 * <p>
 * 行为逻辑：
 * - 在攻击范围内：围绕目标做圆周运动
 * - 太近：远离目标
 * - 太远：靠近目标
 * - 随机切换顺时针/逆时针方向
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-31
 */
public class SummonedMaidFlyStrafingTask extends Behavior<SummonedEntityMaid> {

    /**
     * 默认攻击距离
     */
    private static final float DEFAULT_ATTACK_RANGE = 16.0f;

    /**
     * 逃离距离比例（距离小于 attackRange * FLEE_RATIO 时远离）
     */
    private static final float FLEE_RATIO = 0.5f;

    /**
     * 理想飞行高度（相对于目标）
     */
    private static final float IDEAL_HEIGHT_OFFSET = 3.0f;

    /**
     * 高度容差
     */
    private static final float HEIGHT_TOLERANCE = 2.0f;

    /**
     * 圆周运动的角速度（弧度/tick）
     */
    private static final float STRAFE_ANGULAR_SPEED = 0.08f;

    /**
     * 接近/远离的速度（格/tick）
     */
    private static final float RADIAL_SPEED = 0.5f;

    /**
     * 切换方向的间隔（ticks）
     */
    private static final int DIRECTION_CHANGE_INTERVAL = 50;

    /**
     * 逃离冷却时间
     */
    private static final int FLEE_COOLDOWN_TICKS = 100;

    /**
     * 当前是否顺时针移动
     */
    private boolean strafingClockwise = true;

    /**
     * 扫射计时器
     */
    private int strafingTime = 0;

    /**
     * 逃离冷却计时器
     */
    private int fleeCooldown = 0;

    /**
     * 当前绕行角度（弧度）
     */
    private float currentAngle = 0.0f;

    public SummonedMaidFlyStrafingTask() {
        super(Map.of(
            MaidCastingMemoryModuleTypes.BROOM_ENTITY.get(), MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
            MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(), MemoryStatus.REGISTERED,
            MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(), MemoryStatus.REGISTERED,
            MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(), MemoryStatus.REGISTERED,
            MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get(), MemoryStatus.REGISTERED
        ), 1200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, SummonedEntityMaid maid) {
        if (!maid.isAirForce() || !maid.isPassenger()) {
            return false;
        }
        if (!(maid.getVehicle() instanceof SummonedMaidBroom)) {
            return false;
        }
        // 必须有攻击目标或支援目标
        LivingEntity target = getTarget(maid);
        return target != null && target.isAlive();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        if (!maid.isAirForce() || !maid.isPassenger()) {
            return false;
        }
        if (!(maid.getVehicle() instanceof SummonedMaidBroom)) {
            return false;
        }
        LivingEntity target = getTarget(maid);
        return target != null && target.isAlive();
    }

    @Override
    protected void start(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        // 初始化角度为当前相对目标的角度
        LivingEntity target = getTarget(maid);
        if (target != null) {
            Vec3 toMaid = maid.position().subtract(target.position());
            currentAngle = (float) Mth.atan2(toMaid.z, toMaid.x);
        }
        strafingTime = 0;
    }

    @Override
    protected void tick(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        LivingEntity target = getTarget(maid);
        if (target == null || !target.isAlive()) {
            return;
        }

        // 目标在流体中时悬停
        if (target.isInFluidType()) {
            maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get(), true);
            maid.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get());
            return;
        }

        // 递减逃离冷却
        if (fleeCooldown > 0) {
            fleeCooldown--;
        }

        // 随机切换方向
        strafingTime++;
        if (strafingTime >= DIRECTION_CHANGE_INTERVAL) {
            if (maid.getRandom().nextDouble() < 0.1) {
                strafingClockwise = !strafingClockwise;
            }
            strafingTime = 0;
        }

        // 获取法术范围
        float spellRange = getSpellRange(maid);

        Vec3 targetPos = target.position();
        Vec3 maidPos = maid.position();

        // 计算水平距离
        double dx = maidPos.x - targetPos.x;
        double dz = maidPos.z - targetPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // 计算飞行目标
        Vec3 flightTarget = calculateFlightTarget(maid, targetPos, horizontalDist, spellRange);

        // 设置 FLIGHT_TARGET（扫帚会读取并飞向目标）
        maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get(), new BlockPosTracker(flightTarget));
        maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get(), false);
    }

    @Override
    protected void stop(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        strafingTime = 0;
        fleeCooldown = 0;
        // 清除飞行目标，让其他行为接管
        maid.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get());
    }

    /**
     * 获取当前目标（攻击目标或支援目标）
     */
    private LivingEntity getTarget(SummonedEntityMaid maid) {
        MaidSpellAction action = maid.getBrain()
            .getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get())
            .orElse(MaidSpellAction.ATTACK);

        return switch (action) {
            case POSITIVE, SUPPORT_OTHER -> maid.getBrain()
                .getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get())
                .orElseGet(() -> maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null));
            default -> maid.getBrain()
                .getMemory(MemoryModuleType.ATTACK_TARGET)
                .orElse(null);
        };
    }

    /**
     * 获取当前法术范围
     */
    private float getSpellRange(SummonedEntityMaid maid) {
        SpellData spellData = maid.getBrain()
            .getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get())
            .orElse(null);
        if (spellData != null && spellData != SpellData.EMPTY) {
            return MaidSpellRegistry.getSpellRange(spellData.getSpell());
        }
        return DEFAULT_ATTACK_RANGE;
    }

    /**
     * 计算飞行目标位置
     * <p>
     * 模仿 SpellStrafingTask 的逻辑：
     * - 太近：远离
     * - 太远：靠近
     * - 在范围内：圆周运动
     */
    private Vec3 calculateFlightTarget(SummonedEntityMaid maid, Vec3 targetPos,
                                       double horizontalDist, float spellRange) {
        int strafeDir = strafingClockwise ? 1 : -1;
        float fleeDistance = spellRange * FLEE_RATIO;

        double targetDist;
        float angularSpeed = STRAFE_ANGULAR_SPEED;

        if (fleeCooldown <= 0 && horizontalDist < fleeDistance) {
            // 太近，需要远离
            targetDist = horizontalDist + RADIAL_SPEED * 2;
            angularSpeed *= 0.5f; // 远离时减慢圆周速度
            fleeCooldown = FLEE_COOLDOWN_TICKS;
        } else if (horizontalDist > spellRange) {
            // 太远，需要靠近
            targetDist = horizontalDist - RADIAL_SPEED * 2;
            angularSpeed *= 0.5f; // 靠近时减慢圆周速度
        } else if (horizontalDist < spellRange * 0.6) {
            // 在较近范围内，稍微远离同时圆周运动
            targetDist = horizontalDist + RADIAL_SPEED * 0.5;
        } else {
            // 在理想范围内，保持距离做圆周运动
            targetDist = horizontalDist;
        }

        // 更新角度（圆周运动）
        currentAngle += angularSpeed * strafeDir;

        // 保持角度在 [-PI, PI] 范围内
        if (currentAngle > Math.PI) {
            currentAngle -= 2 * (float) Math.PI;
        } else if (currentAngle < -Math.PI) {
            currentAngle += 2 * (float) Math.PI;
        }

        // 计算目标位置（圆周运动）
        double targetX = targetPos.x + Math.cos(currentAngle) * targetDist;
        double targetZ = targetPos.z + Math.sin(currentAngle) * targetDist;

        // 计算目标高度
        double idealY = targetPos.y + IDEAL_HEIGHT_OFFSET;
        double currentY = maid.getY();
        double heightDiff = Math.abs(currentY - idealY);

        double targetY;
        if (heightDiff > HEIGHT_TOLERANCE) {
            // 高度差太大，快速调整
            targetY = Mth.lerp(0.3, currentY, idealY);
        } else {
            // 高度在容差范围内，缓慢调整
            targetY = Mth.lerp(0.1, currentY, idealY);
        }

        return new Vec3(targetX, targetY, targetZ);
    }
}
