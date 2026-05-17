package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.Iceforkkk.DreamlessAditions.registries.SpellRegistries;

public final class DreamlessSpellsTags {

    private DreamlessSpellsTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.defense().addOptional(SpellRegistries.JADESKIN.get().getSpellResource());
        // MUTE: 单体目标施加 MUTE 沉默 debuff，不直接造成伤害（onCast 只调 addEffect，没有 hurt()）
        ctx.negativeEffect().addOptional(SpellRegistries.MUTE.get().getSpellResource());
        // DRAINED / DULLARD: AOE 攻击 + 给敌人施加削弱 debuff（需要施法者先有 EMPTIED 效果）
        ctx.attack().addOptional(SpellRegistries.DRAINED.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistries.DRAINED.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.DULLARD.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistries.DULLARD.get().getSpellResource());
    }
}
