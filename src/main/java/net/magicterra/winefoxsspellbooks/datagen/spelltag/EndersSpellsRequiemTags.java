package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.ender.ess_requiem.registries.GGSpellRegistry;

public final class EndersSpellsRequiemTags {

    private EndersSpellsRequiemTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(GGSpellRegistry.CLAW.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.WRETCH.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.BOILING_BLOOD.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.NECROTIC_BURST.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.DECAYING_WILL.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SPIKES_OF_AGONY.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.TENTACLE_WHIP.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.PALE_FLAME.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.TWILIGHT_ASSAULT.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.DAMNATION.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.LORD_OF_FROST.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.UNDEAD_PACT.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.STRAIN.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.REAPER.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.EBONY_ARMOR.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.PROTECTION_OF_THE_FALLEN.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.CURSED_IMMORTALITY.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.EBONY_CATAPHRACT.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.BASTION_OF_LIGHT.get().getSpellResource());
        ctx.negativeEffect().addOptional(GGSpellRegistry.FINALITY_OF_DECAY.get().getSpellResource());
        ctx.negativeEffect().addOptional(GGSpellRegistry.ETERNAL_BATTLEFIELD.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SKULLS.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.SKULLS.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SLASH.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SLAM.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.QUICK_SLICE.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.UPPERCUT.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.PARRY.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SOULMASTER.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.SOULMASTER.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.OVERWHELMING.get().getSpellResource());
        ctx.positiveEffect().addOptional(GGSpellRegistry.HONE_EDGE.get().getSpellResource());
        ctx.positiveEffect().addOptional(GGSpellRegistry.UNDYING_DREAD.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.DISMANTLE.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.CLEAVE.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.MALEVOLENT_SLASHING.get().getSpellResource());
        ctx.movement().addOptional(GGSpellRegistry.DISAPPEARING_ACT.get().getSpellResource());
        ctx.positiveEffect().addOptional(GGSpellRegistry.ADRENALINE_RUSH.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SWITCHAROO.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.GILDED_SWORD_SUMMON.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.GILDED_SWORD_SUMMON.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SLASHING_ABILITY.get().getSpellResource());

        // ARM_OF_DECAY_PASSIVE / FIELD_OF_MOURNING / MAGGOT_BURST: 召唤类
        ctx.summon().addOptional(GGSpellRegistry.ARM_OF_DECAY_PASSIVE.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.FIELD_OF_MOURNING.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.MAGGOT_BURST.get().getSpellResource());
        // CORPSE_EXPLOSION / GLACIAL_SCULPTING: 引爆己方召唤物造成 AOE 伤害
        ctx.attack().addOptional(GGSpellRegistry.CORPSE_EXPLOSION.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.GLACIAL_SCULPTING.get().getSpellResource());
        // NIGHT_VEIL / OVERWHELMING_LIGHT: 多次施放的攻击 + 同时给自己加增益
        ctx.attack().addOptional(GGSpellRegistry.NIGHT_VEIL.get().getSpellResource());
        ctx.positiveEffect().addOptional(GGSpellRegistry.NIGHT_VEIL.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.OVERWHELMING_LIGHT.get().getSpellResource());
        ctx.positiveEffect().addOptional(GGSpellRegistry.OVERWHELMING_LIGHT.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(GGSpellRegistry.CLAW.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GGSpellRegistry.BOILING_BLOOD.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GGSpellRegistry.TWILIGHT_ASSAULT.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GGSpellRegistry.NIGHT_VEIL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GGSpellRegistry.OVERWHELMING_LIGHT.get().getSpellResource());
    }
}
