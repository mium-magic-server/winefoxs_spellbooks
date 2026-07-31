package net.magicterra.winefoxsspellbooks.effect;

import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.magic.particle.TransferStreamOption;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 残月烙印效果 — HARMFUL 标记效果，自然到期时触发「弱自爆」。
 * <p>
 * 平时无 tick 逻辑，仅作为引爆系统（{@link net.magicterra.winefoxsspellbooks.event.WinefoxDetonationHandler}） 的标记；到期最后一 tick（{@code duration==1}）对自身及周围小范围造成少量伤害。 被引爆（提前 {@code removeEffect}）的烙印到不了 {@code duration==1}，不会自爆， 天然区分「引爆」与「自然到期」；不用
 * {@code MobEffectEvent.Expired} 是因其触发时机不可靠。
 * <p>
 * 弱自爆用原版 {@code magic} 伤害而非本学派伤害：引爆监听器按学派伤害 type 识别， {@code magic} 不在其列，弱自爆便不会自引爆、也不会连锁周围带印实体。
 * <p>
 * 颜色 #C8203A 取残月血色，与图标一致。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
public class CrescentBrandEffect extends MobEffect {

    public CrescentBrandEffect() {
        super(MobEffectCategory.HARMFUL, 0xC8203A);
    }

    /**
     * 绕身印记的服务端补发周期(tick);客户端粒子同寿命,新旧相位无缝接续。
     */
    private static final int ORBIT_RESEND_TICKS = 20;

    /**
     * 无限时长烙印补发时充当的「剩余时长」,只要大于临期阈值即可(不触发脉动加速)。
     */
    private static final int INFINITE_REMAIN_STUB = 6000;

    /**
     * 两个触发点:剩余 1 tick 时的自然到期弱自爆;每 20 tick 一次的绕身印记补发。 到期最后一 tick 时 {@link MobEffectInstance#tick} 尚未将 duration 递减到 0,效果仍然存在。
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration == 1 || duration % ORBIT_RESEND_TICKS == 0;
    }

    /**
     * 挂印瞬间:胸口落印(盖章式收缩) + 立即补发第一枚绕身印记,不等下个补发周期。
     */
    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
            brandOption(0, entity.getId(), 0, amplifier),
            entity.getX(), entity.getY(), entity.getZ(), 1, 0, 0, 0, 0);
        // 时长恰为补发周期倍数时(法术挂印即是),效果 tick 马上会补发,这里不重复发
        MobEffectInstance instance = entity.getEffect(WsbEffects.CRESCENT_BRAND);
        if (instance == null || instance.isInfiniteDuration()
            || instance.getDuration() % ORBIT_RESEND_TICKS != 0) {
            sendOrbit(serverLevel, entity, amplifier);
        }
    }

    /**
     * 自然到期弱自爆：对带印者本人及其小范围内的敌对实体造成少量原版 magic 伤害。 返回 {@code true} 保持原版语义（返回 {@code false} 会导致提前移除，此处无意义）。
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 仅服务端施加伤害；客户端 tick 只用于渲染
        if (entity.level().isClientSide) return true;

        MobEffectInstance instance = entity.getEffect(WsbEffects.CRESCENT_BRAND);
        // 绕身印记补发(剩余 >1 tick 的周期触发都是补发,不是到期)
        if (instance != null && (instance.isInfiniteDuration() || instance.getDuration() > 1)) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                sendOrbit(serverLevel, entity, amplifier);
            }
            return true;
        }

        // 走到这里必然是剩余 1 tick 的自然到期(无限时长在上方分支已返回——原版对无限实例
        // 传给 shouldApplyEffectTickThisTick 的是 entity.tickCount,重载后 tickCount==1 会误判到期)。
        DamageSource src = entity.damageSources().magic();
        AABB box = entity.getBoundingBox().inflate(WinefoxHexHelper.WEAK_SELF_RADIUS);
        // 自爆以带印者为中心：他本人必伤，周围只波及「敌对生物且与他不同阵营」的。
        // 阵营判定必须再叠一层敌对判定：印通常挂在敌人身上，而站在敌人旁边的玩家/女仆
        // 相对这个敌人正是「不同阵营」，只用阵营谓词会炸到上印的人自己。
        for (LivingEntity e : entity.level().getEntitiesOfClass(LivingEntity.class, box,
            victim -> victim == entity
                || (WinefoxHexHelper.isHexEnemy(victim) && WinefoxHexHelper.isFactionHostile(victim, entity)))) {
            e.hurt(src, WinefoxHexHelper.WEAK_SELF_DAMAGE);
        }
        return true;
    }

    /**
     * 补发一枚绕身印记粒子;time 携带当前剩余时长(客户端脉动/公转时基)。
     */
    private static void sendOrbit(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        MobEffectInstance instance = entity.getEffect(WsbEffects.CRESCENT_BRAND);
        int remaining = instance == null ? INFINITE_REMAIN_STUB
                                         : (instance.isInfiniteDuration() ? INFINITE_REMAIN_STUB : instance.getDuration());
        serverLevel.sendParticles(
            brandOption(1, entity.getId(), remaining, amplifier),
            entity.getX(), entity.getY(), entity.getZ(), 1, 0, 0, 0, 0);
    }

    private static TransferStreamOption brandOption(int mode, int entityId, int remaining, int amplifier) {
        return new TransferStreamOption(WsbParticles.CRESCENT_BRAND.get(), mode, entityId, remaining, amplifier);
    }
}
