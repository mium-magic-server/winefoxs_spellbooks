package net.magicterra.winefoxsspellbooks.entity.loadout.data;

import java.util.List;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.ItemPool;

/**
 * 护甲槽位配置
 *
 * @param chance 生成概率 (0.0-1.0)
 * @param pools  物品池列表
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record ArmorSlotConfig(float chance, List<ItemPool> pools) {
    /**
     * 空配置，表示该槽位不生成护甲
     */
    public static final ArmorSlotConfig EMPTY = new ArmorSlotConfig(0.0f, List.of());

    /**
     * 创建护甲槽位配置
     *
     * @param chance 生成概率
     * @param pools  物品池列表
     * @return ArmorSlotConfig
     */
    public static ArmorSlotConfig of(float chance, List<ItemPool> pools) {
        return new ArmorSlotConfig(chance, pools);
    }

    /**
     * 创建护甲槽位配置（单个池）
     *
     * @param chance 生成概率
     * @param pool   物品池
     * @return ArmorSlotConfig
     */
    public static ArmorSlotConfig of(float chance, ItemPool pool) {
        return new ArmorSlotConfig(chance, List.of(pool));
    }

    /**
     * 检查是否应该生成护甲
     *
     * @return 如果概率大于 0 且有可用池则返回 true
     */
    public boolean shouldGenerate() {
        return chance > 0.0f && !pools.isEmpty();
    }

    /**
     * 获取第一个物品池（如果存在）
     *
     * @return 第一个物品池的 Optional
     */
    public Optional<ItemPool> firstPool() {
        return pools.isEmpty() ? Optional.empty() : Optional.of(pools.getFirst());
    }
}
