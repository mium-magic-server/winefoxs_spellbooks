package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.warphan.iss_magicfromtheeast.registries.MFTESpellRegistries;

public final class MagicFromTheEastTags {

    private MagicFromTheEastTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(MFTESpellRegistries.SWORD_DANCE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.DRAGON_GLIDE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.JADE_JUDGEMENT_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.PUNISHING_HEAVEN_SPELL.get().getSpellResource()); // 召唤玉刑天
        ctx.summon().addOptional(MFTESpellRegistries.PUNISHING_HEAVEN_SPELL.get().getSpellResource()); // 召唤玉刑天
        ctx.attack().addOptional(MFTESpellRegistries.NEPHRITE_SLASH_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.JADE_BULLET_SPELL.get().getSpellResource());
        // SOUL_BURST: AOE 伤害 + 给敌人施加 SOULBURN (HARMFUL) debuff
        ctx.attack().addOptional(MFTESpellRegistries.SOUL_BURST_SPELL.get().getSpellResource());
        ctx.negativeEffect().addOptional(MFTESpellRegistries.SOUL_BURST_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.SPIRIT_CHALLENGING.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.BONE_HANDS_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.CALAMITY_CUT_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.PHANTOM_CHARGE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.ANCHORING_KUNAI.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.SPLITTING_SHURIKEN.get().getSpellResource());
        ctx.defense().addOptional(MFTESpellRegistries.BAGUA_ARRAY_CIRCLE_SPELL.get().getSpellResource());
        ctx.defense().addOptional(MFTESpellRegistries.DRAPES_OF_REFLECTION_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.SOUL_CATALYST_SPELL.get().getSpellResource()); // 发射灵魂骷髅投射物
        ctx.movement().addOptional(MFTESpellRegistries.CLOUD_RIDE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.JIANGSHI_INVOKE_SPELL.get().getSpellResource());
        ctx.summon().addOptional(MFTESpellRegistries.JIANGSHI_INVOKE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.UNDERWORLD_AID_SPELL.get().getSpellResource()); // AOE 伤害区域，非召唤物
        ctx.attack().addOptional(MFTESpellRegistries.KITSUNE_PACK_SPELL.get().getSpellResource());
        ctx.summon().addOptional(MFTESpellRegistries.KITSUNE_PACK_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.REVENANT_OF_HONOR_SPELL.get().getSpellResource());
        ctx.summon().addOptional(MFTESpellRegistries.REVENANT_OF_HONOR_SPELL.get().getSpellResource());
        ctx.attack().addOptional(MFTESpellRegistries.ASHIGARU_SQUAD_SPELL.get().getSpellResource());
        ctx.summon().addOptional(MFTESpellRegistries.ASHIGARU_SQUAD_SPELL.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(MFTESpellRegistries.ANCHORING_KUNAI.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(MFTESpellRegistries.BONE_HANDS_SPELL.get().getSpellResource());
    }
}
