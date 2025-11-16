package net.magicterra.winefoxsspellbooks.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

/**
 * 女仆目标查找行为，仅在无支援目标时启动
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-16 17:39
 */
public class MaidStartAttacking {
    public static <E extends Mob> BehaviorControl<E> create(Predicate<E> canAttack, Function<E, Optional<? extends LivingEntity>> targetFinder) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.absent(MemoryModuleType.ATTACK_TARGET),
                instance.absent(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()),
                instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE))
            .apply(instance, (attackTargetMemory, supportTargetMemory, cantReachMemory) ->
                (serverLevel, mob, cantReachSince) -> {
                    if (!canAttack.test(mob)) {
                        return false;
                    } else {
                        Optional<? extends LivingEntity> optional = targetFinder.apply(mob);
                        if (optional.isEmpty()) {
                            return false;
                        } else {
                            LivingEntity livingentity = optional.get();
                            if (!mob.canAttack(livingentity)) {
                                return false;
                            } else {
                                LivingChangeTargetEvent changeTargetEvent = CommonHooks.onLivingChangeTarget(mob, livingentity, LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET);
                                if (changeTargetEvent.isCanceled() || changeTargetEvent.getNewAboutToBeSetTarget() == null)
                                    return false;

                                attackTargetMemory.set(changeTargetEvent.getNewAboutToBeSetTarget());
                                cantReachMemory.erase();
                                return true;
                            }
                        }
                    }
                })
        );
    }
}
