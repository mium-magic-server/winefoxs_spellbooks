package net.magicterra.winefoxsspellbooks.bauble;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.BaubleContainer;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import net.magicterra.winefoxsspellbooks.api.bauble.ISlotPlaceableBauble;
import net.minecraft.world.item.ItemStack;

/**
 * 可以感知饰品放置变化的 BaubleSlot
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 23:25
 */
public class PlaceableInterceptBaubleSlotHandler extends BaubleContainer.BaubleSlot {

    public PlaceableInterceptBaubleSlotHandler(EntityMaid maid, int index, int xPosition, int yPosition) {
        super(maid, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (getItemHandler() instanceof BaubleItemHandler baubleItemHandler) {
            IMaidBauble baubleInSlot = BaubleManager.getBauble(stack);
            if (baubleInSlot instanceof ISlotPlaceableBauble slotAwareBauble) {
                if (!slotAwareBauble.canPlace(baubleItemHandler, index, stack)) {
                    return false;
                }
            }
        }
        return super.mayPlace(stack);
    }
}
