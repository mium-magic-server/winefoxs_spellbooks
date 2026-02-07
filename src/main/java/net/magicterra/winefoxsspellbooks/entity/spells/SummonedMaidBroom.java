package net.magicterra.winefoxsspellbooks.entity.spells;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 召唤女仆专用扫帚实体
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-11
 */
public class SummonedMaidBroom extends EntityBroom implements IMagicSummon {
    public static final EntityType<SummonedMaidBroom> TYPE = EntityType.Builder.of(SummonedMaidBroom::new, MobCategory.MISC)
        .sized(1.375f, 0.5625f)
        .clientTrackingRange(10)
        .build("summoned_maid_broom");

    private static final String TAG_SUMMONER_UUID = "SummonerUUID";
    private static final String TAG_MAID_UUID = "MaidUUID";

    /**
     * 空气阻力系数（模仿原版飞行生物）
     * <p>
     * 原版悦灵使用 0.91f
     */
    private static final double AIR_FRICTION = 0.91;

    /**
     * 到达目标的判定距离平方
     * <p>
     * 增大到 1.0^2，避免在目标附近频繁切换状态
     */
    private static final double ARRIVAL_DISTANCE_SQ = 1.0;

    /**
     * 最小速度阈值（低于此值视为停止）
     */
    private static final double MIN_SPEED_SQ = 2.5E-7;

    /**
     * 速度插值因子
     * <p>
     * 较大的值使加速更快，较小的值使飞行更平滑。
     * 0.125 提供良好的平衡：既有惯性感又不会太迟钝
     */
    private static final double SPEED_LERP_FACTOR = 0.125;

    // ==================== 扫射相关常量 ====================

    /**
     * 逃离距离比例（距离小于 attackRange * FLEE_RATIO 时远离）
     */
    private static final float STRAFE_FLEE_RATIO = 0.5f;

    /**
     * 理想飞行高度（相对于目标）
     */
    private static final float STRAFE_HEIGHT_OFFSET = 3.0f;

    /**
     * 圆周运动的角速度（弧度/tick）
     */
    private static final float STRAFE_ANGULAR_SPEED = 0.06f;

    /**
     * 接近/远离的速度（格/tick）
     */
    private static final float STRAFE_RADIAL_SPEED = 0.4f;

    /**
     * 切换方向的间隔（ticks）
     */
    private static final int STRAFE_DIRECTION_CHANGE_INTERVAL = 50;

    /**
     * 默认攻击距离
     */
    private static final float DEFAULT_ATTACK_RANGE = 16.0f;

    /**
     * 高度容差（高度差小于此值时不做调整）
     */
    private static final float STRAFE_HEIGHT_TOLERANCE = 2.0f;

    /**
     * 逃离冷却时间（ticks）
     */
    private static final int STRAFE_FLEE_COOLDOWN = 100;

    /**
     * 高度波动振幅（格）
     */
    private static final float STRAFE_HEIGHT_AMPLITUDE = 2.5f;

    /**
     * 高度波动周期（ticks）
     */
    private static final int STRAFE_HEIGHT_PERIOD = 80;

    @Nullable
    private UUID summonerUUID;
    @Nullable
    private UUID maidUUID;

    /**
     * 标记是否已完成首次骑乘（用于区分新创建和加载后恢复）
     */
    private boolean hasBeenRidden = false;

    /**
     * 标记是否需要恢复骑乘关系（从存档加载后）
     */
    private boolean needsRidingRestore = false;

    /**
     * 骑乘恢复尝试计数器（超过一定次数后放弃）
     */
    private int ridingRestoreAttempts = 0;

    /**
     * 最大骑乘恢复尝试次数（约5秒）
     */
    private static final int MAX_RIDING_RESTORE_ATTEMPTS = 100;

    // === 客户端插值相关字段 ===
    /**
     * 客户端插值剩余步数
     */
    private int clientLerpSteps;
    /**
     * 客户端插值目标位置
     */
    private double clientLerpX, clientLerpY, clientLerpZ;
    /**
     * 客户端插值目标旋转
     */
    private double clientLerpYRot, clientLerpXRot;

    // === 扫射状态字段 ===
    /**
     * 当前是否顺时针移动
     */
    private boolean strafingClockwise = true;

    /**
     * 扫射总计时器（用于高度波动，不重置）
     */
    private int strafingTime = 0;

    /**
     * 方向切换计时器（达到阈值后重置）
     */
    private int directionChangeTimer = 0;

    /**
     * 当前绕行角度（弧度）
     */
    private float currentStrafeAngle = 0.0f;

    /**
     * 逃离冷却计时器
     */
    private int fleeCooldown = 0;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SummonedMaidBroom(EntityType<? extends SummonedMaidBroom> entityType, Level level) {
        // EntityBroom 构造函数要求其自身的 EntityType 类型
        super((EntityType) entityType, level);
    }

    /**
     * 为扫帚女仆创建并绑定扫帚
     *
     * @param level    世界
     * @param maid     绑定的女仆
     * @param summoner 召唤者
     * @return 新创建的扫帚实体
     */
    public static SummonedMaidBroom createForMaid(ServerLevel level, SummonedEntityMaid maid, LivingEntity summoner) {
        SummonedMaidBroom broom = new SummonedMaidBroom(SummonedMaidBroom.TYPE, level);
        broom.setPos(maid.getX(), maid.getY(), maid.getZ());
        broom.setYRot(maid.getYRot());
        broom.setXRot(maid.getXRot());
        broom.setSummonerUUID(summoner.getUUID());
        broom.setMaidUUID(maid.getUUID());
        broom.setOwnerUUID(summoner.getUUID());
        return broom;
    }

    @Override
    public void tick() {
        super.tick();

        // 客户端：执行平滑位置插值（模仿 Boat.tickLerp）
        this.tickLerp();

        if (this.level().isClientSide) {
            return;
        }

        // === 服务端逻辑 ===

        // 尝试恢复骑乘关系（从存档加载后）
        if (needsRidingRestore) {
            tryRestoreRiding();
        }

        // 标记已有乘客（用于区分是否曾经被骑乘过）
        if (!hasBeenRidden && !this.getPassengers().isEmpty()) {
            hasBeenRidden = true;
        }

        // 流体接触：销毁扫帚（女仆的下车由 SummonedMaidDismountTask 处理）
        if (handleFluidContact()) {
            return;
        }

        // 女仆下车后：销毁扫帚
        // 注意：女仆的状态切换由 SummonedMaidDismountTask 处理
        if (hasBeenRidden && this.getPassengers().isEmpty()) {
            this.onUnSummon();
        }
    }

    /**
     * 客户端位置插值（模仿 Boat.tickLerp）
     */
    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.clientLerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }

        if (this.clientLerpSteps > 0) {
            this.lerpPositionAndRotationStep(
                this.clientLerpSteps,
                this.clientLerpX, this.clientLerpY, this.clientLerpZ,
                this.clientLerpYRot, this.clientLerpXRot
            );
            this.clientLerpSteps--;
        }
    }

    /**
     * 尝试恢复骑乘关系（从存档加载后调用）
     */
    private void tryRestoreRiding() {
        ridingRestoreAttempts++;

        // 超过最大尝试次数，放弃恢复并销毁扫帚
        if (ridingRestoreAttempts > MAX_RIDING_RESTORE_ATTEMPTS) {
            needsRidingRestore = false;
            // 女仆可能已经不存在了，销毁扫帚
            this.onUnSummon();
            return;
        }

        if (maidUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity entity = serverLevel.getEntity(maidUUID);
        if (entity instanceof SummonedEntityMaid maid) {
            // 女仆存在，尝试恢复骑乘
            if (!maid.isPassenger()) {
                if (maid.startRiding(this, true)) {
                    needsRidingRestore = false;
                    hasBeenRidden = true;
                }
            } else if (maid.getVehicle() == this) {
                // 已经正确骑乘
                needsRidingRestore = false;
                hasBeenRidden = true;
            }
            // 如果女仆骑乘了其他载具，继续等待（可能是临时状态）
        }
        // 如果女仆还没加载，继续等待
    }

    /**
     * 当乘客被移除时调用（女仆下车）
     */
    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        // 标记已被骑乘过，这样下一个 tick 会检测到乘客为空并销毁
        if (passenger instanceof SummonedEntityMaid) {
            hasBeenRidden = true;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (summonerUUID != null) {
            tag.putUUID(TAG_SUMMONER_UUID, summonerUUID);
        }
        if (maidUUID != null) {
            tag.putUUID(TAG_MAID_UUID, maidUUID);
        }
    }

    /**
     * 重写保存方法，阻止保存乘客到 Passengers 标签
     * <p>
     * 女仆由 SummonManager 单独保存和恢复，如果扫帚也保存女仆到 Passengers 中，
     * 会导致加载时创建重复的女仆实体。
     */
    @Override
    public CompoundTag saveWithoutId(CompoundTag compound) {
        CompoundTag result = super.saveWithoutId(compound);
        // 移除 Passengers 标签，避免重复保存女仆
        result.remove("Passengers");
        return result;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(TAG_SUMMONER_UUID)) {
            summonerUUID = tag.getUUID(TAG_SUMMONER_UUID);
        }
        if (tag.contains(TAG_MAID_UUID)) {
            maidUUID = tag.getUUID(TAG_MAID_UUID);
            // 从存档加载时，需要恢复骑乘关系
            // SummonManager 会单独恢复女仆，所以我们需要手动重建骑乘关系
            needsRidingRestore = true;
        }
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity firstPassenger = this.getFirstPassenger();
        if (firstPassenger instanceof SummonedEntityMaid maid) {
            return maid;
        }
        return null;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return entity instanceof SummonedEntityMaid && this.getPassengers().isEmpty();
    }

    /**
     * 重写客户端位置插值（模仿 Boat 的实现）
     * <p>
     * 使用 10 步插值，即使服务端每 3 tick 发送一次更新，
     * 客户端也会用 10 tick 来平滑移动，实现跨网络更新周期的平滑过渡。
     */
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.clientLerpX = x;
        this.clientLerpY = y;
        this.clientLerpZ = z;
        this.clientLerpYRot = yRot;
        this.clientLerpXRot = xRot;
        // 使用 10 步插值（与 Boat 相同），实现更平滑的移动
        this.clientLerpSteps = 10;
    }

    @Override
    public double lerpTargetX() {
        return this.clientLerpSteps > 0 ? this.clientLerpX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.clientLerpSteps > 0 ? this.clientLerpY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.clientLerpSteps > 0 ? this.clientLerpZ : this.getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return this.clientLerpSteps > 0 ? (float) this.clientLerpXRot : this.getXRot();
    }

    @Override
    public float lerpTargetYRot() {
        return this.clientLerpSteps > 0 ? (float) this.clientLerpYRot : this.getYRot();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // 禁止玩家交互
        return InteractionResult.PASS;
    }

    @Override
    public void travel(Vec3 travelVector) {
        LivingEntity controller = this.getControllingPassenger();
        if (controller instanceof SummonedEntityMaid maid) {
            // 优先检查是否有战斗目标（攻击或支援），有则执行扫射
            LivingEntity combatTarget = getCombatTarget(maid);
            Vec3 targetPos;

            if (combatTarget != null && combatTarget.isAlive() && !combatTarget.isInFluidType()) {
                // 有战斗目标：执行扫射逻辑
                targetPos = calculateStrafeTarget(maid, combatTarget);
            } else {
                // 无战斗目标：使用 FLIGHT_TARGET 或 WALK_TARGET
                targetPos = getFlightTargetPosition(maid);
            }

            if (targetPos != null) {
                Vec3 direction = targetPos.subtract(this.position());
                double distanceSq = direction.lengthSqr();

                if (distanceSq >= ARRIVAL_DISTANCE_SQ) {
                    // 计算目标速度向量（直接朝向目标方向）
                    float speed = Config.getFlySpeed();
                    Vec3 normalizedDir = direction.normalize();
                    Vec3 targetMotion = normalizedDir.scale(speed);

                    // 平滑插值到目标速度（使用较低的插值因子获得更好的惯性感）
                    Vec3 currentMotion = this.getDeltaMovement();
                    Vec3 newMotion = new Vec3(
                        Mth.lerp(SPEED_LERP_FACTOR, currentMotion.x, targetMotion.x),
                        Mth.lerp(SPEED_LERP_FACTOR, currentMotion.y, targetMotion.y),
                        Mth.lerp(SPEED_LERP_FACTOR, currentMotion.z, targetMotion.z)
                    );

                    this.setDeltaMovement(newMotion);
                } else {
                    // 到达目标，应用空气阻力减速
                    this.setDeltaMovement(this.getDeltaMovement().scale(AIR_FRICTION));
                }
            } else {
                // 无目标：悬停，缓慢减速
                Vec3 motion = this.getDeltaMovement().scale(AIR_FRICTION);
                if (motion.lengthSqr() < MIN_SPEED_SQ) {
                    motion = Vec3.ZERO;
                }
                this.setDeltaMovement(motion);
            }

            // 执行移动
            this.move(MoverType.SELF, this.getDeltaMovement());

            // 方向由女仆的 rideTick() 控制，这里不设置
            return;
        }

        // 无控制者时自然下落
        if (!this.onGround()) {
            super.travel(new Vec3(0.0, -0.3, 0.0));
            return;
        }
        super.travel(travelVector);
    }

    /**
     * 获取战斗目标（攻击目标或支援目标，取决于当前法术动作）
     *
     * @param maid 女仆实体
     * @return 战斗目标，如果没有则返回 null
     */
    @Nullable
    private LivingEntity getCombatTarget(SummonedEntityMaid maid) {
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
     * 计算扫射目标位置
     * <p>
     * 围绕战斗目标做圆周运动，同时保持在法术范围内和适当高度。
     *
     * @param maid   女仆实体
     * @param target 战斗目标
     * @return 扫射目标位置
     */
    private Vec3 calculateStrafeTarget(SummonedEntityMaid maid, LivingEntity target) {
        // 递减逃离冷却
        if (fleeCooldown > 0) {
            fleeCooldown--;
        }

        // 递增扫射时间（用于高度波动，不重置）
        strafingTime++;

        // 方向切换逻辑
        directionChangeTimer++;
        if (directionChangeTimer >= STRAFE_DIRECTION_CHANGE_INTERVAL) {
            if (maid.getRandom().nextDouble() < 0.1) {
                strafingClockwise = !strafingClockwise;
            }
            directionChangeTimer = 0;
        }

        // 获取法术范围
        float spellRange = getSpellRange(maid);
        float fleeDistance = spellRange * STRAFE_FLEE_RATIO;

        Vec3 targetPos = target.position();
        Vec3 maidPos = maid.position();

        // 计算水平距离
        double dx = maidPos.x - targetPos.x;
        double dz = maidPos.z - targetPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // 初始化角度（如果还没设置）
        if (currentStrafeAngle == 0.0f && horizontalDist > 1.0) {
            currentStrafeAngle = (float) Mth.atan2(dz, dx);
        }

        int strafeDir = strafingClockwise ? 1 : -1;
        double targetDist;
        float angularSpeed = STRAFE_ANGULAR_SPEED;

        if (fleeCooldown <= 0 && horizontalDist < fleeDistance) {
            // 太近，需要远离
            targetDist = horizontalDist + STRAFE_RADIAL_SPEED * 2;
            angularSpeed *= 0.5f; // 远离时减慢圆周速度
            fleeCooldown = STRAFE_FLEE_COOLDOWN;
        } else if (horizontalDist > spellRange) {
            // 太远，需要靠近
            targetDist = horizontalDist - STRAFE_RADIAL_SPEED * 2;
            angularSpeed *= 0.5f; // 靠近时减慢圆周速度
        } else if (horizontalDist < spellRange * 0.6) {
            // 在较近范围内，稍微远离同时圆周运动
            targetDist = horizontalDist + STRAFE_RADIAL_SPEED * 0.5;
        } else {
            // 在理想范围内，保持距离做圆周运动
            targetDist = horizontalDist;
        }

        // 更新角度（圆周运动）
        currentStrafeAngle += angularSpeed * strafeDir;

        // 保持角度在 [-PI, PI] 范围内
        if (currentStrafeAngle > Math.PI) {
            currentStrafeAngle -= 2 * (float) Math.PI;
        } else if (currentStrafeAngle < -Math.PI) {
            currentStrafeAngle += 2 * (float) Math.PI;
        }

        // 计算目标位置（圆周运动）
        double targetX = targetPos.x + Math.cos(currentStrafeAngle) * targetDist;
        double targetZ = targetPos.z + Math.sin(currentStrafeAngle) * targetDist;

        // 计算目标高度（加入波动）
        // 使用 strafingTime 作为时间因子，通过正弦函数实现上下波动
        float heightPhase = (strafingTime % STRAFE_HEIGHT_PERIOD) / (float) STRAFE_HEIGHT_PERIOD * 2.0f * (float) Math.PI;
        float heightWave = Mth.sin(heightPhase) * STRAFE_HEIGHT_AMPLITUDE;
        double idealY = targetPos.y + STRAFE_HEIGHT_OFFSET + heightWave;
        double currentY = maid.getY();
        double heightDiff = Math.abs(currentY - idealY);

        double targetY;
        if (heightDiff > STRAFE_HEIGHT_TOLERANCE) {
            // 高度差太大，快速调整
            targetY = Mth.lerp(0.3, currentY, idealY);
        } else {
            // 高度在容差范围内，缓慢调整
            targetY = Mth.lerp(0.1, currentY, idealY);
        }

        return new Vec3(targetX, targetY, targetZ);
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
     * 获取飞行目标位置
     *
     * @param maid 控制的女仆
     * @return 目标位置，如果悬停或无目标则返回 null
     */
    @Nullable
    private Vec3 getFlightTargetPosition(SummonedEntityMaid maid) {
        // 悬停状态返回 null
        if (maid.getBrain().getMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get()).orElse(false)) {
            return null;
        }

        // 获取飞行目标
        PositionTracker target = maid.getBrain().getMemory(MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get())
            .orElseGet(() -> maid.getBrain().getMemory(MemoryModuleType.WALK_TARGET)
                .map(WalkTarget::getTarget)
                .orElse(null));

        return target != null ? target.currentPosition() : null;
    }

    /**
     * 设置飞行目标位置
     *
     * @param target 目标位置
     */
    public void setFlightTarget(Vec3 target) {
        if (this.getControllingPassenger() instanceof SummonedEntityMaid maid) {
            maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get(), new BlockPosTracker(target));
        }
    }

    /**
     * 设置悬停状态
     *
     * @param hovering 是否悬停
     */
    public void setHovering(boolean hovering) {
        if (this.getControllingPassenger() instanceof SummonedEntityMaid maid) {
            maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get(), hovering);
        }
    }

    /**
     * 是否正在悬停
     */
    public boolean isHovering() {
        if (this.getControllingPassenger() instanceof SummonedEntityMaid maid) {
            return maid.getBrain().getMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get()).orElse(false);
        }
        return false;
    }

    /**
     * 获取绑定的女仆
     */
    public Optional<SummonedEntityMaid> getBoundMaid() {
        if (maidUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        Entity entity = serverLevel.getEntity(maidUUID);
        if (entity instanceof SummonedEntityMaid maid) {
            return Optional.of(maid);
        }
        return Optional.empty();
    }

    /**
     * 获取召唤者
     * <p>
     * IMagicSummon 接口的默认实现会调用 SummonManager.getOwner()，
     * 这里返回 null 让默认实现生效
     */
    @Override
    public Entity getSummoner() {
        return IMagicSummon.super.getSummoner();
    }

    /**
     * 获取召唤者（内部使用，返回 Optional）
     */
    public Optional<LivingEntity> getSummonerOptional() {
        Entity summoner = getSummoner();
        if (summoner instanceof LivingEntity livingEntity) {
            return Optional.of(livingEntity);
        }
        return Optional.empty();
    }

    /**
     * 召唤物被取消召唤时的处理
     */
    @Override
    public void onUnSummon() {
        if (!this.level().isClientSide) {
            this.ejectPassengers();
            // 生成消散粒子
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    this.getX(), this.getY(), this.getZ(),
                    15, 0.3, 0.3, 0.3, 0.02
                );
            }
            this.discard();
        }
    }

    /**
     * 实体被移除时的处理
     * <p>
     * 只有在实体真正被销毁时才调用 onRemovedHelper，
     * 卸载到区块或随玩家卸载时不应该触发清理逻辑
     */
    @Override
    public void onRemovedFromLevel() {
        Entity.RemovalReason reason = this.getRemovalReason();
        // 只有在非卸载情况下才调用 onRemovedHelper
        // UNLOADED_TO_CHUNK: 区块卸载
        // UNLOADED_WITH_PLAYER: 玩家下线时实体卸载
        if (reason != null
            && reason != Entity.RemovalReason.UNLOADED_TO_CHUNK
            && reason != Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            this.onRemovedHelper(this);
        }
        super.onRemovedFromLevel();
    }

    /**
     * 受到伤害时的处理
     */
    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        if (this.shouldIgnoreDamage(source)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    /**
     * 判断是否与实体为同盟
     */
    @Override
    public boolean isAlliedTo(@Nonnull Entity entity) {
        return super.isAlliedTo(entity) || this.isAlliedHelper(entity);
    }

    private boolean handleFluidContact() {
        if (!this.isInFluidType()) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();
        serverLevel.playSound(null, this.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
        serverLevel.sendParticles(
            ParticleTypes.LARGE_SMOKE,
            this.getX(), this.getY(), this.getZ(),
            20, 0.5, 0.5, 0.5, 0.1
        );
        this.ejectPassengers();
        this.discard();
        return true;
    }

    private void setSummonerUUID(UUID summonerUUID) {
        this.summonerUUID = summonerUUID;
    }

    private void setMaidUUID(UUID maidUUID) {
        this.maidUUID = maidUUID;
    }
}
