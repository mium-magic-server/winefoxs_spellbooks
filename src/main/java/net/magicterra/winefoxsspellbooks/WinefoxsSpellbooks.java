package net.magicterra.winefoxsspellbooks;

import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidLoadoutManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSummonManager;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.magicterra.winefoxsspellbooks.magic.data.SpellDataManager;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastHandlers;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.magicterra.winefoxsspellbooks.registry.WsbAttributes;
import net.magicterra.winefoxsspellbooks.registry.WsbCommands;
import net.magicterra.winefoxsspellbooks.registry.WsbCreativeTabs;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbEntities;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.magicterra.winefoxsspellbooks.registry.WsbLootModifiers;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.magicterra.winefoxsspellbooks.registry.WsbSounds;
import net.magicterra.winefoxsspellbooks.registry.WsbSpells;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
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
        WsbAttributes.register(modBus);
        WsbSchools.register(modBus);
        WsbSounds.register(modBus);
        WsbParticles.register(modBus);
        WsbCreativeTabs.register(modBus);
        WsbLootModifiers.register(modBus);
        modBus.addListener(LittleMaidSpellbooksCompat::onRegisterItem);

        NeoForge.EVENT_BUS.addListener(MaidSpellCatalog::registerSpell);
        NeoForge.EVENT_BUS.addListener(MaidSpellCatalog::onTagsUpdated);
        NeoForge.EVENT_BUS.addListener(WinefoxsSpellbooks::onServerStarting);
        // 关服时把女仆的召唤物存档
        NeoForge.EVENT_BUS.addListener(MaidSummonManager::onServerStopping);
        NeoForge.EVENT_BUS.addListener(WinefoxsSpellbooks::onAddReloadListener);
        NeoForge.EVENT_BUS.addListener(WinefoxsSpellbooks::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(WsbCommands::onRegisterCommands);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        warnAboutOrphanedCommonConfig();
        if (dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    /**
     * 配置由 COMMON 改成 SERVER 后文件名跟着变，旧文件不会被自动搬过去，也不会有任何提示。 这里在旧文件还躺着时喊一声，免得整合包调好的数值悄悄退回默认值。
     */
    private static void warnAboutOrphanedCommonConfig() {
        Path legacy = FMLPaths.CONFIGDIR.get().resolve(MODID + "-common.toml");
        if (Files.exists(legacy)) {
            LOGGER.warn("Found a leftover config file at {}. Winefox's Spellbooks now reads {}-server.toml from "
                + "the same folder; copy your values over, the old file is ignored.", legacy, MODID);
        }
    }

    private static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(SpellDataManager.getInstance());
        event.addListener(MaidLoadoutManager.getInstance());
    }

    /**
     * 初次进世界时数据包比 SERVER 配置先加载，这里把配置层补叠回去。
     */
    private static void onServerAboutToStart(ServerAboutToStartEvent event) {
        SpellDataManager.getInstance().applyConfigOverrides();
    }

    /**
     * 启动时校验附属模组 recast 反射引用是否还在。
     */
    private static void onServerStarting(ServerStartingEvent event) {
        RecastHandlers.selfCheck();
    }
}
