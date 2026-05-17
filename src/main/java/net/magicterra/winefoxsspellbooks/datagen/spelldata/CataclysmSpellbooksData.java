package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.acetheeldritchking.cataclysm_spellbooks.registries.CSPotionEffectRegistry;
import net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries;

public final class CataclysmSpellbooksData {

    private CataclysmSpellbooksData() {}

    public static void contribute(SpellDataContext ctx) {
        // ========== 近战攻击法术 ==========
        // ABYSSAL_SLASH: 在施法者前方 2 格位置触发，检测距离 4.1 格
        ctx.range().add(SpellRegistries.ABYSSAL_SLASH.get().getSpellResource(), 2F);

        // Defense
        ctx.effect().add(SpellRegistries.ABYSSAL_PREDATOR.get().getSpellResource(),
            CSPotionEffectRegistry.ABYSSAL_PREDATOR_EFFECT.getId());

        // Positive Effect
        ctx.effect().add(SpellRegistries.FORGONE_RAGE.get().getSpellResource(),
            CSPotionEffectRegistry.WRATHFUL.getId());
        ctx.effect().add(SpellRegistries.PHARAOHS_WRATH.get().getSpellResource(),
            CSPotionEffectRegistry.KINGS_WRATH_EFFECT.getId());
    }
}
