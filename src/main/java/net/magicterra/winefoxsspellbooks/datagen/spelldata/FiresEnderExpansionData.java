package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.fireofpower.firesenderexpansion.registries.EffectRegistry;
import net.fireofpower.firesenderexpansion.registries.SpellRegistries;
import net.minecraft.world.effect.MobEffects;

public final class FiresEnderExpansionData {

    private FiresEnderExpansionData() {}

    public static void contribute(SpellDataContext ctx) {
        // Defense
        ctx.effect().add(SpellRegistries.ASPECT_OF_THE_SHULKER.get().getSpellResource(),
            EffectRegistry.ASPECT_OF_THE_SHULKER_EFFECT.getId());
        // DIMENSIONAL_ADAPTATION 根据维度施加不同效果：主世界夜视、下界抗火、末地缓降、口袋维度饱和
        // 以下界抗火为主要效果（最常用且影响最大）
        ctx.effect().add(SpellRegistries.DIMENSIONAL_ADAPTATION.get().getSpellResource(),
            MobEffects.FIRE_RESISTANCE.getKey().location());

        // Negative Effect
        ctx.effect().add(SpellRegistries.INFINITE_VOID.get().getSpellResource(),
            EffectRegistry.INFINITE_VOID_EFFECT.getId());
    }
}
