package net.magicterra.winefoxsspellbooks.mixin.legendarymage;

import com.legendarymage.legendarymagemod.element.ElementMarkData;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.magicterra.winefoxsspellbooks.event.LegendaryMageExplosionGuard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 传奇法师「狂焰」效果（毒 + 火元素反应施加）的实体死亡时，同样以「无来源」方式触发爆炸，误伤击杀者一方。
 * <p>
 * 包裹 {@code PyroFlameEffect.triggerExplosion} 内的 {@code Level.explode} 调用（仅由其 {@code ModEvents.onLivingDeath} 调用），
 * 做法与 {@link FireMarkDeathExplosionMixin} 相同。狂焰本身不记录施法者，责任方取致死伤害的来源、死者的 {@code getKillCredit()}，
 * 以及触发该反应的火、毒两种标记的施法者。
 * <p>
 * 本补丁待上游修复后移除，见 {@code docs/tentative/legendary-mage-fire-mark-explosion.md}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "com.legendarymage.legendarymagemod.effect.PyroFlameEffect", remap = false)
public abstract class PyroFlameDeathExplosionMixin {
    @WrapOperation(method = "triggerExplosion(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"),
        require = 0)
    private static Explosion winefoxsSpellbooks$spareKillersSide(Level level, Entity source, double x, double y, double z, float power,
                                                                 Level.ExplosionInteraction interaction, Operation<Explosion> original,
                                                                 @Local(argsOnly = true) LivingEntity dying) {
        DamageSource lastDamage = dying.getLastDamageSource();
        Entity fireCaster = null;
        Entity poisonCaster = null;
        if (level instanceof ServerLevel serverLevel) {
            fireCaster = ElementMarkData.getCaster(serverLevel, dying, ElementType.FIRE);
            poisonCaster = ElementMarkData.getCaster(serverLevel, dying, ElementType.POISON);
        }
        LegendaryMageExplosionGuard.enter("狂焰死亡爆炸", lastDamage != null ? lastDamage.getEntity() : null, dying.getKillCredit(),
            fireCaster, poisonCaster);
        try {
            return original.call(level, source, x, y, z, power, interaction);
        } finally {
            LegendaryMageExplosionGuard.exit();
        }
    }
}
