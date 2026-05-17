package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.acetheeldritchking.discerning_the_eldritch.registries.DTEPotionEffectRegistry;
import net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries;
import net.minecraft.world.effect.MobEffects;

public final class DiscerningTheEldritchData {

    private DiscerningTheEldritchData() {}

    public static void contribute(SpellDataContext ctx) {
        // ========== 近战攻击法术 ==========
        // ESOTERIC_STRIKE: 在施法者前方 1.2 格位置触发，检测距离 2.15 格
        ctx.range().add(SpellRegistries.ESOTERIC_STRIKE.get().getSpellResource(), 2F);
        // SOUL_SLICE: 在施法者前方 1.9 格位置触发，检测距离 3.25 格
        ctx.range().add(SpellRegistries.SOUL_SLICE.get().getSpellResource(), 2F);

        // Support
        ctx.effect().add(SpellRegistries.MEND_FLESH.get().getSpellResource(),
            DTEPotionEffectRegistry.MEND_FLESH_EFFECT.getId());

        // Defense
        ctx.effect().add(SpellRegistries.ABRACADABRA.get().getSpellResource(),
            DTEPotionEffectRegistry.ABRACADABRA_EFFECT.getId());
        ctx.effect().add(SpellRegistries.RAVENOUS_REVENANT.get().getSpellResource(),
            DTEPotionEffectRegistry.PREDATOR_POTION_EFFECT.getId());
        // RAVENOUS_REVENANT 同时给敌人施加 PREY 削弱 debuff
        ctx.effect().add(SpellRegistries.RAVENOUS_REVENANT.get().getSpellResource(),
            DTEPotionEffectRegistry.PREY_POTION_EFFECT.getId());

        // Negative Effect
        ctx.effect().add(SpellRegistries.SILENCE.get().getSpellResource(),
            DTEPotionEffectRegistry.SILENCE_POTION_EFFECT.getId());

        // Movement（带 MobEffect 的移动法术）
        ctx.effect().add(SpellRegistries.BOOGIE_WOOGIE.get().getSpellResource(),
            MobEffects.CONFUSION.getKey().location());

        // Attack（带 MobEffect 的攻击法术）
        ctx.effect().add(SpellRegistries.GUARDIANS_GAZE.get().getSpellResource(),
            MobEffects.DIG_SLOWDOWN.getKey().location());
        // CRYSTALLINE_CARVER 命中施加 Iron's 核心的 CHILLED 减速 debuff
        ctx.effect().add(SpellRegistries.CRYSTALLINE_CARVER.get().getSpellResource(),
            MobEffectRegistry.CHILLED.getId());
    }
}
