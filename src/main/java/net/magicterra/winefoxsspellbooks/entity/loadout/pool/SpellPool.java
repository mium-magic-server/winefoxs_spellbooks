package net.magicterra.winefoxsspellbooks.entity.loadout.pool;

import java.util.List;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.RollRange;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.LoadoutEntry;

/**
 * 法术池，包含法术条目和标签条目
 *
 * @param rolls   抽取次数范围
 * @param entries 条目列表（SpellEntry 或 SpellTagEntry）
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record SpellPool(RollRange rolls, List<LoadoutEntry> entries) implements LoadoutPool<LoadoutEntry> {
    /**
     * 创建固定抽取次数的法术池
     *
     * @param rolls   抽取次数
     * @param entries 条目列表
     * @return SpellPool
     */
    public static SpellPool of(int rolls, List<LoadoutEntry> entries) {
        return new SpellPool(RollRange.fixed(rolls), entries);
    }

    /**
     * 创建范围抽取次数的法术池
     *
     * @param minRolls 最小抽取次数
     * @param maxRolls 最大抽取次数
     * @param entries  条目列表
     * @return SpellPool
     */
    public static SpellPool of(int minRolls, int maxRolls, List<LoadoutEntry> entries) {
        return new SpellPool(RollRange.range(minRolls, maxRolls), entries);
    }
}
