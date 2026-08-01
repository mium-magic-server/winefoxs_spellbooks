package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.gametechbc.gametechbcsspellbooks.init.GSSpells;

public final class GtbcsSpellbooksTags {

    private GtbcsSpellbooksTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // ASTRAL_SENSE 是纯探路 / 找宝工具，战斗中没有用处。
        // audit:ignore ASTRAL_SENSE_SPELL

        // ========== 攻击 ==========
        ctx.attack().addOptional(GSSpells.ACID_RAIN_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GSSpells.AERIAL_COLLAPSE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GSSpells.ASHEN_BREATH.get().getSpellResource());
        ctx.attack().addOptional(GSSpells.BLACKOUT_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GSSpells.CRIMSON_DOWNPOUR_SPELL.get().getSpellResource());
        // DISPLACEMENT: 把周围敌人反复传送打散，施法者自身不移动
        ctx.attack().addOptional(GSSpells.DISPLACEMENT_SPELL.get().getSpellResource());
        // ENSNARE: 生成藤蔓强制敌人 startRiding，属于控制
        ctx.attack().addOptional(GSSpells.ENSNARE_SPELL.get().getSpellResource());
        // METEOR_STORM: 自身增益期间持续向敌人落陨石
        ctx.attack().addOptional(GSSpells.METEOR_STORM_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GSSpells.NULLFLARE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GSSpells.PSYCHIC_BOLT_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GSSpells.SHATTERPOINT.get().getSpellResource());

        // ========== 自我强化 ==========
        ctx.defense().addOptional(GSSpells.FLAMES_REBORN_SPELL.get().getSpellResource());
        ctx.defense().addOptional(GSSpells.LINGERING_STRAIN_SPELL.get().getSpellResource());
        ctx.defense().addOptional(GSSpells.REVERSAL_SPELL.get().getSpellResource());

        // ========== 位移 ==========
        ctx.movement().addOptional(GSSpells.SPECTRAL_BLINK_SPELL.get().getSpellResource());

        // ========== 召唤 ==========
        ctx.attack().addOptional(GSSpells.CALL_FORTH_THE_DEAD_KING_SPELL.get().getSpellResource());
        ctx.summon().addOptional(GSSpells.CALL_FORTH_THE_DEAD_KING_SPELL.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(GSSpells.ASHEN_BREATH.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GSSpells.NULLFLARE_SPELL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GSSpells.PSYCHIC_BOLT_SPELL.get().getSpellResource());
    }
}
