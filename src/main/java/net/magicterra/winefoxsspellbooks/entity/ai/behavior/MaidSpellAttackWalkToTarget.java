package net.magicterra.winefoxsspellbooks.entity.ai.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.OptionalBox;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.function.Function;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 修改了原版的走向攻击目标的 AI，现在能够依据法术攻击距离和 home 范围进行行走判断
 */
public class MaidSpellAttackWalkToTarget {
    public static BehaviorControl<Mob> create(float speedModifier) {
        return create(entity -> speedModifier);
    }

    public static BehaviorControl<Mob> create(Function<LivingEntity, Float> speedModifier) {
        return BehaviorBuilder.create(maidInstance -> maidInstance.group(
                        maidInstance.registered(MemoryModuleType.WALK_TARGET),
                        maidInstance.registered(MemoryModuleType.LOOK_TARGET),
                        maidInstance.present(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get()),
                        maidInstance.registered(MemoryModuleType.ATTACK_TARGET),
                        maidInstance.registered(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()),
                        maidInstance.registered(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
                .apply(maidInstance, (
                    walkTargetMemory,
                    positionMemory,
                    spellDataMemory,
                    attackTargetMemory,
                    supportTargetMemory,
                    livingEntitiesMemory
                ) -> setTarget(speedModifier, maidInstance, walkTargetMemory, positionMemory, spellDataMemory, attackTargetMemory, supportTargetMemory, livingEntitiesMemory)));
    }

    @NotNull
    private static Trigger<Mob> setTarget(Function<LivingEntity, Float> speedModifier,
                                                 BehaviorBuilder.Instance<Mob> maidInstance,
                                                 MemoryAccessor<OptionalBox.Mu, WalkTarget> walkTargetMemory,
                                                 MemoryAccessor<OptionalBox.Mu, PositionTracker> positionMemory,
                                                 MemoryAccessor<IdF.Mu, SpellData> spellDataMemory,
                                                 MemoryAccessor<OptionalBox.Mu, LivingEntity> attackTargetMemory,
                                                 MemoryAccessor<OptionalBox.Mu, LivingEntity> supportTargetMemory,
                                                 MemoryAccessor<OptionalBox.Mu, NearestVisibleLivingEntities> livingEntitiesMemory) {
        return (level, mob, gameTime) -> {
            LivingEntity attackTarget = maidInstance.tryGet(attackTargetMemory).orElse(null);
            LivingEntity supportTarget = maidInstance.tryGet(supportTargetMemory).orElse(null);
            LivingEntity target = attackTarget == null ? supportTarget : attackTarget;
            if (target == null) {
                return false;
            }
            boolean canSee;
            if (mob instanceof EntityMaid maid) {
                canSee = maid.canSee(target);
            } else {
                canSee = BehaviorUtils.canSee(mob, target);
            }
            boolean shouldEraseWalkTarget;
            if (mob instanceof TamableAnimal tamableAnimal) {
                shouldEraseWalkTarget = shouldEraseWalkTarget(tamableAnimal, target, maidInstance.get(spellDataMemory));
            } else {
                shouldEraseWalkTarget = shouldEraseWalkTarget(mob, target, maidInstance.get(spellDataMemory));
            }
            if (canSee && shouldEraseWalkTarget) {
                walkTargetMemory.erase();
            } else {
                positionMemory.set(new EntityTracker(target, true));
                walkTargetMemory.set(new WalkTarget(new EntityTracker(target, false), speedModifier.apply(mob), 0));
            }
            return true;
        };
    }

    private static boolean shouldEraseWalkTarget(TamableAnimal tamableAnimal, LivingEntity target, SpellData spellData) {
        float restrictRadius = tamableAnimal.getRestrictRadius() - 2;
        double checkRadius = 8;
        if (tamableAnimal.hasRestriction()) {
            BlockPos center = tamableAnimal.getRestrictCenter();
            checkRadius = Math.sqrt(target.distanceToSqr(center.getX(), center.getY(), center.getZ()));
        } else if (tamableAnimal.getOwner() instanceof Player player) {
            checkRadius = target.distanceTo(player);
        }
        // 在 最大限制范围-2 外，则清除目标
        if (checkRadius >= restrictRadius) {
            return true;
        }
        float reachableRange = MaidSpellRegistry.getSpellRange(spellData.getSpell());
        // 已到达施法范围内，清除目标
        return tamableAnimal.distanceTo(target) <= reachableRange;
    }

    private static boolean shouldEraseWalkTarget(Mob mob, LivingEntity target, SpellData spellData) {
        float restrictRadius = mob.getRestrictRadius() - 2;
        double checkRadius = 8;
        if (mob.hasRestriction()) {
            BlockPos center = mob.getRestrictCenter();
            checkRadius = Math.sqrt(target.distanceToSqr(center.getX(), center.getY(), center.getZ()));
        }
        // 在 最大限制范围-2 外，则清除目标
        if (checkRadius >= restrictRadius) {
            return true;
        }
        float reachableRange = MaidSpellRegistry.getSpellRange(spellData.getSpell());
        // 已到达施法范围内，清除目标
        return mob.distanceTo(target) <= reachableRange;
    }
}
