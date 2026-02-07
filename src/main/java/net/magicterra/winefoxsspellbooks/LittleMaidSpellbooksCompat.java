package net.magicterra.winefoxsspellbooks;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.api.event.AddJadeInfoEvent;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.magicterra.winefoxsspellbooks.bauble.SpellBookBauble;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.ai.brain.MaidMagicBrain;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSummonManager;
import net.magicterra.winefoxsspellbooks.task.MaidCastingTask;
import net.magicterra.winefoxsspellbooks.task.MaidMagicSupportTask;
import net.magicterra.winefoxsspellbooks.task.debug.MaidDrinkPotionTask;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import snownee.jade.api.ITooltip;

/**
 * Little Maid SpellBooks Compat
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-19 00:22
 */
@LittleMaidExtension
public class LittleMaidSpellbooksCompat implements ILittleMaid {
    private static final Set<String> ALL_SPELL_IDS = ConcurrentHashMap.newKeySet();
    private static final Set<Item> REGISTERED_SPELL_BOOKS = ConcurrentHashMap.newKeySet();

    public LittleMaidSpellbooksCompat() {
        if (FMLLoader.getLoadingModList().getModFileById("jade") != null) {
            NeoForge.EVENT_BUS.addListener(this::addJadeInfoEvent);
        }
        NeoForge.EVENT_BUS.addListener(MaidSummonManager::onServerStopping);
    }

    @Override
    public void bindMaidBauble(BaubleManager manager) {
        if (MaidConfig.ENABLE_MAID_CURIOS.get()) {
            return;
        }
        SpellBookBauble bauble = new SpellBookBauble();
        for (Item book : REGISTERED_SPELL_BOOKS) {
            manager.bind(book, bauble);
        }
    }

    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new MaidCastingTask());
        manager.add(new MaidMagicSupportTask());
        if (WinefoxsSpellbooks.DEBUG) {
            manager.add(new MaidDrinkPotionTask());
        }
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        manager.addExtraMaidBrain(new MaidMagicBrain());
    }

    public void addJadeInfoEvent(AddJadeInfoEvent event) {
        ITooltip iTooltip = event.getTooltip();
        EntityMaid maid = event.getMaid();
        IMaidTask task = maid.getTask();
        if (!(task instanceof MaidCastingTask || task instanceof MaidMagicSupportTask)) {
            return;
        }
        MaidMagicEntity magicEntity = (MaidMagicEntity) maid;
        int maxMana = (int) MaidMagicManager.getMaxMana(maid);
        int mana = (int) magicEntity.winefoxsSpellbooks$getMana();
        iTooltip.add(Component.translatable("top.winefoxs_spellbooks.entity_maid.mana", mana, maxMana));
    }

    public static void onRegisterItem(RegisterEvent event) {
        Registry<Item> itemRegistry = event.getRegistry(Registries.ITEM);
        if (itemRegistry != null) {
            for (Item item : itemRegistry) {
                if (!(item instanceof IPresetSpellContainer)) {
                    continue;
                }
                if (item instanceof ArmorItem || item instanceof SwordItem || item instanceof StaffItem) {
                    continue;
                }
                REGISTERED_SPELL_BOOKS.add(item);
            }
        }

        Registry<AbstractSpell> spellRegistry = event.getRegistry(SpellRegistry.SPELL_REGISTRY_KEY);
        if (spellRegistry != null) {
            for (AbstractSpell spell : spellRegistry) {
                ALL_SPELL_IDS.add(spell.getSpellId());
            }
        }
    }

    public static boolean isSpellBook(Item item) {
        return REGISTERED_SPELL_BOOKS.contains(item);
    }

    public static boolean isSpellId(String spellId) {
        return ALL_SPELL_IDS.contains(spellId);
    }

    public static Collection<String> getAllSpellIds() {
        return ALL_SPELL_IDS;
    }
}
