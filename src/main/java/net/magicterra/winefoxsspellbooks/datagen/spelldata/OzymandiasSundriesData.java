package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.minecraft.world.effect.MobEffects;
import net.ozymandias.ozymandiassundries.item.registries.SpellRegistries;

/**
 * Ozymandias' Sundries 附属模组的法术附带效果贡献。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class OzymandiasSundriesData {

    private OzymandiasSundriesData() {
    }

    public static void contribute(SpellDataContext ctx) {
        ctx.effect().add(SpellRegistries.LEVITATE.get().getSpellResource(), MobEffects.LEVITATION.getKey().location());
    }
}
