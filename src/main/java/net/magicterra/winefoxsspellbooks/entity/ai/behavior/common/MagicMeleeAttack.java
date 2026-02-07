package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/**
 * 女仆魔法近战行为
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-12 01:49
 */
public class MagicMeleeAttack {
    public static OneShot<Mob> create(int cooldownBetweenAttacks) {
        return BehaviorBuilder.create(
            instance -> instance.group(
                    instance.registered(MemoryModuleType.LOOK_TARGET),
                    instance.present(MemoryModuleType.ATTACK_TARGET),
                    instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN),
                    instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                )
                .apply(instance, (
                    lookTargetMemory,
                    attackTargetMemory,
                    cooldownMemory,
                    nearestEntitiesMemory) ->
                    (level, mob, gameTick) -> {
                        LivingEntity target = instance.get(attackTargetMemory);
                        if (mob instanceof IMagicEntity magicEntity && magicEntity.isCasting()) {
                            return false;
                        }
                        if (isHoldingUsableWeapon(mob) && mob.canAttack(target) && mob.isWithinMeleeAttackRange(target) && instance.get(nearestEntitiesMemory).contains(target)) {
                            lookTargetMemory.set(new EntityTracker(target, true));
                            mob.swing(InteractionHand.MAIN_HAND);
                            mob.doHurtTarget(target);
                            cooldownMemory.setWithExpiry(true, cooldownBetweenAttacks);
                            return true;
                        } else {
                            return false;
                        }
                    }
                )
        );
    }

    private static boolean isHoldingUsableWeapon(Mob mob) {
        return mob.isHolding(itemStack -> itemStack.getAttributeModifiers().modifiers()
            .stream()
            .anyMatch(modifier -> modifier.attribute().is(Attributes.ATTACK_DAMAGE)));
    }
}
