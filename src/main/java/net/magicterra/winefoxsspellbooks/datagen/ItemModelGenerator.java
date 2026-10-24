package net.magicterra.winefoxsspellbooks.datagen;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.ItemLayerModelBuilder;
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
        // 灵狐精魂全亮显示，掉落、手持、展示框里都不受环境光影响；地面光斑见 VulpineAnimaGroundGlow
        basicItem(WsbItems.VULPINE_ANIMA.get())
            .customLoader(ItemLayerModelBuilder::begin)
            .emissive(15, 15, 0)
            .end();

        // 学派卷轴 / 姻缘戒指：主模组按 SchoolType id 动态加载，需要无 Item 绑定的独立模型条目
        schoolItemModel("scroll_", WsbSchools.WINEFOX_HEX_RESOURCE.getPath());
        schoolItemModel("affinity_ring_", WsbSchools.WINEFOX_HEX_RESOURCE.getPath());
    }

    private void schoolItemModel(String prefix, String schoolPath) {
        withExistingParent(prefix + schoolPath, "item/generated")
            .texture("layer0", modLoc("item/" + prefix + schoolPath));
    }
}
