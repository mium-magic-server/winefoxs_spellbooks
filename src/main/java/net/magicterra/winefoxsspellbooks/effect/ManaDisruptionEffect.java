package net.magicterra.winefoxsspellbooks.effect;

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
        // tickEffects 双端都跑：客户端自己也扣一遍的话，读数会持续低于服务端直到下一个同步包
        if (entity.level().isClientSide) {
            return true;
        }
        // 读写一律走 MaidMagicManager 的路由：女仆落到适配器，玩家落到 MagicData 并补发同步包。
        // 自行分支写 MagicData 会漏掉那一发包，客户端魔力条从此停在旧值
        if (entity instanceof MaidMagicEntity || entity instanceof Player) {
            float drain = (float) MaidMagicManager.getMaxMana(entity) * 0.01f;
            MaidMagicManager.setMana(entity, Math.max(0, MaidMagicManager.getMana(entity) - drain));
        }
        return true;
    }
}
