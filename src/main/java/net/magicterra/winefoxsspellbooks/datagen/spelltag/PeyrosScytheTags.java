package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.rinko1231.peyroscythe.init.NewSpellRegistry;

public final class PeyrosScytheTags {

    private PeyrosScytheTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // MADE_IN_HEAVEN 系列只加速方块实体与随机刻，YOUNG_FOR_YOU 只把怪物变成幼体，
        // 三者都不参与战斗。
        // audit:ignore MADE_IN_HEAVEN, MADE_IN_HEAVEN_REAL, YOUNG_FOR_YOU

        // ========== 攻击 ==========
        ctx.attack().addOptional(NewSpellRegistry.BLACK_FIREBALL.get().getSpellResource());
        ctx.attack().addOptional(NewSpellRegistry.CHAOS_CRADLE.get().getSpellResource());
        ctx.attack().addOptional(NewSpellRegistry.DEATH_SMOKE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(NewSpellRegistry.FROST_HELL_SPELL.get().getSpellResource());
        ctx.attack().addOptional(NewSpellRegistry.FROZEN_WORLD_SPELL.get().getSpellResource());
        ctx.attack().addOptional(NewSpellRegistry.GLACIER_SPELL.get().getSpellResource());
        ctx.attack().addOptional(NewSpellRegistry.HOLY_LANCE.get().getSpellResource());
        ctx.attack().addOptional(NewSpellRegistry.ICY_COMET_RAIN.get().getSpellResource());
        ctx.attack().addOptional(NewSpellRegistry.NERO_HOLY_RAY.get().getSpellResource());

        // ========== 控制 ==========
        // HOLY_BELL: 金钟给敌人挂减攻减速，不造成伤害
        ctx.negativeEffect().addOptional(NewSpellRegistry.HOLY_BELL_SPELL.get().getSpellResource());
        ctx.negativeEffect().addOptional(NewSpellRegistry.ILLUSION_CRAZY.get().getSpellResource());
        ctx.negativeEffect().addOptional(NewSpellRegistry.ILLUSION_FEAR.get().getSpellResource());

        // ========== 自我强化 ==========
        // BLACK_FLAME_WINGS: 飞行 + 加速 + 抗性，同时对贴身敌人造成接触伤害
        ctx.defense().addOptional(NewSpellRegistry.BLACK_FLAME_WINGS.get().getSpellResource());
        ctx.defense().addOptional(NewSpellRegistry.CRIMSON_MOON_SPELL.get().getSpellResource());
        ctx.defense().addOptional(NewSpellRegistry.SINFIRE_EMBRACE_SPELL.get().getSpellResource());

        // ========== 召唤 ==========
        ctx.attack().addOptional(NewSpellRegistry.SUMMON_BLAZE.get().getSpellResource());
        ctx.summon().addOptional(NewSpellRegistry.SUMMON_BLAZE.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(NewSpellRegistry.BLACK_FIREBALL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(NewSpellRegistry.DEATH_SMOKE_SPELL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(NewSpellRegistry.FROST_HELL_SPELL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(NewSpellRegistry.ICY_COMET_RAIN.get().getSpellResource());
    }
}
