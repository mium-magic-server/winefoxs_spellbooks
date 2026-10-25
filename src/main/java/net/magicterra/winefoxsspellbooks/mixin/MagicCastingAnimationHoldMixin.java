package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.AnimationManager;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.AnimationState;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.controller.AnimationController;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 施法结束后不再无限保持循环动画。
 * <p>
 * TLM 的 {@code predicateMagicCastingAnimation} 在施法阶段回到 NONE 时，若上一阶段是 INSTANT 或 END 且控制器尚未 STOPPED，
 * 会继续播放当前动画，等其自然播完。持续施法法术（Iron's 的电击、星落等）的起手动画是循环动画，收招动画为 {@code pass()}，
 * 收招阶段没有新动画替换它；模型缺少某个瞬发动画时 {@code setAnimation} 同样不生效，前一个循环动画原样保留。
 * 循环动画永远不会播完，女仆就一直保持施法姿势，直到下一次带动画的施法。
 * <p>
 * 这里修改上述判断里读取的控制器状态（方法内第一处 {@code getAnimationState()}）：当前动画为循环播放时视作已停止，
 * 让 TLM 照常走到 STOP 分支清除动画。只播一次的收招、瞬发动画仍等其播完，不受影响。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Mixin(AnimationManager.class)
public abstract class MagicCastingAnimationHoldMixin {
    @ModifyExpressionValue(method = "predicateMagicCastingAnimation(Lcom/github/tartaricacid/touhoulittlemaid/geckolib3/core/event/predicate/AnimationEvent;)Lcom/github/tartaricacid/touhoulittlemaid/geckolib3/core/PlayState;",
        at = @At(value = "INVOKE",
            target = "Lcom/github/tartaricacid/touhoulittlemaid/geckolib3/core/controller/AnimationController;getAnimationState()Lcom/github/tartaricacid/touhoulittlemaid/geckolib3/core/AnimationState;",
            ordinal = 0))
    private AnimationState winefoxsSpellbooks$releaseLoopingHold(AnimationState original, @Local AnimationController<?> controller) {
        if (original == AnimationState.STOPPED || controller.getCurrentAnimation() == null) {
            return original;
        }
        ILoopType loopType = ((TlmAnimationControllerAccessor) controller).winefoxsSpellbooks$getCurrentAnimationLoop();
        return loopType != null && loopType.isRepeatingAfterEnd() ? AnimationState.STOPPED : original;
    }
}
