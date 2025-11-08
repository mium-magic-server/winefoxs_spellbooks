package net.magicterra.winefoxsspellbooks.entity.ai.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 女仆走位控制
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-02 00:35
 */
public class MaidSpellStrafingTask extends Behavior<EntityMaid> {
    private final float projectileRange;
    private final float strafeSpeed;
    private boolean strafingClockwise;
    private int fleeCooldown;
    private int strafingTime = -1;

    public MaidSpellStrafingTask(float projectileRange, float strafeSpeed) {
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
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid owner) {
        return owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(LivingEntity::isAlive).isPresent() ||
            owner.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()).filter(LivingEntity::isAlive).isPresent();
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityMaid owner, long gameTime) {
        IMagicEntity magicEntity = (IMagicEntity) owner;
        MaidSpellAction maidSpellAction = owner.getBrain().getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get()).orElse(MaidSpellAction.ATTACK);
        LivingEntity attackTarget = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        LivingEntity supportTarget = owner.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()).orElse(null);
        LivingEntity target;
        switch (maidSpellAction) {
            case POSITIVE, SUPPORT_OTHER -> target = supportTarget;
            default -> target = attackTarget;
        }
        SpellData spellData = owner.getBrain().getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get()).orElse(null);

        float movementSpeed = (float) (strafeSpeed * owner.getAttributeValue(Attributes.MOVEMENT_SPEED));

        double distanceSquared = owner.distanceToSqr(target);
        double spellRange = spellData == null ? projectileRange : MaidSpellRegistry.getSpellRange(spellData.getSpell());
        double spellRangeSqr = spellRange * spellRange;


        float speed = (magicEntity.isCasting() ? .75f : 1f) * movementSpeed;
        // 保持距离, 拉近距离, 或者四处走位
        float fleeDist = 0.5f;
        float ss = 1.2f;
        if (!magicEntity.isCasting() && --fleeCooldown <= 0 && distanceSquared < spellRangeSqr * (fleeDist * fleeDist)) {
            // 尝试远离敌人
            Vec3 flee = DefaultRandomPos.getPosAway(owner, Mth.floor(spellRange), 7, target.position());
            if (flee != null) {
                owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(flee, (float) (speed * 1.5), 1));
            } else {
                owner.getMoveControl().strafe(-(float) speed * ss, (float) speed * ss);
            }
            fleeCooldown = 100;
        } else if (distanceSquared < spellRangeSqr && owner.canSee(target)) {
            // 在敌人附近
            owner.getNavigation().stop();
            if (++strafingTime > 50) {
                if (owner.getRandom().nextDouble() < .1) {
                    strafingClockwise = !strafingClockwise;
                    strafingTime = 0;
                }
            }
            float strafeForward = (distanceSquared * 6 < spellRangeSqr ? -1.2f : .5f) * movementSpeed; // 前后走位矢量
            int strafeDir = strafingClockwise ? 1 : -1; // 左右走位方向
            owner.getMoveControl().strafe(strafeForward * ss, speed * strafeDir * ss);
            if (owner.horizontalCollision && owner.getRandom().nextFloat() < .1f) {
                tryJump(owner);
            }
            owner.setYRot(Mth.rotateIfNecessary(owner.getYRot(), owner.yHeadRot, 0.0F));
        }
        BehaviorUtils.lookAtEntity(owner, target);
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        entityIn.getMoveControl().strafe(0, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        return this.checkExtraStartConditions(worldIn, entityIn);
    }

    protected void tryJump(EntityMaid maid) {
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
