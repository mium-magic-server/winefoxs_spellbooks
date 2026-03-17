package net.magicterra.winefoxsspellbooks.effect;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 魔力紊乱效果 - 禁用魔力恢复，每10tick消耗1%最大魔力
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-03-13
 */
public class ManaDisruptionEffect extends MobEffect {

    public ManaDisruptionEffect() {
        super(MobEffectCategory.HARMFUL, 0x6A0DAD);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof MaidMagicEntity maidMagicEntity) {
            // 女仆侧：消耗1%最大魔力
            float maxMana = (float) MaidMagicManager.getMaxMana(entity);
            float drain = maxMana * 0.01f;
            float currentMana = maidMagicEntity.winefoxsSpellbooks$getMana();
            maidMagicEntity.winefoxsSpellbooks$setMana(Math.max(0, currentMana - drain));
        } else if (entity instanceof Player player) {
            // 玩家侧：消耗1%最大魔力
            MagicData magicData = MagicData.getPlayerMagicData(player);
            float maxMana = (float) MaidMagicManager.getMaxMana(player);
            float drain = maxMana * 0.01f;
            float newMana = Math.max(0, magicData.getMana() - drain);
            magicData.setMana(newMana);
        }
        return true;
    }
}
