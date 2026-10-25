package net.magicterra.winefoxsspellbooks.effect;

import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 狐影伏击 Foxshade Ambush — BENEFICIAL 标记效果，「蓄势待发」的引信。
 * <p>
 * 效果本身无属性修饰、不造成伤害，仅作为伏击触发器 （{@link net.magicterra.winefoxsspellbooks.event.FoxshadeAmbushHandler}）的持有标记： 持有者首次对敌人造成伤害时，处理器召出一只狐影撞咬目标并移除本效果； 若在时长内未出手则自然到期消失。逻辑全在处理器里，效果侧只负责「待发中」的环绕粒子提示。
 * <p>
 * amplifier 携带挂效时的法术强度与口数（编码见 {@link net.magicterra.winefoxsspellbooks.magic.spell.FoxshadeAmbushSpell#encodeAmplifier}），处理器据此换算狐影咬击伤害与连咬口数。 颜色取学派粉，与狐影弹配色一致。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-23
 */
public class FoxshadeAmbushEffect extends MobEffect {

    /**
     * 环绕粒子发射间隔(tick)；粒子仅是「待发中」的视觉提示，不必每 tick 广播。
     */
    private static final int PARTICLE_INTERVAL = 4;

    public FoxshadeAmbushEffect() {
        super(MobEffectCategory.BENEFICIAL, WinefoxHexHelper.SCHOOL_COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 「待发中」提示：绕持有者腰际撒一层学派粉狐火粒子。仅视觉，不改动实体状态。 返回 {@code true} 保持效果存在（本效果的移除由伏击处理器或自然到期负责）。
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 节流：粒子广播每次都要遍历附近玩家逐个发包，无须每 tick
        if (entity.tickCount % PARTICLE_INTERVAL == 0 && entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                WinefoxHexHelper.effectParticle(WinefoxHexHelper.SCHOOL_COLOR),
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                2, entity.getBbWidth() * 0.5, entity.getBbHeight() * 0.3, entity.getBbWidth() * 0.5, 0.0);
        }
        return true;
    }
}
