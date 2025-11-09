package net.magicterra.winefoxsspellbooks.api.bauble;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 可拦截放入栏位事件的饰品
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-02 00:49
 */
public interface ISlotPlaceableBauble extends IMaidBauble {
    /**
     * 是否能放入饰品栏中 (额外的检查)
     *
     * @param itemHandler 饰品栏物品
     * @param index 栏位
     * @param stack 物品
     * @return 是否允许放入饰品栏
     */
    default boolean canPlace(IItemHandler itemHandler, int index, ItemStack stack) {
        return true;
    }
}
