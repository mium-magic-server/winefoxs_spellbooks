package net.magicterra.winefoxsspellbooks.datagen;

import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

/**
 * 合成配方生成
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public class WsbRecipeProvider extends RecipeProvider {
    public WsbRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // 品酒排：四个玻璃瓶插在一块木台阶上，前期即可制作
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BREWING, WsbItems.VIXENS_FLIGHT.get())
            .requires(Items.GLASS_BOTTLE, 4)
            .requires(ItemTags.WOODEN_SLABS)
            .unlockedBy("has_glass_bottle", has(Items.GLASS_BOTTLE))
            .save(output);
    }
}
