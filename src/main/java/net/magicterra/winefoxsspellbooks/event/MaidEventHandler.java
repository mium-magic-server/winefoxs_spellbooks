package net.magicterra.winefoxsspellbooks.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidEquipEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import java.util.ArrayList;
import java.util.List;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.api.event.MaidSpellBookEvent;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

/**
 * 女仆事件处理器
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-02 01:34
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class MaidEventHandler {
    @SubscribeEvent
    public static void onMaidEquipSpellBook(MaidSpellBookEvent.Equipment event) {
        ItemStack stack = event.getStack();
        if (ISpellContainer.isSpellContainer(stack)) {
            onMaidSpellDataChanged(event.getMaid());
        }
    }

    @SubscribeEvent
    public static void onMaidUnEquipSpellBook(MaidSpellBookEvent.UnEquipment event) {
        ItemStack stack = event.getStack();
        if (ISpellContainer.isSpellContainer(stack)) {
            onMaidSpellDataChanged(event.getMaid());
        }
    }

    @SubscribeEvent
    public static void onMaidEquipOther(MaidEquipEvent event) {
        if (event.getSlot() == EquipmentSlot.OFFHAND) {
            return;
        }
        onMaidSpellDataChanged(event.getMaid());
    }

    public static void onMaidSpellDataChanged(EntityMaid maid) {
        if (maid.level.isClientSide()) {
            return;
        }

        List<SpellData> attackSpells = new ArrayList<>();
        List<SpellData> defenseSpells = new ArrayList<>();
        List<SpellData> movementSpells = new ArrayList<>();
        List<SpellData> supportSpells = new ArrayList<>();
        List<SpellData> positiveEffectSpells = new ArrayList<>();
        List<SpellData> negativeEffectSpells = new ArrayList<>();
        List<SpellData> supportEffectSpells = new ArrayList<>();
        // 检查主手、盔甲栏、饰品栏法术
        boolean spellBookLoaded = false;
        IItemHandler invWrapper = new CombinedInvWrapper(maid.getArmorInvWrapper(), new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, maid.getMainHandItem())), maid.getMaidBauble());
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
                    if (MaidSpellRegistry.isAttackSpell(spell)) {
                        attackSpells.add(spellData);
                    } else if (MaidSpellRegistry.isDefenseSpell(spell)) {
                        defenseSpells.add(spellData);
                    } else if (MaidSpellRegistry.isMovementSpell(spell)) {
                        movementSpells.add(spellData);
                    } else if (MaidSpellRegistry.isSupportSpell(spell)) {
                        supportSpells.add(spellData);
                    } else if (MaidSpellRegistry.isPositiveEffectSpell(spell)) {
                        positiveEffectSpells.add(spellData);
                    } else if (MaidSpellRegistry.isNegativeEffectSpell(spell)) {
                        negativeEffectSpells.add(spellData);
                    } else if (MaidSpellRegistry.isSupportEffectSpell(spell)) {
                        supportEffectSpells.add(spellData);
                    }
                }
            }
        }
        MaidMagicEntity magicEntity = (MaidMagicEntity) maid;
        MaidSpellDataHolder spellDataHolder = magicEntity.winefoxsSpellbooks$getSpellDataHolder();
        spellDataHolder.updateAttackSpells(attackSpells);
        spellDataHolder.updateDefenseSpells(defenseSpells);
        spellDataHolder.updateMovementSpells(movementSpells);
        spellDataHolder.updateSupportSpells(supportSpells);
        spellDataHolder.updatePositiveEffectSpells(positiveEffectSpells);
        spellDataHolder.updateNegativeEffectSpells(negativeEffectSpells);
        spellDataHolder.updateSupportEffectSpells(supportEffectSpells);
    }
}
