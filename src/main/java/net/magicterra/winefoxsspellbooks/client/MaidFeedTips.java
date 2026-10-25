package net.magicterra.winefoxsspellbooks.client;

import com.github.tartaricacid.touhoulittlemaid.client.overlay.MaidTipsOverlay;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.RenderConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.item.consumables.DrinkableItem;
import net.magicterra.winefoxsspellbooks.item.VixensFlightItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 指向自家女仆时的喂药提示：品酒排与铁魔法的 {@link DrinkableItem} 沿用车万女仆喂药水的提示文案与开关
 * <p>
 * 车万女仆的药水提示只按物品登记 {@code Items.POTION}，这里改用条件提示，免得逐个登记铁魔法及其附属的饮品。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class MaidFeedTips {
    private static final String POTION_TIP = "overlay.touhou_little_maid.potion.tips";

    private MaidFeedTips() {
    }

    public static void register(MaidTipsOverlay overlay) {
        overlay.addSpecialTips(POTION_TIP, MaidFeedTips::canFeed);
    }

    private static boolean canFeed(ItemStack stack, EntityMaid maid, LocalPlayer player) {
        if (!RenderConfig.ENABLE_POTION_TIP.get() || !maid.isOwnedBy(player)) {
            return false;
        }
        if (stack.getItem() instanceof VixensFlightItem) {
            return VixensFlightItem.vials(stack).lastFilled() >= 0;
        }
        return stack.getItem() instanceof DrinkableItem;
    }
}
