package net.magicterra.winefoxsspellbooks.mixin.touhou_little_maid_spell;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.TlmSpellCompat;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.EngageBand;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.GatedBehavior;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.MaidLeash;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellAttackWalkToTarget;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellCastingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellChooseTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellStrafingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 接管万法皆通近战 / 远程法术战斗任务中的铁魔法：选术、施法、支援与走位
 * <p>
 * 索敌、视线控制与其余模组 provider 的施法仍用万法皆通的行为；其铁魔法 provider 已换成只做识别的 {@code WsbIronsSpellbooksProvider}。
 * <ul>
 *   <li>选术放开全部行为类别，战斗中也会支援主人与队友（法术支援任务仍保留为只支援的模式）</li>
 *   <li>走位换成本模组的接近与走位行为，站位落在任务的距离带内（近战贴身、远程保持距离），见 {@link EngageBand}；
 *       万法皆通的接近行为只在本模组没有选中法术时运行，供只带其他模组法术书的女仆使用</li>
 *   <li>跟随模式下索敌只锁定站在主人身边活动范围边缘也够得着的目标，口径同 {@link MaidLeash}</li>
 *   <li>坐下或骑乘时须佩戴法术射表才施法，同万法皆通</li>
 * </ul>
 * 远程任务只覆写了 {@code createBrainTasks}，骑乘、索敌与目标失效判定沿用近战任务的版本。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = {
    "com.github.yimeng261.maidspell.task.SpellCombatMeleeTask",
    "com.github.yimeng261.maidspell.task.SpellCombatFarTask"
}, remap = false)
public abstract class SpellCombatTaskMixin {
    /**
     * 与万法皆通自己的战斗行为同一优先级
     */
    @Unique
    private static final int WINEFOXS_SPELLBOOKS$PRIORITY = 2;

    /**
     * 战斗中攻击与支援都做：全部行为类别
     */
    @Unique
    private static final Set<MaidSpellAction> WINEFOXS_SPELLBOOKS$ALL_ACTIONS = Collections.unmodifiableSet(EnumSet.allOf(MaidSpellAction.class));

    @Unique
    private static final String WINEFOXS_SPELLBOOKS$FAR_TASK_PATH = "spell_combat_far";

    /**
     * 万法皆通走位行为的类名后缀（近战与远程任务各一个同名内部类）
     */
    @Unique
    private static final String WINEFOXS_SPELLBOOKS$STRAFING_SUFFIX = "$SpellStrafingTask";

    @ModifyExpressionValue(method = "createBrainTasks",
        at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/ai/brain/task/MaidRangedWalkToTarget;create(F)Lnet/minecraft/world/entity/ai/behavior/BehaviorControl;"),
        require = 0)
    private BehaviorControl<EntityMaid> winefoxsSpellbooks$gateWalkToTarget(BehaviorControl<EntityMaid> original) {
        // 选中铁魔法后由本模组按法术射程接近，避免两套接近行为争抢行走目标
        return new GatedBehavior<>(original,
            maid -> !maid.getBrain().hasMemoryValue(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get()));
    }

    @ModifyReturnValue(method = "createBrainTasks", at = @At("RETURN"), require = 0)
    private List<Pair<Integer, BehaviorControl<? super EntityMaid>>> winefoxsSpellbooks$addSpellBehaviors(
        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> behaviors, EntityMaid maid) {
        boolean far = winefoxsSpellbooks$isFarTask();
        EngageBand band = far ? TlmSpellCompat.farBand() : TlmSpellCompat.meleeBand();
        float walkSpeed = (float) Config.getBattleWalkSpeed();

        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> result = new ArrayList<>(behaviors);
        result.removeIf(pair -> pair.getSecond().getClass().getName().endsWith(WINEFOXS_SPELLBOOKS$STRAFING_SUFFIX));
        // 远程任务不选够不着距离带下限的攻击法术：她不会为了它离开站位
        result.add(Pair.of(WINEFOXS_SPELLBOOKS$PRIORITY, new SpellChooseTask(Config.getStartSpellRange(),
            Config.getMaxComboDelayTick(), maid, WINEFOXS_SPELLBOOKS$ALL_ACTIONS).withMinAttackRange(far ? band.min() : 0)));
        result.add(Pair.of(WINEFOXS_SPELLBOOKS$PRIORITY, SpellAttackWalkToTarget.create(walkSpeed)));
        result.add(Pair.of(WINEFOXS_SPELLBOOKS$PRIORITY, new SpellStrafingTask((float) band.max(), walkSpeed, band)));
        result.add(Pair.of(WINEFOXS_SPELLBOOKS$PRIORITY, new SpellCastingTask((IMagicEntity) maid)));
        return result;
    }

    @ModifyReturnValue(method = "createRideBrainTasks", at = @At("RETURN"), require = 0)
    private List<Pair<Integer, BehaviorControl<? super EntityMaid>>> winefoxsSpellbooks$addRideSpellBehaviors(
        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> behaviors, EntityMaid maid) {
        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> result = new ArrayList<>(behaviors);
        result.add(Pair.of(WINEFOXS_SPELLBOOKS$PRIORITY, new GatedBehavior<>(new SpellChooseTask(Config.getStartSpellRange(),
            Config.getMaxComboDelayTick(), maid, WINEFOXS_SPELLBOOKS$ALL_ACTIONS), TlmSpellCompat::canCastInRidePose)));
        result.add(Pair.of(WINEFOXS_SPELLBOOKS$PRIORITY, new SpellCastingTask((IMagicEntity) maid)));
        return result;
    }

    @WrapOperation(method = "findValidAttackTarget",
        at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/touhoulittlemaid/api/task/IAttackTask;findFirstValidAttackTarget(Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)Ljava/util/Optional;"),
        require = 0)
    private Optional<? extends LivingEntity> winefoxsSpellbooks$findEngageableTarget(EntityMaid maid,
                                                                                    Operation<Optional<? extends LivingEntity>> original) {
        if (MaidLeash.leashOwner(maid) == null) {
            return original.call(maid);
        }
        // 跟随模式下本模组的接近行为不会走出主人身边的活动范围，站在边缘也够不着的目标不锁定，
        // 否则女仆会举着选中的法术停在边缘，一直放不出去。条件与 IAttackTask.findFirstValidAttackTarget 相同，另加可及距离
        Predicate<LivingEntity> canEngage = MaidLeash.canEngage(maid);
        return maid.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap(entities -> entities.findClosest(
            entity -> maid.canAttack(entity) && maid.isWithinRestriction(entity.blockPosition()) && canEngage.test(entity)));
    }

    @ModifyReturnValue(method = "farAway", at = @At("RETURN"), require = 0)
    private boolean winefoxsSpellbooks$dropUnreachableTarget(boolean farAway, LivingEntity target, EntityMaid maid) {
        // 主人走远后目标出了可及距离，放弃它重新索敌
        return farAway || !MaidLeash.canEngage(maid).test(target);
    }

    @Unique
    private boolean winefoxsSpellbooks$isFarTask() {
        return (Object) this instanceof IMaidTask task && WINEFOXS_SPELLBOOKS$FAR_TASK_PATH.equals(task.getUid().getPath());
    }
}
