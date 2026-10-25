package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.hazen.echoing_magic.Spells.EMSpellRegistries;

/**
 * Echoing Magic（回响魔法）的法术标签分类。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class EchoingMagicTags {

    private EchoingMagicTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // ========== 宇宙学派 ==========
        // COLLAPSE: 在落点周围 10 格内随机落下一批坠块，伤害以施法者为所有者；首次施放后可在窗口内追加施放
        ctx.attack().addOptional(EMSpellRegistries.COLLAPSE.get().getSpellResource());
        // ECHOING_EXPLOSION: 以施法者为中心的电击爆发，并给范围内目标挂护甲破碎
        ctx.attack().addOptional(EMSpellRegistries.ECHOING_EXPLOSION.get().getSpellResource());
        // ECHOING_SLAM: 前方落点生成冲击环与半径 8 格的震荡波
        ctx.attack().addOptional(EMSpellRegistries.ECHOING_SLAM.get().getSpellResource());
        ctx.attack().addOptional(EMSpellRegistries.ECHO_BLAST.get().getSpellResource());
        // ECHO_STAR: 向前方散射 5 枚追踪弹道
        ctx.attack().addOptional(EMSpellRegistries.ECHO_STAR.get().getSpellResource());

        // ========== 唤术学派 ==========
        // CONGURE_GRIMORAY: 召唤带毒云、伤害、治疗三种形态的魔导书，均以施法者为所有者
        ctx.attack().addOptional(EMSpellRegistries.CONGURE_GRIMORAY.get().getSpellResource());
        ctx.summon().addOptional(EMSpellRegistries.CONGURE_GRIMORAY.get().getSpellResource());
        ctx.attack().addOptional(EMSpellRegistries.SUMMON_BRAVE.get().getSpellResource());
        ctx.summon().addOptional(EMSpellRegistries.SUMMON_BRAVE.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(EMSpellRegistries.COLLAPSE.get().getSpellResource());
    }
}
