package net.magicterra.winefoxsspellbooks.entity.loadout.data;

import net.minecraft.util.RandomSource;

/**
 * 数量范围配置，支持固定值或 min/max 范围
 *
 * @param min 最小值
 * @param max 最大值
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record RollRange(int min, int max) {
    /**
     * 创建固定值范围
     *
     * @param value 固定值
     * @return RollRange
     */
    public static RollRange fixed(int value) {
        return new RollRange(value, value);
    }

    /**
     * 创建范围
     *
     * @param min 最小值
     * @param max 最大值
     * @return RollRange
     */
    public static RollRange range(int min, int max) {
        return new RollRange(min, max);
    }

    /**
     * 是否为固定值
     *
     * @return true 如果 min == max
     */
    public boolean isFixed() {
        return min == max;
    }

    /**
     * 在范围内随机取值
     *
     * @param random 随机数生成器
     * @return 随机值
     */
    public int roll(RandomSource random) {
        if (min == max) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }
}
