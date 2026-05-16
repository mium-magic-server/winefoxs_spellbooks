package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidGiftDelivery;
import net.magicterra.winefoxsspellbooks.event.MaidGiftPickupBlocker;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 女仆晨赠交付行为
 * <p>
 * 仿原版猫的 {@code CatRelaxOnOwnerGoal#giveMorningGift}，
 * 但改为 Brain 驱动：监听到 {@link MaidCastingMemoryModuleTypes#GIFT_DELIVERY} 记忆后，
 * 走向目标玩家，到达后挥手并以朝向玩家的初速度投出礼物。
 * <p>
 * 超时（{@link #MAX_DURATION_TICKS}）仍未到达时，会在女仆当前位置兜底掉落，避免礼物吞掉。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-15
 */
public class MaidDeliverGiftBehavior extends Behavior<EntityMaid> {
    /** 视为"已到达玩家"的距离平方（约 2 格） */
    private static final double DELIVER_DISTANCE_SQR = 4.0D;

    /** 移动速度 */
    private static final float WALK_SPEED = 0.8F;

    /** 行为最长持续 tick（兜底，避免被卡住时礼物悬挂） */
    private static final int MAX_DURATION_TICKS = 200;

    /** 抛出方向上的初速度系数 */
    private static final double THROW_HORIZONTAL_VELOCITY = 0.3D;

    /** 额外竖直分量，形成抛物线 */
    private static final double THROW_VERTICAL_VELOCITY = 0.18D;

    public MaidDeliverGiftBehavior() {
        super(ImmutableMap.of(
            MaidCastingMemoryModuleTypes.GIFT_DELIVERY.get(), MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
            MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ), MAX_DURATION_TICKS);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return hasValidDelivery(level, maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        // EntityTracker 会持续跟随玩家位置，MoveToTargetSink 会按需重新寻路，
        // 因此不必每 tick 重写 WALK_TARGET/LOOK_TARGET
        getDelivery(maid).ifPresent(d -> BehaviorUtils.setWalkAndLookTargetMemories(maid, d.target(), WALK_SPEED, 1));
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return hasValidDelivery(level, maid);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        getDelivery(maid).ifPresent(delivery -> {
            Player target = delivery.target();
            if (maid.distanceToSqr(target) <= DELIVER_DISTANCE_SQR) {
                deliver(level, maid, target, delivery.drops());
                maid.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.GIFT_DELIVERY.get());
            }
        });
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        // 超时但还有礼物没送出 -> 在女仆当前位置兜底掉落，避免吞物
        getDelivery(maid).ifPresent(d -> {
            Player target = d.target();
            // 用 isTargetValid 而不是裸 isAlive：玩家跨维度时 target 仍存活但坐标在另一个 world，
            // 直接相减会得到一个跨维度的怪向量，需要回退到只有竖直分量的兜底掉落。
            Vec3 throwDir = isTargetValid(target, level)
                ? target.position().subtract(maid.position()).normalize()
                : Vec3.ZERO;
            spawnDrops(level, maid, d.drops(), throwDir);
            maid.swing(InteractionHand.MAIN_HAND);
        });
        maid.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.GIFT_DELIVERY.get());
    }

    private boolean hasValidDelivery(ServerLevel level, EntityMaid maid) {
        return getDelivery(maid).map(d -> isTargetValid(d.target(), level)).orElse(false);
    }

    private static Optional<MaidGiftDelivery> getDelivery(EntityMaid maid) {
        return maid.getBrain().getMemory(MaidCastingMemoryModuleTypes.GIFT_DELIVERY.get());
    }

    private static boolean isTargetValid(Player target, ServerLevel level) {
        return target != null && target.isAlive() && target.level() == level;
    }

    private void deliver(ServerLevel level, EntityMaid maid, Player target, List<ItemStack> drops) {
        maid.swing(InteractionHand.MAIN_HAND);

        Vec3 from = maid.position().add(0, maid.getBbHeight() * 0.6D, 0);
        Vec3 toward = target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(from);
        Vec3 dir = toward.lengthSqr() > 1.0E-4D ? toward.normalize() : Vec3.ZERO;
        spawnDrops(level, maid, drops, dir);

        level.sendParticles(ParticleTypes.HEART,
            from.x, maid.getY() + maid.getBbHeight() + 0.2D, from.z,
            5, 0.3D, 0.2D, 0.3D, 0.0D);
    }

    private static void spawnDrops(ServerLevel level, EntityMaid maid, List<ItemStack> drops, Vec3 throwDir) {
        if (drops.isEmpty()) {
            return;
        }
        Vec3 from = maid.position().add(0, maid.getBbHeight() * 0.6D, 0);
        Vec3 velocity = throwDir.lengthSqr() > 1.0E-4D
            ? throwDir.scale(THROW_HORIZONTAL_VELOCITY).add(0, THROW_VERTICAL_VELOCITY, 0)
            : new Vec3(0, THROW_VERTICAL_VELOCITY, 0);
        for (ItemStack stack : drops) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemEntity item = new ItemEntity(level, from.x, from.y, from.z, stack.copy());
            item.setDeltaMovement(velocity);
            item.setDefaultPickUpDelay();
            // setThrower：让 vanilla 的 10-tick 投手自身拾取延迟生效，避免投出去立刻又被自己捡回
            item.setThrower(maid);
            // 给 ItemEntity 打上"晨赠"标记；MaidGiftPickupBlocker 据此拦截女仆拾取（仅限本模组礼物，
            // 避免误伤其他模组里由女仆投出但允许女仆拾取的物品）
            item.addTag(MaidGiftPickupBlocker.MAID_GIFT_TAG);
            level.addFreshEntity(item);
        }
    }
}
