package net.magicterra.winefoxsspellbooks.mixin.legendarymage;

import com.legendarymage.legendarymagemod.element.ElementMarkData;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.MaidAllyHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 传奇法师「感电」效果的周期连锁闪电不分敌我，误伤施法者一方。
 * <p>
 * 雷电标记达到 3 级后移除时，承受者获得「感电」。其 {@code ElectrocutedBuffEffect.triggerChainLightning} 按间隔向周围
 * 8 格内最近的若干个非玩家活体各造成一次无来源的 {@code magic} 伤害，只排除玩家，不排除施法者的女仆、宠物与召唤物；
 * 女仆用雷系法术近身攻击时会被自己引发的感电反复命中直至死亡。
 * <p>
 * 这里包裹其中的 {@code getEntitiesOfClass} 调用，从候选列表中剔除与责任方同一召唤链（顶层主人相同）的实体，
 * 使友方既不受伤，也不占用连锁目标名额。承受者本身保留，沿用上游行为。责任方取雷电标记的施法者
 * （{@link ElementMarkData#getCaster}，标记移除后仍保存在承受者的持久化数据中）、承受者的 {@code getKillCredit()}
 * 与最近的攻击者，三者可能各自缺失，全部缺失时不做任何剔除。
 * <p>
 * 本补丁待上游修复后移除，见 {@code docs/tentative/legendary-mage-fire-mark-explosion.md}。
 * 仅在传奇法师已加载时应用（{@code WsbMixinPlugin} 按包名 {@code legendarymage} 判定），并以 {@code require = 0} 兜底其内部改名。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "com.legendarymage.legendarymagemod.effect.ElectrocutedBuffEffect", remap = false)
public abstract class ElectrocutedChainLightningMixin {
    @WrapOperation(method = "triggerChainLightning(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;I)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"),
        require = 0)
    private <T extends Entity> List<T> winefoxsSpellbooks$spareCastersSide(ServerLevel level, Class<T> clazz, AABB area, Predicate<? super T> predicate,
                                                                           Operation<List<T>> original,
                                                                           @Local(argsOnly = true) LivingEntity bearer) {
        List<T> candidates = original.call(level, clazz, area, predicate);
        List<Entity> parties = Stream.of(ElementMarkData.getCaster(level, bearer, ElementType.LIGHTNING), bearer.getKillCredit(), bearer.getLastHurtByMob())
            .filter(Objects::nonNull)
            .map(Entity.class::cast)
            .distinct()
            .toList();
        if (parties.isEmpty()) {
            return candidates;
        }
        // 上游随后会对列表排序并截取，返回的是可变的 ArrayList，可以原地移除
        int before = candidates.size();
        candidates.removeIf(entity -> entity != bearer && parties.stream().anyMatch(party -> MaidAllyHelper.isAllied(entity, party)));
        int spared = before - candidates.size();
        if (WinefoxsSpellbooks.DEBUG && spared > 0) {
            WinefoxsSpellbooks.LOGGER.info("传奇法师感电连锁闪电：已为 {} 豁免 {} 个友方实体", bearer.getName().getString(), spared);
        }
        return candidates;
    }
}
