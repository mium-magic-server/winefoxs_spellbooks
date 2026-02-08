package net.magicterra.winefoxsspellbooks.task.debug;

import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import java.util.List;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellAttackWalkToTarget;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellCastingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellTestChooseTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.StartAttacking;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.task.MaidCastingTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 法术测试任务
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-02-08
 */
public class MaidSpellTestTask extends MaidCastingTask {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "spell_test_task");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return Items.COMMAND_BLOCK.getDefaultInstance();
    }

    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        return true;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasSpells, IRangedAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        BehaviorControl<Mob> spellChooseTask = new SpellTestChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid, Set.of(MaidSpellAction.values()));
        BehaviorControl<Mob> moveToTargetTask = SpellAttackWalkToTarget.create((float) Config.getBattleWalkSpeed());
        // BehaviorControl<PathfinderMob> maidAttackStrafingTask = new SpellStrafingTask(Config.getStartSpellRange(), (float) Config.getBattleWalkSpeed());
        BehaviorControl<Mob> shootTargetTask = new SpellCastingTask(((IMagicEntity) maid));

        return Lists.newArrayList(
            Pair.of(5, supplementedTask),
            Pair.of(5, findTargetTask),
            Pair.of(5, spellChooseTask),
            Pair.of(5, moveToTargetTask),
            // Pair.of(5, maidAttackStrafingTask),
            Pair.of(5, shootTargetTask)
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasSpells, IRangedAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create((target) -> !hasSpells(maid) || farAway(target, maid));
        BehaviorControl<Mob> spellChooseTask = new SpellTestChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid, Set.of(MaidSpellAction.values()));
        BehaviorControl<Mob> shootTargetTask = new SpellCastingTask(((IMagicEntity) maid));

        return Lists.newArrayList(
            Pair.of(5, supplementedTask),
            Pair.of(5, findTargetTask),
            Pair.of(5, spellChooseTask),
            Pair.of(5, shootTargetTask)
        );
    }
}
