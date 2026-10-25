package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.fireofpower.firesenderexpansion.registries.SpellRegistries;

public final class FiresEnderExpansionTags {

    private FiresEnderExpansionTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(SpellRegistries.ARCANE_SLICE.get().getSpellResource());
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
        // ENDS_REJUVENATION: 在身周放置末影水晶，范围 32 格内每秒为施法者回复 1 点生命；
        // 带 getRecastCount 但属于纯召唤，女仆再次施放只会召回水晶，故不加 maidShouldRecast
        ctx.support().addOptional(SpellRegistries.ENDS_REJUVENATION.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistries.ENDS_REJUVENATION.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(SpellRegistries.HOLLOW_CRYSTAL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.BINARY_STARS.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.SCINTILLATING_STRIDE.get().getSpellResource());
    }
}
