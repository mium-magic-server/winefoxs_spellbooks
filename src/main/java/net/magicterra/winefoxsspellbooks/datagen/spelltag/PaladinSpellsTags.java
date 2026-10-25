package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import eu.kaufko.paladin_spells.registry.PaladinSpellRegistry;

/**
 * Paladin Spells（paladin_spells）的法术分类：圣骑士风格的减伤与冲撞。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class PaladinSpellsTags {

    private PaladinSpellsTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // SWORN_PROTECTOR 的 onCast 整体包在 level.isClientSide 分支里，服务端施放时不会添加任何效果；
        // 且其重定向逻辑只针对玩家受到的伤害，并让施法者代为承受，女仆施放既无效也无益。
        // audit:ignore SWORN_PROTECTOR_SPELL
        // 原因：TAUNT_SPELL 对范围内所有 Enemy 类生物（含 Iron's 的亡灵召唤物）施加嘲讽并强制以施法者为目标，没有任何敌我筛选，
        // 范围约 10 格起并随法术强度扩大，这些生物可能转而攻击女仆与主人。
        // audit:ignore TAUNT_SPELL

        // ========== 攻击 ==========
        // RAM_SPELL: 向前冲撞，对冲撞范围内所有活体造成伤害（不区分敌我），伤害随护甲值提高；
        // 判定盒仅为施法者碰撞箱沿冲刺方向外扩后再放大 1.5 格，且施法距离已覆盖为 2 格，只在敌人贴身时施放，主人只有站在女仆与敌人的连线上才会被波及，保留
        ctx.attack().addOptional(PaladinSpellRegistry.RAM_SPELL.get().getSpellResource());

        // ========== 自我强化 ==========
        // BEDROCK_SKIN_SPELL: 大幅减伤，期间施法者骑乘一个只能垂直移动的锚点实体，无法位移
        ctx.defense().addOptional(PaladinSpellRegistry.BEDROCK_SKIN_SPELL.get().getSpellResource());
        ctx.defense().addOptional(PaladinSpellRegistry.BULWARK_SPELL.get().getSpellResource());
    }
}
