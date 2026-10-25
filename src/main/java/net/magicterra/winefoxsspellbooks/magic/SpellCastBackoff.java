package net.magicterra.winefoxsspellbooks.magic;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * 法术起手失败后的递增退避。每只女仆一份，仅存于内存，不随存档保存。
 * <p>
 * 起手前置检查（可用性、蓝量、法术自身条件）每次都不通过的法术，选术方若每轮照选，会挤掉其余法术并刷屏日志。
 * 某个法术起手失败后，在一段时间内不再选用：首次 {@link #BASE_BACKOFF_TICKS}，同一法术连续失败则逐次翻倍，封顶 {@link #MAX_BACKOFF_TICKS}；
 * 该法术一次起手成功即 {@link #reset 清零}。
 * <p>
 * 失败记录带「对主人施放」标记：白名单放行的法术被改作对主人施放的增益、因目标过滤对主人起不了手时，只暂停这种改作，
 * 仍按原类别参与选择（见 {@link #isOwnerCastSuspended}）；之后若该法术在原类别下再次失败，标记被覆盖，整体暂停选用。
 * <p>
 * 记录以法术为键，数量不超过法术总数。连续失败的计数在退避期结束后仍需保留才能递增，
 * 因此到期后再静置 {@link #FORGET_AFTER_TICKS} 仍无新失败的记录才会被 {@link #purgeStale 清除}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public class SpellCastBackoff {
    /**
     * 首次失败的退避时长：3 秒
     */
    public static final long BASE_BACKOFF_TICKS = 60;

    /**
     * 退避时长上限：60 秒
     */
    public static final long MAX_BACKOFF_TICKS = 1200;

    /**
     * 退避期结束后，再静置多久没有新失败就忘掉连续失败计数：60 秒
     */
    private static final long FORGET_AFTER_TICKS = 1200;

    /**
     * 单个法术的失败记录
     *
     * @param failures  连续失败次数
     * @param until     退避截止游戏刻（不含）
     * @param ownerCast 最近一次失败是否发生在对主人施放时
     */
    private record Entry(int failures, long until, boolean ownerCast) {
    }

    private final Map<AbstractSpell, Entry> entries = new HashMap<>();

    /**
     * 记录一次起手失败并计算退避。
     *
     * @param spell     失败的法术
     * @param gameTime  当前游戏刻
     * @param ownerCast 本次是否作为对主人施放的增益选中
     * @return 本次的退避时长（tick）
     */
    public long recordFailure(AbstractSpell spell, long gameTime, boolean ownerCast) {
        Entry previous = entries.get(spell);
        int failures = previous == null ? 1 : Math.min(previous.failures() + 1, 30);
        // 3 秒起逐次翻倍，位移上限只为防溢出，实际由 MAX_BACKOFF_TICKS 封顶
        long ticks = Math.min(MAX_BACKOFF_TICKS, BASE_BACKOFF_TICKS << (failures - 1));
        entries.put(spell, new Entry(failures, gameTime + ticks, ownerCast));
        return ticks;
    }

    /**
     * 该法术一次起手成功，清除其连续失败记录。
     */
    public void reset(AbstractSpell spell) {
        if (!entries.isEmpty()) {
            entries.remove(spell);
        }
    }

    /**
     * 连续失败次数；无记录为 0。
     */
    public int failures(AbstractSpell spell) {
        Entry entry = entries.get(spell);
        return entry == null ? 0 : entry.failures();
    }

    /**
     * 该法术当前是否整体暂停选用（退避期内，且最近一次失败不是对主人施放）。
     */
    public boolean isExcluded(AbstractSpell spell, long gameTime) {
        Entry entry = active(spell, gameTime);
        return entry != null && !entry.ownerCast();
    }

    /**
     * 该法术当前是否暂停「改作对主人施放的增益」（退避期内，且最近一次失败发生在对主人施放时）；此时应按原类别对待。
     */
    public boolean isOwnerCastSuspended(AbstractSpell spell, long gameTime) {
        Entry entry = active(spell, gameTime);
        return entry != null && entry.ownerCast();
    }

    /**
     * 清除已静置足够久的记录，防止长期不再失败的法术占着条目，并让其下次失败从首档重新计起。
     */
    public void purgeStale(long gameTime) {
        entries.values().removeIf(entry -> gameTime >= entry.until() + FORGET_AFTER_TICKS);
    }

    @Nullable
    private Entry active(AbstractSpell spell, long gameTime) {
        Entry entry = entries.get(spell);
        return entry != null && gameTime < entry.until() ? entry : null;
    }
}
