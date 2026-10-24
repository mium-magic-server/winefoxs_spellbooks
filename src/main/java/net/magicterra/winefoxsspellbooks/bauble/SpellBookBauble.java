package net.magicterra.winefoxsspellbooks.bauble;

import com.github.tartaricacid.touhoulittlemaid.compat.curios.CuriosCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.compat.Curios;
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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

/**
 * 魔法书饰品
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-19 00:29
 */
public class SpellBookBauble implements ISlotPlaceableBauble {
    private static final Predicate<Holder<Item>> IS_SPELL_BOOK = itemHolder -> itemHolder.value() instanceof ISpellbook;

    @Override
    public boolean canPlace(IItemHandler itemHandler, int index, ItemStack stack) {
        if (!isSpellBook(stack)) {
            return true;
        }
        // 只允许女仆装备一个法术书；目标栏位自身不算，原地换书照常放行
        return !containsSpellBook(itemHandler, index);
    }

    @Override
    public boolean canPlace(EntityMaid maid, IItemHandler itemHandler, int index, ItemStack stack) {
        if (!canPlace(itemHandler, index, stack)) {
            return false;
        }
        // Curios 模式下 TLM 饰品栏和 Curios 法术书栏同时可用，两边合计也只能装一本，反向由 onCurioCanEquip 拦截
        return !isSpellBook(stack) || !CuriosCompat.isLoadedOrEnable()
            || !CuriosApi.getCuriosInventory(maid).map(curios -> containsSpellBook(curios.getEquippedCurios(), -1)).orElse(false);
    }

    /**
     * 物品栏中除 {@code skipIndex} 外是否已有法术书
     *
     * @param itemHandler 物品栏
     * @param skipIndex   跳过的栏位，-1 表示不跳过
     * @return 是否已有法术书
     */
    public static boolean containsSpellBook(IItemHandler itemHandler, int skipIndex) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (i != skipIndex && isSpellBook(itemHandler.getStackInSlot(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 物品是否为法术书
     */
    public static boolean isSpellBook(ItemStack stack) {
        return stack.is(IS_SPELL_BOOK);
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
