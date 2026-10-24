package net.magicterra.winefoxsspellbooks.api.bauble;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
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
     * @param index       栏位
     * @param stack       物品
     * @return 是否允许放入饰品栏
     */
    default boolean canPlace(IItemHandler itemHandler, int index, ItemStack stack) {
        return true;
    }

    /**
     * 是否能放入饰品栏中 (额外的检查，可查看女仆饰品栏以外的装备)，默认委托给 {@link #canPlace(IItemHandler, int, ItemStack)}
     *
     * @param maid        女仆
     * @param itemHandler 饰品栏物品
     * @param index       栏位
     * @param stack       物品
     * @return 是否允许放入饰品栏
     */
    default boolean canPlace(EntityMaid maid, IItemHandler itemHandler, int index, ItemStack stack) {
        return canPlace(itemHandler, index, stack);
    }
}
