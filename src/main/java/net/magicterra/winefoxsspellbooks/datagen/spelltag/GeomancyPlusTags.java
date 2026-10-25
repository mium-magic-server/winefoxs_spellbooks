package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.gametechbc.gtbcs_geomancy_plus.init.GGSpells;

public final class GeomancyPlusTags {

    private GeomancyPlusTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：SEISMIC_SURF 召唤的 SeismicRideEntity 是只能由玩家骑乘的坐骑（mobInteract 要求交互者是召唤者本人且为 Player，
        // 受控乘客也限定 Player），女仆无法上去，岩板升空后无人驾驶、没有任何伤害，纯属白耗蓝量，也不会移动施法者。
        // audit:ignore SEISMIC_SURF

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

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(GGSpells.GEO_CONDUCTOR_SPELL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GGSpells.TREMOR_SPIKE_SPELL.get().getSpellResource());
    }
}
