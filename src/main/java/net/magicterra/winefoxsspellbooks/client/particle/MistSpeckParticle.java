package net.magicterra.winefoxsspellbooks.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 水汽斑点粒子：醉狐之雾的雾气视觉——淡白色的细小斑点,模仿水蒸气。
 * <p>
 * 缓慢升腾、轻微飘摆,首尾各几 tick 淡入/淡出,整体半透明; 贴图复用白色圆点(mana_bolt),按普通环境光渲染(蒸气不发光)。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-19
 */
public class MistSpeckParticle extends TextureSheetParticle {

    /**
     * 淡入/淡出的 tick 数。
     */
    private static final int FADE_TICKS = 5;

    /**
     * 整体最大不透明度:雾气若隐若现。
     */
    private static final float MAX_ALPHA = 0.55f;

    /**
     * 升腾终端速度(格/tick)。
     */
    private static final double RISE_MAX = 0.02;

    /**
     * 飘摆速度增量幅值。
     */
    private static final double SWAY = 0.0015;

    private final float swayPhase;

    private final float swayFreq;

    protected MistSpeckParticle(ClientLevel level, double x, double y, double z,
                                double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = Math.max(yd, 0.004);
        this.zd = zd;
        this.friction = 1.0f;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.quadSize = 0.05f + this.random.nextFloat() * 0.07f;
        this.lifetime = 18 + this.random.nextInt(15);
        // 微暖的淡白,不是纯白,避免像雪
        this.setColor(0.95f, 0.94f, 0.91f);
        this.alpha = 0.0f;
        this.swayPhase = (float) (this.random.nextDouble() * Math.PI * 2);
        this.swayFreq = 0.1f + this.random.nextFloat() * 0.08f;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        // 缓慢升腾 + 正弦飘摆
        this.yd = Math.min(this.yd + 0.001, RISE_MAX);
        this.xd += Math.cos(this.swayPhase + this.age * this.swayFreq) * SWAY;
        this.zd += Math.sin(this.swayPhase + this.age * this.swayFreq) * SWAY;
        // 首尾淡入淡出
        int remaining = this.lifetime - this.age;
        float in = Math.min(this.age / (float) FADE_TICKS, 1.0f);
        float out = Math.min(remaining / (float) FADE_TICKS, 1.0f);
        this.alpha = MAX_ALPHA * Math.min(in, out);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /**
     * 水汽斑点无附加数据,走 SimpleParticleType。
     */
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType option, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new MistSpeckParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
