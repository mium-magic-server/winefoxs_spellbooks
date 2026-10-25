package net.magicterra.winefoxsspellbooks.dev.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 仅开发环境：绕过 Cataclysm 与 Accessories 在 runData 中的冲突
 * <p>
 * runData 以客户端 Dist 启动，但此时 {@code Minecraft.getInstance()} 为 null。
 * Cataclysm 的构造函数会在客户端 Dist 下初始化 {@code ClientEvent}，其静态初始化块会构造 {@code Model}，
 * 而 Accessories 的 {@code ModelMixin} 会在 {@code Model.<init>} 中调用 {@code Minecraft.getInstance().getEntityModels()}，
 * 于是抛出 NullPointerException，Cataclysm 无法完成构造。
 * <p>
 * 这里在 {@code Minecraft.getInstance()} 为 null（即数据生成阶段，不存在渲染环境）时跳过 {@code ClientEvent.ClientEvent()}，
 * 正常客户端运行不受影响。该补丁只存在于 dev 源集，不会进入发布 jar。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "com.github.L_Ender.cataclysm.Cataclysm")
public abstract class CataclysmDataRunMixin {

    /**
     * 数据生成阶段不存在 Minecraft 实例，此时不注册 Cataclysm 的客户端事件
     */
    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/L_Ender/cataclysm/client/event/ClientEvent;ClientEvent()V"
        ),
        require = 0
    )
    private void wsb$skipClientEventInDataRun(Operation<Void> original) {
        if (Minecraft.getInstance() == null) {
            return;
        }
        original.call();
    }
}
