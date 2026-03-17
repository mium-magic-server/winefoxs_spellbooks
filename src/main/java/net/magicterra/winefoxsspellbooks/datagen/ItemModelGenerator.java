package net.magicterra.winefoxsspellbooks.datagen;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * 物品模型生成
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-16 12:03
 */
public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, WinefoxsSpellbooks.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(WsbItems.CASTING_TASK_ICON.get());
        basicItem(WsbItems.MAGIC_SUPPORT_TASK_ICON.get());
        basicItem(WsbItems.CRESCENT_BLOOD_VINTAGE.get());
    }
}
