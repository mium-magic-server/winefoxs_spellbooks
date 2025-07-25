package net.magicterra.winefoxsspellbooks;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.magicterra.winefoxsspellbooks.bauble.SpellBookBauble;
import net.magicterra.winefoxsspellbooks.task.MaidCastingTask;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.SwordItem;

/**
 * Little Maid SpellBooks Compat
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-19 00:22
 */
@LittleMaidExtension
public class LittleMaidSpellbooksCompat implements ILittleMaid {
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
}
