package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.hypernhieis.registry.AnimeSpells;
import com.hypernhieis.registry.ModEffects;

/**
 * Hyper N Hiei's Shenanigans（hyper_n_hieis_shenanigans）：施放距离覆盖与法术挂在目标身上的主要效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class HyperNHieisShenanigansData {

    private HyperNHieisShenanigansData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 以施法者为中心的花海，友方需要位于花海之内
        ctx.range().add(AnimeSpells.FIELD_OF_FLOWERS.get().getSpellResource(), 6F);

        // 治疗
        ctx.effect().add(AnimeSpells.FIELD_OF_FLOWERS.get().getSpellResource(), ModEffects.HOPE.getId());
    }
}
