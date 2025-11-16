package net.magicterra.winefoxsspellbooks.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 图标物品
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-16 11:54
 */
public class ItemIcon extends Item {
    public ItemIcon() {
        super((new Properties()).stacksTo(1));
    }

    @Override
    public String getDescriptionId() {
        return "item.winefoxs_spellbooks.icons";
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> components, TooltipFlag pIsAdvanced) {
        components.add(Component.translatable("tooltips.winefoxs_spellbooks.icons.desc").withStyle(ChatFormatting.GRAY));
    }
}
