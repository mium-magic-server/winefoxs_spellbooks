package net.magicterra.winefoxsspellbooks.registry;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.item.ItemIcon;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-16 11:59
 */
public class InitItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WinefoxsSpellbooks.MODID);

    public static DeferredItem<Item> CASTING_TASK_ICON = ITEMS.register("casting_task_icon", ItemIcon::new);

    public static DeferredItem<Item> MAGIC_SUPPORT_TASK_ICON = ITEMS.register("magic_support_task_icon", ItemIcon::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
