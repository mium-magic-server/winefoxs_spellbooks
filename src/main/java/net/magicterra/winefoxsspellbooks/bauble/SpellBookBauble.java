package net.magicterra.winefoxsspellbooks.bauble;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.api.bauble.ISlotPlaceableBauble;
import net.magicterra.winefoxsspellbooks.api.event.MaidSpellBookEvent;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.SlotContext;

/**
 * 魔法书饰品
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-19 00:29
 */
public class SpellBookBauble implements ISlotPlaceableBauble {
    @Override
    public boolean canPlace(IItemHandler itemHandler, int index, ItemStack stack) {
        Predicate<Holder<Item>> predicate = itemHolder -> itemHolder.value() instanceof SpellBook;
        if (!stack.is(predicate)) {
            return false;
        }
        int slots = itemHandler.getSlots();
        for (int i = 0; i < slots; i++) {
            // 只允许女仆装备一个法术书
            var exist = itemHandler.getStackInSlot(i);
            if (exist.is(predicate)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onPutOn(EntityMaid maid, ItemStack stack) {
        if (stack.getItem() instanceof CurioBaseItem curioBaseItem) {
            SlotContext slotContext = new SlotContext(Curios.SPELLBOOK_SLOT, maid, 0, false, true);
            Multimap<Holder<Attribute>, AttributeModifier> map = curioBaseItem.getAttributeModifiers(slotContext, ResourceLocation.withDefaultNamespace("empty"), stack);
            if (!map.isEmpty()) {
                maid.getAttributes().addTransientAttributeModifiers(map);
            }
        }

        NeoForge.EVENT_BUS.post(new MaidSpellBookEvent.Equipment(maid, stack));
    }

    @Override
    public void onTakeOff(EntityMaid maid, ItemStack stack) {
        if (stack.getItem() instanceof CurioBaseItem curioBaseItem) {
            SlotContext slotContext = new SlotContext(Curios.SPELLBOOK_SLOT, maid, 0, false, true);
            Multimap<Holder<Attribute>, AttributeModifier> map = curioBaseItem.getAttributeModifiers(slotContext, ResourceLocation.withDefaultNamespace("empty"), stack);
            if (!map.isEmpty()) {
                maid.getAttributes().removeAttributeModifiers(map);
            }
        }

        NeoForge.EVENT_BUS.post(new MaidSpellBookEvent.UnEquipment(maid, stack));
    }
}
