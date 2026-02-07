package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.spells.holy.CleanseSpell;
import io.redspace.ironsspellbooks.util.ModTags;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.api.event.MaidSpellActionChooseEvent;
import net.magicterra.winefoxsspellbooks.api.event.MaidSpellChooseEvent;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 选择法术任务
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-03 01:35
 */
public class SpellChooseTask extends Behavior<Mob> {
    protected final Set<MaidSpellAction> allowedActions;
    protected final Mob mob;
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

    public SpellChooseTask(double startCastingRange, int maxDelayTicks, Mob mob, Set<MaidSpellAction> allowedActions) {
        super(ImmutableMap.of(
            MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(), MemoryStatus.REGISTERED,
            MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(), MemoryStatus.VALUE_ABSENT,
            MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(), MemoryStatus.REGISTERED,
            MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED
        ), 1200);
        this.mob = mob;
        this.magicEntity = (IMagicEntity) mob;
        this.allowedActions = allowedActions;
        this.startCastingRange = startCastingRange;
        this.startCastingRangeSqr = startCastingRange * startCastingRange;
        this.alliedEntityPredicate = (entity) -> {
            if (Objects.equals(entity, mob)) {
                return false;
            }
            if (entity.isDeadOrDying()) {
                return false;
            }
            if (mob instanceof TamableAnimal tamableAnimal && Objects.equals(tamableAnimal.getOwner(), entity)) {
                return true;
            }
            return entity.isAlliedTo(mob);
        };
        this.maxDelayTicks = maxDelayTicks;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
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
            AABB aabb;
            if (mob instanceof EntityMaid maid) {
                aabb = maid.searchDimension();
            } else if (mob.hasRestriction()) {
                float restrictRadius = mob.getRestrictRadius();
                aabb = new AABB(mob.getRestrictCenter()).inflate(restrictRadius, 4, restrictRadius);
            } else {
                double radius = 16;
                aabb = mob.getBoundingBox().inflate(radius, 4, radius);
            }
            List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, aabb, this.alliedEntityPredicate);
            list.sort(Comparator.comparingDouble(owner::distanceToSqr));
            nearbyAlliedEntities.addAll(list);
        }

        // 计算下一步操作，根据分数
        MaidSpellAction nextSpellAction = getNextSpellAction();
        if (nextSpellAction == null) {
            return false;
        }

        mob.getBrain().setMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(), nextSpellAction);
        return true;
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        // 设置记忆
        MaidSpellAction nextSpellAction = entity.getBrain().getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get()).orElseThrow();
        // 随机挑选法术，根据操作类型
        List<SpellData> availableSpells = getSpellListForAction(nextSpellAction);
        if (availableSpells == null || availableSpells.isEmpty()) {
            return;
        }

        // 触发 MaidSpellChooseEvent 事件
        LivingEntity attackTarget = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        LivingEntity supportTarget = entity.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()).orElse(null);
        MaidSpellChooseEvent spellEvent = new MaidSpellChooseEvent(
            entity, attackTarget, supportTarget, nearbyAlliedEntities,
            nextSpellAction, availableSpells
        );
        NeoForge.EVENT_BUS.post(spellEvent);
        SpellData spellData = spellEvent.getSelectedSpell();
        if (spellData == null) {
            // 未被事件指定，使用默认随机选择
            spellData = availableSpells.get(entity.getRandom().nextInt(availableSpells.size()));
        }

        entity.getBrain().setMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(), spellData);
        if (entity instanceof EntityMaid maid) {
            MaidMagicManager.showCurrentSpellInChatBubble(maid, spellData, false);
        }

        if (nextSpellAction == MaidSpellAction.SUPPORT_OTHER || nextSpellAction == MaidSpellAction.POSITIVE) {
            for (LivingEntity nearbyAlliedEntity : nearbyAlliedEntities) {
                int weight = nextSpellAction == MaidSpellAction.SUPPORT_OTHER ?
                             getSupportOtherWeightForSpecifiedEntity(nearbyAlliedEntity) :
                             getPositiveWeightForSpecifiedEntity(nearbyAlliedEntity);
                if (weight > 0) {
                    mob.getBrain().setMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(), nearbyAlliedEntity);
                    if (mob.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)) {
                        // 转换目标
                        mob.getNavigation().stop();
                        mob.getMoveControl().strafe(0, 0);
                        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                        mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
                    }
                    break;
                }
            }
        }

        lastAction = nextSpellAction;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        // 记忆没有清除就继续
        return entity.getBrain().hasMemoryValue(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get());
    }

    @Override
    protected void stop(ServerLevel level, Mob entity, long gameTime) {
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

    /**
     * 根据行为类型获取对应的法术列表
     *
     * @param action 行为类型
     * @return 对应的可用法术列表，未知类型返回 null
     */
    protected List<SpellData> getSpellListForAction(MaidSpellAction action) {
        return switch (action) {
            case ATTACK -> usableAttackSpells;
            case DEFENSE -> usableDefenseSpells;
            case MOVEMENT -> usableMovementSpells;
            case SUPPORT -> usableSupportSpells;
            case POSITIVE -> usablePositiveEffectSpells;
            case NEGATIVE -> usableNegativeEffectSpells;
            case SUPPORT_OTHER -> usableSupportEffectSpells;
        };
    }

    /**
     * 构建各行为类型对应的可用法术映射（只读副本，用于事件）
     *
     * @return 行为类型 -&gt; 可用法术列表的不可变映射
     */
    protected Map<MaidSpellAction, List<SpellData>> buildAvailableSpellsByAction() {
        EnumMap<MaidSpellAction, List<SpellData>> map = new EnumMap<>(MaidSpellAction.class);
        if (!usableAttackSpells.isEmpty()) map.put(MaidSpellAction.ATTACK, List.copyOf(usableAttackSpells));
        if (!usableDefenseSpells.isEmpty()) map.put(MaidSpellAction.DEFENSE, List.copyOf(usableDefenseSpells));
        if (!usableMovementSpells.isEmpty()) map.put(MaidSpellAction.MOVEMENT, List.copyOf(usableMovementSpells));
        if (!usableSupportSpells.isEmpty()) map.put(MaidSpellAction.SUPPORT, List.copyOf(usableSupportSpells));
        if (!usablePositiveEffectSpells.isEmpty()) map.put(MaidSpellAction.POSITIVE, List.copyOf(usablePositiveEffectSpells));
        if (!usableNegativeEffectSpells.isEmpty()) map.put(MaidSpellAction.NEGATIVE, List.copyOf(usableNegativeEffectSpells));
        if (!usableSupportEffectSpells.isEmpty()) map.put(MaidSpellAction.SUPPORT_OTHER, List.copyOf(usableSupportEffectSpells));
        return Collections.unmodifiableMap(map);
    }

    /**
     * 计算所有行为类型的默认权重
     *
     * @param attackTarget    攻击目标
     * @param supportTarget   支援目标
     * @param distanceSquared 与攻击目标的距离平方
     * @param canSee          是否可见攻击目标
     * @return 行为类型 -&gt; 权重的映射
     */
    protected EnumMap<MaidSpellAction, Integer> calculateDefaultActionWeights(
            LivingEntity attackTarget, LivingEntity supportTarget,
            double distanceSquared, boolean canSee) {
        EnumMap<MaidSpellAction, Integer> weights = new EnumMap<>(MaidSpellAction.class);

        // 治疗自己
        int supportWeight = getSupportWeight(attackTarget, distanceSquared);
        if (!usableSupportSpells.isEmpty()) {
            weights.put(MaidSpellAction.SUPPORT, supportWeight);
        }

        // 支援队友
        if (!nearbyAlliedEntities.isEmpty() || supportTarget != null) {
            int positiveEffectWeight = getPositiveWeight(supportTarget);
            int supportEffectWeight = getSupportOtherWeight(supportTarget);
            if (!usablePositiveEffectSpells.isEmpty()) {
                weights.put(MaidSpellAction.POSITIVE, positiveEffectWeight);
            }
            if (!usableSupportEffectSpells.isEmpty()) {
                weights.put(MaidSpellAction.SUPPORT_OTHER, supportEffectWeight);
            }
        }

        // 攻击相关
        int attackWeight = 0;
        int defenseWeight = lastAction == MaidSpellAction.DEFENSE ? -100 : 0;
        int movementWeight = lastAction == MaidSpellAction.MOVEMENT ? -50 : 0;
        int negativeEffectWeight = lastAction == MaidSpellAction.NEGATIVE ? -100 : 0;

        if (attackTarget != null) {
            attackWeight += getAttackWeight(attackTarget, distanceSquared, canSee);
            defenseWeight += getDefenseWeight(attackTarget);
            movementWeight += getMovementWeight(attackTarget, distanceSquared, canSee);
            negativeEffectWeight += getNegativeWeight(attackTarget, distanceSquared, canSee);
        }

        if (!usableAttackSpells.isEmpty()) weights.put(MaidSpellAction.ATTACK, attackWeight);
        if (!usableDefenseSpells.isEmpty()) weights.put(MaidSpellAction.DEFENSE, defenseWeight);
        if (!usableMovementSpells.isEmpty()) weights.put(MaidSpellAction.MOVEMENT, movementWeight);
        if (!usableNegativeEffectSpells.isEmpty()) weights.put(MaidSpellAction.NEGATIVE, negativeEffectWeight);

        return weights;
    }

    /**
     * 根据权重映射选择行为类型
     * <p>
     * 保留原有的两阶段加权随机逻辑：
     * 第一阶段（支援类优先）：SUPPORT + POSITIVE + SUPPORT_OTHER 参与加权随机，
     * 若选中则直接返回；
     * 第二阶段（战斗类）：ATTACK + DEFENSE + MOVEMENT + NEGATIVE 参与加权随机。
     *
     * @param weights 行为类型 -&gt; 权重的映射
     * @return 选中的行为类型，null 表示没有可用的行为
     */
    protected MaidSpellAction selectActionByWeights(Map<MaidSpellAction, Integer> weights) {
        NavigableMap<Integer, MaidSpellAction> weightedSpells = new TreeMap<>();
        int total = 0;

        // 第一阶段：支援类优先
        int supportWeight = weights.getOrDefault(MaidSpellAction.SUPPORT, 0);
        if (!usableSupportSpells.isEmpty() && supportWeight > 0) {
            total += supportWeight;
            weightedSpells.put(total, MaidSpellAction.SUPPORT);
        }

        LivingEntity supportTarget = mob.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()).orElse(null);
        if (!nearbyAlliedEntities.isEmpty() || supportTarget != null) {
            int positiveEffectWeight = weights.getOrDefault(MaidSpellAction.POSITIVE, 0);
            int supportEffectWeight = weights.getOrDefault(MaidSpellAction.SUPPORT_OTHER, 0);

            if (!usablePositiveEffectSpells.isEmpty() && positiveEffectWeight > 0) {
                total += positiveEffectWeight;
                weightedSpells.put(total, MaidSpellAction.POSITIVE);
            }

            if (!usableSupportEffectSpells.isEmpty() && supportEffectWeight > 0) {
                total += supportEffectWeight;
                weightedSpells.put(total, MaidSpellAction.SUPPORT_OTHER);
            }

            if (total > 0) {
                int seed = mob.getRandom().nextInt(total);
                return weightedSpells.higherEntry(seed).getValue();
            }
            if (mob instanceof EntityMaid maid && usableSupportEffectSpells.isEmpty() && supportEffectWeight > 0) {
                MaidMagicManager.showCurrentSpellInChatBubble(maid, SpellData.EMPTY, true);
            }
        }

        // 第二阶段：战斗类
        int attackWeight = weights.getOrDefault(MaidSpellAction.ATTACK, 0);
        int defenseWeight = weights.getOrDefault(MaidSpellAction.DEFENSE, 0);
        int movementWeight = weights.getOrDefault(MaidSpellAction.MOVEMENT, 0);
        int negativeEffectWeight = weights.getOrDefault(MaidSpellAction.NEGATIVE, 0);

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
            int seed = mob.getRandom().nextInt(total);
            return weightedSpells.higherEntry(seed).getValue();
        }

        return null;
    }

    protected MaidSpellAction getNextSpellAction() {
        LivingEntity attackTarget = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        LivingEntity supportTarget = mob.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()).orElse(null);
        boolean canSee;
        if (mob instanceof EntityMaid maid) {
            canSee = attackTarget != null && maid.canSee(attackTarget);
        } else {
            canSee = attackTarget != null && BehaviorUtils.canSee(mob, attackTarget);
        }
        double distanceSquared = attackTarget != null ? mob.distanceToSqr(attackTarget) : 0;

        // 计算默认权重
        EnumMap<MaidSpellAction, Integer> actionWeights = calculateDefaultActionWeights(
            attackTarget, supportTarget, distanceSquared, canSee
        );

        // 触发 MaidSpellActionChooseEvent 事件
        Map<MaidSpellAction, List<SpellData>> availableSpellsByAction = buildAvailableSpellsByAction();
        MaidSpellActionChooseEvent actionEvent = new MaidSpellActionChooseEvent(
            mob, attackTarget, supportTarget, nearbyAlliedEntities,
            availableSpellsByAction, actionWeights
        );
        NeoForge.EVENT_BUS.post(actionEvent);

        // 如果事件直接指定了行为类型，跳过权重计算
        MaidSpellAction selectedAction = actionEvent.getSelectedAction();
        if (selectedAction != null) {
            return selectedAction;
        }

        // 使用事件可能修改过的权重进行选择
        return selectActionByWeights(actionEvent.getActionWeightsInternal());
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
        float x = mob.getHealth();
        float m = mob.getMaxHealth();
        //int healthWeight = (int) (50 * (Math.pow(-(x / m) * (x - m), 3) / Math.pow(m / 2, 3)) * 8);
        int healthWeight = (int) (50 * (-(x * x * x) / (m * m * m) + 1));

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) (1 - targetHealth) * -35;

        //this count be finicky due to the fact that projectiles don't stick around for long, so it might be easy to miss them
        // int threatWeight = projectileCount * 95;

        int usableSpellWeight = 0;
        for (SpellData spellData : usableDefenseSpells) {
            Holder<MobEffect> causedEffect = MaidSpellRegistry.getSpellCausedEffect(spellData.getSpell());
            if (causedEffect != null && !mob.hasEffect(causedEffect)) {
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

        float healthInverted = 1 - mob.getHealth() / mob.getMaxHealth();
        float distanceInverted = (float) (1 - distancePercent);
        int runWeight = (int) (400 * healthInverted * healthInverted * distanceInverted * distanceInverted);

        return distanceWeight + losWeight + runWeight;
    }

    protected int getSupportWeight(LivingEntity target, double distanceSquared) {
        //We want to support/buff ourselves if we are weak
        int baseWeight = -10;

        float health = 1 - mob.getHealth() / mob.getMaxHealth();
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
