package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries;

public final class DiscerningTheEldritchTags {

    private DiscerningTheEldritchTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(SpellRegistries.VEIN_RIPPER.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.ESOTERIC_EDGE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.ESOTERIC_STRIKE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.GUARDIANS_GAZE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.SOUL_SLICE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.SOUL_SET_ABLAZE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.GLACIAL_EDGE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.BLADES_OF_RANCOR.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.ZEALOUS_HARBINGER.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.LIBRAS_JUDGEMENT.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistries.ABRACADABRA.get().getSpellResource());
        // RAVENOUS_REVENANT: 给施法者施加 PREDATOR buff、给周围敌人施加 PREY debuff
        ctx.defense().addOptional(SpellRegistries.RAVENOUS_REVENANT.get().getSpellResource());
        ctx.positiveEffect().addOptional(SpellRegistries.RAVENOUS_REVENANT.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistries.RAVENOUS_REVENANT.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistries.OTHERWORLDLY_PRESENCE.get().getSpellResource());
        // RIFT_WALKER: 闪现 + 在落点生成造成伤害的 UnstableRiftEntity
        ctx.movement().addOptional(SpellRegistries.RIFT_WALKER.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.RIFT_WALKER.get().getSpellResource());
        // BOOGIE_WOOGIE: 与目标交换位置 + 给目标施加 CONFUSION debuff
        ctx.movement().addOptional(SpellRegistries.BOOGIE_WOOGIE.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistries.BOOGIE_WOOGIE.get().getSpellResource());
        ctx.support().addOptional(SpellRegistries.MEND_FLESH.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistries.SILENCE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistries.CONJURE_FORSAKE_AID.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistries.CONJURE_FORSAKE_AID.get().getSpellResource());

        // CRYSTALLINE_CARVER: 多段近战，命中施加 CHILLED 减速 debuff
        ctx.attack().addOptional(SpellRegistries.CRYSTALLINE_CARVER.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistries.CRYSTALLINE_CARVER.get().getSpellResource());
        // EXORCISM 是清除"疯狂值"（insanity）的玩家专属机制，对女仆基本无意义，保留注释
        // ctx.support().addOptional(SpellRegistries.EXORCISM.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(SpellRegistries.RIFT_WALKER.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.BOOGIE_WOOGIE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.BLADES_OF_RANCOR.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistries.CRYSTALLINE_CARVER.get().getSpellResource());
        // CONJURE_FORSAKE_AID: getRecastCount() > 0，多段召唤
        ctx.maidShouldRecast().addOptional(SpellRegistries.CONJURE_FORSAKE_AID.get().getSpellResource());
    }
}
