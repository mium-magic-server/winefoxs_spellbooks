package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.gametechbc.gtbcs_geomancy_plus.init.GGEffects;
import com.gametechbc.gtbcs_geomancy_plus.init.GGSpells;

public final class GeomancyPlusData {

    private GeomancyPlusData() {}

    public static void contribute(SpellDataContext ctx) {
        // Defense
        // GEO_CONDUCTOR 通过召唤 ResonatorEntity 间接给范围内友军施加 AEGIS_EFFECT
        ctx.effect().add(GGSpells.GEO_CONDUCTOR_SPELL.get().getSpellResource(), GGEffects.AEGIS_EFFECT.getId());

        // Movement
        ctx.effect().add(GGSpells.TREMOR_STEP_SPELL.get().getSpellResource(), GGEffects.TREMOR_STEP_EFFECT.getId());
        // SEISMIC_SURF 给施法者施加 SEISMIC_RIDE_TIMER buff（地震波冲浪期间持续状态）
        ctx.effect().add(GGSpells.SEISMIC_SURF.get().getSpellResource(), GGEffects.SEISMIC_RIDE_TIMER_EFFECT.getId());

        // Attack（带 MobEffect 的攻击法术）
        // SOLAR_STORM: 召唤太阳风暴并对范围内目标施加 SOLAR_STORM_EFFECT
        ctx.effect().add(GGSpells.SOLAR_STORM_SPELL.get().getSpellResource(), GGEffects.SOLAR_STORM_EFFECT.getId());
        // SOLAR_BEAM: 长射程聚焦光束，命中目标时附加 CASTING_EFFECT
        ctx.effect().add(GGSpells.SOLAR_BEAM_SPELL.get().getSpellResource(), GGEffects.CASTING_EFFECT.getId());
    }
}
