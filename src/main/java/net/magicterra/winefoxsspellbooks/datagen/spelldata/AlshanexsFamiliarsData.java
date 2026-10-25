package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.alshanex.alshanex_familiars.registry.EffectRegistry;
import net.alshanex.alshanex_familiars.registry.PetSpellRegistry;

/**
 * Alshanex's Familiars（alshanex_familiars）的法术数据：施放后挂在施法者身上的主要效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class AlshanexsFamiliarsData {

    private AlshanexsFamiliarsData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 自我强化：MAYHEM 持续约 20 秒，生效期间不必重复施放
        ctx.effect().add(PetSpellRegistry.MAYHEM.get().getSpellResource(), EffectRegistry.MAYHEM.getId());
    }
}
