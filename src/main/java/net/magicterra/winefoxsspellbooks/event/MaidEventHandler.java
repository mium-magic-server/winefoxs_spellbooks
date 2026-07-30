package net.magicterra.winefoxsspellbooks.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidEquipEvent;
import com.github.tartaricacid.touhoulittlemaid.compat.curios.CuriosCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.api.event.MaidSpellBookEvent;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * 女仆事件处理器
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-02 01:34
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class MaidEventHandler {
    /**
     * 魔力紊乱效果：玩家魔力恢复时取消
     */
    @SubscribeEvent
    public static void onChangeMana(ChangeManaEvent event) {
        if (event.getEntity().hasEffect(WsbEffects.MANA_DISRUPTION) && event.getNewMana() > event.getOldMana()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMaidEquipSpellBook(MaidSpellBookEvent.Equipment event) {
        onSpellBookChanged(event.getStack(), event.getMaid());
    }

    @SubscribeEvent
    public static void onMaidUnEquipSpellBook(MaidSpellBookEvent.UnEquipment event) {
        onSpellBookChanged(event.getStack(), event.getMaid());
    }

    private static void onSpellBookChanged(ItemStack stack, EntityMaid maid) {
        if (ISpellContainer.isSpellContainer(stack)) {
            onMaidSpellDataChanged(maid);
        }
    }

    @SubscribeEvent
    public static void onMaidEquipOther(MaidEquipEvent event) {
        if (event.getSlot() == EquipmentSlot.OFFHAND) {
            return;
        }
        onMaidSpellDataChanged(event.getMaid());
    }

    @SubscribeEvent
    public static void onCurioChangeEvent(CurioChangeEvent event) {
        if (event.getEntity() instanceof EntityMaid maid && CuriosCompat.isLoadedOrEnable()) {
            onMaidSpellDataChanged(maid);
        }
    }

    public static void onMaidSpellDataChanged(EntityMaid maid) {
        if (maid.level().isClientSide()) {
            return;
        }

        // 按行为类别分桶
        Map<MaidSpellAction, List<SpellData>> sorted = new EnumMap<>(MaidSpellAction.class);
        for (MaidSpellAction action : MaidSpellAction.ALL) {
            sorted.put(action, new ArrayList<>());
        }
        IItemHandlerModifiable curiosItems = (IItemHandlerModifiable) EmptyItemHandler.INSTANCE;
        if (CuriosCompat.isLoadedOrEnable()) {
            curiosItems = CuriosApi.getCuriosInventory(maid).map(ICuriosItemHandler::getEquippedCurios).orElse(curiosItems);
        }
        // 检查主手、盔甲栏、饰品栏法术
        boolean spellBookLoaded = false;
        IItemHandler invWrapper = new CombinedInvWrapper(maid.getArmorInvWrapper(), new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, maid.getMainHandItem())), maid.getMaidBauble(), curiosItems);
        for (int i = 0; i < invWrapper.getSlots(); i++) {
            ItemStack stack = invWrapper.getStackInSlot(i);
            Item item = stack.getItem();
            if (item instanceof ISpellbook) {
                // 如果饰品栏放入多个魔法书，只有一本能生效（和玩家保持一致）
                if (spellBookLoaded) {
                    continue;
                } else {
                    spellBookLoaded = true;
                }
            }
            if (!stack.isEmpty() && ISpellContainer.isSpellContainer(stack)) {
                ISpellContainer spellContainer = ISpellContainer.get(stack);
                List<SpellSlot> activeSpells = spellContainer.getActiveSpells();
                for (SpellSlot spellSlot : activeSpells) {
                    AbstractSpell spell = spellSlot.getSpell();
                    int level = MaidMagicManager.getLevelFor(maid, spell, spellSlot.getLevel());
                    SpellData spellData = new SpellData(spell, level);
                    // 行为类别互斥，首个命中即归属；不属于任何类别的法术女仆用不了，丢弃
                    MaidSpellAction action = MaidSpellCatalog.actionOf(spell);
                    if (action != null) {
                        sorted.get(action).add(spellData);
                    }
                }
            }
        }
        MaidMagicEntity magicEntity = (MaidMagicEntity) maid;
        MaidSpellDataHolder spellDataHolder = magicEntity.winefoxsSpellbooks$getSpellDataHolder();
        sorted.forEach(spellDataHolder::update);
        maid.syncData(WsbAttachments.MAID_SPELL_DATA);
    }
}
