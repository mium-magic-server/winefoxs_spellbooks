package net.magicterra.winefoxsspellbooks.mixin.touhou_little_maid_spell;

import com.github.tartaricacid.touhoulittlemaid.api.animation.IMagicCastingState;
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 女仆的铁魔法施法动画只由本模组的动画状态驱动，万法皆通的铁魔法动画 provider 对女仆让出
 * <p>
 * 两边的动画状态都由同一个 Iron's 同步包更新，TLM 取优先级最高、给出动画的那个 provider。落选一方的瞬发阶段不会被消费，
 * 胜出一方播完后它会把同一个瞬发动画再播一遍。装了万法皆通时女仆的铁魔法施放全由本模组负责，所以让其 provider 对
 * {@link EntityMaid} 返回空状态；其万法酒狐 Boss 走另一个 provider，不受影响。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "com.github.yimeng261.maidspell.client.animation.ISSCastingAnimationProvider", remap = false)
public abstract class IssCastingAnimationProviderMixin {
    @Inject(method = "getMagicCastingState", at = @At("HEAD"), cancellable = true, require = 0)
    private void winefoxsSpellbooks$yieldMaidAnimation(IMaid maid, CallbackInfoReturnable<IMagicCastingState> cir) {
        if (maid.asEntity() instanceof EntityMaid) {
            cir.setReturnValue(null);
        }
    }
}
