package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.magicterra.winefoxsspellbooks.registry.WsbSpells;

/**
 * 本 mod 自带法术的标签贡献。
 * 注意：本 mod 的法术已经在自己的 registry 里，不需要 {@code addOptional} 防御，可以直接 {@code add}。
 */
public final class WinefoxsSpellbooksTags {

    private WinefoxsSpellbooksTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.summon().add(WsbSpells.SUMMON_MAID_SPELL.get());
        ctx.positiveEffect().add(WsbSpells.MANA_TRANSFER_SPELL.get());
        // 残月烙印：对单个目标施加 HARMFUL 标记效果，归入负面效果类
        ctx.negativeEffect().add(WsbSpells.CRESCENT_BRAND_SPELL.get());
        // 酒杯狐影：放出追踪弹打出酒狐巫法学派伤害，归入攻击类（女仆 AI 据此选用为攻击法术）
        ctx.attack().add(WsbSpells.FOXSHADE_WISPS_SPELL.get());
        // 酒杯狐影需要逐次 recast（共 6 发），加入 maidShouldRecast 让女仆 AI 自动逐发
        ctx.maidShouldRecast().add(WsbSpells.FOXSHADE_WISPS_SPELL.get());
        // 残月披风：自身+女仆吸收/隐身脱险，归入防御类
        ctx.defense().add(WsbSpells.CRESCENT_MANTLE_SPELL.get());
        // 醒酒之礼：解除/减轻自身或女仆负面效果，归入支援类
        ctx.support().add(WsbSpells.DECANT_THE_BIND_SPELL.get());
        // 醉狐之雾：落地生成负面效果领域，归入负面效果类
        ctx.negativeEffect().add(WsbSpells.TIPSY_MIST_SPELL.get());
        // 狐酿炸弹:投掷弹直击打出酒狐巫法学派伤害,归入攻击类
        ctx.attack().add(WsbSpells.VINTNERS_BOMB_SPELL.get());
    }
}
