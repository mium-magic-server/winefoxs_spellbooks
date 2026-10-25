package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.hypernhieis.registry.AnimeSpells;

/**
 * Hyper N Hiei's Shenanigans（hyper_n_hieis_shenanigans）：动漫题材的大招类法术，伤害均经 Iron's 的友伤判定结算。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class HyperNHieisShenanigansTags {

    private HyperNHieisShenanigansTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：CRESCENT_SHIFT 与 CRIMSON_REQUIEM 的 onCast 只处理 ServerPlayer，且施法前要求手持 Crescent Rose，女仆施放无任何效果。
        // 原因：GEPPO 的 onCast 只处理 ServerPlayer（空中连跳需要玩家输入），女仆施放无任何效果。
        // audit:ignore CRESCENT_SHIFT, CRIMSON_REQUIEM, GEPPO

        // ========== 攻击 ==========
        // CRUEL_SUN: 蓄力后掷出烈日，飞行途中持续灼烧，命中后爆炸
        ctx.attack().addOptional(AnimeSpells.CRUEL_SUN.get().getSpellResource());
        // DISINTEGRATION: 在目标处升起持续光柱，并跟随目标
        ctx.attack().addOptional(AnimeSpells.DISINTEGRATION.get().getSpellResource());
        // HELL_FLARE: 抛出火球，落点形成困住并灼烧目标的火焰穹顶
        ctx.attack().addOptional(AnimeSpells.HELL_FLARE.get().getSpellResource());
        // HOLLOW_PURPLE: 蓄力后发射紫球（recast 引爆分支只对 Player 开放，女仆不可用）
        ctx.attack().addOptional(AnimeSpells.HOLLOW_PURPLE.get().getSpellResource());
        ctx.attack().addOptional(AnimeSpells.KAMEHAMEHA.get().getSpellResource());
        // WORLD_CUTTING_SLASH: 向前方斩出超宽斩击，按最大生命值百分比结算伤害，不可通过战利品或合成获得
        ctx.attack().addOptional(AnimeSpells.WORLD_CUTTING_SLASH.get().getSpellResource());

        // ========== 自我强化 ==========
        // HOLLOW_MASK: 面具提升施法者的近战攻击力，持续一段时间后碎裂
        ctx.defense().addOptional(AnimeSpells.HOLLOW_MASK.get().getSpellResource());
        // INFINITY: 无限力场挡下弹射物与近身攻击；对非玩家施法者不消耗法力，持续时间内不会被耗尽
        ctx.defense().addOptional(AnimeSpells.INFINITY.get().getSpellResource());

        // ========== 治疗 ==========
        // FIELD_OF_FLOWERS: 以施法者为中心的花海，治疗施法者与友方并赋予希望效果，同时缠住范围内的敌对生物
        ctx.supportEffect().addOptional(AnimeSpells.FIELD_OF_FLOWERS.get().getSpellResource());
    }
}
