package net.magicterra.winefoxsspellbooks.item;

import net.magicterra.winefoxsspellbooks.datagen.MaidEntityTagsProvider;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
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

    private static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.3f)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 0.6f)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 60), 0.6f)
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
                // 女仆：100%触发狐火增幅
                entity.addEffect(new MobEffectInstance(WsbEffects.FOXFIRE_BOOST, 200));
            } else {
                // 其他生物：60%概率触发魔力紊乱
                if (entity.getRandom().nextFloat() < 0.6f) {
                    entity.addEffect(new MobEffectInstance(WsbEffects.MANA_DISRUPTION, 200));
                }
            }
        }
        return result;
    }
}
