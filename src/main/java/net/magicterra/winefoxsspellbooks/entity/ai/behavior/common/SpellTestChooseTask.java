package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;

/**
 * 测试用法术选择任务
 * <p>
 * 使用所有 {@link MaidSpellRegistry} 注册的法术（最高等级）， 并以轮流（round-robin）方式依次选择下一个动作类型和法术， 而非使用权重随机选择。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-02-08 20:53
 */
public class SpellTestChooseTask extends SpellChooseTask {

    /**
     * 当前动作类型的轮流索引
     */
    private int actionIndex = 0;

    /**
     * 每个动作类型的法术轮流索引
     */
    private final EnumMap<MaidSpellAction, Integer> spellIndices = new EnumMap<>(MaidSpellAction.class);

    public SpellTestChooseTask(double startCastingRange, int maxDelayTicks, Mob mob, Set<MaidSpellAction> allowedActions) {
        super(startCastingRange, maxDelayTicks, mob, allowedActions);
        // 初始化所有动作类型的法术索引为 0
        for (MaidSpellAction action : allowedActions) {
            spellIndices.put(action, 0);
        }
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

        // 初始化所有可用法术（先清空），全部从 MaidSpellRegistry 获取，使用最高等级
        usableAttackSpells.clear();
        usableDefenseSpells.clear();
        usableMovementSpells.clear();
        usableSupportSpells.clear();
        usablePositiveEffectSpells.clear();
        usableNegativeEffectSpells.clear();
        usableSupportEffectSpells.clear();

        if (allowedActions.contains(MaidSpellAction.ATTACK)) {
            populateFromRegistry(MaidSpellRegistry.ATTACK_SPELLS, usableAttackSpells);
        }
        if (allowedActions.contains(MaidSpellAction.DEFENSE)) {
            populateFromRegistry(MaidSpellRegistry.DEFENSE_SPELLS, usableDefenseSpells);
        }
        if (allowedActions.contains(MaidSpellAction.MOVEMENT)) {
            populateFromRegistry(MaidSpellRegistry.MOVEMENT_SPELLS, usableMovementSpells);
        }
        if (allowedActions.contains(MaidSpellAction.SUPPORT)) {
            populateFromRegistry(MaidSpellRegistry.SUPPORT_SPELLS, usableSupportSpells);
        }
        if (allowedActions.contains(MaidSpellAction.POSITIVE)) {
            populateFromRegistry(MaidSpellRegistry.POSITIVE_EFFECT_SPELLS, usablePositiveEffectSpells);
        }
        if (allowedActions.contains(MaidSpellAction.NEGATIVE)) {
            populateFromRegistry(MaidSpellRegistry.NEGATIVE_EFFECT_SPELLS, usableNegativeEffectSpells);
        }
        if (allowedActions.contains(MaidSpellAction.SUPPORT_OTHER)) {
            populateFromRegistry(MaidSpellRegistry.SUPPORT_EFFECT_SPELLS, usableSupportEffectSpells);
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

        // 轮流选择下一个动作类型
        MaidSpellAction nextSpellAction = getNextSpellAction();
        if (nextSpellAction == null) {
            return false;
        }

        mob.getBrain().setMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(), nextSpellAction);
        return true;
    }

    /**
     * 从注册表的法术集合中填充可用法术列表，使用最高等级
     *
     * @param registry   注册表中的法术集合
     * @param targetList 要填充的目标列表
     */
    private void populateFromRegistry(Set<AbstractSpell> registry, List<SpellData> targetList) {
        for (AbstractSpell spell : registry) {
            SpellData spellData = new SpellData(spell, spell.getMaxLevel());
            targetList.add(spellData);
        }
    }

    /**
     * 轮流选择下一个动作类型（round-robin）
     * <p>
     * 在所有拥有可用法术的动作类型中依次轮转， 跳过没有可用法术的动作类型。
     *
     * @return 下一个动作类型，如果所有动作都没有可用法术则返回 null
     */
    @Override
    protected MaidSpellAction getNextSpellAction() {
        // 收集有可用法术的动作类型（保持枚举声明顺序）
        List<MaidSpellAction> availableActions = new ArrayList<>();
        for (MaidSpellAction action : allowedActions) {
            if (!allowedActions.contains(action)) {
                continue;
            }
            List<SpellData> spells = getSpellListForAction(action);
            if (spells != null && !spells.isEmpty()) {
                availableActions.add(action);
            }
        }

        if (availableActions.isEmpty()) {
            return null;
        }

        // 确保索引在有效范围内
        if (actionIndex >= availableActions.size()) {
            actionIndex = 0;
        }

        MaidSpellAction selected = availableActions.get(actionIndex);
        actionIndex = (actionIndex + 1) % availableActions.size();
        return selected;
    }

    /**
     * 覆盖父类 start 方法，使用轮流方式选择法术而非随机
     */
    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        MaidSpellAction nextSpellAction = entity.getBrain()
            .getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get())
            .orElseThrow();

        List<SpellData> availableSpells = getSpellListForAction(nextSpellAction);
        if (availableSpells == null || availableSpells.isEmpty()) {
            return;
        }

        // 轮流选择法术（round-robin）
        int currentSpellIndex = spellIndices.getOrDefault(nextSpellAction, 0);
        if (currentSpellIndex >= availableSpells.size()) {
            currentSpellIndex = 0;
        }
        SpellData spellData = availableSpells.get(currentSpellIndex);
        spellIndices.put(nextSpellAction, (currentSpellIndex + 1) % availableSpells.size());

        entity.getBrain().setMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(), spellData);
        if (entity instanceof EntityMaid maid) {
            MaidMagicManager.showCurrentSpellInChatBubble(maid, spellData, false);
        }

        // 支援类动作需要选择队友目标
        if (nextSpellAction == MaidSpellAction.SUPPORT_OTHER || nextSpellAction == MaidSpellAction.POSITIVE) {
            for (LivingEntity nearbyAlliedEntity : nearbyAlliedEntities) {
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

        lastAction = nextSpellAction;

        if (entity instanceof MaidMagicEntity maid) {
            maid.winefoxsSpellbooks$getMagicMaidAdapter().setBypassCastCheck(true);
        }
    }

    @Override
    protected void stop(ServerLevel level, Mob entity, long gameTime) {
        super.stop(level, entity, gameTime);
        if (entity instanceof MaidMagicEntity maid) {
            maid.winefoxsSpellbooks$getMagicMaidAdapter().setBypassCastCheck(false);
        }
    }
}
