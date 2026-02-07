package net.magicterra.winefoxsspellbooks.api.event;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * 女仆行为类型选择事件
 * <p>
 * 在女仆 AI 决定使用哪种行为类型（攻击/防御/治疗等）时触发。
 * 监听者可以：
 * <ol>
 *     <li>修改各行为类型的权重 {@link #setActionWeight}</li>
 *     <li>直接指定行为类型 {@link #setSelectedAction}（跳过权重计算）</li>
 * </ol>
 * <p>
 * 此事件在 {@code SpellChooseTask#checkExtraStartConditions} 末尾触发。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-02-05
 */
public class MaidSpellActionChooseEvent extends LivingEvent {
    private final Mob mob;
    @Nullable
    private final LivingEntity attackTarget;
    @Nullable
    private final LivingEntity supportTarget;
    private final List<LivingEntity> nearbyAlliedEntities;

    /**
     * 各行为类型对应的可用法术列表（只读）
     */
    private final Map<MaidSpellAction, List<SpellData>> availableSpellsByAction;

    /**
     * 各行为类型的权重（可修改）
     */
    private final EnumMap<MaidSpellAction, Integer> actionWeights;

    /**
     * 选中的行为类型（如果设置，将跳过默认权重计算）
     */
    @Nullable
    private MaidSpellAction selectedAction;

    /**
     * 构造事件
     *
     * @param mob                      生物实体
     * @param attackTarget             当前攻击目标，可为 null
     * @param supportTarget            当前支援目标，可为 null
     * @param nearbyAlliedEntities     附近的友方实体列表
     * @param availableSpellsByAction  各行为类型对应的可用法术列表
     * @param actionWeights            各行为类型的初始权重
     */
    public MaidSpellActionChooseEvent(
            Mob mob,
            @Nullable LivingEntity attackTarget,
            @Nullable LivingEntity supportTarget,
            List<LivingEntity> nearbyAlliedEntities,
            Map<MaidSpellAction, List<SpellData>> availableSpellsByAction,
            Map<MaidSpellAction, Integer> actionWeights) {
        super(mob);
        this.mob = mob;
        this.attackTarget = attackTarget;
        this.supportTarget = supportTarget;
        this.nearbyAlliedEntities = nearbyAlliedEntities;
        this.availableSpellsByAction = availableSpellsByAction;
        this.actionWeights = new EnumMap<>(actionWeights);
        this.selectedAction = null;
    }

    /**
     * 获取生物实体
     *
     * @return 生物实体
     */
    public Mob getMob() {
        return mob;
    }

    /**
     * 获取当前攻击目标
     *
     * @return 攻击目标，可能为 null
     */
    @Nullable
    public LivingEntity getAttackTarget() {
        return attackTarget;
    }

    /**
     * 获取当前支援目标
     *
     * @return 支援目标，可能为 null
     */
    @Nullable
    public LivingEntity getSupportTarget() {
        return supportTarget;
    }

    /**
     * 获取附近的友方实体列表（不可修改）
     *
     * @return 友方实体列表的不可变副本
     */
    public List<LivingEntity> getNearbyAlliedEntities() {
        return List.copyOf(nearbyAlliedEntities);
    }

    /**
     * 获取各行为类型对应的可用法术列表（只读）
     *
     * @return 行为类型 -&gt; 可用法术列表的映射
     */
    public Map<MaidSpellAction, List<SpellData>> getAvailableSpellsByAction() {
        return Collections.unmodifiableMap(availableSpellsByAction);
    }

    /**
     * 获取某个行为类型的可用法术列表
     *
     * @param action 行为类型
     * @return 可用法术列表，若不存在则返回空列表
     */
    public List<SpellData> getAvailableSpells(MaidSpellAction action) {
        return availableSpellsByAction.getOrDefault(action, List.of());
    }

    /**
     * 获取各行为类型的权重
     * <p>
     * 返回的 Map 可以通过 {@link #setActionWeight} 方法修改
     *
     * @return 行为类型 -&gt; 权重的映射
     */
    public Map<MaidSpellAction, Integer> getActionWeights() {
        return Collections.unmodifiableMap(actionWeights);
    }

    /**
     * 获取某个行为类型的权重
     *
     * @param action 行为类型
     * @return 权重值，若不存在则返回 0
     */
    public int getActionWeight(MaidSpellAction action) {
        return actionWeights.getOrDefault(action, 0);
    }

    /**
     * 设置某个行为类型的权重
     *
     * @param action 行为类型
     * @param weight 权重值（0 或负数表示禁用该行为）
     */
    public void setActionWeight(MaidSpellAction action, int weight) {
        actionWeights.put(action, weight);
    }

    /**
     * 获取内部权重 Map（供 SpellChooseTask 使用）
     *
     * @return 可修改的权重 Map
     */
    public EnumMap<MaidSpellAction, Integer> getActionWeightsInternal() {
        return actionWeights;
    }

    /**
     * 获取选中的行为类型
     *
     * @return 选中的行为类型，null 表示使用默认权重计算
     */
    @Nullable
    public MaidSpellAction getSelectedAction() {
        return selectedAction;
    }

    /**
     * 直接设置选中的行为类型，将跳过默认的权重随机选择逻辑
     *
     * @param action 要选择的行为类型，null 表示使用默认逻辑
     */
    public void setSelectedAction(@Nullable MaidSpellAction action) {
        this.selectedAction = action;
    }
}
