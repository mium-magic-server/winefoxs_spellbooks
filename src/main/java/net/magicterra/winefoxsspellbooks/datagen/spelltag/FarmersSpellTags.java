package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.chenjdy.farmers_spell.init.ModSpells;

/**
 * Farmer's Spell 附属：为其 Gluttony 学派法术分类，玩家专用或会误伤己方的法术一律排除。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class FarmersSpellTags {

    private FarmersSpellTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // BAD_APPLE_SPELL: 生成无主人的嘲讽诱饵，范围内所有 Mob（含女仆自身与友方生物）都会被强制改为攻击它，并向全体玩家播放长达五分钟的音乐。
        // GOODBERRY_SPELL: 效果只对 Player 生效（战斗中回血，否则向背包发放浆果），女仆施放无任何作用。
        // PHANTOM_LOOT_SPELL: 发放战利品的 grantLoot 把施法者强制转换为 Player，女仆施放会抛出 ClassCastException。
        // audit:ignore BAD_APPLE_SPELL, GOODBERRY_SPELL, PHANTOM_LOOT_SPELL

        // ========== 攻击 ==========
        ctx.attack().addOptional(ModSpells.CHAOS_SLASH_SPELL.get().getSpellResource());
        // PAN_SPELL: 在身前约 1 格处挥出平底锅，仅结算前方 3x3 格、眼高上下 0.5 格的薄层，附带 CLAW_BREAK（不区分敌友）；
        // 施法距离已覆盖为 2 格，只在敌人贴身时施放，主人只有紧贴在女仆正前方才会被波及，保留
        ctx.attack().addOptional(ModSpells.PAN_SPELL.get().getSpellResource());

        // ========== 控制 ==========
        // PRESERVE_CIRCLE_SPELL: 在脚下展开圆形领域，使范围内敌人缓慢并附加魔法易伤（MAGICAL_INGREDIENT）
        ctx.negativeEffect().addOptional(ModSpells.PRESERVE_CIRCLE_SPELL.get().getSpellResource());

        // ========== 自我强化 ==========
        // SEAL_COAT_SPELL: 受伤时按最大生命百分比回血（间隔 5 秒），代价是攻速与施法速度各降低 25%
        ctx.defense().addOptional(ModSpells.SEAL_COAT_SPELL.get().getSpellResource());
    }
}
