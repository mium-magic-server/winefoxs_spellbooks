package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.chenjdy.farmers_spell.init.ModEffects;
import com.chenjdy.farmers_spell.init.ModSpells;

/**
 * Farmer's Spell 附属：近身结算法术的施法距离与法术附带的主要状态效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class FarmersSpellData {

    private FarmersSpellData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 近身结算的法术
        ctx.range().add(ModSpells.PAN_SPELL.get().getSpellResource(), 2F);

        // 自我强化
        ctx.effect().add(ModSpells.SEAL_COAT_SPELL.get().getSpellResource(), ModEffects.SEAL_OIL.getId());

        // 攻击 / 负面
        ctx.effect().add(ModSpells.CHAOS_SLASH_SPELL.get().getSpellResource(), ModEffects.CLAW_BREAK.getId());
        ctx.effect().add(ModSpells.PAN_SPELL.get().getSpellResource(), ModEffects.CLAW_BREAK.getId());
        ctx.effect().add(ModSpells.PRESERVE_CIRCLE_SPELL.get().getSpellResource(),
            ModEffects.MAGICAL_INGREDIENT.getId());
    }
}
