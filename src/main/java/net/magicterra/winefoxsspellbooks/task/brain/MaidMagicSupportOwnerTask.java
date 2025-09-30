package net.magicterra.winefoxsspellbooks.task.brain;

import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.spells.holy.CleanseSpell;
import io.redspace.ironsspellbooks.util.ModTags;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 支援主人
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-30 23:31
 */
public class MaidMagicSupportOwnerTask extends MaidCheckRateTask {
    private static final int MAX_DELAY_TIME = 20;
    private final float walkSpeed;
    protected final EntityMaid mob;
    protected final Predicate<EntityMaid> hasValidSpell;
    protected final IMagicEntity spellCastingMob;
    protected final MaidMagicEntity magicEntity;

    protected LivingEntity target;
    protected float spellcastingRange;
    protected float spellcastingRangeSqr;
    protected SpellData currentSpell = SpellData.EMPTY;
    protected CurrentAction currentAction;

    public MaidMagicSupportOwnerTask(EntityMaid maid, Predicate<EntityMaid> hasValidSpells, float walkSpeed) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.mob = maid;
        this.hasValidSpell = hasValidSpells;
        this.spellCastingMob = (IMagicEntity) maid;
        this.magicEntity = (MaidMagicEntity) maid;
        this.walkSpeed = walkSpeed;
        this.setMaxCheckRate(MAX_DELAY_TIME);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        if (!super.checkExtraStartConditions(worldIn, maid)) {
            return false;
        }
        if (!hasValidSpell.test(maid)) {
            return false;
        }
        MaidSpellDataHolder spellDataHolder = magicEntity.winefoxsSpellbooks$getSpellDataHolder();
        if (!spellDataHolder.hasAnySpells()) {
            return false;
        }
        double supportOwnerEffectChange = 0.05;
        double supportOtherEffectChange = 0.05;
        double defenseEffectChange = 0.05;
        LivingEntity owner = maid.getOwner();
        if (owner instanceof Player && owner.isAlive()) {
            if (owner.getHealth() < owner.getMaxHealth()) {
                if (initialCurrentSpell(CurrentAction.SUPPORT_OTHER, owner) != SpellData.EMPTY) {
                    return true;
                }
            }
            if (owner.getActiveEffects().isEmpty()) {
                supportOwnerEffectChange *= 2;
            }
            if (maid.getRandom().nextDouble() <= supportOwnerEffectChange && initialCurrentSpell(CurrentAction.POSITIVE, owner) != SpellData.EMPTY) {
                return true;
            }
        }
        if (maid.getActiveEffects().isEmpty()) {
            defenseEffectChange *= 2;
        }
        if (maid.getRandom().nextDouble() <= defenseEffectChange && initialCurrentSpell(CurrentAction.DEFENSE, maid) != SpellData.EMPTY) {
            return true;
        }
        if (maid.getHealth() < maid.getMaxHealth() && initialCurrentSpell(CurrentAction.SUPPORT, maid) != SpellData.EMPTY) {
            return true;
        }
        // TODO 最远检查距离，需要一个单独的配置
        TargetingConditions supportTargeting = TargetingConditions.forNonCombat().range(MaidConfig.DANMAKU_RANGE.get());
        AABB aabb = maid.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
        List<EntityMaid> nearbyMaids = worldIn.getNearbyEntities(EntityMaid.class, supportTargeting, maid, aabb);
        for (EntityMaid nearbyMaid : nearbyMaids) {
            if (!Objects.equals(nearbyMaid.getOwner(), owner)) {
                continue;
            }
            if (nearbyMaid.getHealth() < nearbyMaid.getMaxHealth()) {
                if (initialCurrentSpell(CurrentAction.SUPPORT_OTHER, nearbyMaid) != SpellData.EMPTY) {
                    return true;
                }
            }
            if (nearbyMaid.getActiveEffects().isEmpty()) {
                supportOtherEffectChange *= 2;
            }
            if (maid.getRandom().nextDouble() <= supportOtherEffectChange && initialCurrentSpell(CurrentAction.POSITIVE, nearbyMaid) != SpellData.EMPTY) {
                return true;
            }
            LivingEntity maidTarget = nearbyMaid.getTarget();
            if (maidTarget != null && !maid.isAlliedTo(maidTarget)) {
                if (initialCurrentSpell(CurrentAction.NEGATIVE, maidTarget) != SpellData.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid entity, long gameTime) {
        if (entity.guiOpening) {
            return false;
        }
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (spellCastingMob.isCasting()) {
            return true;
        }
        if (currentSpell == SpellData.EMPTY) {
            return false;
        }
        switch (currentAction) {
            case DEFENSE, POSITIVE -> {
                Holder<MobEffect> causedEffect = MaidSpellRegistry.getSpellCausedEffect(currentSpell.getSpell());
                return causedEffect == null || !target.hasEffect(causedEffect);
            }
            case SUPPORT, SUPPORT_OTHER -> {
                return !(target.getHealth() >= target.getMaxHealth());
            }
            case NEGATIVE -> {
                return target.isAlive();
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        if (target.isAlive()) {
            mob.setAggressive(true);
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid entity, long gameTime) {
        target = null;
        mob.setTarget(null);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid owner, long gameTime) {
        if (target == null || currentSpell == SpellData.EMPTY) {
            return;
        }

        if (MaidSpellRegistry.isDefenseSpell(currentSpell.getSpell()) || MaidSpellRegistry.isSupportSpell(currentSpell.getSpell())) {
            handleSupportLogic();
            return;
        }

        if (owner.closerThan(target, spellcastingRange)) {
            handleSupportLogic();
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories(owner, target, walkSpeed, 1);
        }
    }

    protected void handleSupportLogic() {
        if (!spellCastingMob.isCasting() && !spellCastingMob.isDrinkingPotion()) {
            doSpellAction();
        }
        if (spellCastingMob.isCasting()) {
            var spellData = spellCastingMob.getMagicData().getCastingSpell();
            if (target.isDeadOrDying() || spellData.getSpell().shouldAIStopCasting(spellData.getLevel(), mob, target)) {
                spellCastingMob.cancelCast();
                target = null;
            } else {
                mob.lookAt(target, 30, 30);
            }
        }
    }

    protected void doSpellAction() {
        // 获取咒语类型
        var spellData = currentSpell;
        var spell = spellData.getSpell();
        int spellLevel = spellData.getLevel();
        spellLevel = Math.max(spellLevel, 1);

        PlayerRecasts playerRecasts = spellCastingMob.getMagicData().getPlayerRecasts();
        boolean hasRecastForSpell = playerRecasts.hasRecastForSpell(spell);

        //Make sure cast is valid. if not, try again shortly
        if (currentSpell != SpellData.EMPTY && !hasRecastForSpell && !spell.shouldAIStopCasting(spellLevel, mob, target)) {
            mob.lookAt(target, 360, 360);
            spellCastingMob.initiateCastSpell(spell, spellLevel);
        }
        currentSpell = SpellData.EMPTY;
    }

    protected SpellData initialCurrentSpell(CurrentAction action, LivingEntity targetEntity) {
        MaidSpellDataHolder spellDataHolder = magicEntity.winefoxsSpellbooks$getSpellDataHolder();
        if (!spellDataHolder.hasAnySpells()) {
            return SpellData.EMPTY;
        }
        switch (action) {
            case DEFENSE, POSITIVE, NEGATIVE -> {
                List<SpellData> spells;
                switch (action) {
                    case DEFENSE -> spells = new ArrayList<>(spellDataHolder.getDefenseSpells());
                    case POSITIVE -> spells = new ArrayList<>(spellDataHolder.getPositiveEffectSpells());
                    case NEGATIVE -> spells = new ArrayList<>(spellDataHolder.getNegativeEffectSpells());
                    default -> throw new IllegalStateException("Unexpected value: " + action);
                }
                for (var iterator = spells.iterator(); iterator.hasNext(); ) {
                    SpellData spell = iterator.next();
                    Holder<MobEffect> causedEffect = MaidSpellRegistry.getSpellCausedEffect(spell.getSpell());
                    if (causedEffect != null && targetEntity.hasEffect(causedEffect)) {
                        iterator.remove();
                        continue;
                    }
                    if (spell.getSpell() instanceof CleanseSpell) {
                        boolean hasHarmfulEffect = targetEntity.getActiveEffects()
                            .stream()
                            .map(MobEffectInstance::getEffect)
                            .anyMatch(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL && !effect.is(ModTags.CLEANSE_IMMUNE));
                        if (!hasHarmfulEffect) {
                            iterator.remove();
                        }
                    }
                }
                currentSpell = MaidMagicManager.getAvailableSpell(mob, spells);
            }
            case SUPPORT -> currentSpell = MaidMagicManager.getAvailableSpell(mob, spellDataHolder.getSupportSpells());
            case SUPPORT_OTHER -> currentSpell = MaidMagicManager.getAvailableSpell(mob, spellDataHolder.getSupportEffectSpells());
        }
        spellcastingRange = MaidSpellRegistry.getSpellRange(currentSpell.getSpell());
        spellcastingRangeSqr = spellcastingRange * spellcastingRange;
        if (currentSpell != SpellData.EMPTY) {
            target = targetEntity;
            if (action == CurrentAction.NEGATIVE) {
                mob.setTarget(target);
            }
            currentAction = action;
        }
        return currentSpell;
    }

    protected enum CurrentAction {
        DEFENSE,
        SUPPORT,
        POSITIVE,
        NEGATIVE,
        SUPPORT_OTHER
    }
}
