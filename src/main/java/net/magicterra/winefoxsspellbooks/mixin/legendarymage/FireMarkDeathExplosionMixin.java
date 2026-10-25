package net.magicterra.winefoxsspellbooks.mixin.legendarymage;

import com.legendarymage.legendarymagemod.element.ElementMarkData;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.magicterra.winefoxsspellbooks.event.LegendaryMageExplosionGuard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 传奇法师带火焰标记的实体死亡时会以「无来源」方式触发爆炸，范围内所有实体（击杀者的主人、女仆、宠物乃至击杀者本人）一并受伤。
 * <p>
 * 这里包裹其 {@code onLivingDeath} 内的 {@code ServerLevel.explode} 调用，在调用期间登记责任方，
 * 由 {@link LegendaryMageExplosionGuard} 在爆炸的 {@code Detonate} 事件中把责任方一边从受影响列表剔除。
 * 责任方取致死伤害的责任实体、死者的 {@code getKillCredit()} 与火焰标记的施法者（{@link ElementMarkData#getCaster}），
 * 三者可能各自缺失，后者在死者靠持续灼烧死去、击杀记录已过期时仍然可用。
 * <p>
 * 本补丁待上游修复后移除，见 {@code docs/tentative/legendary-mage-fire-mark-explosion.md}。
 * 仅在传奇法师已加载时应用（{@code WsbMixinPlugin} 按包名 {@code legendarymage} 判定），并以 {@code require = 0} 兜底其内部改名。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "com.legendarymage.legendarymagemod.element.ElementReactionEvents", remap = false)
public abstract class FireMarkDeathExplosionMixin {
    @WrapOperation(method = "onLivingDeath(Lnet/neoforged/neoforge/event/entity/living/LivingDeathEvent;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"),
        require = 0)
    private static Explosion winefoxsSpellbooks$spareKillersSide(ServerLevel level, Entity source, double x, double y, double z, float power,
                                                                 Level.ExplosionInteraction interaction, Operation<Explosion> original,
                                                                 @Local(argsOnly = true) LivingDeathEvent event) {
        LivingEntity dying = event.getEntity();
        LegendaryMageExplosionGuard.enter("火焰标记死亡爆炸", event.getSource().getEntity(), dying.getKillCredit(),
            ElementMarkData.getCaster(level, dying, ElementType.FIRE));
        try {
            return original.call(level, source, x, y, z, power, interaction);
        } finally {
            // 无论爆炸是否抛异常都要清除，避免登记残留到同线程的后续爆炸
            LegendaryMageExplosionGuard.exit();
        }
    }
}
