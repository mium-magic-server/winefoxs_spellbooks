package net.magicterra.winefoxsspellbooks.dev.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 仅开发环境：绕过 Moonlight Lib 与 AAA Particles 的冲突
 * <p>
 * AAA Particles（Echoing Magic 的硬性前置）通过 {@code MixinResourceLocation} 关闭了原版的命名空间校验，
 * Moonlight 的 {@code ResourceLocationSanityCheck.validateNamespace} 检测到后会抛出
 * “Some mod disabled vanilla id validation”，导致客户端加载失败。
 * <p>
 * 这里在开发环境中直接取消该校验。Moonlight 以用户放置的 jar 形式位于 run/mods，不在编译类路径上，
 * 因此使用 {@link Pseudo} 与字符串目标；未安装 Moonlight 时本 Mixin 会被忽略。
 * 该补丁只存在于 dev 源集，不会进入发布 jar。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "net.mehvahdjukaar.moonlight.core.misc.ResourceLocationSanityCheck")
public abstract class MoonlightSanityCheckMixin {

    /**
     * 取消 Moonlight 的命名空间校验
     */
    @Inject(method = "validateNamespace", at = @At("HEAD"), cancellable = true, require = 0)
    private static void wsb$skipNamespaceValidation(String namespace, String path, CallbackInfo ci) {
        ci.cancel();
    }
}
