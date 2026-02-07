package net.magicterra.winefoxsspellbooks.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * IMagicSummonMixin
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-29 01:32
 */
@Mixin(IMagicSummon.class)
public interface IMagicSummonMixin {
    @Definition(id = "getSummoner", method = "Lio/redspace/ironsspellbooks/entity/mobs/IMagicSummon;getSummoner()Lnet/minecraft/world/entity/Entity;")
    @Expression("? = ?.getSummoner()")
    @Inject(method = "isAlliedHelper",
        at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER),
        cancellable = true
    )
    default void injectIsAlliedHelper(Entity entity, CallbackInfoReturnable<Boolean> cir, @Local(name = "owner") Entity owner) {
        if (owner != null && owner.equals(entity)) {
            cir.setReturnValue(true);
        }
    }
}
