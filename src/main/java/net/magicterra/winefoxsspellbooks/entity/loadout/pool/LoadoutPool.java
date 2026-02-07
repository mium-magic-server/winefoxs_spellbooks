package net.magicterra.winefoxsspellbooks.entity.loadout.pool;

import java.util.List;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.RollRange;

/**
 * 装备池接口，定义物品池和法术池的公共行为
 *
 * @param <E> 条目类型
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public interface LoadoutPool<E> {
    /**
     * 获取抽取次数范围
     *
     * @return 抽取次数范围
     */
    RollRange rolls();

    /**
     * 获取条目列表
     *
     * @return 条目列表
     */
    List<E> entries();
}
