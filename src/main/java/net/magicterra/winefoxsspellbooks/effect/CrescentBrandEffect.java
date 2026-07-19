package net.magicterra.winefoxsspellbooks.effect;

import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 残月烙印效果 — HARMFUL 标记效果，自然到期时触发「弱自爆」。
 * <p>
 * 平时无 tick 逻辑，仅作为引爆系统（{@link net.magicterra.winefoxsspellbooks.event.WinefoxDetonationHandler}）
 * 的标记；到期最后一 tick（{@code duration==1}）对自身及周围小范围造成少量伤害。
 * 被引爆（提前 {@code removeEffect}）的烙印到不了 {@code duration==1}，不会自爆，
 * 天然区分「引爆」与「自然到期」；不用 {@code MobEffectEvent.Expired} 是因其触发时机不可靠。
 * <p>
 * 弱自爆用原版 {@code magic} 伤害而非本学派伤害：引爆监听器按学派伤害 type 识别，
 * {@code magic} 不在其列，弱自爆便不会自引爆、也不会连锁周围带印实体。
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
     * 仅在效果自然到期的最后一 tick（剩余时长为 1）执行 tick 逻辑。
     * 此时 {@link net.minecraft.world.effect.MobEffectInstance#tick} 尚未将 duration 递减到 0，效果仍然存在。
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration == 1;
    }

    /**
     * 自然到期弱自爆：对附近小范围内的存活实体（含自身）造成少量原版 magic 伤害。
     * 返回 {@code true} 保持原版语义（返回 {@code false} 会导致提前移除，此处无意义）。
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 仅服务端施加伤害；客户端 tick 只用于渲染
        if (entity.level().isClientSide) return true;

        // 原版对无限时长(-1)实例传给 shouldApplyEffectTickThisTick 的是 entity.tickCount 而非剩余时长，
        // 实体每次重新加载后 tickCount==1 会误判为「到期」；无限烙印（仅指令/数据包可施加）不自爆
        MobEffectInstance instance = entity.getEffect(WsbEffects.CRESCENT_BRAND);
        if (instance != null && instance.isInfiniteDuration()) {
            return true;
        }

        // 非学派 magic 伤害：引爆系统按学派伤害 type 判定，magic 不在其列 → 弱自爆天然「无连锁、不自引爆」。
        // 原版伤害路径对所有目标(含爆心自身)一视同仁,不经 Iron's applyDamage 的「施法者打自己」拦截。
        DamageSource src = entity.damageSources().magic();
        AABB box = entity.getBoundingBox().inflate(WinefoxHexHelper.WEAK_SELF_RADIUS);
        for (LivingEntity e : entity.level().getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            e.hurt(src, WinefoxHexHelper.WEAK_SELF_DAMAGE);
        }
        return true;
    }
}
