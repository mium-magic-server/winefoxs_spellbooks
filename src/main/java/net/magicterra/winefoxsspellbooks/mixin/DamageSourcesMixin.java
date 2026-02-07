package net.magicterra.winefoxsspellbooks.mixin;

import io.redspace.ironsspellbooks.damage.DamageSources;
import net.magicterra.winefoxsspellbooks.magic.MaidSummonManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复嵌套召唤物之间的友军伤害判断
 * <p>
 * 原版 DamageSources.isFriendlyFireBetween() 只追溯一层召唤者，
 * 对于嵌套召唤（玩家 → 女仆 → 召唤物）无法正确判断。
 * 此 Mixin 在原方法执行前检查是否属于同一召唤链。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-31
 */
@Mixin(DamageSources.class)
public class DamageSourcesMixin {

    /**
     * 在 isFriendlyFireBetween 方法开始时检查召唤链
     * <p>
     * 如果两个实体属于同一召唤链，直接返回 true（是友军伤害，应该阻止）
     */
    @Inject(method = "isFriendlyFireBetween", at = @At("HEAD"), cancellable = true)
    private static void checkSummonChain(Entity attacker, Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (MaidSummonManager.isSameSummonChain(attacker, target)) {
            cir.setReturnValue(true);
        }
    }
}
