package net.magicterra.winefoxsspellbooks.entity.loadout.entry;

import net.minecraft.resources.ResourceLocation;

/**
 * 法术条目，引用单个法术
 *
 * @param name   法术的完整命名空间 ID
 * @param weight 权重，默认为 1
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record SpellEntry(ResourceLocation name, int weight) implements LoadoutEntry {
    public static final String TYPE = "spell";

    /**
     * 创建权重为 1 的法术条目
     *
     * @param name 法术 ID
     * @return SpellEntry
     */
    public static SpellEntry of(ResourceLocation name) {
        return new SpellEntry(name, 1);
    }

    /**
     * 创建指定权重的法术条目
     *
     * @param name   法术 ID
     * @param weight 权重
     * @return SpellEntry
     */
    public static SpellEntry of(ResourceLocation name, int weight) {
        return new SpellEntry(name, weight);
    }

    @Override
    public String type() {
        return TYPE;
    }
}
