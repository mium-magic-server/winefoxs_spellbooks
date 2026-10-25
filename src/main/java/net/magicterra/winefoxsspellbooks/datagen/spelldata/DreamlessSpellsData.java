package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.Iceforkkk.DreamlessAditions.effect.DSSEffects;
import net.Iceforkkk.DreamlessAditions.registries.SpellRegistries;

public final class DreamlessSpellsData {

    private DreamlessSpellsData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // Defense
        ctx.effect().add(SpellRegistries.JADESKIN.get().getSpellResource(), DSSEffects.JADESKIN_EFFECT.getKey().location());
    }
}
