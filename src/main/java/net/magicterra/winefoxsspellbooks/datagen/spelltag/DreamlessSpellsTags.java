package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.Iceforkkk.DreamlessAditions.registries.SpellRegistries;

public final class DreamlessSpellsTags {

    private DreamlessSpellsTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：DRAINED / DULLARD / MUTE 的 checkPreCastConditions 要求施法者自身带有 EMPTIED 效果，
        // 且 onCast 中向目标施加 DRAINED / DULLARD / MUTE 效果的全部代码都包在 entity instanceof ServerPlayer 分支内，
        // 女仆施放只会白耗冷却，对目标没有任何影响。
        // audit:ignore DRAINED, DULLARD, MUTE

        ctx.defense().addOptional(SpellRegistries.JADESKIN.get().getSpellResource());
    }
}
