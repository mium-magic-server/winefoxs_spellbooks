package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.OptionalBox;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.function.Function;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 修改了原版的走向攻击目标的 AI，现在能够依据法术攻击距离和 home 范围进行行走判断
 */
public class SpellAttackWalkToTarget {
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
            float range = MaidSpellCatalog.getSpellRange(maidInstance.get(spellDataMemory).getSpell());
            if (canSee && mob.distanceToSqr(target) <= range * range && mob.isWithinRestriction()) {
                walkTargetMemory.erase();
            } else {
                positionMemory.set(new EntityTracker(target, true));
                // 目标在 Home 外或跟随范围外不等于不能攻击：走到边界内的接战点，而不是取消整个追击。
                // 跟随模式下以前是直接放弃靠近，女仆会在主人身边干站着，够不着目标也放不出法术。
                Vec3 approach = restrictedApproach(mob, target);
                if (approach == null) {
                    approach = MaidLeash.approach(mob, target);
                }
                float speed = speedModifier.apply(mob);
                if (approach == null) {
                    walkTargetMemory.set(new WalkTarget(new EntityTracker(target, false), speed, 0));
                } else if (!approachDirectly(mob, approach, speed)) {
                    walkTargetMemory.set(new WalkTarget(approach, speed, 0));
                }
            }
            return true;
        };
    }

    /**
     * Home 外目标的合法接战点；无边界干涉时继续使用跟踪实体的原有路径。
     */
    @Nullable
    static Vec3 restrictedApproach(Mob mob, LivingEntity target) {
        if (!mob.hasRestriction() || (mob.isWithinRestriction() && mob.isWithinRestriction(target.blockPosition()))) {
            return null;
        }
        return approachWithin(mob, target, Vec3.atBottomCenterOf(mob.getRestrictCenter()),
            Math.max(0, mob.getRestrictRadius() - .1), mob::isWithinRestriction);
    }

    /**
     * 以 {@code center} 为圆心、{@code radius} 为半径的区域内离目标最近的可站立位置，Home 与跟随范围共用。
     *
     * @param allowed 判定某个方块是否在区域内
     */
    static Vec3 approachWithin(Mob mob, LivingEntity target, Vec3 center, double radius, Predicate<BlockPos> allowed) {
        Vec3 offset = new Vec3(target.getX(), mob.getY(), target.getZ()).subtract(center);
        double length = offset.length();
        Vec3 result = length > radius ? center.add(offset.scale(radius / length)) : center.add(offset);
        // 区域按 BlockPos 判定；投影后的方块也必须合法（尤其是不同高度或负坐标）。
        while (!allowed.test(BlockPos.containing(result)) && radius > 0) {
            radius = Math.max(0, radius - .5);
            result = length > 0 ? center.add(offset.scale(Math.min(1, radius / length))) : center;
        }
        // 路径的目标是方块中心，不能把连续投影的“在射程内”当作实际到达后也够得到。
        BlockPos projected = BlockPos.containing(result);
        Vec3 best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos candidate = projected.offset(dx, 0, dz);
                if (!allowed.test(candidate)) continue;
                Vec3 point = Vec3.atBottomCenterOf(candidate);
                double distance = point.distanceToSqr(target.position());
                if (distance < bestDistance && mob.level().noCollision(mob,
                    mob.getBoundingBox().move(point.subtract(mob.position())))) {
                    best = point;
                    bestDistance = distance;
                }
            }
        }
        return best == null ? result : best;
    }

    /**
     * 路径认为已到达目标方块时，仍须走到块内接战点，不能留下小于一格的射程死区。
     */
    static boolean approachDirectly(Mob mob, Vec3 approach, float speed) {
        if (mob.position().distanceToSqr(approach) > 2.25) return false;
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        mob.getNavigation().stop();
        mob.getMoveControl().setWantedPosition(approach.x, approach.y, approach.z, speed);
        return true;
    }
}
