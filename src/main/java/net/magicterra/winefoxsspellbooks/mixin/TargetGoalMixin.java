package net.magicterra.winefoxsspellbooks.mixin;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修改是否能攻击逻辑，防止女仆召唤物攻击玩家
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-31 01:04
 */
@Mixin(TargetGoal.class)
public abstract class TargetGoalMixin {
    @Final
    @Shadow
    protected Mob mob;

    @Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
    public void beforeCanAttack(LivingEntity potentialTarget, TargetingConditions targetPredicate, CallbackInfoReturnable<Boolean> cir) {
        if (mob == null || potentialTarget == null) {
            return;
        }
        Entity owner = SummonManager.getOwner(mob);
        if (owner != null) {
            LivingEntity livingEntity = (LivingEntity) owner;
            if (!livingEntity.canAttack(potentialTarget)) {
                cir.setReturnValue(false);
            }
        }
    }
}
