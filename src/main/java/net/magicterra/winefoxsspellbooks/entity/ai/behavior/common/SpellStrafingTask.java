package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 女仆走位控制
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-02 00:35
 */
public class SpellStrafingTask extends Behavior<PathfinderMob> {
    private final float projectileRange;

    private final float strafeSpeed;

    private boolean strafingClockwise;

    /**
     * 倒退连续被挡住多少刻后才改为寻路远离（会转身背对敌人）。
     */
    private static final int BLOCKED_RETREAT_TICKS = 20;

    private int fleeCooldown;

    private int blockedRetreatTicks;

    private int strafingTime = -1;

    public SpellStrafingTask(float projectileRange, float strafeSpeed) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
                MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(), MemoryStatus.REGISTERED),
            1200);
        this.projectileRange = projectileRange;
        this.strafeSpeed = strafeSpeed;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, PathfinderMob owner) {
        if (!(owner instanceof IMagicEntity)) {
            return false;
        }
        if (owner instanceof EntityMaid maid && maid.isOrderedToSit()) {
            return false;
        }
        if (owner.getBrain().checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT)) {
            return false;
        }
        return owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(LivingEntity::isAlive).isPresent() ||
            owner.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()).filter(LivingEntity::isAlive).isPresent();
    }

    @Override
    protected void tick(ServerLevel worldIn, PathfinderMob owner, long gameTime) {
        if (!(owner instanceof IMagicEntity magicEntity)) {
            return;
        }
        MaidSpellAction maidSpellAction = owner.getBrain().getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get()).orElse(MaidSpellAction.ATTACK);
        LivingEntity attackTarget = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        LivingEntity supportTarget = owner.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()).orElse(null);
        LivingEntity target;
        switch (maidSpellAction) {
            case POSITIVE, SUPPORT_OTHER -> target = supportTarget;
            default -> target = attackTarget;
        }
        SpellData spellData = owner.getBrain().getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get()).orElse(null);

        // 如果目标为空，提前返回
        if (target == null) {
            return;
        }

        boolean canSee;
        if (owner instanceof EntityMaid maid) {
            canSee = maid.canSee(target);
        } else {
            canSee = BehaviorUtils.canSee(owner, target);
        }

        float movementSpeed = (float) (strafeSpeed * owner.getAttributeValue(Attributes.MOVEMENT_SPEED));

        double distanceSquared = owner.distanceToSqr(target);
        double spellRange = spellData == null ? projectileRange : MaidSpellCatalog.getSpellRange(spellData.getSpell());
        double spellRangeSqr = spellRange * spellRange;

        float speed = (magicEntity.isCasting() ? .75f : 1f) * movementSpeed;
        // 保持距离, 拉近距离, 或者四处走位
        float fleeDist = 0.5f;
        float ss = 1.2f;
        if (++strafingTime > 50) {
            if (owner.getRandom().nextDouble() < .1) {
                strafingClockwise = !strafingClockwise;
                strafingTime = 0;
            }
        }
        int strafeDir = strafingClockwise ? 1 : -1; // 左右走位方向
        // 跟随模式下的活动范围：走位一旦把女仆推出去，TLM 的跟随行为就会把视线和行走目标都改成主人
        LivingEntity leashOwner = MaidLeash.leashOwner(owner);
        double leashRadius = leashOwner != null ? MaidLeash.radius((EntityMaid) owner) : 0;
        --fleeCooldown;
        if (!magicEntity.isCasting() && distanceSquared < spellRangeSqr * (fleeDist * fleeDist)) {
            if (owner.horizontalCollision) {
                blockedRetreatTicks++;
            } else {
                blockedRetreatTicks = 0;
            }
            if (blockedRetreatTicks > BLOCKED_RETREAT_TICKS && fleeCooldown <= 0) {
                // 背后被堵死：只能寻路绕出去，此时才允许转身
                Vec3 flee = DefaultRandomPos.getPosAway(owner, Mth.clamp(Mth.floor(spellRange), 1, 10), 4, target.position());
                if (flee != null && (leashOwner == null || MaidLeash.contains(leashOwner, leashRadius, flee))) {
                    owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(flee, speed * 1.5F, 1));
                }
                blockedRetreatTicks = 0;
                fleeCooldown = 100;
            } else {
                // 面向敌人后撤：寻路后撤会让女仆转向移动方向、背对敌人，这里改为倒退并带少量横移，
                // 身体始终对准目标。原版 MoveControl 横移前会检查落脚点，不可走时改为向前一步，
                // 所以不会倒退下悬崖。
                owner.getNavigation().stop();
                if (owner.horizontalCollision) {
                    // 倒退被挡住时换个横移方向绕开，并尝试跳上台阶
                    if (owner.getRandom().nextFloat() < .2f) {
                        strafingClockwise = !strafingClockwise;
                        strafeDir = -strafeDir;
                    }
                    tryJump(owner);
                }
                faceTarget(owner, target);
                float forward = -speed * ss;
                float strafe = speed * strafeDir * ss * .5f;
                strafeWithinLeash(owner, leashOwner, leashRadius, forward, strafe);
            }
        } else if (distanceSquared < spellRangeSqr && canSee) {
            blockedRetreatTicks = 0;
            // 在敌人附近
            owner.getNavigation().stop();
            faceTarget(owner, target);
            float strafeForward = (distanceSquared * 6 < spellRangeSqr ? -1.2f : .5f) * movementSpeed; // 前后走位矢量
            float forward = strafeForward * ss;
            float strafe = speed * strafeDir * ss;
            strafeWithinLeash(owner, leashOwner, leashRadius, forward, strafe);
            if (owner.getNavigation().isStuck() || checkNeedJump(owner, forward, strafe)) {
                owner.getJumpControl().jump();
            }
            if (owner.horizontalCollision && owner.getRandom().nextFloat() < .1f) {
                tryJump(owner);
            }
        } else if (distanceSquared > spellRangeSqr && canSee) {
            // 超出施法范围，拉近。跟随模式下不自行寻路：随机点可能落在活动范围外，
            // 由 SpellAttackWalkToTarget 走到活动范围边缘的接战点
            if (leashOwner == null) {
                Vec3 towards = DefaultRandomPos.getPosTowards(owner, Mth.floor(spellRange), 4, target.position(), Mth.HALF_PI);
                if (towards != null) {
                    owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(towards, speed, 1));
                } else {
                    float forward = speed * ss;
                    float strafe = speed * strafeDir * ss;
                    owner.getMoveControl().strafe(forward, strafe);
                }
            }
            faceTarget(owner, target);
            fleeCooldown = 100;
        }
        BehaviorUtils.lookAtEntity(owner, target);
    }

    @Override
    protected void start(ServerLevel worldIn, PathfinderMob entityIn, long gameTimeIn) {
        blockedRetreatTicks = 0;
    }

    @Override
    protected void stop(ServerLevel worldIn, PathfinderMob entityIn, long gameTimeIn) {
        entityIn.getMoveControl().strafe(0, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, PathfinderMob entityIn, long gameTimeIn) {
        return this.checkExtraStartConditions(worldIn, entityIn);
    }

    /**
     * 身体直接对准目标，头部也转向目标。
     * <p>
     * 以前是让身体对齐头部朝向，但头部由 LOOK_TARGET 驱动，TLM 的跟随主人行为在战斗中也会把它改成主人； 身体跟着头转、横移又是相对身体朝向计算的，女仆就会面朝主人绕着主人转圈。
     */
    private static void faceTarget(PathfinderMob mob, LivingEntity target) {
        float yaw = (float) (Mth.atan2(target.getZ() - mob.getZ(), target.getX() - mob.getX()) * Mth.RAD_TO_DEG) - 90.0F;
        mob.setYRot(yaw);
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    /**
     * 跟随模式下走位不出活动范围：前后分量会越界就取消，横移会越界就换向，仍越界则原地站定。 已经在范围外时，朝主人方向收拢的位移也放行，否则女仆会在边界外原地僵住。 调用前须先 {@link #faceTarget}，越界判断按当前朝向换算位移。
     */
    private void strafeWithinLeash(PathfinderMob mob, @Nullable LivingEntity leashOwner, double radius,
                                   float forward, float strafe) {
        if (leashOwner != null && !leashAllows(mob, leashOwner, radius, forward, strafe)) {
            if (leashAllows(mob, leashOwner, radius, 0, strafe)) {
                forward = 0;
            } else if (leashAllows(mob, leashOwner, radius, 0, -strafe)) {
                strafingClockwise = !strafingClockwise;
                forward = 0;
                strafe = -strafe;
            } else {
                forward = 0;
                strafe = 0;
            }
        }
        mob.getMoveControl().strafe(forward, strafe);
    }

    private static boolean leashAllows(PathfinderMob mob, LivingEntity leashOwner, double radius,
                                       float forward, float strafe) {
        Vec3 step = stepTarget(mob, forward, strafe);
        return MaidLeash.contains(leashOwner, radius, step)
            || step.distanceToSqr(leashOwner.position()) < mob.distanceToSqr(leashOwner);
    }

    /**
     * 以当前朝向执行这组走位、前进一格后的位置，换算方式与 {@code Entity#getInputVector} 一致。
     */
    private static Vec3 stepTarget(PathfinderMob mob, float forward, float strafe) {
        double length = Math.sqrt(forward * forward + strafe * strafe);
        if (length < 1.0E-4) {
            return mob.position();
        }
        float sin = Mth.sin(mob.getYRot() * Mth.DEG_TO_RAD);
        float cos = Mth.cos(mob.getYRot() * Mth.DEG_TO_RAD);
        return mob.position().add((strafe * cos - forward * sin) / length, 0, (forward * cos + strafe * sin) / length);
    }

    private boolean checkNeedJump(PathfinderMob maid, float forward, float strafe) {
        BlockPos targetBlockPos = getStrafeTargetBlockPos(maid, forward, strafe, 1);
        return maid.level.getBlockState(targetBlockPos).isSolidRender(maid.level, targetBlockPos) &&
            maid.level.getBlockState(targetBlockPos.above()).isAir() &&
            maid.level.getBlockState(targetBlockPos.above(2)).isAir();
    }

    private BlockPos getStrafeTargetBlockPos(PathfinderMob maid, float forward, float strafe, float factor) {
        Vec3 facing = maid.getForward();
        Vec3 position = maid.position();
        Vec2 offset = new Vec2(forward, strafe).normalized().scale(factor);
        Vec3 nextPosition = position.add(facing.x * offset.x + facing.z * offset.y, 0, facing.z * offset.x + facing.x * offset.y);
        return BlockPos.containing(nextPosition.x, nextPosition.y, nextPosition.z);
    }

    protected void tryJump(PathfinderMob maid) {
        Vec3 nextBlock = new Vec3(maid.xxa, 0, maid.zza).normalize();

        BlockPos blockpos = BlockPos.containing(maid.position().add(nextBlock));
        BlockState blockstate = maid.level.getBlockState(blockpos);
        VoxelShape voxelshape = blockstate.getCollisionShape(maid.level, blockpos);
        if (!voxelshape.isEmpty() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
            BlockPos blockposAbove = blockpos.above();
            BlockState blockstateAbove = maid.level.getBlockState(blockposAbove);
            VoxelShape voxelshapeAbove = blockstateAbove.getCollisionShape(maid.level, blockposAbove);
            if (voxelshapeAbove.isEmpty()) {
                maid.getJumpControl().jump();
                //boost to get over the edge
                maid.setXxa(maid.xxa * 5);
                maid.setZza(maid.zza * 5);
            }
        }
    }
}
