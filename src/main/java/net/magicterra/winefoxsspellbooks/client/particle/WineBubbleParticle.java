package net.magicterra.winefoxsspellbooks.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 酒泡粒子：模仿酒液里升起的肥皂泡。
 * <p>
 * 缓慢上浮（浮力逐 tick 加速至上限）、左右正弦摇摆、随高度轻微胀大， 生命末段或随机时机切到破裂帧「啪」地消失。半透明泡壁贴图 + 全亮度渲染， 夜里酒池上也看得见。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-19
 */
public class WineBubbleParticle extends TextureSheetParticle {

    /**
     * 破裂帧占生命末段的 tick 数。
     */
    private static final int POP_TICKS = 2;

    /**
     * 上浮终端速度(格/tick);连同寿命决定飘升高度,典型 0.6~0.9 格。
     */
    private static final double RISE_MAX = 0.035;

    /**
     * 浮力加速度(格/tick²)。
     */
    private static final double BUOYANCY = 0.003;

    /**
     * 摇摆速度增量幅值。
     */
    private static final double SWAY = 0.0025;

    private final SpriteSet sprites;

    private final float swayPhase;

    private final float swayFreq;

    private final float baseSize;

    protected WineBubbleParticle(ClientLevel level, double x, double y, double z,
                                 double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = xd;
        this.yd = Math.max(yd, 0.01);
        this.zd = zd;
        this.friction = 1.0f;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.baseSize = 0.05f + this.random.nextFloat() * 0.13f;
        this.quadSize = this.baseSize;
        this.lifetime = 22 + this.random.nextInt(16);
        this.swayPhase = (float) (this.random.nextDouble() * Math.PI * 2);
        this.swayFreq = 0.12f + this.random.nextFloat() * 0.08f;
        this.setSprite(sprites.get(0, 1));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        int remaining = this.lifetime - this.age;
        if (remaining <= POP_TICKS) {
            // 破裂:换破裂帧,停住不再上浮
            this.setSprite(this.sprites.get(1, 1));
            this.xd = this.yd = this.zd = 0;
            return;
        }
        // 泡到中年后小概率提前破裂,免得整片泡同生同灭
        if (this.age > 12 && this.random.nextFloat() < 0.015f) {
            this.age = this.lifetime - POP_TICKS;
            return;
        }
        // 浮力上升 + 正弦摇摆
        this.yd = Math.min(this.yd + BUOYANCY, RISE_MAX);
        double sway = Math.cos(this.swayPhase + this.age * this.swayFreq) * SWAY;
        double swayZ = Math.sin(this.swayPhase + this.age * this.swayFreq) * SWAY;
        this.xd += sway;
        this.zd += swayZ;
        // 随上升轻微胀大(至 +30%)
        this.quadSize = this.baseSize * (1.0f + 0.3f * this.age / this.lifetime);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    /**
     * 酒泡无附加数据,走 SimpleParticleType。
     */
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType option, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new WineBubbleParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
