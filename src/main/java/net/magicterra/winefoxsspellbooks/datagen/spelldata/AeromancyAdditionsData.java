package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.snackpirate.aeromancy.spells.AASpells;
import net.minecraft.world.effect.MobEffects;

public final class AeromancyAdditionsData {

    private AeromancyAdditionsData() {}

    public static void contribute(SpellDataContext ctx) {
        // Defense
        ctx.effect().add(AASpells.AIRSTEP.get().getSpellResource(), AASpells.MobEffects.AIRSTEPPING.getId());
        ctx.effect().add(AASpells.WIND_SHIELD.get().getSpellResource(), AASpells.MobEffects.WIND_SHIELD.getId());

        // Positive Effect
        ctx.effect().add(AASpells.FEATHER_FALL.get().getSpellResource(), AASpells.MobEffects.FLIGHT.getId());

        // Attack
        ctx.effect().add(AASpells.ASPHYXIATE.get().getSpellResource(), AASpells.MobEffects.BREATHLESS.getId());
        // UPDRAFT: 击飞 + MOVEMENT_SLOWDOWN（眩晕短时间）
        ctx.effect().add(AASpells.UPDRAFT.get().getSpellResource(), MobEffects.MOVEMENT_SLOWDOWN.getKey().location());
    }
}
