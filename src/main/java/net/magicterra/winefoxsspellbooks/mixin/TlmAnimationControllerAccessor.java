package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.controller.AnimationController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入 TLM 内置 GeckoLib 的动画控制器，开放当前动画实际采用的循环方式。
 * <p>
 * {@code Animation.loop} 只是动画文件里的默认值，动画序列中的每一段可以另行指定，控制器实际按 {@code currentAnimationLoop} 播放。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Mixin(AnimationController.class)
public interface TlmAnimationControllerAccessor {
    @Accessor("currentAnimationLoop")
    ILoopType winefoxsSpellbooks$getCurrentAnimationLoop();
}
