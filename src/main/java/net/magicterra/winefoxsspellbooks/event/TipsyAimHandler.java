package net.magicterra.winefoxsspellbooks.event;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * 微醺 Tipsy 的弹道偏转——真·打偏而非视觉抖动。当「持有 Tipsy 者」发射的弹射物加入世界时, 按高斯随机角旋转其速度向量(模长守恒),让攻击/法术弹射物打偏。
 *
 * <p>覆盖原版箭、三叉戟、Iron's Spellbooks 法术弹射物及投掷物；不区分敌我。</p>
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-21
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class TipsyAimHandler {

    /**
     * 初速过小(放置/追踪类)不偏转的阈值。
     */
    private static final double MIN_SPEED_SQR = 1.0E-4;

    @SubscribeEvent
    public static void onProjectileSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return; // 仅服务端;速度变更经实体追踪同步客户端
        }
        if (event.loadedFromDisk()) {
            return; // 只偏转新发射的弹射物;区块重载/重登回读的存留弹射物不再二次偏转
        }
        if (!(event.getEntity() instanceof Projectile proj)) {
            return;
        }
        if (!(proj.getOwner() instanceof LivingEntity owner)) {
            return;
        }
        MobEffectInstance tipsy = owner.getEffect(WsbEffects.TIPSY);
        if (tipsy == null) {
            return;
        }
        Vec3 v = proj.getDeltaMovement();
        double speedSqr = v.lengthSqr();
        if (speedSqr < MIN_SPEED_SQR) {
            return; // 初速≈0:追踪弹/放置类,偏转无意义
        }
        double speed = Math.sqrt(speedSqr);
        RandomSource rng = owner.getRandom();
        // 偏转幅度按 amplifier 线性放大(II 级 = 2 倍),与效果侧的抖动/漂移缩放一致
        double scale = 1.0 + tipsy.getAmplifier();
        // 速度向量分解为 (yaw, pitch, speed),加高斯扰动后还原 —— 单位方向向量保证模长守恒
        double yaw = Math.atan2(v.z, v.x);
        double pitch = Math.asin(Mth.clamp(v.y / speed, -1.0, 1.0));
        yaw += Math.toRadians(rng.nextGaussian() * WinefoxHexHelper.TIPSY_YAW_DEG * scale);
        pitch += Math.toRadians(rng.nextGaussian() * WinefoxHexHelper.TIPSY_YAW_DEG
            * WinefoxHexHelper.TIPSY_PITCH_RATIO * scale);
        pitch = Mth.clamp(pitch, -Math.PI / 2.0, Math.PI / 2.0);
        double cosPitch = Math.cos(pitch);
        Vec3 dir = new Vec3(cosPitch * Math.cos(yaw), Math.sin(pitch), cosPitch * Math.sin(yaw));
        // 原版 shoot 负责 setDeltaMovement + 渲染朝向同步(inaccuracy=0 → 严格沿 dir,模长守恒)
        proj.shoot(dir.x, dir.y, dir.z, (float) speed, 0.0f);
    }
}
