package net.magicterra.winefoxsspellbooks.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * JEI 集成入口
 * <p>
 * 遍历 {@link WsbItems#ITEMS} 全部物品，对每个 {@code <descriptionId>.desc} 翻译键存在的物品，
 * 自动挂一个 JEI 信息页（右上角 "i" 图标）。新物品只要在 lang 里加一条 {@code .desc} 即可，
 * 不需要再改本文件。
 * <p>
 * 直接传 {@link Component#translatable(String, Object...)}，JEI 在渲染时再解析翻译，
 * 玩家中途切语言也能立刻生效。{@code \n} 由 JEI 信息页自带的换行逻辑处理。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-16
 */
@JeiPlugin
public class WsbJeiPlugin implements IModPlugin {
    private static final String DESC_SUFFIX = ".desc";
    private static final ResourceLocation UID =
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (var holder : WsbItems.ITEMS.getEntries()) {
            Item item = holder.get();
            String descKey = item.getDescriptionId() + DESC_SUFFIX;
            if (Language.getInstance().has(descKey)) {
                registration.addItemStackInfo(new ItemStack(item), Component.translatable(descKey));
            }
        }
    }
}
