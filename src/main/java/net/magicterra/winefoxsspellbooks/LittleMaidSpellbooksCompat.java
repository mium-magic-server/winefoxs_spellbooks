package net.magicterra.winefoxsspellbooks;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.api.event.AddJadeInfoEvent;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.magicterra.winefoxsspellbooks.bauble.SpellBookBauble;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.task.MaidCastingTask;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import snownee.jade.api.ITooltip;

/**
 * Little Maid SpellBooks Compat
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-19 00:22
 */
@LittleMaidExtension
public class LittleMaidSpellbooksCompat implements ILittleMaid {
    public LittleMaidSpellbooksCompat() {
        if (FMLLoader.getLoadingModList().getModFileById("jade") != null) {
            NeoForge.EVENT_BUS.addListener(this::addJadeInfoEvent);
        }
    }

    @Override
    public void bindMaidBauble(BaubleManager manager) {
        SpellBookBauble bauble = new SpellBookBauble();
        ItemRegistry.getIronsItems().stream()
            .filter(item -> item.get() instanceof IPresetSpellContainer)
            .filter(item -> !(item.get() instanceof ArmorItem))
            .filter(item -> !(item.get() instanceof SwordItem))
            .forEach((item) -> manager.bind(item.get(), bauble));
    }

    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new MaidCastingTask());
    }

    public void addJadeInfoEvent(AddJadeInfoEvent event) {
        ITooltip iTooltip = event.getTooltip();
        EntityMaid maid = event.getMaid();
        IMaidTask task = maid.getTask();
        if (!(task instanceof MaidCastingTask)) {
            return;
        }
        MaidMagicEntity magicEntity = (MaidMagicEntity) maid;
        int maxMana = (int) maid.getAttributeValue(AttributeRegistry.MAX_MANA);
        int mana = (int) magicEntity.winefoxsSpellbooks$getMana();
        iTooltip.add(Component.translatable("top.winefoxs_spellbooks.entity_maid.mana", mana, maxMana));
    }
}
