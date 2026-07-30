package net.magicterra.winefoxsspellbooks.client.particle;

import net.magicterra.winefoxsspellbooks.magic.particle.TransferStreamOption;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/**
 * 醒酒之礼净化发射器（客户端）。自身不渲染,服务端一包触发,围绕「滗酒去浊」分帧铺设:
 * <ul>
 *   <li>浊滴上滗:暗紫褐的酒渣尘从躯干浮出体外,数量正比于实际净除的效果数——
 *       法术做了多少事直接画出来;</li>
 *   <li>清醒环洗落:清白金光尘环从头顶沿身体轮廓扫到脚底(与浊滴上下对流);</li>
 *   <li>收尾:头顶金星一闪(「叮,醒了」),肩侧几颗酒泡散去酒意。</li>
 * </ul>
 * 满级整列净除时环更亮更密、金星 3 枚;所有位置逐帧从本地实体重算,目标移动也贴身。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-21
 */
public class DecantCleanseEmitter extends NoRenderParticle {

    /**
     * 清醒环从头扫到脚的帧数。
     */
    private static final int SWEEP_TICKS = 10;

    /**
     * 浊滴分摊在前几帧陆续浮出。
     */
    private static final int DRIP_TICKS = 6;

    /**
     * 浊滴数上限。
     */
    private static final int MAX_DRIPS = 12;

    /**
     * 清白金:醒后的清光。
     */
    private static final Vector3f CLEAN_COLOR = new Vector3f(245 / 255f, 233 / 255f, 206 / 255f);

    /**
     * 浊酒沉淀的暗紫褐。
     */
    private static final DustParticleOptions DREG_DUST =
        new DustParticleOptions(new Vector3f(92 / 255f, 58 / 255f, 80 / 255f), 0.8f);

    private final int targetId;

    private final int dripTotal;

    private final boolean purge;

    private final DustParticleOptions cleanDust;

    protected DecantCleanseEmitter(ClientLevel level, double x, double y, double z, TransferStreamOption option) {
        super(level, x, y, z);
        this.lifetime = SWEEP_TICKS;
        this.targetId = option.targetId();
        // time 携带实际净除的效果数;一个没净掉也出 1~2 粒意思一下,不显得法术坏了
        int cleansed = (int) option.time();
        this.dripTotal = cleansed <= 0 ? 2 : Math.min(cleansed * 2, MAX_DRIPS);
        this.purge = option.dist() >= 2;
        this.cleanDust = new DustParticleOptions(CLEAN_COLOR, this.purge ? 1.0f : 0.7f);
        emitFrame(0);
    }

    @Override
    public void tick() {
        if (++this.age >= this.lifetime) {
            this.remove();
            return;
        }
        emitFrame(this.age);
    }

    private void emitFrame(int n) {
        if (!(this.level.getEntity(this.targetId) instanceof LivingEntity target)) {
            return;
        }
        double feetY = target.getY();
        double height = target.getBbHeight();
        double bodyR = target.getBbWidth() * 0.5 + 0.25;

        // —— 清醒环:从头顶洗落到脚底,微微旋转,粒子带一点下坠速度加强「浇下」感 ——
        double sweepY = feetY + height * (1.0 - n / (double) (SWEEP_TICKS - 1));
        int ringCount = this.purge ? 14 : 10;
        for (int i = 0; i < ringCount; i++) {
            double a = Math.PI * 2 * i / ringCount + n * 0.25;
            this.level.addParticle(this.cleanDust,
                target.getX() + Math.cos(a) * bodyR, sweepY, target.getZ() + Math.sin(a) * bodyR,
                0, -0.03, 0);
        }

        // —— 浊滴上滗:前几帧从躯干陆续浮出,向外向上,像滗掉的酒渣 ——
        if (n < DRIP_TICKS) {
            int toSpawn = this.dripTotal * (n + 1) / DRIP_TICKS - this.dripTotal * n / DRIP_TICKS;
            for (int i = 0; i < toSpawn; i++) {
                double a = this.random.nextDouble() * Math.PI * 2;
                double y = feetY + height * (0.3 + this.random.nextDouble() * 0.4);
                this.level.addParticle(DREG_DUST,
                    target.getX() + Math.cos(a) * 0.25, y, target.getZ() + Math.sin(a) * 0.25,
                    Math.cos(a) * 0.04, 0.14, Math.sin(a) * 0.04);
            }
        }

        // —— 肩侧酒泡:酒意散去 ——
        if (n == 2) {
            for (int i = 0; i < 3; i++) {
                double a = this.random.nextDouble() * Math.PI * 2;
                this.level.addParticle(WsbParticles.WINE_BUBBLE.get(),
                    target.getX() + Math.cos(a) * bodyR * 0.8, feetY + height * 0.8,
                    target.getZ() + Math.sin(a) * bodyR * 0.8,
                    0, 0.02, 0);
            }
        }

        // —— 扫环到底:头顶金星一闪,满级 3 枚 ——
        if (n == SWEEP_TICKS - 1) {
            int stars = this.purge ? 3 : 1;
            for (int i = 0; i < stars; i++) {
                double dx = i == 0 ? 0 : (this.random.nextDouble() - 0.5) * 0.6;
                double dz = i == 0 ? 0 : (this.random.nextDouble() - 0.5) * 0.6;
                this.level.addParticle(
                    new TransferStreamOption(WsbParticles.GOLD_CROSS.get(), -1, 0, 0,
                        this.purge ? 1.3f : 1.0f),
                    target.getX() + dx, feetY + height + 0.35, target.getZ() + dz, 0, 0, 0);
            }
        }
    }

    /**
     * 无贴图,直接实例化,注册走 registerSpecial。
     */
    public static final class Provider implements ParticleProvider<TransferStreamOption> {
        @Override
        public Particle createParticle(TransferStreamOption option, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new DecantCleanseEmitter(level, x, y, z, option);
        }
    }
}
