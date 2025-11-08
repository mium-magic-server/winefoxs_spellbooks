package net.magicterra.winefoxsspellbooks;

import com.mojang.logging.LogUtils;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(WinefoxsSpellbooks.MODID)
public class WinefoxsSpellbooks {
    public static final boolean DEBUG = !FMLEnvironment.production;
    public static final String MODID = "winefoxs_spellbooks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WinefoxsSpellbooks(IEventBus modBus) {
        MaidCastingMemoryModuleTypes.MAID_CASTING_MEMORY_MODULE_TYPES.register(modBus);
    }
}
