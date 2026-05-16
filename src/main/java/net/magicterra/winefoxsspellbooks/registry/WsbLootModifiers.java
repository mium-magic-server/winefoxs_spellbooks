package net.magicterra.winefoxsspellbooks.registry;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.loot.AddItemLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Global Loot Modifier 序列化器注册
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public class WsbLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
        DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, WinefoxsSpellbooks.MODID);

    public static final Supplier<MapCodec<AddItemLootModifier>> ADD_ITEM =
        SERIALIZERS.register("add_item", () -> AddItemLootModifier.CODEC);

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
