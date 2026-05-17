package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries;

public final class CataclysmSpellbooksTags {

    private CataclysmSpellbooksTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(SpellRegistries.VOID_BEAM.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.ABYSSAL_BLAST.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.DIMENSIONAL_RIFT.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.DEPTH_CHARGE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.ABYSSAL_SLASH.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.TIDAL_GRAB.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.VOID_RUNE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.VOID_BULWARK.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.INCINERATION.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.INFERNAL_STRIKE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.HELLISH_BLADE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.BONE_STORM.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.BONE_PIERCE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.ASHEN_BREATH.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.ABYSS_FIREBALL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.TECTONIC_TREMBLE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.MALEVOLENT_BATTLEFIELD.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.SANDSTORM.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.DESERT_WINDS.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.MONOLITH_CRASH.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.AMETHYST_PUNCTURE.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistries.ABYSSAL_PREDATOR.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistries.CURSED_RUSH.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistries.GRAVITY_STORM.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistries.PILFER.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.CONJURE_KOBOLDIATOR.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistries.CONJURE_KOBOLDIATOR.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.CONJURE_KOBOLETON.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistries.CONJURE_KOBOLETON.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.CONJURE_IGNITED_REINFORCEMENT.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistries.CONJURE_IGNITED_REINFORCEMENT.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.CONJURE_THRALL.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistries.CONJURE_THRALL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.CONJURE_AMETHYST_CRAB.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistries.CONJURE_AMETHYST_CRAB.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.GRAVITATION_PULL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.THOTHS_WITNESS.get().getSpellResource());
        ctx.positiveEffect().addOptional(SpellRegistries.FORGONE_RAGE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.PHARAOHS_WRATH.get().getSpellResource());
        ctx.positiveEffect().addOptional(SpellRegistries.PHARAOHS_WRATH.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(SpellRegistries.VOID_BEAM.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.BONE_PIERCE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.AMETHYST_PUNCTURE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.ABYSS_FIREBALL.get().getSpellResource());
    }
}
