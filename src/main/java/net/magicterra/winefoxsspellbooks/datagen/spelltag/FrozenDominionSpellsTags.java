package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.jastkub.fdspells.registry.FDSRegistry;

/**
 * Frozen Dominion Spells（frozen_dominion_spells）：冰系法术。全部以施法者所有者身份结算，女仆施放可正常生效。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class FrozenDominionSpellsTags {

    private FrozenDominionSpellsTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // ========== 攻击 ==========
        // AVALANCHE: 从施法者面前向前推进的雪崩波，沿途伤害并减速
        ctx.attack().addOptional(FDSRegistry.AVALANCHE.get().getSpellResource());
        // FROST_HEART: 在准心落点放置冰霜之心，周期性释放扩散冰环
        ctx.attack().addOptional(FDSRegistry.FROST_HEART.get().getSpellResource());
        ctx.attack().addOptional(FDSRegistry.FROST_JAVELIN.get().getSpellResource());
        ctx.attack().addOptional(FDSRegistry.ICICLE_RAIN.get().getSpellResource());
        // RIME_PATH: 沿施法者朝向向前依次升起冰刺
        ctx.attack().addOptional(FDSRegistry.RIME_PATH.get().getSpellResource());
        // WINTERS_SORROW: 以施法者为中心一圈圈向外升起冰刺（第一圈半径约 3.3 格）
        ctx.attack().addOptional(FDSRegistry.WINTERS_SORROW.get().getSpellResource());

        // ========== 控制 ==========
        // FROST_SHACKLES: 需要目标，锁链将目标定在原地并造成一次伤害，伤害低而以控制为主
        ctx.negativeEffect().addOptional(FDSRegistry.FROST_SHACKLES.get().getSpellResource());

        // ========== 自我强化 ==========
        // FROST_SHELL: 为施法者附加吸收护盾，护盾破碎或到期时爆发冰片与范围伤害
        ctx.defense().addOptional(FDSRegistry.FROST_SHELL.get().getSpellResource());
        // RUNE_LITANY: 符文环绕施法者，每当施法者伤害敌人就派出一枚符文追击，属于待机式的姿态法术
        ctx.defense().addOptional(FDSRegistry.RUNE_LITANY.get().getSpellResource());

        // ========== 召唤 ==========
        // ICE_SENTINEL_SPELL: 召唤冰霜哨卫（以施法者为主人，协助其战斗并跟随）
        // 已核对：哨卫由自定义 summon(caster) 记录主人 UUID（扫描器的 SUMMON_NO_OWNER 为误报），只攻击主人最近伤害 / 攻击过的敌人
        // 或 12 格内的 Enemy，且 FxEntity.isFoe 双向检查 isAlliedTo，不会转头攻击女仆或主人
        ctx.attack().addOptional(FDSRegistry.ICE_SENTINEL_SPELL.get().getSpellResource());
        ctx.summon().addOptional(FDSRegistry.ICE_SENTINEL_SPELL.get().getSpellResource());
    }
}
