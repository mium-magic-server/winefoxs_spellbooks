package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.gametechbc.gtbcs_geomancy_plus.init.GGSpells;

public final class GeomancyPlusTags {

    private GeomancyPlusTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(GGSpells.FISSURE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GGSpells.TREMOR_SPIKE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GGSpells.ERODING_BOULDER_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GGSpells.CHUNKER_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GGSpells.DRIPSTONE_BOLT.get().getSpellResource());
        ctx.attack().addOptional(GGSpells.PILLAR_OF_THE_RESOUNDING_EARTH.get().getSpellResource());
        ctx.attack().addOptional(GGSpells.PETRIVISE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GGSpells.SOLAR_STORM_SPELL.get().getSpellResource());
        ctx.attack().addOptional(GGSpells.SOLAR_BEAM_SPELL.get().getSpellResource());
        ctx.defense().addOptional(GGSpells.GEO_CONDUCTOR_SPELL.get().getSpellResource());
        ctx.movement().addOptional(GGSpells.TREMOR_STEP_SPELL.get().getSpellResource());
        ctx.movement().addOptional(GGSpells.SEISMIC_SURF.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(GGSpells.GEO_CONDUCTOR_SPELL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GGSpells.TREMOR_SPIKE_SPELL.get().getSpellResource());
    }
}
