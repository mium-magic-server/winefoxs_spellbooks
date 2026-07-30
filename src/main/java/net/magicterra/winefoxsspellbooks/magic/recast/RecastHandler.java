package net.magicterra.winefoxsspellbooks.magic.recast;

/**
 * 一个法术的 recast 收尾动作。实现一律写成静态方法引用，施法者从 {@link RecastContext} 拿。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
@FunctionalInterface
public interface RecastHandler {
    /**
     * recast 结束时调用。<b>实现负责自己挂冷却</b>（{@link RecastContext#addCooldown()}）—— 有些法术在某些结束原因下故意不挂（例如被反制时召唤术要把 recast 还回去）。
     *
     * @param ctx 本次 recast 的上下文
     */
    void onRecastFinished(RecastContext ctx);
}
