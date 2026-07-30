package net.magicterra.winefoxsspellbooks.effect;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.registry.WsbAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 酒涌 Brewsurge — 引爆残月烙印时为指挥者叠层刷新的增益
 * <br>
 * 每层通过 WINEFOX_HEX_SPELL_POWER 属性 +10% bouquet（按 amplifier+1 缩放，3 层封顶 = +30%）
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
public class BrewsurgeEffect extends MobEffect {

    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "brewsurge_bouquet");

    /**
     * 每层 bouquet 加成（ADD_MULTIPLIED_TOTAL，vanilla 按 amplifier+1 缩放）
     */
    private static final double BOUQUET_BONUS_PER_STACK = 0.10;

    public BrewsurgeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE8A030);
        addAttributeModifier(WsbAttributes.WINEFOX_HEX_SPELL_POWER, MODIFIER_ID,
            BOUQUET_BONUS_PER_STACK, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
