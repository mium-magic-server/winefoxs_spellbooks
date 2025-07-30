package net.magicterra.winefoxsspellbooks.mixin;

import com.llamalad7.mixinextras.platform.neoforge.MixinExtrasConfigPlugin;
import net.magicterra.winefoxsspellbooks.Config;
import net.neoforged.fml.loading.FMLLoader;

/**
 * YSM Mixin 插件
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 02:40
 */
public class MixinPlugin extends MixinExtrasConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String name = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
        if (name.startsWith("Ysm")) {
            return Config.ysmSupport() && FMLLoader.getLoadingModList().getModFileById("yes_steve_model") != null;
        }
        return super.shouldApplyMixin(targetClassName, mixinClassName);
    }
}
