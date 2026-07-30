package net.magicterra.winefoxsspellbooks.item;

import net.magicterra.winefoxsspellbooks.datagen.MaidEntityTagsProvider;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * 残月血酿 - 可饮用物品
 * <br>
 * 女仆饮用触发狐火增幅（冷却缩减），其他生物触发魔力紊乱
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-03-13
 */
public class CrescentBloodVintage extends Item {

    /**
     * 非女仆饮用时每种副作用的触发概率。
     */
    private static final float SIDE_EFFECT_CHANCE = 0.6f;

    private static final FoodProperties FOOD = new FoodProperties.Builder()
        .nutrition(3)
        .saturationModifier(0.3f)
        .alwaysEdible()
        .build();

    public CrescentBloodVintage() {
        super(new Properties().food(FOOD).stacksTo(16));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            if (entity.getType().is(MaidEntityTagsProvider.MAID_TAG)) {
                entity.addEffect(new MobEffectInstance(WsbEffects.FOXFIRE_BOOST, 200));
            } else {
                maybeApply(entity, MobEffects.CONFUSION, 200);
                maybeApply(entity, MobEffects.POISON, 60);
                maybeApply(entity, WsbEffects.MANA_DISRUPTION, 200);
            }
        }
        return result;
    }

    private static void maybeApply(LivingEntity entity, Holder<MobEffect> effect, int duration) {
        if (entity.getRandom().nextFloat() < SIDE_EFFECT_CHANCE) {
            entity.addEffect(new MobEffectInstance(effect, duration));
        }
    }
}
