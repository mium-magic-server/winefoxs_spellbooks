package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.eldenring.spells.registry.ModEffects;
import com.eldenring.spells.registry.ModSpells;

/**
 * Elden Ring Spells（iss_elden_ring）的法术数据：近身结算范围与施放后的主要效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class IssEldenRingData {

    private IssEldenRingData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 近身结算的法术
        ctx.range().add(ModSpells.CARIAN_SLICER.get().getSpellResource(), 2F);
        ctx.range().add(ModSpells.GAVEL_OF_HAIMA.get().getSpellResource(), 2F);

        // 自我强化
        ctx.effect().add(ModSpells.TERRA_MAGICA.get().getSpellResource(), ModEffects.TERRA_MAGICA.getId());
    }
}
