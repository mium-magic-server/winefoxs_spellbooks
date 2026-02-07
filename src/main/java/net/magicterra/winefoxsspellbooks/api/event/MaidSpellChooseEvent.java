package net.magicterra.winefoxsspellbooks.api.event;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.List;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * 女仆法术选择事件
 * <p>
 * 在女仆从选定的行为类型对应的法术列表中选择具体法术时触发。
 * 监听者可以：
 * <ol>
 *     <li>查看可用法术列表</li>
 *     <li>直接指定选中的法术 {@link #setSelectedSpell}（跳过随机选择）</li>
 * </ol>
 * <p>
 * 此事件在 {@code SpellChooseTask#start} 方法中触发。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-19 01:48
 */
public class MaidSpellChooseEvent extends LivingEvent {
    private final Mob mob;
    @Nullable
    private final LivingEntity attackTarget;
    @Nullable
    private final LivingEntity supportTarget;
    private final List<LivingEntity> nearbyAlliedEntities;

    /**
     * 已选定的行为类型
     */
    private final MaidSpellAction action;

    /**
     * 该行为类型下可用的法术列表
     */
    private final List<SpellData> availableSpells;

    /**
     * 选中的法术（如果设置，将跳过随机选择）
     */
    @Nullable
    private SpellData selectedSpell;

    /**
     * 构造事件
     *
     * @param mob                  生物实体
     * @param attackTarget         当前攻击目标，可为 null
     * @param supportTarget        当前支援目标，可为 null
     * @param nearbyAlliedEntities 附近的友方实体列表
     * @param action               已选定的行为类型
     * @param availableSpells      该行为类型下可用的法术列表
     */
    public MaidSpellChooseEvent(
            Mob mob,
            @Nullable LivingEntity attackTarget,
            @Nullable LivingEntity supportTarget,
            List<LivingEntity> nearbyAlliedEntities,
            MaidSpellAction action,
            List<SpellData> availableSpells) {
        super(mob);
        this.mob = mob;
        this.attackTarget = attackTarget;
        this.supportTarget = supportTarget;
        this.nearbyAlliedEntities = nearbyAlliedEntities;
        this.action = action;
        this.availableSpells = availableSpells;
        this.selectedSpell = null;
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
     * 获取已选定的行为类型
     *
     * @return 行为类型
     */
    public MaidSpellAction getAction() {
        return action;
    }

    /**
     * 获取当前行为类型下可用的法术列表（只读）
     *
     * @return 可用法术列表的不可变副本
     */
    public List<SpellData> getAvailableSpells() {
        return List.copyOf(availableSpells);
    }

    /**
     * 获取选中的法术
     *
     * @return 选中的法术，null 表示使用默认随机选择
     */
    @Nullable
    public SpellData getSelectedSpell() {
        return selectedSpell;
    }

    /**
     * 直接设置选中的法术，将跳过默认的随机选择逻辑
     *
     * @param spell 要选择的法术，null 表示使用默认逻辑
     */
    public void setSelectedSpell(@Nullable SpellData spell) {
        this.selectedSpell = spell;
    }
}
