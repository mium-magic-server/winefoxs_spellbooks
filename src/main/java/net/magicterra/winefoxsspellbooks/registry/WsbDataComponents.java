package net.magicterra.winefoxsspellbooks.registry;

import com.mojang.serialization.Codec;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.item.VialsContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品数据组件注册
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public class WsbDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, WinefoxsSpellbooks.MODID);

    /**
     * 品酒排的四支酒签
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VialsContents>> VIALS =
        COMPONENTS.registerComponentType("vials", builder -> builder
            .persistent(VialsContents.CODEC)
            .networkSynchronized(VialsContents.STREAM_CODEC)
            .cacheEncoding());

    /**
     * 女仆饮用前选好的酒签下标：女仆跳过此刻不该喝的酒签，喝完即移除；没有时按最右一支喝
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> VIAL_CHOICE =
        COMPONENTS.registerComponentType("vial_choice", builder -> builder
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
