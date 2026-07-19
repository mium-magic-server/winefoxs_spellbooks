package net.magicterra.winefoxsspellbooks.registry;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 本模组自有音效事件注册表。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-06
 */
public final class WsbSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(Registries.SOUND_EVENT, WinefoxsSpellbooks.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> WINEFOX_HEX_CAST =
        SOUND_EVENTS.register("cast.winefox_hex", () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "cast.winefox_hex")));

    private WsbSounds() {
    }

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }
}
