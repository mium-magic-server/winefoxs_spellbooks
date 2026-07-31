package net.magicterra.winefoxsspellbooks.client.particle;

import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.magic.particle.CrescentBrandOption;
import net.magicterra.winefoxsspellbooks.magic.particle.CrescentBurstOption;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

/**
 * 残月引爆发射器（客户端）。自身不渲染,服务端一包触发,分帧铺完整套引爆视觉:
 * <ul>
 *   <li>第 0 帧:胸口白金闪光 + 十余枚月牙碎片沿月弧甩出 + 数颗酒泡点缀;</li>
 *   <li>第 0..7 帧:血红/酒粉相间的尘粒环波从脚下扩散到实际 AoE 半径——
 *       把伤害范围画在地上,女仆引爆半径更大也随之可视化。</li>
 * </ul>
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-21
 */
public class CrescentBurstEmitter extends NoRenderParticle {

    /**
     * 环波扩散帧数。
     */
    private static final int WAVE_TICKS = 8;

    /**
     * 引爆甩出的碎片数。
     */
    private static final int SHARD_COUNT = 14;

    /**
     * 碎片弧的张角(弧度),约 240°。
     */
    private static final double ARC_SPAN = Math.PI * 4 / 3;

    private static final DustParticleOptions BLOOD_DUST =
        new DustParticleOptions(new Vector3f(200 / 255f, 32 / 255f, 58 / 255f), 1.0f);

    /**
     * 酒色尘粒取学派主色，不另写字面量——学派配色改动时这里自动跟随。
     */
    private static final DustParticleOptions WINE_DUST =
        new DustParticleOptions(WinefoxHexHelper.SCHOOL_COLOR_VEC, 1.0f);

    private final double radius;

    protected CrescentBurstEmitter(ClientLevel level, double x, double y, double z, CrescentBurstOption option) {
        super(level, x, y, z);
        this.lifetime = WAVE_TICKS;
        this.radius = Math.max(option.radius(), 1);

        double chestY = y + 0.9;
        // 闪光
        this.level.addParticle(brandOption(CrescentBrandOption.MODE_FLASH), x, chestY, z, 0, 0, 0);
        // 月牙碎片:沿随机朝向的月弧甩出,略带上抛
        double arcStart = this.random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < SHARD_COUNT; i++) {
            double a = arcStart + ARC_SPAN * i / (SHARD_COUNT - 1);
            double speed = 0.26 + this.random.nextDouble() * 0.16;
            this.level.addParticle(brandOption(CrescentBrandOption.MODE_SHARD), x, chestY, z,
                Math.cos(a) * speed, 0.12 + this.random.nextDouble() * 0.1, Math.sin(a) * speed);
        }
        // 酒泡点缀,把「酒狐」主题串起来
        for (int i = 0; i < 3; i++) {
            this.level.addParticle(WsbParticles.WINE_BUBBLE.get(),
                x + this.random.nextGaussian() * 0.3, y + 0.3, z + this.random.nextGaussian() * 0.3,
                0, 0.02, 0);
        }
        emitWave(0);
    }

    private static CrescentBrandOption brandOption(int mode) {
        return new CrescentBrandOption(WsbParticles.CRESCENT_BRAND.get(), mode, 0, 0, 0);
    }

    @Override
    public void tick() {
        if (++this.age >= WAVE_TICKS) {
            this.remove();
            return;
        }
        emitWave(this.age);
    }

    /**
     * 第 n 帧环波:半径线性扩散到 AoE 边界,血红/酒粉相间。
     */
    private void emitWave(int n) {
        double r = this.radius * (n + 1) / WAVE_TICKS;
        int count = Math.max(10, (int) (Math.PI * 2 * r / 0.45));
        for (int i = 0; i < count; i++) {
            double a = Math.PI * 2 * i / count;
            this.level.addParticle(i % 2 == 0 ? BLOOD_DUST : WINE_DUST,
                this.x + Math.cos(a) * r, this.y + 0.15, this.z + Math.sin(a) * r,
                0, 0.01, 0);
        }
    }

    /**
     * 无贴图,直接实例化,注册走 registerSpecial。
     */
    public static final class Provider implements ParticleProvider<CrescentBurstOption> {
        @Override
        public Particle createParticle(CrescentBurstOption option, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new CrescentBurstEmitter(level, x, y, z, option);
        }
    }
}
