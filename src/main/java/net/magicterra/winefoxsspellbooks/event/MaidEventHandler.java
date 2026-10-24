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
import net.magicterra.winefoxsspellbooks.bauble.SpellBookBauble;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.TlmSpellCompat;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

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

    /**
     * TLM 饰品栏已有法术书时，禁止再往 Curios 栏装法术书，两边合计只能装一本。反向由 {@link SpellBookBauble#canPlace} 拦截。
     */
    @SubscribeEvent
    public static void onCurioCanEquip(CurioCanEquipEvent event) {
        if (!TlmSpellCompat.isLoaded() && event.getEntity() instanceof EntityMaid maid && SpellBookBauble.isSpellBook(event.getStack())
            && SpellBookBauble.containsSpellBook(maid.getMaidBauble(), -1)) {
            event.setEquipResult(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onCurioChangeEvent(CurioChangeEvent event) {
        if (event.getEntity() instanceof EntityMaid maid && CuriosCompat.isLoadedOrEnable()) {
            onMaidSpellDataChanged(maid);
        }
    }

    /**
     * 比目录重建更晚才加载进来的女仆（例如 {@code /reload} 之后才载入的区块）在这里补算。
     */
    @SubscribeEvent
    public static void onMaidJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }
        onMaidSpellDataChanged(maid);
    }

    /**
     * 重扫当前服务器上全部已加载女仆的可用法术，由 {@link MaidSpellCatalog} 在重建完目录后调用。
     * <p>
     * 女仆手上的法术是一份存进附件、跟着实体落盘的快照，其余几个监听只在装备真的动了时才重算。 目录一变，存量女仆的快照不会跟着变——不补这一刀，被移出标签的法术会一直被已经拿着那本书的 女仆施放到玩家亲手动一下她的装备为止。
     * <p>
     * 只扫 {@link EntityMaid}。召唤女仆的法术来自配装随机而非法术书，走 {@link #onMaidSpellDataChanged} 会把她的法术表清空。
     */
    public static void refreshLoadedMaidSpellData() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof EntityMaid maid) {
                    onMaidSpellDataChanged(maid);
                    count++;
                }
            }
        }
        if (count > 0) {
            WinefoxsSpellbooks.LOGGER.debug("Rebuilt maid spell data for {} loaded maids", count);
        }
    }

    /**
     * 按女仆当前装备重算她的可用法术表。
     */
    public static void onMaidSpellDataChanged(EntityMaid maid) {
        if (maid.level().isClientSide()) {
            return;
        }

        // 按行为类别分桶
        Map<MaidSpellAction, List<SpellData>> sorted = new EnumMap<>(MaidSpellAction.class);
        for (MaidSpellAction action : MaidSpellAction.ALL) {
            sorted.put(action, new ArrayList<>());
        }
        IItemHandler invWrapper = MaidMagicManager.getSpellGearView(maid);
        if (TlmSpellCompat.isLoaded()) {
            // 装了万法皆通时法术来源以其识别为准（手持、背包、Curios，可多本叠加），亲和加成仍按施法装备视图计算
            for (SpellSlot spellSlot : TlmSpellCompat.collectSpells(maid)) {
                addSpell(sorted, maid, invWrapper, spellSlot);
            }
            commitSpellData(maid, sorted);
            return;
        }
        // 检查主手、盔甲栏、饰品栏法术
        boolean spellBookLoaded = false;
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
                    addSpell(sorted, maid, invWrapper, spellSlot);
                }
            }
        }
        commitSpellData(maid, sorted);
    }

    /**
     * 计入亲和加成后按行为类别归桶。行为类别互斥，首个命中即归属；不属于任何类别的法术女仆用不了，丢弃
     */
    private static void addSpell(Map<MaidSpellAction, List<SpellData>> sorted, EntityMaid maid, IItemHandler gearView,
                                 SpellSlot spellSlot) {
        AbstractSpell spell = spellSlot.getSpell();
        MaidSpellAction action = MaidSpellCatalog.actionOf(spell);
        if (action != null) {
            int level = MaidMagicManager.getLevelFor(maid, gearView, spell, spellSlot.getLevel());
            sorted.get(action).add(new SpellData(spell, level));
        }
    }

    private static void commitSpellData(EntityMaid maid, Map<MaidSpellAction, List<SpellData>> sorted) {
        MaidMagicEntity magicEntity = (MaidMagicEntity) maid;
        MaidSpellDataHolder spellDataHolder = magicEntity.winefoxsSpellbooks$getSpellDataHolder();
        sorted.forEach(spellDataHolder::update);
        maid.syncData(WsbAttachments.MAID_SPELL_DATA);
    }
}
