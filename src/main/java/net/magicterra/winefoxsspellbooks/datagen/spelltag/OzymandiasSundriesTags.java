package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.ozymandias.ozymandiassundries.item.registries.SpellRegistries;

/**
 * Ozymandias' Sundries 附属模组的法术标签贡献。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class OzymandiasSundriesTags {

    private OzymandiasSundriesTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // ========== 控制 ==========
        // LEVITATE: 对视线内目标施加漂浮，持续时间随法术强度增长，漂浮结束后目标会摔落
        ctx.negativeEffect().addOptional(SpellRegistries.LEVITATE.get().getSpellResource());

        // ========== 位移 ==========
        // LIGHTNING_WARP: 优先闪现到视线内敌人的身后，没有目标时沿视线前移
        ctx.movement().addOptional(SpellRegistries.LIGHTNING_WARP.get().getSpellResource());
    }
}
