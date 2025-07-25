package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.api.event.client.DefaultGeckoAnimationEvent;
import com.github.tartaricacid.touhoulittlemaid.client.resource.GeckoModelLoader;
import net.magicterra.winefoxsspellbooks.event.CastingAnimationLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 使用混入加载动画
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-24 02:35
 */
@Mixin(GeckoModelLoader.class)
public class GeckoModelLoaderMixin {
    @Inject(method = "loadDefaultAnimation", at = @At("TAIL"))
    private static void afterLoadDefaultAnimation(CallbackInfo ci) {
        CastingAnimationLoader.onDefaultGeckoAnimationEvent(new DefaultGeckoAnimationEvent(
            GeckoModelLoader.DEFAULT_MAID_ANIMATION_FILE,
            GeckoModelLoader.DEFAULT_TAC_ANIMATION_FILE,
            GeckoModelLoader.DEFAULT_CHAIR_ANIMATION_FILE));
    }
}
