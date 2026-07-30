package net.magicterra.winefoxsspellbooks.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 狐影追踪弹的狐火火星：全亮度软焰光点，短暂膨起后收缩淡出。
 * <p>
 * {@code xd} 仅作为本地颜色标记：正值为粉色，负值为金色；粒子出生后缓慢滞留， 让连续旋转挂点自然拉出清晰螺旋。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-25
 */
public class FoxshadeSparkParticle extends TextureSheetParticle {

    private static final float PRIMARY_RED = 1.0f;

    private static final float PRIMARY_GREEN = 105.0f / 255.0f;

    private static final float PRIMARY_BLUE = 180.0f / 255.0f;

    private static final float HIGHLIGHT_RED = 1.0f;

    private static final float HIGHLIGHT_GREEN = 205.0f / 255.0f;

    private static final float HIGHLIGHT_BLUE = 72.0f / 255.0f;

    private final float baseSize;

    protected FoxshadeSparkParticle(ClientLevel level, double x, double y, double z,
                                    double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        boolean highlight = xd < 0.0;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.friction = 0.92f;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.baseSize = highlight ? 0.14f : 0.16f;
        this.quadSize = this.baseSize * 0.45f;
        this.lifetime = highlight ? 14 : 16;
        this.alpha = 0.9f;
        if (highlight) {
            this.setColor(HIGHLIGHT_RED, HIGHLIGHT_GREEN, HIGHLIGHT_BLUE);
        } else {
            this.setColor(PRIMARY_RED, PRIMARY_GREEN, PRIMARY_BLUE);
        }
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        float progress = this.age / (float) this.lifetime;
        float flare = Math.min(this.age / 2.0f, 1.0f);
        this.quadSize = this.baseSize * flare * (1.0f - progress * 0.65f);
        this.alpha = 0.9f * (1.0f - progress * progress);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType option, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new FoxshadeSparkParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
