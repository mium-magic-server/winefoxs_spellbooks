package net.magicterra.winefoxsspellbooks.bauble;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import top.theillusivec4.curios.api.SlotContext;

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
        Predicate<Holder<Item>> predicate = itemHolder -> itemHolder.value() instanceof SpellBook;
        if (!stack.is(predicate)) {
            return super.mayPlace(stack);
        }
        int slots = getItemHandler().getSlots();
        for (int i = 0; i < slots; i++) {
            var exist = getItemHandler().getStackInSlot(i);
            if (exist.is(predicate)) {
                return false;
            }
        }
        return super.mayPlace(stack);
    }

    @Override
    public void set(ItemStack stack) {
        super.set(stack);

        if (maid.level.isClientSide || stack.isEmpty()) {
            return;
        }
        onBookInstall(maid, stack);
    }

    @Override
    public ItemStack remove(int amount) {
        if (maid.level.isClientSide) {
            return super.remove(amount);
        }
        ItemStack stack = getItem();
        if (stack.getItem() instanceof CurioBaseItem curioBaseItem) {
            SlotContext slotContext = new SlotContext(Curios.SPELLBOOK_SLOT, maid, 0, false, true);
            Multimap<Holder<Attribute>, AttributeModifier> map = curioBaseItem.getAttributeModifiers(slotContext, ResourceLocation.withDefaultNamespace("empty"), stack);
            if (!map.isEmpty()) {
                maid.getAttributes().removeAttributeModifiers(map);
            }
        }
        return super.remove(amount);
    }

    public static void onBookInstall(LivingEntity maid, ItemStack stack) {
        if (stack.getItem() instanceof CurioBaseItem curioBaseItem) {
            SlotContext slotContext = new SlotContext(Curios.SPELLBOOK_SLOT, maid, 0, false, true);
            Multimap<Holder<Attribute>, AttributeModifier> map = curioBaseItem.getAttributeModifiers(slotContext, ResourceLocation.withDefaultNamespace("empty"), stack);
            if (!map.isEmpty()) {
                maid.getAttributes().addTransientAttributeModifiers(map);
            }
        }
    }
}
