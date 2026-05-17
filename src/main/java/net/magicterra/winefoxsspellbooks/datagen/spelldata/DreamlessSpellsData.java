package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.Iceforkkk.DreamlessAditions.effect.DSSEffects;
import net.Iceforkkk.DreamlessAditions.registries.SpellRegistries;

public final class DreamlessSpellsData {

    private DreamlessSpellsData() {}

    public static void contribute(SpellDataContext ctx) {
        // Defense
        ctx.effect().add(SpellRegistries.JADESKIN.get().getSpellResource(), DSSEffects.JADESKIN_EFFECT.getKey().location());

        // Negative Effect
        ctx.effect().add(SpellRegistries.DRAINED.get().getSpellResource(), DSSEffects.DRAINED_EFFECT.getKey().location());
        ctx.effect().add(SpellRegistries.DULLARD.get().getSpellResource(), DSSEffects.DULLARD_EFFECT.getKey().location());
    }
}
