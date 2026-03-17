package net.magicterra.winefoxsspellbooks.effect;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.CooldownInstance;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerCooldowns;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

/**
 * 狐火增幅效果 - 通过 COOLDOWN_REDUCTION 属性将冷却缩减到10%
 * <br>
 * 效果添加时同步缩减已有冷却
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-03-17
 */
public class FoxfireBoostEffect extends MobEffect {

    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "foxfire_boost");

    /**
     * COOLDOWN_REDUCTION 默认值为 1.0，加上 2.5 后为 3.5
     * softCapFormula(3.5) = -0.25/(3.5-1)+2 = 1.9
     * 冷却乘数 = 2 - 1.9 = 0.1（即10%冷却）
     */
    private static final double COOLDOWN_REDUCTION_BONUS = 2.5;

    public FoxfireBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF6600);
        addAttributeModifier(AttributeRegistry.COOLDOWN_REDUCTION, MODIFIER_ID,
                COOLDOWN_REDUCTION_BONUS, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        super.onEffectAdded(entity, amplifier);
        if (entity.level().isClientSide) {
            return;
        }
        // 缩减已有冷却到10%
        shrinkExistingCooldowns(entity);
    }

    /**
     * 将已有冷却的剩余时间缩减到10%
     */
    private void shrinkExistingCooldowns(LivingEntity entity) {
        PlayerCooldowns cooldowns = null;
        if (entity instanceof MaidMagicEntity maidMagicEntity) {
            cooldowns = maidMagicEntity.winefoxsSpellbooks$getMagicMaidAdapter().getMagicData().getPlayerCooldowns();
        } else if (entity instanceof Player player) {
            cooldowns = MagicData.getPlayerMagicData(player).getPlayerCooldowns();
        }
        if (cooldowns == null) {
            return;
        }
        for (CooldownInstance instance : cooldowns.getSpellCooldowns().values()) {
            int remaining = instance.getCooldownRemaining();
            // 缩减90%的剩余时间
            int toRemove = (int) (remaining * 0.9);
            if (toRemove > 0) {
                instance.decrementBy(toRemove);
            }
        }
    }
}
