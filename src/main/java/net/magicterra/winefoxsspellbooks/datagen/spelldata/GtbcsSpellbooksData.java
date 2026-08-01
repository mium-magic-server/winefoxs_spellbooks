package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.gametechbc.gametechbcsspellbooks.init.GSEffects;
import com.gametechbc.gametechbcsspellbooks.init.GSSpells;

public final class GtbcsSpellbooksData {

    private GtbcsSpellbooksData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 自我强化
        ctx.effect().add(GSSpells.FLAMES_REBORN_SPELL.get().getSpellResource(), GSEffects.FLAMES_REBORN.getId());
        ctx.effect().add(GSSpells.LINGERING_STRAIN_SPELL.get().getSpellResource(),
            GSEffects.LINGERING_STRAIN.getId());
        ctx.effect().add(GSSpells.REVERSAL_SPELL.get().getSpellResource(), GSEffects.REVERSAL.getId());

        // 攻击 / 负面
        ctx.effect().add(GSSpells.ACID_RAIN_SPELL.get().getSpellResource(), GSEffects.CORRODED.getId());
        ctx.effect().add(GSSpells.AERIAL_COLLAPSE_SPELL.get().getSpellResource(),
            GSEffects.AERIAL_COLLAPSE.getId());
        ctx.effect().add(GSSpells.BLACKOUT_SPELL.get().getSpellResource(), GSEffects.BLACKOUT.getId());
        ctx.effect().add(GSSpells.CRIMSON_DOWNPOUR_SPELL.get().getSpellResource(), GSEffects.HEMORRHAGE.getId());
        ctx.effect().add(GSSpells.NULLFLARE_SPELL.get().getSpellResource(), GSEffects.NULLFLARE_ICE.getId());
        ctx.effect().add(GSSpells.PSYCHIC_BOLT_SPELL.get().getSpellResource(), GSEffects.PSYCHIC_CONTROL.getId());
    }
}
