package net.magicterra.winefoxsspellbooks.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellAttackWalkToTarget;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellCastingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellChooseTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellStrafingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.StartAttacking;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.registry.InitItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 法术支援任务
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-30 02:29
 */
public class MaidMagicSupportTask extends MaidCastingTask {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "magic_support_task");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return InitItems.MAGIC_SUPPORT_TASK_ICON.toStack();
    }

    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        TargetingConditions targetingConditions = TargetingConditions.forNonCombat();
        targetingConditions.range(Config.getMaxSpellRange());
        return targetingConditions.test(maid, target);
    }

    static Optional<? extends LivingEntity> findNearbyFriendsAttackTarget(EntityMaid maid) {
        // 检查攻击主人的对象
        LivingEntity owner = maid.getOwner();
        LivingEntity lastAttacker = owner !=null ? owner.getLastHurtByMob() : null;
        if (lastAttacker != null && maid.canAttack(lastAttacker) && maid.canSee(lastAttacker)) {
            return Optional.of(lastAttacker);
        }
        // 检查附近女仆正在攻击的对象
        AABB aabb = maid.searchDimension();
        List<LivingEntity> list = maid.level.getEntitiesOfClass(LivingEntity.class, aabb);
        list.sort(Comparator.comparingDouble(maid::distanceToSqr));
        for (LivingEntity e : list) {
            if (!maid.isAlliedTo(e)) {
                continue;
            }
            if (!e.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)) {
                continue;
            }
            LivingEntity attackTarget = e.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElseThrow();
            if (maid.canAttack(attackTarget) && maid.canSee(attackTarget)) {
                return Optional.of(attackTarget);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasSpells, MaidMagicSupportTask::findNearbyFriendsAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        BehaviorControl<Mob> spellChooseTask = new SpellChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid,
            Set.of(MaidSpellAction.DEFENSE, MaidSpellAction.SUPPORT, MaidSpellAction.POSITIVE, MaidSpellAction.NEGATIVE, MaidSpellAction.SUPPORT_OTHER));
        BehaviorControl<Mob> moveToTargetTask = SpellAttackWalkToTarget.create((float) Config.getBattleWalkSpeed());
        BehaviorControl<PathfinderMob> maidAttackStrafingTask = new SpellStrafingTask(Config.getStartSpellRange(), (float) Config.getBattleWalkSpeed());
        BehaviorControl<Mob> shootTargetTask = new SpellCastingTask(((IMagicEntity) maid));

        return Lists.newArrayList(
            Pair.of(5, supplementedTask),
            Pair.of(5, findTargetTask),
            Pair.of(5, spellChooseTask),
            Pair.of(5, moveToTargetTask),
            Pair.of(5, maidAttackStrafingTask),
            Pair.of(5, shootTargetTask)
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasSpells, IRangedAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        BehaviorControl<Mob> spellChooseTask = new SpellChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid,
            Set.of(MaidSpellAction.DEFENSE, MaidSpellAction.SUPPORT, MaidSpellAction.POSITIVE, MaidSpellAction.NEGATIVE, MaidSpellAction.SUPPORT_OTHER));
        BehaviorControl<Mob> shootTargetTask = new SpellCastingTask(((IMagicEntity) maid));

        return Lists.newArrayList(
            Pair.of(5, supplementedTask),
            Pair.of(5, findTargetTask),
            Pair.of(5, spellChooseTask),
            Pair.of(5, shootTargetTask)
        );
    }
}
