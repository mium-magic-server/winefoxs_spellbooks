package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.item.VialsContents;
import net.magicterra.winefoxsspellbooks.item.VixensFlightItem;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 女仆战斗中从背包里的品酒排挑一支此刻该喝的酒签
 * <p>
 * 每排从右到左找，跳过此刻不该喝的；只喝不含有害效果、且至少有一种效果此刻用得上的酒签。药水与铁魔法的药剂同样按效果判断。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class MaidVialPicker {
    /**
     * 生命低于这个比例才喝治疗类
     */
    public static final float HEAL_HEALTH_RATIO = 0.6f;

    /**
     * 身上同种效果剩余不足这么多 tick 时视为需要补喝
     */
    private static final int REFRESH_TICKS = 200;

    /**
     * 治疗类：只在低血时喝，喝时后撤
     */
    private static final Set<Holder<MobEffect>> HEALING = Set.of(MobEffects.HEAL, MobEffects.REGENERATION, MobEffects.ABSORPTION);

    /**
     * 对战斗中的女仆没有用处的增益，单独出现时不喝
     */
    private static final Set<Holder<MobEffect>> USELESS = Set.of(MobEffects.NIGHT_VISION, MobEffects.LUCK, MobEffects.SLOW_FALLING,
        MobEffects.DOLPHINS_GRACE, MobEffects.CONDUIT_POWER, MobEffects.HERO_OF_THE_VILLAGE);

    private MaidVialPicker() {
    }

    /**
     * 选中的酒签
     *
     * @param slot    品酒排在 {@link EntityMaid#getAvailableBackpackInv()} 中的格子
     * @param vial    酒签下标
     * @param healing 是否治疗类（喝时后撤）
     */
    public record Pick(int slot, int vial, boolean healing) {
    }

    public static Optional<Pick> find(EntityMaid maid) {
        IItemHandler backpack = maid.getAvailableBackpackInv();
        for (int slot = 0; slot < backpack.getSlots(); slot++) {
            ItemStack stack = backpack.getStackInSlot(slot);
            if (!(stack.getItem() instanceof VixensFlightItem)) {
                continue;
            }
            VialsContents vials = VixensFlightItem.vials(stack);
            for (int i = VialsContents.SIZE - 1; i >= 0; i--) {
                if (vials.isEmpty(i)) {
                    continue;
                }
                List<MobEffectInstance> effects = VialsContents.effects(vials.get(i));
                if (wanted(maid, effects)) {
                    return Optional.of(new Pick(slot, i, isHealing(effects)));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean wanted(LivingEntity entity, List<MobEffectInstance> effects) {
        boolean needed = false;
        for (MobEffectInstance effect : effects) {
            if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                return false;
            }
            needed |= needs(entity, effect.getEffect());
        }
        // 水瓶、无效果的药水没有效果可遍历，自然不喝
        return needed;
    }

    private static boolean needs(LivingEntity entity, Holder<MobEffect> effect) {
        if (HEALING.contains(effect)) {
            return entity.getHealth() < entity.getMaxHealth() * HEAL_HEALTH_RATIO;
        }
        if (effect.equals(MobEffects.FIRE_RESISTANCE)) {
            return (entity.isOnFire() || entity.isInLava()) && expiring(entity, effect);
        }
        if (effect.equals(MobEffects.WATER_BREATHING)) {
            return entity.isInWater() && entity.getAirSupply() < entity.getMaxAirSupply() && expiring(entity, effect);
        }
        if (USELESS.contains(effect) || effect.value().getCategory() != MobEffectCategory.BENEFICIAL) {
            return false;
        }
        return expiring(entity, effect);
    }

    private static boolean expiring(LivingEntity entity, Holder<MobEffect> effect) {
        MobEffectInstance current = entity.getEffect(effect);
        return current == null || current.endsWithin(REFRESH_TICKS);
    }

    private static boolean isHealing(List<MobEffectInstance> effects) {
        for (MobEffectInstance effect : effects) {
            if (HEALING.contains(effect.getEffect())) {
                return true;
            }
        }
        return false;
    }
}
