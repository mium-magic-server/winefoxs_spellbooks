package net.magicterra.winefoxsspellbooks.entity;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.world.level.Level;

/**
 * 各维度内存活的召唤女仆计数（仅逻辑服务端维护）。
 * <p>
 * 供 AI 行为在「本维度一只召唤女仆都没有」时 O(1) 短路，省掉 AABB 实体查询。计数由
 * {@link net.magicterra.winefoxsspellbooks.entity.summon.SummonedEntityMaid} 的
 * {@code onAddedToLevel} / {@code onRemovedFromLevel} 成对增减，按 {@link Level} 实例分桶。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public final class SummonedMaidPresence {
    private SummonedMaidPresence() {
    }

    /**
     * 维度 → 存活召唤女仆数。按引用哈希，计数归零即摘除条目，故不会随维度卸载泄漏。 只在服务端主线程读写，无需同步。
     */
    private static final Reference2IntMap<Level> COUNTS = new Reference2IntOpenHashMap<>();

    /**
     * 召唤女仆加入世界。客户端不计——消费方全在服务端 AI 侧。
     */
    public static void onAdded(Level level) {
        if (level.isClientSide) {
            return;
        }
        COUNTS.mergeInt(level, 1, Integer::sum);
    }

    /**
     * 召唤女仆离开世界。归零时摘除条目，避免维度卸载后留下空桶。
     */
    public static void onRemoved(Level level) {
        if (level.isClientSide) {
            return;
        }
        int next = COUNTS.getInt(level) - 1;
        if (next <= 0) {
            COUNTS.removeInt(level);
        } else {
            COUNTS.put(level, next);
        }
    }

    /**
     * 该维度是否存在至少一只存活的召唤女仆。
     */
    public static boolean isPresentIn(Level level) {
        return COUNTS.getInt(level) > 0;
    }
}
