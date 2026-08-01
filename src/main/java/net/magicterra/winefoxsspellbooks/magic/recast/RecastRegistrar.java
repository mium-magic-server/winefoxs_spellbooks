package net.magicterra.winefoxsspellbooks.magic.recast;

import java.util.Map;

/**
 * 一个附属模组往全局 recast 表里登记自己法术的入口。
 * <p>
 * 模组 id 由它带着，各附属的 {@code contribute} 只写法术路径，注册 id 拼不错也不必重复模组名。 实例只由 {@link RecastHandlers} 建，附属拿到的就是绑好自己 id 的那一个。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-08-01
 */
public final class RecastRegistrar {
    private final String modId;

    private final Map<String, RecastHandler> target;

    RecastRegistrar(String modId, Map<String, RecastHandler> target) {
        this.modId = modId;
        this.target = target;
    }

    /**
     * 登记一个法术的收尾动作。
     *
     * @param spellPath 法术注册 id 的路径部分（不含模组 id 与冒号），以对方注册时用的名字为准
     * @param handler   收尾动作
     */
    public void register(String spellPath, RecastHandler handler) {
        target.put(modId + ":" + spellPath, handler);
    }
}
