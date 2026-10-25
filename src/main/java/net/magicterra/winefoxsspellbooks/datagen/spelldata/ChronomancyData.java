package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.chronomancy.registry.ChronoMobEffectRegistry;
import com.chronomancy.registry.ChronoSpellRegistry;

/**
 * Chronomancy：法术施放后挂在目标身上的主要效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class ChronomancyData {

    private ChronomancyData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 控制
        ctx.effect().add(ChronoSpellRegistry.TEMPORAL_STASIS_BEAM_SPELL.getSpellResource(),
            ChronoMobEffectRegistry.TEMPORAL_STASIS.getId());
    }
}
