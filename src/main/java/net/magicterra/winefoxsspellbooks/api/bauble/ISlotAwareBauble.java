package net.magicterra.winefoxsspellbooks.api.bauble;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 可感知栏位变化的饰品
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-02 00:49
 */
public interface ISlotAwareBauble extends IMaidBauble {
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

    /**
     * 当饰品被装备
     *
     * @param maid 女仆实体
     * @param itemHandler 饰品栏物品
     * @param index 栏位
     * @param stack 物品
     */
    default void onEquipped(EntityMaid maid, IItemHandler itemHandler, int index, ItemStack stack) {
    }

    /**
     * 当饰品被移除
     *
     * @param maid 女仆实体
     * @param itemHandler 饰品栏物品
     * @param index 栏位
     * @param stack 物品
     */
    default void onUnequipped(EntityMaid maid, IItemHandler itemHandler, int index, ItemStack stack) {
    }
}
