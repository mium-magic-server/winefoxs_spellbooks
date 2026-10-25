package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import dev.semeth.leylines.registry.LeySpellRegistry;

/**
 * Leylines（leylines）的法术分类：光束、时停、裂隙与夜裔召唤。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class LeylinesTags {

    private LeylinesTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 以下法术的 onCast 依赖玩家专属的状态（奥术充能、地脉石柱、传送锚点与传送门），
        // 非玩家施法者施放时分支不会执行，女仆放了只会白耗法力。
        // ANCHOR_RECALL: 只对玩家记录并传送回锚点
        // CHARGE_LEYLINE: 只对玩家向附近地脉石柱注入能量
        // LEYLINE_DISCHARGE: 只对玩家结算奥术充能的范围伤害
        // LEY_RESONANCE: 只对玩家及附近玩家治疗并消耗奥术充能
        // RIFT_GATE: 只对玩家生成成对传送门，且为纯出行用途
        // audit:ignore ANCHOR_RECALL, CHARGE_LEYLINE, LEYLINE_DISCHARGE, LEY_RESONANCE, RIFT_GATE

        // ========== 攻击 ==========
        // BEAM: 瞬发射线，命中第一个活体
        // FISSURE: 沿前方地面裂开，伤害并挑飞路径上的生物
        ctx.attack().addOptional(LeySpellRegistry.BEAM.get().getSpellResource());
        ctx.attack().addOptional(LeySpellRegistry.FISSURE.get().getSpellResource());
        // LEY_BLAST: 伤害取决于玩家专属的奥术充能，女仆施放时按零充能结算，仅造成基础伤害
        ctx.attack().addOptional(LeySpellRegistry.LEY_BLAST.get().getSpellResource());
        // LEY_ECLIPSE: 以施法者为中心的范围伤害，并附加黑暗
        ctx.attack().addOptional(LeySpellRegistry.LEY_ECLIPSE.get().getSpellResource());

        // ========== 召唤 ==========
        ctx.attack().addOptional(LeySpellRegistry.SUMMON_NIGHTBORN.get().getSpellResource());
        ctx.summon().addOptional(LeySpellRegistry.SUMMON_NIGHTBORN.get().getSpellResource());

        // ========== 位移 ==========
        // BLINK_STEP: 沿当前移动方向（无移动时沿水平视线）短距离瞬移，高等级附带加速
        ctx.movement().addOptional(LeySpellRegistry.BLINK_STEP.get().getSpellResource());

        // ========== 自我强化 ==========
        // LEY_VEIL: 隐身，3 级起清除周围生物对施法者的仇恨
        ctx.defense().addOptional(LeySpellRegistry.LEY_VEIL.get().getSpellResource());

        // ========== 控制 ==========
        // CHRONO_TETHER: 减速前方扇形范围内的生物，同时给施法者加速
        // TEMPORAL_STUTTER / TETHER_GAZE: 对目标施加时停（TETHER_GAZE 只对 Mob 生效）
        ctx.negativeEffect().addOptional(LeySpellRegistry.CHRONO_TETHER.get().getSpellResource());
        ctx.negativeEffect().addOptional(LeySpellRegistry.TEMPORAL_STUTTER.get().getSpellResource());
        ctx.negativeEffect().addOptional(LeySpellRegistry.TETHER_GAZE.get().getSpellResource());
    }
}
