package net.magicterra.winefoxsspellbooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

/**
 * 配置文件
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-31 02:08
 */
public class Config {
    private static final MixinConfigSpec MIXIN_CONFIG_SPEC;

    static {
        // 混入配置只能在 neoforge 初始化前读取，不能动态修改
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        var configPath = FMLLoader.getGamePath().resolve(FMLPaths.CONFIGDIR.relative()).resolve(WinefoxsSpellbooks.MODID + "_mixin.json");
        MixinConfigSpec mixinConfigSpec0 = new MixinConfigSpec();
        if (Files.exists(configPath)) {
            try {
                mixinConfigSpec0 = gson.fromJson(Files.readString(configPath), MixinConfigSpec.class);
            } catch (IOException e) {
                WinefoxsSpellbooks.LOGGER.error("Failed to load config file", e);
            }
        } else {
            String json = gson.toJson(mixinConfigSpec0);
            try {
                Files.writeString(configPath, json);
            } catch (IOException e) {
                WinefoxsSpellbooks.LOGGER.error("Could not write config", e);
            }
        }
        MIXIN_CONFIG_SPEC = mixinConfigSpec0;
    }

    public static boolean ysmSupport() {
        return MIXIN_CONFIG_SPEC.ysmSupport;
    }

    static class MixinConfigSpec {
        public boolean ysmSupport = true;
    }
}
