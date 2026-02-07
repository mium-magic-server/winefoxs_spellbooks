package net.magicterra.winefoxsspellbooks.entity.loadout.entry;

/**
 * 装备条目接口，所有条目类型的基础接口
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public sealed interface LoadoutEntry permits ItemEntry, ItemTagEntry, SpellEntry, SpellTagEntry {
    /**
     * 获取条目权重
     *
     * @return 权重值，默认为 1
     */
    int weight();

    /**
     * 获取条目类型
     *
     * @return 条目类型字符串
     */
    String type();
}
