package net.magicterra.winefoxsspellbooks.registry;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.CreativeTabRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 创造模式物品栏：本模组物品 + 酒狐巫法学派全部卷轴（各法术逐等级）。
 * <p>
 * 学派卷轴在 {@link BuildCreativeModeTabContentsEvent} 中按运行期启用的法术动态生成。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-19
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class WsbCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WinefoxsSpellbooks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = TABS.register("main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + WinefoxsSpellbooks.MODID + ".main"))
            .icon(() -> new ItemStack(WsbItems.VULPINE_ANIMA.get()))
            .displayItems((enabledFeatures, entries) -> {
                // 任务图标物品仅作 GUI 图标用，不进创造栏
                entries.accept(WsbItems.VULPINE_ANIMA.get());
                entries.accept(WsbItems.CRESCENT_BLOOD_VINTAGE.get());
                entries.accept(WsbItems.VIXENS_FLIGHT.get());
            })
            .withTabsBefore(CreativeTabRegistry.EQUIPMENT_TAB.getId())
            .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    @SubscribeEvent
    public static void fillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != MAIN_TAB.get()) {
            return;
        }
        SpellRegistry.getEnabledSpells().stream()
            .filter(spell -> spell != SpellRegistry.none()
                && WinefoxHexHelper.isWinefoxHexSchool(spell.getSchoolType()))
            .forEach(spell -> {
                for (int level = spell.getMinLevel(); level <= spell.getMaxLevel(); level++) {
                    ItemStack scroll = new ItemStack(ItemRegistry.SCROLL.get());
                    ISpellContainer.createScrollContainer(spell, level, scroll);
                    event.accept(scroll);
                }
            });
    }
}
