package net.magicterra.winefoxsspellbooks.bauble;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import net.magicterra.winefoxsspellbooks.api.bauble.ISlotAwareBauble;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * 可以感知魔法书变化的 SlotItemHandler
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 23:25
 */
public class SpellBookAwareSlotItemHandler extends SlotItemHandler {
    private final EntityMaid maid;

    public SpellBookAwareSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition, EntityMaid maid) {
        super(itemHandler, index, xPosition, yPosition);
        this.maid = maid;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (getItemHandler() instanceof BaubleItemHandler baubleItemHandler) {
            IMaidBauble baubleInSlot = BaubleManager.getBauble(stack);
            if (baubleInSlot instanceof ISlotAwareBauble slotAwareBauble) {
                if (!slotAwareBauble.canPlace(baubleItemHandler, index, stack)) {
                    return false;
                }
            }
        }
        return super.mayPlace(stack);
    }

    @Override
    public void set(ItemStack stack) {
        super.set(stack);

        if (stack.isEmpty()) {
            return;
        }
        if (getItemHandler() instanceof BaubleItemHandler baubleItemHandler) {
            IMaidBauble baubleInSlot = BaubleManager.getBauble(stack);
            if (baubleInSlot instanceof ISlotAwareBauble slotAwareBauble) {
                slotAwareBauble.onEquipped(maid, baubleItemHandler, index, stack);
            }
        }
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack stack = getItem();
        ItemStack removed = super.remove(amount);
        if (getItemHandler() instanceof BaubleItemHandler baubleItemHandler) {
            IMaidBauble baubleInSlot = BaubleManager.getBauble(stack);
            if (baubleInSlot instanceof ISlotAwareBauble slotAwareBauble) {
                slotAwareBauble.onUnequipped(maid, baubleItemHandler, index, stack);
            }
        }
        return removed;
    }
}
