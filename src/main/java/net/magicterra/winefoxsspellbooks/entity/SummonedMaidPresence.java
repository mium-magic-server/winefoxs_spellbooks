package net.magicterra.winefoxsspellbooks.entity;

import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * 各维度内存活的召唤女仆计数（仅逻辑服务端维护）。
 * <p>
 * 供 AI 行为在「本维度一只召唤女仆都没有」时 O(1) 短路，省掉 AABB 实体查询。计数存在
 * {@link Level} 自己的附件上（{@link WsbAttachments#SUMMONED_MAID_COUNT}），由
 * {@link net.magicterra.winefoxsspellbooks.entity.summon.SummonedEntityMaid} 的
 * {@code onAddedToLevel} / {@code onRemovedFromLevel} 成对增减。只在服务端主线程读写，无需同步。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public final class SummonedMaidPresence {
    private SummonedMaidPresence() {
    }

    /**
     * 召唤女仆加入世界。客户端不计——消费方全在服务端 AI 侧。
     */
    public static void onAdded(Level level) {
        if (level.isClientSide) {
            return;
        }
        level.getData(WsbAttachments.SUMMONED_MAID_COUNT).increment();
    }

    /**
     * 召唤女仆离开世界。
     * <p>
     * 夹到 0 而不是任其变负：卸载路径不保证每只实体都补一次 {@code onRemovedFromLevel}， 计数一旦漏减又被后来的移除压成负数，短路就会对这个维度永远返回假。
     */
    public static void onRemoved(Level level) {
        if (level.isClientSide) {
            return;
        }
        MutableInt count = level.getData(WsbAttachments.SUMMONED_MAID_COUNT);
        count.setValue(Math.max(count.intValue() - 1, 0));
    }

    /**
     * 该维度是否存在至少一只存活的召唤女仆。
     */
    public static boolean isPresentIn(Level level) {
        MutableInt count = level.getExistingDataOrNull(WsbAttachments.SUMMONED_MAID_COUNT);
        return count != null && count.intValue() > 0;
    }
}
