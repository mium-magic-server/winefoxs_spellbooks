package net.magicterra.winefoxsspellbooks.mixin;

import io.redspace.ironsspellbooks.effect.InstantManaEffect;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 瞬间法力对女仆改写入真实魔力池
 * <p>
 * Iron's 经 {@code MagicData.getPlayerMagicData} 加蓝，对女仆返回的是无人读写的孤儿附件，加蓝落空；
 * 女仆的真实魔力在 {@code MagicMaidAdapter} 里，须经 {@link MaidMagicManager#setMana} 写入。
 * 加蓝量沿用 Iron's 公式（每级 25 点 + 魔力上限的 5%），上限取女仆自己的魔力上限（含倍率）。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-26
 */
@Mixin(InstantManaEffect.class)
public class InstantManaEffectMixin {
    @Inject(method = "applyInstantenousEffect", at = @At("HEAD"), cancellable = true)
    private void winefoxsSpellbooks$applyToMaid(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entity,
                                                int amplifier, double health, CallbackInfo ci) {
        if (!(entity instanceof MaidMagicEntity)) {
            return;
        }
        ci.cancel();
        // 魔力紊乱期间禁止一切回魔（与 MaidMagicManager.regenMana、酒池的门禁一致）
        if (entity.hasEffect(WsbEffects.MANA_DISRUPTION)) {
            return;
        }
        int level = amplifier + 1;
        double max = MaidMagicManager.getMaxMana(entity);
        float add = (float) (level * InstantManaEffect.manaPerAmplifier + max * level * InstantManaEffect.manaPerAmplifierPercent);
        MaidMagicManager.setMana(entity, (float) Math.min(MaidMagicManager.getMana(entity) + add, max));
    }
}
