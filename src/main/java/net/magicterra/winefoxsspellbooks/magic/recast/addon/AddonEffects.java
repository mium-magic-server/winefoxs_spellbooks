package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

/**
 * 按 id 字符串查附属模组状态效果的小工具。
 * <p>
 * 走注册表而非反射：效果本来就在动态注册表里，用 {@link ResourceLocation} 查即可， 不需要碰模组的类。模组没装时查不到，静默返回——这是正常情况，不报错。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
final class AddonEffects {
    private AddonEffects() {
    }

    /**
     * 目标身上是否挂着该 id 的效果。id 无效或效果未注册时返回 false。
     */
    static boolean has(LivingEntity entity, String effectId) {
        return withEffect(entity, effectId, (holder, registry) -> entity.hasEffect(registry.wrapAsHolder(holder)));
    }

    /**
     * 摘掉该 id 的效果。id 无效或效果未注册时什么也不做。
     */
    static void remove(LivingEntity entity, String effectId) {
        withEffect(entity, effectId, (holder, registry) -> entity.removeEffect(registry.wrapAsHolder(holder)));
    }

    private interface EffectAction {
        boolean apply(MobEffect effect, net.minecraft.core.Registry<MobEffect> registry);
    }

    private static boolean withEffect(LivingEntity entity, String effectId, EffectAction action) {
        ResourceLocation id = ResourceLocation.tryParse(effectId);
        if (id == null) {
            return false;
        }
        var registry = entity.level().registryAccess().registryOrThrow(Registries.MOB_EFFECT);
        MobEffect effect = registry.get(id);
        return effect != null && action.apply(effect, registry);
    }
}
