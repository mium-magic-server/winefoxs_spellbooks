package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.hazen.hazennstuff.Spells.HnSSpellRegistries;

public final class HazenNStuffTags {

    private HazenNStuffTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(HnSSpellRegistries.SCORCHING_SLASH.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.FIERY_DAGGER.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.ICE_ARROW.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.HAILSTORM.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.ENERGY_BURST.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.IONIC_SLASH.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.THORN_CHAKRAM.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.COUNTERSPELL_SPIDER_LILY.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.SHARD_SWORD.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.DEATH_SENTENCE.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.SPECTRAL_AXE.get().getSpellResource());
        ctx.defense().addOptional(HnSSpellRegistries.PARRY.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.SYRINGE_BARRAGE.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.SHOOTING_STAR.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.TERRAPRISMIC_BARRAGE.get().getSpellResource());
        ctx.movement().addOptional(HnSSpellRegistries.PRISMATIC_SHIFT.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.NIGHTS_EDGE_STRIKE.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.UMBRASHIFT_BARRAGE.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.SOUL_SEEKERS.get().getSpellResource());
        ctx.movement().addOptional(HnSSpellRegistries.CINDEROUS_STEP.get().getSpellResource());
        ctx.negativeEffect().addOptional(HnSSpellRegistries.GOLDEN_SHOWER.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.CALL_FORTH_TERRAPRISMA.get().getSpellResource());
        ctx.summon().addOptional(HnSSpellRegistries.CALL_FORTH_TERRAPRISMA.get().getSpellResource());

        ctx.attack().addOptional(HnSSpellRegistries.ARCANE_CARDS.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.BONE_BOLT.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.COSMIC_BOLT.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.DAZZLING_OBLITERATION.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.ENDRACONIC_METEOR.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.EVERCOMET_BARRAGE.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.HORN_SHELL.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.HYDROBULLET.get().getSpellResource());
        ctx.positiveEffect().addOptional(HnSSpellRegistries.MOONKISSED.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.RAZORBLADE_TYPHOON.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.SHADOW_REAVER.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.TRIDENT_JETSTREAM.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.VIOLENT_REGURGITATION.get().getSpellResource());
        ctx.attack().addOptional(HnSSpellRegistries.WATER_BOLT.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(HnSSpellRegistries.DAZZLING_OBLITERATION.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(HnSSpellRegistries.EVERCOMET_BARRAGE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(HnSSpellRegistries.TRIDENT_JETSTREAM.get().getSpellResource());
        // CONTINUOUS 雨/雹幕弹幕、多段召唤
        ctx.maidShouldRecast().addOptional(HnSSpellRegistries.GOLDEN_SHOWER.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(HnSSpellRegistries.HAILSTORM.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(HnSSpellRegistries.CALL_FORTH_TERRAPRISMA.get().getSpellResource());
    }
}
