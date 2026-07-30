package net.magicterra.winefoxsspellbooks.effect;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * 微醺 Tipsy。对所有持有者统一施加的「醉」减益: 移速 −30%、朝向逐 tick 抖动(踉跄观感)、水平乱走漂移、水青醉雾粒子。本效果<b>不造成伤害</b>。 强度 II(amplifier 1)时减速与抖动/漂移幅度加倍。 真正的「弹道打偏」由 {@link net.magicterra.winefoxsspellbooks.event.TipsyAimHandler} 在弹射物生成时实现。 朝向抖动与漂移仅对非玩家生效:玩家的移动与朝向由客户端权威,服务端写入不会同步, 玩家实际受「减速
 * + 弹道打偏」影响。本类不做玩家/女仆豁免。
 *
 * @author Gardel
 * @since 2026-06-06
 */
public class TipsyEffect extends MobEffect {

    private static final ResourceLocation SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "tipsy_speed");

    /**
     * 水青主色调:效果 tint 与醉雾粒子共用(与图标一致)。
     */
    private static final int TIPSY_COLOR = 0x4FB8D8;

    /**
     * 统一移速倍率增量(ADD_MULTIPLIED_TOTAL:−30%)。
     */
    private static final double SPEED = -0.30;

    /**
     * 水平乱走强度(deltaMovement)。
     */
    private static final double DRIFT = 0.02;

    /**
     * 醉雾粒子发射间隔(tick);粒子仅是视觉,不必每 tick 广播。
     */
    private static final int PARTICLE_INTERVAL = 5;

    public TipsyEffect() {
        super(MobEffectCategory.NEUTRAL, TIPSY_COLOR);
        // 减速走原版内建注册:所有增删/刷新路径自动挂卸,并按 ×(amplifier+1) 缩放。
        // 不要用 onEffectAdded 手动挂载——效果刷新(onEffectUpdated)会经 remove→re-add
        // 流程剥掉手动修饰符且不补回,减速会在第一次刷新后静默失效
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID, SPEED,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // 每 tick 都扰动
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return true;
        }
        double scale = 1.0 + amplifier;
        // 朝向扰动 + 水平乱走漂移:仅对非玩家(玩家朝向为客户端权威)
        if (!(entity instanceof Player)) {
            double yawDev = entity.getRandom().nextGaussian() * WinefoxHexHelper.TIPSY_YAW_DEG * scale;
            entity.setYRot(entity.getYRot() + (float) yawDev);
            entity.setXRot(Mth.clamp(entity.getXRot() + (float) (yawDev * WinefoxHexHelper.TIPSY_PITCH_RATIO),
                -90.0f, 90.0f));
            double gx = entity.getRandom().nextGaussian() * DRIFT * scale;
            double gz = entity.getRandom().nextGaussian() * DRIFT * scale;
            entity.setDeltaMovement(entity.getDeltaMovement().add(gx, 0.0, gz));
        }
        // —— 水青醉雾粒子(#4FB8D8 调性,与图标/tint 一致)——
        // 节流:sendParticles 每次都要遍历玩家列表并逐个发包,拥挤雾团里每 tick 广播开销可观
        if (entity.tickCount % PARTICLE_INTERVAL == 0 && entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(WinefoxHexHelper.effectParticle(TIPSY_COLOR),
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.8, entity.getZ(),
                2, 0.1, 0.1, 0.1, 0.0);
        }
        return true;
    }
}
