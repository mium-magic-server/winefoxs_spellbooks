package net.magicterra.winefoxsspellbooks.datagen;

import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * 数据生成
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-06-30 01:26
 */
@Mod(value = WinefoxsSpellbooks.MODID)
public class DataGenerateMod {

    public DataGenerateMod(IEventBus eventBus) {
        eventBus.addListener(this::gatherData);

        // 模拟生成默认配置
        ConfigTracker.INSTANCE.loadDefaultServerConfigs();
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Spell tags provider
        generator.addProvider(event.includeServer(), new MaidSpellTagsProvider(output, lookupProvider, WinefoxsSpellbooks.MODID, existingFileHelper));

        // Block tags provider (needed for item tags)
        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(output, lookupProvider, WinefoxsSpellbooks.MODID, existingFileHelper) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                // No block tags needed
            }
        };
        generator.addProvider(event.includeServer(), blockTagsProvider);

        // Item tags provider for maid loadout
        generator.addProvider(event.includeServer(), new MaidLoadoutTagsProvider(
            output, lookupProvider, blockTagsProvider.contentsGetter(), WinefoxsSpellbooks.MODID, existingFileHelper));

        // Maid loadout JSON data provider
        generator.addProvider(event.includeServer(), new MaidLoadoutDataProvider(output));

        // Spell data provider (casting range and caused effects)
        generator.addProvider(event.includeServer(), new SpellDataProvider(output));

        // Client providers
        generator.addProvider(event.includeClient(), new ItemModelGenerator(output, existingFileHelper));
    }
}
