package net.magicterra.winefoxsspellbooks.client.particle;

import net.magicterra.winefoxsspellbooks.magic.particle.TransferStreamOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 魔力转移传输流粒子：彩色追踪魔力弹与金光星芒共用的客户端基类。
 * <p>
 * 无重力、无摩擦——给定的速度即每 tick 位移；全亮度渲染（不受环境光影响）。 两个变体都持有目标/施法者实体 ID，逐 tick 从本地实体实时重算行为，跟手无滞后。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-19
 */
public class ManaStreamParticle extends TextureSheetParticle {

    private static final int FADE_TICKS = 4;

    /**
     * 弹幕彩虹色盘:红橙黄绿青蓝紫粉,ARGB。
     */
    private static final int[] BOLT_COLORS = {
        0xFFFF5A5A, 0xFFFFB347, 0xFFFFF35C, 0xFF7CFF6B,
        0xFF5CD9FF, 0xFF7B8CFF, 0xFFC86BFF, 0xFFFF7BD5,
    };

    protected ManaStreamParticle(ClientLevel level, double x, double y, double z,
                                 double xd, double yd, double zd,
                                 float size, int lifetime, SpriteSet sprites) {
        // 用 3 参构造再手动写入速度:6 参构造会给速度掺随机抖动,弹幕会偏出弹道
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = size;
        this.lifetime = lifetime;
        this.friction = 1.0f;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.pickSprite(sprites);
    }

    /**
     * 生命末段是否用 alpha 淡出;星芒自带消散帧,不叠加淡出。
     */
    protected boolean fadeOut = true;

    @Override
    public void tick() {
        super.tick();
        if (this.fadeOut) {
            int remaining = this.lifetime - this.age;
            if (remaining < FADE_TICKS) {
                this.alpha = remaining / (float) FADE_TICKS;
            }
        }
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
     * 彩色魔力弹:白点贴图按发射序号轮转彩虹盘染色。
     */
    public record BoltProvider(SpriteSet sprites) implements ParticleProvider<TransferStreamOption> {
        @Override
        public Particle createParticle(TransferStreamOption option, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new HomingBolt(level, x, y, z, xd, yd, zd, option, sprites);
        }
    }

    /**
     * 追踪目标的魔力弹。逐颗从手部出膛,每 tick 把速度向「当前位置 → 目标胸口」方向 部分转向(弹道呈弧线,目标移动也能追上),抵达目标即消亡。
     */
    private static final class HomingBolt extends ManaStreamParticle {
        /**
         * 每 tick 速度向目标方向的转向比例。
         */
        private static final double STEER = 0.35;

        /**
         * 判定命中的距离(格)。
         */
        private static final double HIT_RANGE = 0.6;

        private final int targetId;

        HomingBolt(ClientLevel level, double x, double y, double z,
                   double xd, double yd, double zd, TransferStreamOption option, SpriteSet sprites) {
            // 寿命上限只是兜底,正常路径是追到目标即灭(32 格远弹 ~27 tick 抵达)
            super(level, x, y, z, xd, yd, zd, 0.14f, 60, sprites);
            this.targetId = option.targetId();
            // time 字段携带发射序号,按彩虹盘轮转染色,弹链呈流动的七彩
            int color = BOLT_COLORS[Math.floorMod((int) option.time(), BOLT_COLORS.length)];
            this.setColor((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f);
        }

        @Override
        public void tick() {
            super.tick();
            if (this.removed) {
                return;
            }
            if (!(this.level.getEntity(this.targetId) instanceof LivingEntity target)) {
                return; // 目标消失则直线飞完余生
            }
            Vec3 aim = target.getEyePosition().subtract(0, 0.35, 0);
            Vec3 to = new Vec3(aim.x - this.x, aim.y - this.y, aim.z - this.z);
            double dist = to.length();
            if (dist < HIT_RANGE) {
                this.remove();
                return;
            }
            Vec3 desired = to.scale(TransferStreamEmitter.BOLT_SPEED / dist);
            this.xd = this.xd * (1 - STEER) + desired.x * STEER;
            this.yd = this.yd * (1 - STEER) + desired.y * STEER;
            this.zd = this.zd * (1 - STEER) + desired.z * STEER;
        }
    }

    /**
     * 金光星芒:贴图自带金色,不染色。casterId = -1 时为脱离光束的原地闪烁星 (dist 作尺寸倍率),否则为传输光束上的螺旋星(option 携带实体 ID + 出生时基 + 轴向距离)。
     */
    public record CrossProvider(SpriteSet sprites) implements ParticleProvider<TransferStreamOption> {
        @Override
        public Particle createParticle(TransferStreamOption option, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            if (option.casterId() == TransferStreamOption.DETACHED_CASTER) {
                return new TwinkleStar(level, x, y, z, option.dist(), sprites);
            }
            return new BeamStar(level, x, y, z, option, sprites);
        }
    }

    /**
     * 原地闪烁的金星:静止播完 7 帧「初生 → 满臂 → 碎裂」即逝,供各法术做点睛一闪。
     */
    private static final class TwinkleStar extends ManaStreamParticle {
        private final SpriteSet sprites;

        TwinkleStar(ClientLevel level, double x, double y, double z, float sizeScale, SpriteSet sprites) {
            super(level, x, y, z, 0, 0.01, 0, 0.2f * (sizeScale > 0 ? sizeScale : 1.0f),
                TransferStreamEmitter.STAR_LIFE, sprites);
            this.sprites = sprites;
            this.fadeOut = false;
            this.setSpriteFromAge(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.removed) {
                this.setSpriteFromAge(this.sprites);
            }
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }
    }

    /**
     * 螺旋光带上的金色星芒。每 tick 从本地实体实时重算光束几何,再按 「公转角 = (出生时基 + age) × 角速度 + 轴向距离 × 缠绕率」定位—— 既沿螺旋转着前进,又完全跟手;寿命内经 setSpriteFromAge 播完 7 帧「初生 → 满臂 → 碎裂消散」闪烁动画。
     */
    private static final class BeamStar extends ManaStreamParticle {
        private final SpriteSet sprites;

        private final int casterId;

        private final int targetId;

        private final double dist;

        private final boolean secondArm;

        private final float birthTime;

        BeamStar(ClientLevel level, double x, double y, double z,
                 TransferStreamOption option, SpriteSet sprites) {
            super(level, x, y, z, 0, 0, 0, 0.12f, TransferStreamEmitter.STAR_LIFE, sprites);
            this.sprites = sprites;
            this.casterId = option.casterId();
            this.targetId = option.targetId();
            // dist 的符号编码螺旋臂:负值为相差 180° 的第二条臂
            this.dist = Math.abs(option.dist());
            this.secondArm = option.dist() < 0;
            this.birthTime = option.time();
            this.fadeOut = false;
            this.setSpriteFromAge(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            if (this.removed) {
                return;
            }
            this.setSpriteFromAge(this.sprites);
            TransferStreamEmitter.BeamGeometry g =
                TransferStreamEmitter.resolveBeam(this.level, this.casterId, this.targetId);
            if (g == null || this.dist > g.distance() + 0.5) {
                this.remove();
                return;
            }
            double a = (this.birthTime + this.age) * TransferStreamEmitter.HELIX_OMEGA
                + this.dist * TransferStreamEmitter.HELIX_WIND
                + (this.secondArm ? Math.PI : 0);
            Vec3 pos = g.start().add(g.dir().scale(this.dist))
                .add(g.u().scale(Math.cos(a) * TransferStreamEmitter.HELIX_RADIUS))
                .add(g.v().scale(Math.sin(a) * TransferStreamEmitter.HELIX_RADIUS));
            this.setPos(pos.x, pos.y, pos.z);
        }

        @Override
        public ParticleRenderType getRenderType() {
            // 不透明裁切渲染:无半透明混合的糊边,像素十字保持锐利
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }
    }
}
