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
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.magicterra.winefoxsspellbooks.magic.spell.ManaTransferSpell;
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

    /**
     * 本轮筛出的可用法术，按行为类别分桶。桶实例在构造时建好后不再更换—— 子类（如 {@code SpellTestChooseTask}）持有它们的引用直接填充。
     */
    protected final Map<MaidSpellAction, List<SpellData>> usableSpells = new EnumMap<>(MaidSpellAction.class);

    protected final List<LivingEntity> nearbyAlliedEntities = new ArrayList<>();

    protected Predicate<LivingEntity> alliedEntityPredicate;

    protected MaidSpellAction lastAction;

    /**
     * 本轮 {@link #calculateDefaultActionWeights} 算出的「救队友」权重。
     * <p>
     * 该权重只由队友血量决定，本轮内不会变，而 {@link #selectActionByWeights} 判空桶时还要用它一次—— 存下来而不是重算：{@link #getSupportOtherWeight} 会走一遍附近盟友表，队友都满血时走满全表。
     */
    protected int lastSupportOtherWeight;

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
        for (MaidSpellAction action : MaidSpellAction.ALL) {
            usableSpells.put(action, new ArrayList<>());
        }
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
        for (MaidSpellAction action : MaidSpellAction.ALL) {
            List<SpellData> bucket = usableSpells.get(action);
            bucket.clear();
            if (!allowedActions.contains(action)) {
                continue;
            }
            for (SpellData spellData : spellDataHolder.get(action)) {
                if (MaidMagicManager.isSpellUsable(owner, spellData)) {
                    bucket.add(spellData);
                }
            }
        }

        // 计算附近队友，检查有无可用支援队友的法术，如果没有可用法术则跳过
        nearbyAlliedEntities.clear();
        if (!usable(MaidSpellAction.POSITIVE).isEmpty() || !usable(MaidSpellAction.SUPPORT_OTHER).isEmpty()) {
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
        MaidSpellAction nextSpellAction = entity.getBrain()
            .getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get()).orElseThrow();
        // 随机挑选法术，根据操作类型
        List<SpellData> availableSpells = usable(nextSpellAction);
        if (availableSpells.isEmpty()) {
            return;
        }

        // 触发 MaidSpellChooseEvent 事件
        LivingEntity attackTarget = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        LivingEntity supportTarget = entity.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get())
            .orElse(null);
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
        usableSpells.values().forEach(List::clear);
        // 清理队友列表
        nearbyAlliedEntities.clear();
    }

    /**
     * 本轮某一行为类型下筛出的可用法术（活引用，只读使用）。
     *
     * @param action 行为类型
     * @return 对应的可用法术列表，恒非 null（无可用时为空表）
     */
    protected List<SpellData> usable(MaidSpellAction action) {
        return usableSpells.get(action);
    }

    /**
     * 构建各行为类型对应的可用法术映射（只读副本，用于事件）
     *
     * @return 行为类型 -&gt; 可用法术列表的不可变映射
     */
    protected Map<MaidSpellAction, List<SpellData>> buildAvailableSpellsByAction() {
        EnumMap<MaidSpellAction, List<SpellData>> map = new EnumMap<>(MaidSpellAction.class);
        usableSpells.forEach((action, spells) -> {
            if (!spells.isEmpty()) {
                map.put(action, List.copyOf(spells));
            }
        });
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
        if (!usable(MaidSpellAction.SUPPORT).isEmpty()) {
            weights.put(MaidSpellAction.SUPPORT, supportWeight);
        }

        // 支援队友
        lastSupportOtherWeight = 0;
        if (!nearbyAlliedEntities.isEmpty() || supportTarget != null) {
            int positiveEffectWeight = getPositiveWeight(supportTarget);
            lastSupportOtherWeight = getSupportOtherWeight(supportTarget);
            if (!usable(MaidSpellAction.POSITIVE).isEmpty()) {
                weights.put(MaidSpellAction.POSITIVE, positiveEffectWeight);
            }
            if (!usable(MaidSpellAction.SUPPORT_OTHER).isEmpty()) {
                weights.put(MaidSpellAction.SUPPORT_OTHER, lastSupportOtherWeight);
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

        if (!usable(MaidSpellAction.ATTACK).isEmpty())
            weights.put(MaidSpellAction.ATTACK, attackWeight);
        if (!usable(MaidSpellAction.DEFENSE).isEmpty())
            weights.put(MaidSpellAction.DEFENSE, defenseWeight);
        if (!usable(MaidSpellAction.MOVEMENT).isEmpty())
            weights.put(MaidSpellAction.MOVEMENT, movementWeight);
        if (!usable(MaidSpellAction.NEGATIVE).isEmpty())
            weights.put(MaidSpellAction.NEGATIVE, negativeEffectWeight);

        return weights;
    }

    /**
     * 两阶段加权随机：先看能不能救队友（POSITIVE / SUPPORT_OTHER），救不成再从自己这边挑
     * （SUPPORT / ATTACK / DEFENSE / MOVEMENT / NEGATIVE）。
     * <p>
     * 自愈（SUPPORT）归第二阶段：它若参与第一阶段的早退判断，只要附近站着任何一个盟友——哪怕 不需要救——半血女仆就会无条件重选自愈而不还手。「优先」的对象是队友，不是自己。
     *
     * @param weights 行为类型 -&gt; 权重的映射
     * @return 选中的行为类型，null 表示没有可用的行为
     */
    protected MaidSpellAction selectActionByWeights(Map<MaidSpellAction, Integer> weights) {
        NavigableMap<Integer, MaidSpellAction> weightedSpells = new TreeMap<>();
        int total = 0;

        // 第一阶段：支援队友
        LivingEntity supportTarget = mob.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get())
            .orElse(null);
        if (!nearbyAlliedEntities.isEmpty() || supportTarget != null) {
            total = accumulate(weights, weightedSpells, total,
                MaidSpellAction.POSITIVE, MaidSpellAction.SUPPORT_OTHER);

            if (total > 0) {
                int seed = mob.getRandom().nextInt(total);
                return weightedSpells.higherEntry(seed).getValue();
            }
            // 有人等着救却拿不出治疗法术，冒个泡提示玩家配一本。这里不能拿 weights 里的
            // SUPPORT_OTHER 判：它只在该桶非空时才有，用来判空桶恒不成立
            if (mob instanceof EntityMaid maid && usable(MaidSpellAction.SUPPORT_OTHER).isEmpty()
                && lastSupportOtherWeight > 0) {
                MaidMagicManager.showCurrentSpellInChatBubble(maid, SpellData.EMPTY, true);
            }
        }

        // 第二阶段：自愈与战斗类同池竞争
        total = accumulate(weights, weightedSpells, total, MaidSpellAction.SUPPORT, MaidSpellAction.ATTACK,
            MaidSpellAction.DEFENSE, MaidSpellAction.MOVEMENT, MaidSpellAction.NEGATIVE);

        if (total > 0) {
            int seed = mob.getRandom().nextInt(total);
            return weightedSpells.higherEntry(seed).getValue();
        }

        return null;
    }

    /**
     * 把给定类别按权重累加进轮盘，返回新的总权重。权重非正或该类无可用法术的跳过。
     * <p>
     * 传入顺序即轮盘的落点顺序，改动会改变同一随机种子选中的类别。
     */
    private int accumulate(Map<MaidSpellAction, Integer> weights,
                           NavigableMap<Integer, MaidSpellAction> weightedSpells,
                           int total, MaidSpellAction... actions) {
        for (MaidSpellAction action : actions) {
            int weight = weights.getOrDefault(action, 0);
            if (weight > 0 && !usable(action).isEmpty()) {
                total += weight;
                weightedSpells.put(total, action);
            }
        }
        return total;
    }

    protected MaidSpellAction getNextSpellAction() {
        LivingEntity attackTarget = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        LivingEntity supportTarget = mob.getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get())
            .orElse(null);
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

    // 攻击是常态动作：己方血厚或目标血薄时权重更高
    protected int getAttackWeight(LivingEntity target, double distanceSquared, boolean hasLineOfSight) {
        int baseWeight = 80;
        if (!hasLineOfSight || target == null) {
            return 0;
        }

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) ((1 - targetHealth) * baseWeight * .75f);
        int distanceWeight = (int) (1 - (distanceSquared / startCastingRangeSqr) * -60);
        return baseWeight + targetHealthWeight + distanceWeight;
    }

    // 血量越低、被压制越狠，防御法术权重越高
    protected int getDefenseWeight(LivingEntity target) {
        int baseWeight = 20;

        if (target == null) {
            return baseWeight;
        }

        float x = mob.getHealth();
        float m = mob.getMaxHealth();
        int healthWeight = (int) (50 * (-(x * x * x) / (m * m * m) + 1));

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) ((1 - targetHealth) * -35);

        int usableSpellWeight = 0;
        for (SpellData spellData : usable(MaidSpellAction.DEFENSE)) {
            Holder<MobEffect> causedEffect = MaidSpellCatalog.getSpellCausedEffect(spellData.getSpell());
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

    // 位置不利或没视线时才走位
    protected int getMovementWeight(LivingEntity target, double distanceSquared, boolean hasLineOfSight) {
        if (target == null) {
            return 0;
        }
        double distancePercent = Mth.clamp(distanceSquared / startCastingRangeSqr, 0, 1);

        int distanceWeight = (int) ((distancePercent) * 50);

        int losWeight = hasLineOfSight ? 0 : 80;

        float healthInverted = 1 - mob.getHealth() / mob.getMaxHealth();
        float distanceInverted = (float) (1 - distancePercent);
        int runWeight = (int) (400 * healthInverted * healthInverted * distanceInverted * distanceInverted);

        return distanceWeight + losWeight + runWeight;
    }

    // 血量越低越想自保；目标贴脸时不喝药
    protected int getSupportWeight(LivingEntity target, double distanceSquared) {
        int baseWeight = -10;

        float health = 1 - mob.getHealth() / mob.getMaxHealth();
        int healthWeight = (int) (200 * health);

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

    /**
     * 目标身上有没有能被净化术清掉的负面效果。
     * <p>
     * 用普通循环而非 stream：本方法处在「每个附近队友 × 每个可用法术」的双重循环内层， 每次调用都新建一条 stream 管线纯属浪费；短路语义与 {@code anyMatch} 一致。
     */
    private static boolean hasCleansableEffect(LivingEntity target) {
        for (MobEffectInstance instance : target.getActiveEffects()) {
            Holder<MobEffect> effect = instance.getEffect();
            if (effect.value().getCategory() == MobEffectCategory.HARMFUL && !effect.is(ModTags.CLEANSE_IMMUNE)) {
                return true;
            }
        }
        return false;
    }

    private int getPositiveWeightForSpecifiedEntity(LivingEntity supportTarget) {
        for (SpellData spellData : usable(MaidSpellAction.POSITIVE)) {
            Holder<MobEffect> causedEffect = MaidSpellCatalog.getSpellCausedEffect(spellData.getSpell());
            if (causedEffect != null && !supportTarget.hasEffect(causedEffect)) {
                // 队友还没上 buff
                return 500;
            }
            if (spellData.getSpell() instanceof CleanseSpell) {
                // 队友有负面 buff，我们有净化
                if (hasCleansableEffect(supportTarget)) {
                    return 1000;
                }
            } else if (spellData.getSpell() instanceof ManaTransferSpell spell) {
                if (spell.isTargetNeedsSupport(supportTarget)) {
                    return 1000;
                } else {
                    return 0;
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
        int baseWeight =
            usable(MaidSpellAction.NEGATIVE).isEmpty() ? 0 : getAttackWeight(target, distanceSquared, hasLineOfSight);
        for (SpellData spellData : usable(MaidSpellAction.NEGATIVE)) {
            Holder<MobEffect> causedEffect = MaidSpellCatalog.getSpellCausedEffect(spellData.getSpell());
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
