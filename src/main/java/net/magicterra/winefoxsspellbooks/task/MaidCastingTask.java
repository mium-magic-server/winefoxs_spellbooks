package net.magicterra.winefoxsspellbooks.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.DrinkPotionsTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.MagicMeleeAttack;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellAttackWalkToTarget;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellCastingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellChooseTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellStrafingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.StartAttacking;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.registry.InitItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
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
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasSpells, IRangedAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        BehaviorControl<Mob> spellChooseTask = new SpellChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid,
            Set.of(MaidSpellAction.ATTACK, MaidSpellAction.DEFENSE, MaidSpellAction.MOVEMENT, MaidSpellAction.SUPPORT, MaidSpellAction.NEGATIVE));
        BehaviorControl<Mob> moveToTargetTask = SpellAttackWalkToTarget.create((float) Config.getBattleWalkSpeed());
        BehaviorControl<Mob> drinkPotionTask = new DrinkPotionsTask((float) Config.getBattleWalkSpeed(), 100);
        BehaviorControl<PathfinderMob> maidAttackStrafingTask = new SpellStrafingTask(Config.getStartSpellRange(), (float) Config.getBattleWalkSpeed());
        BehaviorControl<Mob> shootTargetTask = new SpellCastingTask(((IMagicEntity) maid));

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
            BehaviorControl<Mob> attackTargetTask = MagicMeleeAttack.create(20);
            behaviors.add(Pair.of(5, attackTargetTask));
        }

        return behaviors;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasSpells, IRangedAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        BehaviorControl<Mob> spellChooseTask = new SpellChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid,
            Set.of(MaidSpellAction.ATTACK, MaidSpellAction.DEFENSE, MaidSpellAction.MOVEMENT, MaidSpellAction.SUPPORT, MaidSpellAction.NEGATIVE));
        BehaviorControl<Mob> drinkPotionTask = new DrinkPotionsTask((float) Config.getBattleWalkSpeed(), 100);
        BehaviorControl<Mob> shootTargetTask = new SpellCastingTask(((IMagicEntity) maid));

        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> behaviors = Lists.newArrayList(
            Pair.of(5, supplementedTask),
            Pair.of(5, findTargetTask),
            Pair.of(5, spellChooseTask),
            Pair.of(5, drinkPotionTask),
            Pair.of(5, shootTargetTask)
        );

        if (Config.getMeleeAttackInMagicTask()) {
            BehaviorControl<Mob> attackTargetTask = MagicMeleeAttack.create(20);
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
        MaidMagicEntity magicEntity = (MaidMagicEntity) maid;
        MaidSpellDataHolder spellDataHolder = magicEntity.winefoxsSpellbooks$getSpellDataHolder();
        return spellDataHolder.hasAnyCastingTaskSpells();
    }

    protected boolean farAway(LivingEntity target, EntityMaid maid) {
        return maid.distanceTo(target) > this.searchRadius(maid);
    }
}
