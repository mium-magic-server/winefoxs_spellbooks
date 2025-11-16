package net.magicterra.winefoxsspellbooks.entity.ai.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.spells.holy.CleanseSpell;
import io.redspace.ironsspellbooks.util.ModTags;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;

/**
 * 选择法术任务
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-03 01:35
 */
public class MaidSpellChooseTask extends Behavior<EntityMaid> {
    protected final Set<MaidSpellAction> allowedActions;
    protected final EntityMaid maid;
    protected final IMagicEntity magicEntity;
    protected final double startCastingRange;
    protected final double startCastingRangeSqr;
    protected final List<SpellData> usableAttackSpells = new ArrayList<>();
    protected final List<SpellData> usableDefenseSpells = new ArrayList<>();
    protected final List<SpellData> usableMovementSpells = new ArrayList<>();
    protected final List<SpellData> usableSupportSpells = new ArrayList<>();
    protected final List<SpellData> usablePositiveEffectSpells = new ArrayList<>();
    protected final List<SpellData> usableNegativeEffectSpells = new ArrayList<>();
    protected final List<SpellData> usableSupportEffectSpells = new ArrayList<>();
    protected final List<LivingEntity> nearbyAlliedEntities = new ArrayList<>();

    protected Predicate<LivingEntity> alliedEntityPredicate;

    protected MaidSpellAction lastAction;
    protected int maxDelayTicks;
    protected int nextSpellTickCount;

    public MaidSpellChooseTask(double startCastingRange, int maxDelayTicks, EntityMaid maid, Set<MaidSpellAction> allowedActions) {
        super(ImmutableMap.of(
            MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(), MemoryStatus.REGISTERED,
            MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(), MemoryStatus.VALUE_ABSENT,
            MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(), MemoryStatus.REGISTERED,
            MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED
        ), 1200);
        this.maid = maid;
        this.magicEntity = (IMagicEntity) maid;
        this.allowedActions = allowedActions;
        this.startCastingRange = startCastingRange;
        this.startCastingRangeSqr = startCastingRange * startCastingRange;
        this.alliedEntityPredicate = (entity) -> {
            if (Objects.equals(entity, maid)) {
                return false;
            }
            if (entity.isDeadOrDying()) {
                return false;
            }
            return Objects.equals(maid.getOwner(), entity) || entity.isAlliedTo(maid);
        };
        this.maxDelayTicks = maxDelayTicks;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid owner) {
        if (this.nextSpellTickCount > 0) {
            --this.nextSpellTickCount;
            return false;
        }
        // 重置冷却
        this.nextSpellTickCount = owner.getRandom().nextInt(maxDelayTicks);

        IMagicEntity magicEntity = (IMagicEntity) owner;
        if (magicEntity.isCasting()) {
            // 正在施法
            return false;
        }
        if (magicEntity.isDrinkingPotion()) {
            // 正在喝药
            return false;
        }

        // 初始化所有可用法术 （先清空）
        MaidMagicEntity maidMagicEntity = (MaidMagicEntity) owner;
        MaidSpellDataHolder spellDataHolder = maidMagicEntity.winefoxsSpellbooks$getSpellDataHolder();
        usableAttackSpells.clear();
        usableDefenseSpells.clear();
        usableMovementSpells.clear();
        usableSupportSpells.clear();
        usablePositiveEffectSpells.clear();
        usableNegativeEffectSpells.clear();
        usableSupportEffectSpells.clear();
        if (allowedActions.contains(MaidSpellAction.ATTACK)) {
            for (SpellData spellData : spellDataHolder.getAttackSpells()) {
                if (MaidMagicManager.isSpellUsable(owner, spellData)) {
                    usableAttackSpells.add(spellData);
                }
            }
        }
        if (allowedActions.contains(MaidSpellAction.DEFENSE)) {
            for (SpellData spellData : spellDataHolder.getDefenseSpells()) {
                if (MaidMagicManager.isSpellUsable(owner, spellData)) {
                    usableDefenseSpells.add(spellData);
                }
            }
        }
        if (allowedActions.contains(MaidSpellAction.MOVEMENT)) {
            for (SpellData spellData : spellDataHolder.getMovementSpells()) {
                if (MaidMagicManager.isSpellUsable(owner, spellData)) {
                    usableMovementSpells.add(spellData);
                }
            }
        }
        if (allowedActions.contains(MaidSpellAction.SUPPORT)) {
            for (SpellData spellData : spellDataHolder.getSupportSpells()) {
                if (MaidMagicManager.isSpellUsable(owner, spellData)) {
                    usableSupportSpells.add(spellData);
                }
            }
        }
        if (allowedActions.contains(MaidSpellAction.POSITIVE)) {
            for (SpellData spellData : spellDataHolder.getPositiveEffectSpells()) {
                if (MaidMagicManager.isSpellUsable(owner, spellData)) {
                    usablePositiveEffectSpells.add(spellData);
                }
            }
        }
        if (allowedActions.contains(MaidSpellAction.NEGATIVE)) {
            for (SpellData spellData : spellDataHolder.getNegativeEffectSpells()) {
                if (MaidMagicManager.isSpellUsable(owner, spellData)) {
                    usableNegativeEffectSpells.add(spellData);
                }
            }
        }
        if (allowedActions.contains(MaidSpellAction.SUPPORT_OTHER)) {
            for (SpellData spellData : spellDataHolder.getSupportEffectSpells()) {
                if (MaidMagicManager.isSpellUsable(owner, spellData)) {
                    usableSupportEffectSpells.add(spellData);
                }
            }
        }

        // 计算附近队友，检查有无可用支援队友的法术，如果没有可用法术则跳过
        nearbyAlliedEntities.clear();
        if (usablePositiveEffectSpells.size() + usableSupportEffectSpells.size() > 0) {
            AABB aabb = owner.searchDimension();
            List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, aabb, this.alliedEntityPredicate);
            list.sort(Comparator.comparingDouble(owner::distanceToSqr));
            nearbyAlliedEntities.addAll(list);
        }

        // 计算下一步操作，根据分数
        MaidSpellAction nextSpellAction = getNextSpellAction();
        if (nextSpellAction == null) {
            return false;
        }

        maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(), nextSpellAction);
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid entity, long gameTime) {
        // 设置记忆
        MaidSpellAction nextSpellAction = entity.getBrain().getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get()).orElseThrow();
        // 随机挑选法术，根据操作类型
        List<SpellData> availableSpells;
        switch (nextSpellAction) {
            case ATTACK -> availableSpells = usableAttackSpells;
            case DEFENSE -> availableSpells = usableDefenseSpells;
            case MOVEMENT -> availableSpells = usableMovementSpells;
            case SUPPORT -> availableSpells = usableSupportSpells;
            case POSITIVE -> availableSpells = usablePositiveEffectSpells;
            case NEGATIVE -> availableSpells = usableNegativeEffectSpells;
            case SUPPORT_OTHER -> availableSpells = usableSupportEffectSpells;
            default -> {
                return;
            }
        }
        SpellData spellData = availableSpells.get(entity.getRandom().nextInt(availableSpells.size()));
        entity.getBrain().setMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(), spellData);
        MaidMagicManager.showCurrentSpellInChatBubble(entity, spellData, false);

        if (nextSpellAction == MaidSpellAction.SUPPORT_OTHER || nextSpellAction == MaidSpellAction.POSITIVE) {
            for (LivingEntity nearbyAlliedEntity : nearbyAlliedEntities) {
                int weight = nextSpellAction == MaidSpellAction.SUPPORT_OTHER ?
                             getSupportOtherWeightForSpecifiedEntity(nearbyAlliedEntity) :
                             getPositiveWeightForSpecifiedEntity(nearbyAlliedEntity);
                if (weight > 0) {
                    maid.getBrain().setMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(), nearbyAlliedEntity);
                    if (maid.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)) {
                        // 转换目标
                        maid.getNavigation().stop();
                        maid.getMoveControl().strafe(0, 0);
                        maid.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                        maid.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
                    }
                    break;
                }
            }
        }

        lastAction = nextSpellAction;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid entity, long gameTime) {
        // 记忆没有清除就继续
        return entity.getBrain().hasMemoryValue(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get());
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid entity, long gameTime) {
        // 清理记忆
        entity.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get());
        entity.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get());
        entity.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get());
        // 清理可用法术避免泄露
        usableAttackSpells.clear();
        usableDefenseSpells.clear();
        usableMovementSpells.clear();
        usableSupportSpells.clear();
        usablePositiveEffectSpells.clear();
        usableNegativeEffectSpells.clear();
        usableSupportEffectSpells.clear();
        // 清理队友列表
        nearbyAlliedEntities.clear();
    }

    protected MaidSpellAction getNextSpellAction() {
        LivingEntity attackTarget = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        LivingEntity supportTarget = maid.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()).orElse(null);
        boolean canSee = attackTarget != null && maid.canSee(attackTarget);
        double distanceSquared = attackTarget != null ? maid.distanceToSqr(attackTarget) : 0;

        NavigableMap<Integer, MaidSpellAction> weightedSpells = new TreeMap<>();
        int attackWeight = 0;
        int defenseWeight = lastAction == MaidSpellAction.DEFENSE ? -100 : 0;
        int movementWeight = lastAction == MaidSpellAction.MOVEMENT ? -50 : 0;
        int supportWeight = 0;
        int positiveEffectWeight = 0;
        int negativeEffectWeight = lastAction == MaidSpellAction.NEGATIVE ? -100 : 0;
        int supportEffectWeight = 0;
        int total = 0;

        // 治疗自己，始终优先
        supportWeight += getSupportWeight(attackTarget, distanceSquared);
        if (!usableSupportSpells.isEmpty() && supportWeight > 0) {
            total += supportWeight;
            weightedSpells.put(total, MaidSpellAction.SUPPORT);
        }

        // 附近有队友，或者其他模组指定了 SUPPORT_TARGET，优先选择支援队友类型，判断强化还是治疗
        if (!nearbyAlliedEntities.isEmpty() || supportTarget != null) {
            positiveEffectWeight += getPositiveWeight(supportTarget);
            supportEffectWeight += getSupportOtherWeight(supportTarget);

            if (!usablePositiveEffectSpells.isEmpty() && positiveEffectWeight > 0) {
                total += positiveEffectWeight;
                weightedSpells.put(total, MaidSpellAction.POSITIVE);
            }

            if (!usableSupportEffectSpells.isEmpty() && supportEffectWeight > 0) {
                total += supportEffectWeight;
                weightedSpells.put(total, MaidSpellAction.SUPPORT_OTHER);
            }

            if (total > 0) {
                int seed = maid.getRandom().nextInt(total);
                return weightedSpells.higherEntry(seed).getValue();
            }
            if (usableSupportEffectSpells.isEmpty() && supportEffectWeight > 0) {
                MaidMagicManager.showCurrentSpellInChatBubble(maid, SpellData.EMPTY, true);
            }
        }

        // attackTarget 不为空则选择瞬移、防卫、治疗或攻击
        if (attackTarget != null) {
            attackWeight += getAttackWeight(attackTarget, distanceSquared, canSee);
            defenseWeight += getDefenseWeight(attackTarget);
            movementWeight += getMovementWeight(attackTarget, distanceSquared, canSee);
            negativeEffectWeight += getNegativeWeight(attackTarget, distanceSquared, canSee);
        }

        if (!usableAttackSpells.isEmpty() && attackWeight > 0) {
            total += attackWeight;
            weightedSpells.put(total, MaidSpellAction.ATTACK);
        }

        if (!usableDefenseSpells.isEmpty() && defenseWeight > 0) {
            total += defenseWeight;
            weightedSpells.put(total, MaidSpellAction.DEFENSE);
        }

        if (!usableMovementSpells.isEmpty() && movementWeight > 0) {
            total += movementWeight;
            weightedSpells.put(total, MaidSpellAction.MOVEMENT);
        }

        if (!usableNegativeEffectSpells.isEmpty() && negativeEffectWeight > 0) {
            total += negativeEffectWeight;
            weightedSpells.put(total, MaidSpellAction.NEGATIVE);
        }

        if (total > 0) {
            int seed = maid.getRandom().nextInt(total);
            return weightedSpells.higherEntry(seed).getValue();
        }


        return null;
    }

    // The follow is from WizardAttackGoal
    protected int getAttackWeight(LivingEntity target, double distanceSquared, boolean hasLineOfSight) {
        //We want attack to be a common action in any circumstance, but the more "confident" we are the more likely we are to attack (we have health or our target is weak)
        int baseWeight = 80;
        if (!hasLineOfSight || target == null) {
            return 0;
        }

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) ((1 - targetHealth) * baseWeight * .75f);
        int distanceWeight = (int) (1 - (distanceSquared / startCastingRangeSqr) * -60);
        return baseWeight + targetHealthWeight + distanceWeight;
    }

    protected int getDefenseWeight(LivingEntity target) {
        //We want defensive spells to be used when we feel "threatened", meaning we aren't confident, or we're actively being attacked
        int baseWeight = 20;

        if (target == null) {
            return baseWeight;
        }

        //https://www.desmos.com/calculator/tqs7dudcmv
        //https://www.desmos.com/calculator/7skhcvpic0
        float x = maid.getHealth();
        float m = maid.getMaxHealth();
        //int healthWeight = (int) (50 * (Math.pow(-(x / m) * (x - m), 3) / Math.pow(m / 2, 3)) * 8);
        int healthWeight = (int) (50 * (-(x * x * x) / (m * m * m) + 1));

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) (1 - targetHealth) * -35;

        //this count be finicky due to the fact that projectiles don't stick around for long, so it might be easy to miss them
        // int threatWeight = projectileCount * 95;

        int usableSpellWeight = 0;
        for (SpellData spellData : usableDefenseSpells) {
            Holder<MobEffect> causedEffect = MaidSpellRegistry.getSpellCausedEffect(spellData.getSpell());
            if (causedEffect != null && !maid.hasEffect(causedEffect)) {
                // 还没上此 buff
                usableSpellWeight += 20;
            } else {
                // 未知效果的法术，暂且计 10 分
                usableSpellWeight += 10;
            }
        }

        return baseWeight + healthWeight + targetHealthWeight + usableSpellWeight;
    }

    protected int getMovementWeight(LivingEntity target, double distanceSquared, boolean hasLineOfSight) {
        if (target == null) {
            return 0;
        }
        //We want to move if we're in a disadvantageous spot, or we need a better angle on our target
        double distancePercent = Mth.clamp(distanceSquared / startCastingRangeSqr, 0, 1);

        int distanceWeight = (int) ((distancePercent) * 50);

        int losWeight = hasLineOfSight ? 0 : 80;

        float healthInverted = 1 - maid.getHealth() / maid.getMaxHealth();
        float distanceInverted = (float) (1 - distancePercent);
        int runWeight = (int) (400 * healthInverted * healthInverted * distanceInverted * distanceInverted);

        return distanceWeight + losWeight + runWeight;
    }

    protected int getSupportWeight(LivingEntity target, double distanceSquared) {
        //We want to support/buff ourselves if we are weak
        int baseWeight = -10;

        float health = 1 - maid.getHealth() / maid.getMaxHealth();
        int healthWeight = (int) (200 * health);

        //If our target is close we should probably not drink a potion right in front of them
        double distancePercent = Mth.clamp(distanceSquared / startCastingRangeSqr, 0, 1);
        int distanceWeight = (int) ((1 - distancePercent) * -75);

        if (target == null) {
            return baseWeight + healthWeight;
        }

        return baseWeight + healthWeight + distanceWeight;
    }

    protected int getPositiveWeight(LivingEntity supportTarget) {
        int baseWeight = 0;
        if (supportTarget == null) {
            for (LivingEntity nearbyAlliedEntity : nearbyAlliedEntities) {
                int weight = getPositiveWeightForSpecifiedEntity(nearbyAlliedEntity);
                if (weight > 0) {
                    baseWeight += weight;
                    break;
                }
            }
        } else {
            baseWeight += getPositiveWeightForSpecifiedEntity(supportTarget);
        }
        return baseWeight;
    }

    private int getPositiveWeightForSpecifiedEntity(LivingEntity supportTarget) {
        for (SpellData spellData : usableSupportEffectSpells) {
            Holder<MobEffect> causedEffect = MaidSpellRegistry.getSpellCausedEffect(spellData.getSpell());
            if (causedEffect != null && !supportTarget.hasEffect(causedEffect)) {
                // 队友还没上 buff
                return 500;
            }
            if (spellData.getSpell() instanceof CleanseSpell) {
                // 队友有负面 buff，我们有净化
                boolean hasHarmfulEffect = supportTarget.getActiveEffects()
                    .stream()
                    .map(MobEffectInstance::getEffect)
                    .anyMatch(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL && !effect.is(ModTags.CLEANSE_IMMUNE));
                if (hasHarmfulEffect) {
                    return 1000;
                }
            }
            if (causedEffect == null) {
                // 未知效果的法术，暂且计 10 分
                return 10;
            }
        }
        return 0;
    }

    protected int getNegativeWeight(LivingEntity target, double distanceSquared, boolean hasLineOfSight) {
        // 权重基于攻击法术，使得此类型的法术可用的越多优先级更高
        int baseWeight = usableNegativeEffectSpells.isEmpty() ? 0 : getAttackWeight(target, distanceSquared, hasLineOfSight);
        for (SpellData spellData : usableNegativeEffectSpells) {
            Holder<MobEffect> causedEffect = MaidSpellRegistry.getSpellCausedEffect(spellData.getSpell());
            if (causedEffect != null && !target.hasEffect(causedEffect)) {
                // 敌人没有这个 buff
                baseWeight += 10;
            } else {
                // 未知效果的法术，暂且计 5 分
                baseWeight += 5;
            }
        }
        return baseWeight;
    }

    protected int getSupportOtherWeight(LivingEntity supportTarget) {
        int baseWeight = 0;
        if (supportTarget == null) {
            for (LivingEntity nearbyAlliedEntity : nearbyAlliedEntities) {
                int weight = getSupportOtherWeightForSpecifiedEntity(nearbyAlliedEntity);
                if (weight > 0) {
                    baseWeight += weight;
                    break;
                }
            }
        } else {
            baseWeight += getSupportOtherWeightForSpecifiedEntity(supportTarget);
        }
        return baseWeight;
    }

    private int getSupportOtherWeightForSpecifiedEntity(LivingEntity supportTarget) {
        double healthScore = 1 - supportTarget.getHealth() / supportTarget.getMaxHealth();
        return Mth.floor(healthScore * 2500);
    }
}
