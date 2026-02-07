package net.magicterra.winefoxsspellbooks.entity.loadout.entry;

import net.minecraft.resources.ResourceLocation;

/**
 * 物品条目，引用单个物品
 *
 * @param name   物品的完整命名空间 ID
 * @param weight 权重，默认为 1
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record ItemEntry(ResourceLocation name, int weight) implements LoadoutEntry {
    public static final String TYPE = "item";

    /**
     * 创建权重为 1 的物品条目
     *
     * @param name 物品 ID
     * @return ItemEntry
     */
    public static ItemEntry of(ResourceLocation name) {
        return new ItemEntry(name, 1);
    }

    /**
     * 创建指定权重的物品条目
     *
     * @param name   物品 ID
     * @param weight 权重
     * @return ItemEntry
     */
    public static ItemEntry of(ResourceLocation name, int weight) {
        return new ItemEntry(name, weight);
    }

    @Override
    public String type() {
        return TYPE;
    }
}
