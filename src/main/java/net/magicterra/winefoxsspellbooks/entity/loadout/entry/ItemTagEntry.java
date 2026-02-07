package net.magicterra.winefoxsspellbooks.entity.loadout.entry;

import net.minecraft.resources.ResourceLocation;

/**
 * 物品标签条目，引用物品标签
 *
 * @param name   物品标签的完整命名空间 ID
 * @param weight 权重，默认为 1
 * @param expand 是否展开标签内所有物品（每个物品独立参与权重计算）
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record ItemTagEntry(ResourceLocation name, int weight, boolean expand) implements LoadoutEntry {
    public static final String TYPE = "tag";

    /**
     * 创建不展开的标签条目
     *
     * @param name 标签 ID
     * @return ItemTagEntry
     */
    public static ItemTagEntry of(ResourceLocation name) {
        return new ItemTagEntry(name, 1, false);
    }

    /**
     * 创建指定权重的标签条目
     *
     * @param name   标签 ID
     * @param weight 权重
     * @return ItemTagEntry
     */
    public static ItemTagEntry of(ResourceLocation name, int weight) {
        return new ItemTagEntry(name, weight, false);
    }

    /**
     * 创建展开的标签条目
     *
     * @param name   标签 ID
     * @param weight 权重
     * @param expand 是否展开
     * @return ItemTagEntry
     */
    public static ItemTagEntry of(ResourceLocation name, int weight, boolean expand) {
        return new ItemTagEntry(name, weight, expand);
    }

    @Override
    public String type() {
        return TYPE;
    }
}
