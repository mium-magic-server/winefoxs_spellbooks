package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.config.GeneralConfig;
import net.magicterra.winefoxsspellbooks.Config;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * 添加配置
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-10-26 21:57
 */
@Mixin(GeneralConfig.class)
public class GeneralConfigMixin {
    @Inject(method = "getConfigSpec", at = @At(
        value = "INVOKE",
        target = "Lcom/github/tartaricacid/touhoulittlemaid/config/subconfig/ChairConfig;init(Lnet/neoforged/neoforge/common/ModConfigSpec$Builder;)V",
        shift = At.Shift.AFTER
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void initConfig(CallbackInfoReturnable<ModConfigSpec> cir, ModConfigSpec.Builder builder) {
        Config.init(builder);
    }
}
