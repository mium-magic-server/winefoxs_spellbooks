package net.magicterra.winefoxsspellbooks;

import com.mojang.logging.LogUtils;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidLoadoutManager;
import net.magicterra.winefoxsspellbooks.magic.data.SpellDataManager;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.magicterra.winefoxsspellbooks.registry.WsbCommands;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbEntities;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.magicterra.winefoxsspellbooks.registry.WsbSpells;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(WinefoxsSpellbooks.MODID)
public class WinefoxsSpellbooks {
    public static final boolean DEBUG = !FMLEnvironment.production;
    public static final String MODID = "winefoxs_spellbooks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WinefoxsSpellbooks(ModContainer modContainer, IEventBus modBus, Dist dist) {
        MaidCastingMemoryModuleTypes.MAID_CASTING_MEMORY_MODULE_TYPES.register(modBus);
        WsbSpells.register(modBus);
        WsbItems.register(modBus);
        WsbAttachments.register(modBus);
        WsbEntities.register(modBus);
        WsbEffects.register(modBus);
        modBus.addListener(LittleMaidSpellbooksCompat::onRegisterItem);

        NeoForge.EVENT_BUS.addListener(MaidSpellRegistry::registerSpell);
        NeoForge.EVENT_BUS.addListener(MaidSpellRegistry::onTagsUpdated);
        NeoForge.EVENT_BUS.addListener(WinefoxsSpellbooks::onAddReloadListener);
        NeoForge.EVENT_BUS.addListener(WsbCommands::onRegisterCommands);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        if (dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    /**
     * 注册资源重载监听器
     */
    private static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(SpellDataManager.getInstance());
        event.addListener(MaidLoadoutManager.getInstance());
    }
}
