package net.magicterra.winefoxsspellbooks.registry;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.effect.BrewsurgeEffect;
import net.magicterra.winefoxsspellbooks.effect.CrescentBrandEffect;
import net.magicterra.winefoxsspellbooks.effect.FoxfireBoostEffect;
import net.magicterra.winefoxsspellbooks.effect.ManaDisruptionEffect;
import net.magicterra.winefoxsspellbooks.effect.TipsyEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 药水效果注册
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-03-13
 */
public class WsbEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, WinefoxsSpellbooks.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> MANA_DISRUPTION = MOB_EFFECTS.register("mana_disruption", ManaDisruptionEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FOXFIRE_BOOST = MOB_EFFECTS.register("foxfire_boost", FoxfireBoostEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> BREWSURGE = MOB_EFFECTS.register("brewsurge", BrewsurgeEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> CRESCENT_BRAND = MOB_EFFECTS.register("crescent_brand", CrescentBrandEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> TIPSY = MOB_EFFECTS.register("tipsy", TipsyEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
