package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

/**
 * 走位的距离带：与目标保持的距离落在 [{@code min}, {@code max}] 之内，再与当前法术射程取交集
 * <p>
 * 本模组自己的任务不设距离带（{@link #NONE}），走位只看法术射程；万法皆通的近战 / 远程法术任务各有一条，
 * 由任务决定站位风格，本模组的走位负责执行。
 *
 * @param min 距离下限，低于它就后撤；0 表示不设下限
 * @param max 距离上限，法术射程更远时也只站到这里
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public record EngageBand(double min, double max) {
    public static final EngageBand NONE = new EngageBand(0, Double.MAX_VALUE);

    /**
     * 走位用的有效射程：法术射程与距离带上限取小
     */
    public double clampRange(double spellRange) {
        return Math.min(spellRange, max);
    }

    /**
     * 低于此距离就后撤：默认按射程的一定比例；距离带下限更大时以下限为准，下限超过射程时不再参与
     */
    public double fleeRange(double range, double fleeRatio) {
        double ratioRange = range * fleeRatio;
        return min < range ? Math.max(ratioRange, min) : ratioRange;
    }
}
