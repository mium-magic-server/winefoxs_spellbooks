package net.magicterra.winefoxsspellbooks.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.TlmSpellCompat;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.GatedBehavior;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.MaidLeash;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellAttackWalkToTarget;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellCastingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellChooseTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellStrafingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.StartAttacking;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.magicterra.winefoxsspellbooks.util.LineOfSightGuard;
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
    /**
     * 主人攻击目标的有效期，与原版 lastHurtByMob 的过期时长一致
     */
    private static final int OWNER_TARGET_MEMORY_TICKS = 100;

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return WsbItems.MAGIC_SUPPORT_TASK_ICON.toStack();
    }

    /**
     * 装了万法皆通时法术书由其识别（副手、主手、背包），描述改用与其战斗任务一致的说法
     */
    @Override
    public List<String> getDescription(EntityMaid maid) {
        if (TlmSpellCompat.isLoaded()) {
            return Lists.newArrayList("task.winefoxs_spellbooks.magic_support_task.tlm_spell.desc");
        }
        return super.getDescription(maid);
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        if (TlmSpellCompat.isLoaded()) {
            // 条件键为 task.<ns>.<path>.condition.<名称>
            return Collections.singletonList(Pair.of("tlm_spell.spell_book", this::hasSpells));
        }
        return super.getConditionDescription(maid);
    }

    /**
     * 支援任务能用的法术集与施法任务不同：多 POSITIVE / SUPPORT_OTHER，无 ATTACK / MOVEMENT。 沿用父类那套会让只带治疗与强化的女仆被判成「没法术」，索敌永不触发、任务整个空转。
     */
    @Override
    protected Set<MaidSpellAction> allowedActions() {
        return Set.of(MaidSpellAction.DEFENSE, MaidSpellAction.SUPPORT, MaidSpellAction.POSITIVE,
            MaidSpellAction.NEGATIVE, MaidSpellAction.SUPPORT_OTHER);
    }

    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        TargetingConditions targetingConditions = TargetingConditions.forNonCombat();
        targetingConditions.range(Config.getMaxSpellRange());
        return LineOfSightGuard.test(targetingConditions, maid, target);
    }

    static Optional<? extends LivingEntity> findNearbyFriendsAttackTarget(EntityMaid maid) {
        // 跟随模式下站在活动范围边缘也够不着的目标不锁定，口径见 MaidLeash；装了万法皆通时其敌我判定为友好的也不打
        Predicate<LivingEntity> canEngage = MaidLeash.canEngage(maid).and(target -> !TlmSpellCompat.isFriendly(maid, target));
        // 检查攻击主人的对象
        LivingEntity owner = maid.getOwner();
        LivingEntity lastAttacker = owner != null ? owner.getLastHurtByMob() : null;
        if (lastAttacker != null && maid.canAttack(lastAttacker) && maid.canSee(lastAttacker)
            && canEngage.test(lastAttacker)) {
            return Optional.of(lastAttacker);
        }
        // 检查主人正在攻击的对象，与 TLM DefaultMonsterType 同样读 getLastHurtMob
        // 原版 lastHurtMob 只在目标死亡时清空，不像 lastHurtByMob 有 100 tick 过期，这里补上同样的时效，免得追打主人很久以前碰过的目标
        LivingEntity ownerTarget = owner != null ? owner.getLastHurtMob() : null;
        if (ownerTarget != null && owner.tickCount - owner.getLastHurtMobTimestamp() <= OWNER_TARGET_MEMORY_TICKS
            && maid.canAttack(ownerTarget) && maid.canSee(ownerTarget) && canEngage.test(ownerTarget)) {
            return Optional.of(ownerTarget);
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
            if (maid.canAttack(attackTarget) && maid.canSee(attackTarget) && canEngage.test(attackTarget)) {
                return Optional.of(attackTarget);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasSpells, MaidMagicSupportTask::findNearbyFriendsAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        BehaviorControl<Mob> spellChooseTask = new SpellChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid, allowedActions());
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
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasSpells, MaidMagicSupportTask::findNearbyFriendsAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        // 坐下或骑乘时施法的条件与万法皆通一致：须佩戴法术射表（未安装时不限制）
        BehaviorControl<EntityMaid> spellChooseTask = new GatedBehavior<>(new SpellChooseTask(Config.getStartSpellRange(),
            Config.getMaxComboDelayTick(), maid, allowedActions()), TlmSpellCompat::canCastInRidePose);
        BehaviorControl<Mob> shootTargetTask = new SpellCastingTask(((IMagicEntity) maid));

        return Lists.newArrayList(
            Pair.of(5, supplementedTask),
            Pair.of(5, findTargetTask),
            Pair.of(5, spellChooseTask),
            Pair.of(5, shootTargetTask)
        );
    }
}
