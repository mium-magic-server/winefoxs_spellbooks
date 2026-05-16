package net.magicterra.winefoxsspellbooks.datagen;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
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
        basicItem(WsbItems.VULPINE_ANIMA.get());

        // 学派卷轴模型：主模组 ScrollModel 按 SchoolType id 动态加载
        // <school_namespace>:item/scroll_<school_path>，所以这里需要一个无 Item
        // 绑定的独立模型条目，由 ScrollModel#getScrollModelLocation() 引用。
        scrollModel(WsbSchools.WINEFOX_HEX_RESOURCE.getPath());
    }

    private void scrollModel(String schoolPath) {
        withExistingParent("scroll_" + schoolPath, "item/generated")
            .texture("layer0", modLoc("item/scroll_" + schoolPath));
    }
}
