package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.util.LineOfSightGuard;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

/**
 * 协同攻击和紧急支援行为
 * <p>
 * 参考 MaidMagicSupportTask 的设计，实现两种支援模式：
 * 1. 协同攻击：查找盟友正在攻击的敌人，设置为 ATTACK_TARGET
 * 2. 紧急支援：查找生命危急的盟友，设置为 SUPPORT_TARGET
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-03-07
 */
public class StartSupporting {
    /**
     * 支援搜索范围
     */
    private static final double SUPPORT_SEARCH_RANGE = 16.0;

    /**
     * 紧急支援的生命值阈值（百分比）
     */
    private static final double CRITICAL_HEALTH_THRESHOLD = 0.3;

    /**
     * 主人紧急支援的生命值阈值
     */
    private static final double OWNER_CRITICAL_HEALTH_THRESHOLD = 0.4;

    /**
     * 创建协同攻击行为（查找盟友正在攻击的敌人）
     *
     * @param canSupport 是否可以支援的条件
     * @param <E>        实体类型
     * @return 行为控制器
     */
    public static <E extends Mob> BehaviorControl<E> create(Predicate<E> canSupport) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.absent(MemoryModuleType.ATTACK_TARGET),
                instance.absent(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()),
                instance.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES))
            .apply(instance, (attackTargetMemory, supportTargetMemory, nearestLivingEntitiesMemory) ->
                (serverLevel, mob, gameTime) -> {
                    if (!canSupport.test(mob)) {
                        return false;
                    }

                    // 优先级1：查找盟友正在攻击的敌人（协同攻击）
                    Optional<? extends LivingEntity> allyTarget = findAllyAttackTarget(mob);
                    if (allyTarget.isPresent()) {
                        LivingEntity target = allyTarget.get();
                        if (mob.canAttack(target)) {
                            LivingChangeTargetEvent changeTargetEvent = CommonHooks.onLivingChangeTarget(
                                mob, target, LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET);
                            if (!changeTargetEvent.isCanceled() && changeTargetEvent.getNewAboutToBeSetTarget() != null) {
                                attackTargetMemory.set(changeTargetEvent.getNewAboutToBeSetTarget());
                                return true;
                            }
                        }
                    }

                    // 优先级2：查找生命危急的盟友（紧急支援）
                    Optional<? extends LivingEntity> criticalAlly = findCriticalAlly(mob);
                    if (criticalAlly.isPresent()) {
                        supportTargetMemory.set(criticalAlly.get());
                        return true;
                    }

                    return false;
                })
        );
    }

    /**
     * 查找盟友正在攻击的敌人（协同攻击）
     *
     * @param mob 当前实体
     * @return 盟友正在攻击的敌人
     */
    private static Optional<? extends LivingEntity> findAllyAttackTarget(Mob mob) {
        // 检查主人正在攻击的敌人
        LivingEntity owner = mob instanceof net.minecraft.world.entity.TamableAnimal tamable ? tamable.getOwner() : null;
        if (owner != null) {
            LivingEntity ownerTarget = owner.getLastHurtByMob();
            if (ownerTarget != null && ownerTarget.isAlive() && mob.canAttack(ownerTarget) && LineOfSightGuard.hasLineOfSight(mob, ownerTarget)) {
                return Optional.of(ownerTarget);
            }
        }

        // 检查附近盟友正在攻击的敌人
        AABB searchBox = mob.getBoundingBox().inflate(SUPPORT_SEARCH_RANGE, 4, SUPPORT_SEARCH_RANGE);
        List<LivingEntity> nearbyEntities = mob.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        nearbyEntities.sort(Comparator.comparingDouble(mob::distanceToSqr));

        for (LivingEntity entity : nearbyEntities) {
            // 跳过自己
            if (entity == mob) {
                continue;
            }

            // 必须是盟友
            if (!mob.isAlliedTo(entity)) {
                continue;
            }

            // 必须存活
            if (!entity.isAlive()) {
                continue;
            }

            // 检查盟友是否有攻击目标
            if (!entity.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)) {
                continue;
            }

            LivingEntity allyTarget = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (allyTarget != null && allyTarget.isAlive() && mob.canAttack(allyTarget) && LineOfSightGuard.hasLineOfSight(mob, allyTarget)) {
                return Optional.of(allyTarget);
            }
        }

        return Optional.empty();
    }

    /**
     * 查找生命危急的盟友（紧急支援）
     *
     * @param mob 当前实体
     * @return 生命危急的盟友
     */
    private static Optional<? extends LivingEntity> findCriticalAlly(Mob mob) {
        // 检查主人是否生命危急
        LivingEntity owner = mob instanceof net.minecraft.world.entity.TamableAnimal tamable ? tamable.getOwner() : null;
        if (owner != null && isCritical(owner, OWNER_CRITICAL_HEALTH_THRESHOLD) && LineOfSightGuard.hasLineOfSight(mob, owner)) {
            return Optional.of(owner);
        }

        // 检查附近盟友
        AABB searchBox = mob.getBoundingBox().inflate(SUPPORT_SEARCH_RANGE, 4, SUPPORT_SEARCH_RANGE);
        List<LivingEntity> nearbyEntities = mob.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        nearbyEntities.sort(Comparator.comparingDouble(mob::distanceToSqr));

        for (LivingEntity entity : nearbyEntities) {
            // 跳过自己
            if (entity == mob) {
                continue;
            }

            // 必须是盟友
            if (!mob.isAlliedTo(entity)) {
                continue;
            }

            // 必须存活
            if (!entity.isAlive()) {
                continue;
            }

            // 必须可见
            if (!LineOfSightGuard.hasLineOfSight(mob, entity)) {
                continue;
            }

            // 检查是否生命危急
            if (isCritical(entity, CRITICAL_HEALTH_THRESHOLD)) {
                return Optional.of(entity);
            }
        }

        return Optional.empty();
    }

    /**
     * 判断实体是否生命危急
     *
     * @param entity    实体
     * @param threshold 生命值阈值
     * @return 是否生命危急
     */
    private static boolean isCritical(LivingEntity entity, double threshold) {
        // 生命值低于阈值
        return entity.getHealth() / entity.getMaxHealth() < threshold;
    }
}
