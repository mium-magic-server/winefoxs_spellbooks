package net.magicterra.winefoxsspellbooks.mixin;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * IMagicSummonMixin
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-29 01:32
 */
@Mixin(IMagicSummon.class)
public interface IMagicSummonMixin {
    @Inject(method = "isAlliedHelper",
        at = @At(value = "INVOKE_ASSIGN", target = "Lio/redspace/ironsspellbooks/entity/mobs/IMagicSummon;getSummoner()Lnet/minecraft/world/entity/Entity;", ordinal = 0),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION,
        cancellable = true
    )
    default void injectIsAlliedHelper(Entity entity, CallbackInfoReturnable<Boolean> cir, Entity owner) {
        if (owner != null && owner.equals(entity)) {
            cir.setReturnValue(true);
        }
    }
}
