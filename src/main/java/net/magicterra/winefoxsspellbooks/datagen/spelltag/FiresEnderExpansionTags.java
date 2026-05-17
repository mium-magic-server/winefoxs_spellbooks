package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.fireofpower.firesenderexpansion.registries.SpellRegistries;

public final class FiresEnderExpansionTags {

    private FiresEnderExpansionTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(SpellRegistries.PARTIAL_TELEPORT.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.HOLLOW_CRYSTAL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.OBSIDIAN_ROD.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.DRAGONS_FURY.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.GATE_OF_ENDER.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.BINARY_STARS.get().getSpellResource());
        ctx.positiveEffect().addOptional(SpellRegistries.ASPECT_OF_THE_SHULKER.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistries.DIMENSIONAL_ADAPTATION.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistries.SCINTILLATING_STRIDE.get().getSpellResource());
        // INFINITE_VOID: 召唤造成伤害的虚空实体。INFINITE_VOID_EFFECT 是 NEUTRAL 类别，不归入 negativeEffect。
        ctx.attack().addOptional(SpellRegistries.INFINITE_VOID.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistries.DISPLACEMENT_CAGE.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(SpellRegistries.HOLLOW_CRYSTAL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.BINARY_STARS.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.SCINTILLATING_STRIDE.get().getSpellResource());
    }
}
