package net.magicterra.winefoxsspellbooks.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.MaidDrinkPotionsTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.MaidMagicMeleeAttack;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.MaidSpellAttackWalkToTarget;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.MaidSpellCastingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.MaidSpellChooseTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.MaidSpellStrafingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.MaidStartAttacking;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.registry.InitItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * 魔法攻击任务
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-20 20:38
 */
public class MaidCastingTask implements IRangedAttackTask {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "casting_task");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return InitItems.CASTING_TASK_ICON.toStack();
    }

    @Override
    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.attackSound(maid, InitSounds.MAID_RANGE_ATTACK.get(), 0.5f);
    }

    @Override
    public boolean isWeapon(EntityMaid maid, ItemStack stack) {
        // 允许空手施法
        return true;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = MaidStartAttacking.create(this::hasSpells, IRangedAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        BehaviorControl<EntityMaid> spellChooseTask = new MaidSpellChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid,
            Set.of(MaidSpellAction.ATTACK, MaidSpellAction.DEFENSE, MaidSpellAction.MOVEMENT, MaidSpellAction.SUPPORT, MaidSpellAction.NEGATIVE));
        BehaviorControl<EntityMaid> moveToTargetTask = MaidSpellAttackWalkToTarget.create((float) Config.getBattleWalkSpeed());
        BehaviorControl<EntityMaid> drinkPotionTask = new MaidDrinkPotionsTask((float) Config.getBattleWalkSpeed(), 100);
        BehaviorControl<EntityMaid> maidAttackStrafingTask = new MaidSpellStrafingTask(Config.getStartSpellRange(), (float) Config.getBattleWalkSpeed());
        BehaviorControl<EntityMaid> shootTargetTask = new MaidSpellCastingTask(maid);

        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> behaviors = Lists.newArrayList(
            Pair.of(5, supplementedTask),
            Pair.of(5, findTargetTask),
            Pair.of(5, spellChooseTask),
            Pair.of(5, moveToTargetTask),
            Pair.of(5, drinkPotionTask),
            Pair.of(5, maidAttackStrafingTask),
            Pair.of(5, shootTargetTask)
        );

        if (Config.getMeleeAttackInMagicTask()) {
            BehaviorControl<Mob> attackTargetTask = MeleeAttack.create(20);
            behaviors.add(Pair.of(5, attackTargetTask));
        }

        return behaviors;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = MaidStartAttacking.create(this::hasSpells, IRangedAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        BehaviorControl<EntityMaid> spellChooseTask = new MaidSpellChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid,
            Set.of(MaidSpellAction.ATTACK, MaidSpellAction.DEFENSE, MaidSpellAction.MOVEMENT, MaidSpellAction.SUPPORT, MaidSpellAction.NEGATIVE));
        BehaviorControl<EntityMaid> drinkPotionTask = new MaidDrinkPotionsTask((float) Config.getBattleWalkSpeed(), 100);
        BehaviorControl<EntityMaid> shootTargetTask = new MaidSpellCastingTask(maid);

        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> behaviors = Lists.newArrayList(
            Pair.of(5, supplementedTask),
            Pair.of(5, findTargetTask),
            Pair.of(5, spellChooseTask),
            Pair.of(5, drinkPotionTask),
            Pair.of(5, shootTargetTask)
        );

        if (Config.getMeleeAttackInMagicTask()) {
            BehaviorControl<Mob> attackTargetTask = MaidMagicMeleeAttack.create(20);
            behaviors.add(Pair.of(5, attackTargetTask));
        }

        return behaviors;
    }

    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        TargetingConditions targetingConditions = TargetingConditions.forCombat();
        targetingConditions.range(Config.getMaxSpellRange());
        return targetingConditions.test(maid, target);
    }

    @Override
    public float searchRadius(EntityMaid maid) {
        return Config.getMaxSpellRange();
    }

    @Override
    public void performRangedAttack(EntityMaid shooter, LivingEntity target, float distanceFactor) {
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        // 打开 Gui 的时候才会触发
        return Collections.singletonList(Pair.of("has_spells", this::hasSpells));
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return maid.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
            && maid.getBrain().checkMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(), MemoryStatus.VALUE_ABSENT);
    }

    protected boolean hasSpells(EntityMaid maid) {
        if (maid.level.isClientSide()) {
            IItemHandler invWrapper = new CombinedInvWrapper(maid.getArmorInvWrapper(), new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, maid.getMainHandItem())), maid.getMaidBauble());
            for (int i = 0; i < invWrapper.getSlots(); i++) {
                ItemStack stack = invWrapper.getStackInSlot(i);
                if (!stack.isEmpty() && ISpellContainer.isSpellContainer(stack)) {
                    return true;
                }
            }
            return false;
        }
        MaidMagicEntity magicEntity = (MaidMagicEntity) maid;
        MaidSpellDataHolder spellDataHolder = magicEntity.winefoxsSpellbooks$getSpellDataHolder();
        return spellDataHolder.hasAnyCastingTaskSpells();
    }

    protected boolean farAway(LivingEntity target, EntityMaid maid) {
        return maid.distanceTo(target) > this.searchRadius(maid);
    }
}
