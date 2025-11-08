package net.magicterra.winefoxsspellbooks.api.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 女仆装备法术书装备卸下事件
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-02 01:17
 */
public class MaidSpellBookEvent extends LivingEvent {
    /**
     * 女仆实体
     */
    private final EntityMaid maid;

    /**
     * 饰品栏物品
     */
    private final IItemHandler itemHandler;

    /**
     * 栏位
     */
    private final int slotIndex;

    /**
     * 饰品物品
     */
    private final ItemStack stack;

    /**
     * 构造事件
     *
     * @param maid 女仆实体
     * @param itemHandler 饰品栏物品
     * @param slotIndex 栏位
     * @param stack 物品
     */
    protected MaidSpellBookEvent(EntityMaid maid, IItemHandler itemHandler, int slotIndex, ItemStack stack) {
        super(maid);
        this.maid = maid;
        this.itemHandler = itemHandler;
        this.slotIndex = slotIndex;
        this.stack = stack;
    }

    public EntityMaid getMaid() {
        return maid;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public ItemStack getStack() {
        return stack;
    }

    public static class Equipment extends MaidSpellBookEvent {
        /**
         * 构造事件
         *
         * @param maid        女仆实体
         * @param itemHandler 饰品栏物品
         * @param slotIndex   栏位
         * @param stack       物品
         */
        public Equipment(EntityMaid maid, IItemHandler itemHandler, int slotIndex, ItemStack stack) {
            super(maid, itemHandler, slotIndex, stack);
        }
    }

    public static class UnEquipment extends MaidSpellBookEvent {
        /**
         * 构造事件
         *
         * @param maid        女仆实体
         * @param itemHandler 饰品栏物品
         * @param slotIndex   栏位
         * @param stack       物品
         */
        public UnEquipment(EntityMaid maid, IItemHandler itemHandler, int slotIndex, ItemStack stack) {
            super(maid, itemHandler, slotIndex, stack);
        }
    }
}
