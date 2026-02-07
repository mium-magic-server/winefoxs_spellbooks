package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * 召唤物跟随召唤者的行为
 * <p>
 * 与 MaidFollowOwnerTask 保持完全一致的行为逻辑。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-12-29
 */
public class FollowSummonerBehavior extends Behavior<PathfinderMob> {

    /** 移动速度 */
    private final float speedModifier;

    /** 停止跟随的距离 */
    private final int stopDistance;

    /** 开始跟随的距离 */
    private static final int START_DISTANCE_OFFSET = 16;

    /** 传送距离偏移（基于 startDistance） */
    private static final int TELEPORT_DISTANCE_OFFSET = 4;

    /** 最大传送尝试次数 */
    private static final int MAX_TELEPORT_ATTEMPTS = 10;

    /** 向下搜索地面的最大距离 */
    private static final int MAX_GROUND_SEARCH_DEPTH = 32;

    /**
     * 构造跟随召唤者行为
     *
     * @param speedModifier 移动速度修正
     * @param stopDistance  停止跟随的距离
     */
    public FollowSummonerBehavior(float speedModifier, int stopDistance) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
        this.speedModifier = speedModifier;
        this.stopDistance = stopDistance;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
        if (!(mob instanceof IMagicSummon summon)) {
            return false;
        }

        if (mob instanceof SummonedEntityMaid maid && maid.isAirForce() && maid.isPassenger()) {
            return false;
        }

        Entity summoner = summon.getSummoner();
        if (!(summoner instanceof LivingEntity livingSummoner)) {
            return false;
        }
        return summonerStateConditions(livingSummoner, mob);
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        if (!(mob instanceof IMagicSummon summon)) {
            return;
        }

        Entity summonerEntity = summon.getSummoner();
        if (!(summonerEntity instanceof LivingEntity summoner)) {
            return;
        }
        if (!summonerStateConditions(summoner, mob)) {
            return;
        }

        // 计算距离阈值（与原版 MaidFollowOwnerTask 一致）
        int startDistance = START_DISTANCE_OFFSET;
        if (startDistance < 5) {
            startDistance = 10; // 默认值
        }
        int minTeleportDistance = startDistance + TELEPORT_DISTANCE_OFFSET;

        // 检查是否需要跟随
        if (!mob.closerThan(summoner, startDistance)) {
            if (!mob.closerThan(summoner, minTeleportDistance)) {
                // 距离过远，传送
                teleportToSummoner(mob, summoner);
            } else if (!summonerIsWalkTarget(mob, summoner)) {
                // 设置行走和看向目标
                BehaviorUtils.setWalkAndLookTargetMemories(mob, summoner, speedModifier, stopDistance);
            }
        }
    }

    /**
     * 检查召唤者状态是否满足条件
     * <p>
     * 与原版 ownerStateConditions 保持一致
     */
    private boolean summonerStateConditions(LivingEntity summoner, PathfinderMob mob) {
        return summoner != null
            && !summoner.isSpectator()
            && !summoner.isDeadOrDying()
            && mob.level() == summoner.level(); // 维度检查
    }

    /**
     * 检查召唤者是否已经是行走目标
     * <p>
     * 与原版 ownerIsWalkTarget 保持一致
     */
    private boolean summonerIsWalkTarget(PathfinderMob mob, LivingEntity summoner) {
        return mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).map(target -> {
            if (target.getTarget() instanceof EntityTracker tracker) {
                return tracker.getEntity().equals(summoner);
            }
            return false;
        }).orElse(false);
    }

    /**
     * 传送到召唤者附近
     * <p>
     * 当召唤者在空中时，会向下搜索最近的地面位置
     */
    private boolean teleportToSummoner(PathfinderMob mob, LivingEntity summoner) {
        BlockPos summonerPos = summoner.blockPosition();
        RandomSource random = mob.getRandom();

        // 首先尝试在召唤者位置附近传送
        for (int i = 0; i < MAX_TELEPORT_ATTEMPTS; ++i) {
            int x = randomIntInclusive(random, -3, 3);
            int y = randomIntInclusive(random, -1, 1);
            int z = randomIntInclusive(random, -3, 3);
            if (maybeTeleportTo(mob, summoner, summonerPos.getX() + x, summonerPos.getY() + y, summonerPos.getZ() + z)) {
                return true;
            }
        }

        // 如果召唤者附近传送失败（可能在空中），向下搜索地面
        BlockPos groundPos = findGroundBelow(mob, summonerPos);
        if (groundPos != null) {
            for (int i = 0; i < MAX_TELEPORT_ATTEMPTS; ++i) {
                int x = randomIntInclusive(random, -3, 3);
                int z = randomIntInclusive(random, -3, 3);
                // 在地面位置附近尝试传送
                if (maybeTeleportTo(mob, summoner, groundPos.getX() + x, groundPos.getY(), groundPos.getZ() + z)) {
                    return true;
                }
            }
            // 如果随机位置都失败，尝试直接传送到地面位置
            if (maybeTeleportTo(mob, summoner, groundPos.getX(), groundPos.getY(), groundPos.getZ())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 向下搜索最近的可站立地面位置
     *
     * @param mob 需要传送的实体
     * @param startPos 开始搜索的位置
     * @return 地面位置，如果找不到则返回 null
     */
    private BlockPos findGroundBelow(PathfinderMob mob, BlockPos startPos) {
        for (int y = 0; y < MAX_GROUND_SEARCH_DEPTH; y++) {
            BlockPos checkPos = startPos.below(y);
            if (canTeleportTo(mob, checkPos)) {
                return checkPos;
            }
        }
        return null;
    }

    /**
     * 尝试传送到指定位置
     * <p>
     * 与原版 maybeTeleportTo 保持一致
     */
    private boolean maybeTeleportTo(PathfinderMob mob, LivingEntity summoner, int x, int y, int z) {
        if (teleportTooClosed(summoner, x, z)) {
            return false;
        } else if (!canTeleportTo(mob, new BlockPos(x, y, z))) {
            return false;
        } else {
            mob.moveTo(x + 0.5, y, z + 0.5, mob.getYRot(), mob.getXRot());
            mob.getNavigation().stop();
            // 与原版一致，清除相关 Memory
            mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.PATH);
            return true;
        }
    }

    /**
     * 检查传送位置是否太近
     * <p>
     * 与原版 teleportTooClosed 保持一致
     */
    private boolean teleportTooClosed(LivingEntity summoner, int x, int z) {
        return Math.abs(x - summoner.getX()) < 2 && Math.abs(z - summoner.getZ()) < 2;
    }

    /**
     * 检查是否可以传送到指定位置
     * <p>
     * 与原版 canTeleportTo 保持一致，支持 WALKABLE 和 WATER
     */
    private boolean canTeleportTo(PathfinderMob mob, BlockPos pos) {
        PathType pathType = WalkNodeEvaluator.getPathTypeStatic(mob, pos);
        if (pathType == PathType.WALKABLE || pathType == PathType.WATER) {
            BlockPos blockPos = pos.subtract(mob.blockPosition());
            return mob.level().noCollision(mob, mob.getBoundingBox().move(blockPos));
        }
        return false;
    }

    /**
     * 生成指定范围内的随机整数
     * <p>
     * 与原版 randomIntInclusive 保持一致
     */
    private int randomIntInclusive(RandomSource random, int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
