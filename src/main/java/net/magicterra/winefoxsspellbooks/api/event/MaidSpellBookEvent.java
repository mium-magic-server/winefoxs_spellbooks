package net.magicterra.winefoxsspellbooks.api.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

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
     * 饰品物品
     */
    private final ItemStack stack;

    /**
     * 构造事件
     *
     * @param maid  女仆实体
     * @param stack 物品
     */
    protected MaidSpellBookEvent(EntityMaid maid, ItemStack stack) {
        super(maid);
        this.maid = maid;
        this.stack = stack;
    }

    public EntityMaid getMaid() {
        return maid;
    }

    public ItemStack getStack() {
        return stack;
    }

    public static class Equipment extends MaidSpellBookEvent {
        /**
         * 构造事件
         *
         * @param maid  女仆实体
         * @param stack 物品
         */
        public Equipment(EntityMaid maid, ItemStack stack) {
            super(maid, stack);
        }
    }

    public static class UnEquipment extends MaidSpellBookEvent {
        /**
         * 构造事件
         *
         * @param maid  女仆实体
         * @param stack 物品
         */
        public UnEquipment(EntityMaid maid, ItemStack stack) {
            super(maid, stack);
        }
    }
}
