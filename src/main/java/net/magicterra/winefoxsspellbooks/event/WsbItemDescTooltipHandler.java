package net.magicterra.winefoxsspellbooks.event;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 自动把本模组物品的 {@code <descriptionId>.desc} 翻译键拼进 hover tooltip。
 * <p>
 * 约定：新物品只要在 lang 文件里加一条 {@code item.winefoxs_spellbooks.<path>.desc}，
 * 鼠标停在物品上就会自动显示，无须给每个物品 override {@code appendHoverText}。
 * {@code \n} 会被显式拆成多行 {@link Component}（vanilla tooltip 不会自动断行）。
 * <p>
 * 仅对命名空间为本模组的物品生效，避免误伤别家。{@link Language#has(String)} 兜底，
 * 没写 {@code .desc} 的物品零成本跳过。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-16
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class WsbItemDescTooltipHandler {
    private static final String DESC_SUFFIX = ".desc";

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (!WinefoxsSpellbooks.MODID.equals(id.getNamespace())) {
            return;
        }
        String descKey = item.getDescriptionId() + DESC_SUFFIX;
        if (!Language.getInstance().has(descKey)) {
            return;
        }
        String text = Language.getInstance().getOrDefault(descKey);
        for (String line : text.split("\n")) {
            event.getToolTip().add(Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
    }
}
