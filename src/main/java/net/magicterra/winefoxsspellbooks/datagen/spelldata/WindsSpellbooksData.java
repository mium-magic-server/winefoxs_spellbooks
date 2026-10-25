package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.raptorzizi.wind_spellbooks.registries.ModMobEffectRegistry;
import net.raptorzizi.wind_spellbooks.registries.ModSpellRegistry;

/**
 * Wind's Spellbooks 附属：法术附带的主要状态效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class WindsSpellbooksData {

    private WindsSpellbooksData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 自我强化
        ctx.effect().add(ModSpellRegistry.TAILWIND_SPELL.get().getSpellResource(),
            ModMobEffectRegistry.TAILWIND.getId());
    }
}
