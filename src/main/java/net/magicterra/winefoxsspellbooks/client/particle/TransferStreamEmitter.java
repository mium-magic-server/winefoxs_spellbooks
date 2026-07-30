package net.magicterra.winefoxsspellbooks.client.particle;

import net.magicterra.winefoxsspellbooks.magic.particle.TransferStreamOption;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 魔力传输流发射器（客户端）。自身不渲染，存活 {@value #PACKET_INTERVAL} tick： 服务端每 {@value #PACKET_INTERVAL} tick 只发一个本粒子，中间帧由客户端插帧， 光束几何每帧从包内实体 ID 对应的本地实体实时重算（转头/移动不滞后）。
 * <p>
 * 每帧职责：从手部逐颗发射追踪目标的彩色魔力弹（连续弹链），并按错相节奏 补种螺旋光带上的金十字星芒——星芒出生后自行公转前进并播闪烁动画。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-19
 */
public class TransferStreamEmitter extends NoRenderParticle {

    /**
     * 服务端发包间隔(tick),客户端在两包之间插出等量的帧。
     */
    public static final int PACKET_INTERVAL = 5;

    /**
     * 星芒公转角速度(弧度/tick),约 9 tick 一整圈。
     */
    static final float HELIX_OMEGA = 0.7f;

    /**
     * 沿光束的螺旋槽位步距(格)。
     */
    static final double HELIX_STEP = 0.7;

    /**
     * 螺旋半径(格)。
     */
    static final double HELIX_RADIUS = 0.35;

    /**
     * 缠绕率(弧度/格):约 4 格一整圈,10~30 格的光束能看到 3~7 个螺距。
     */
    static final double HELIX_WIND = 1.6;

    /**
     * 星芒寿命(tick):7 帧闪烁动画 × 2 tick/帧。
     */
    static final int STAR_LIFE = 14;

    /**
     * 相邻槽位的出生时刻错开量(tick),让闪烁沿光带成波浪推进。
     */
    private static final int STAR_STAGGER = 3;

    /**
     * 魔力弹飞行速度(格/tick)。
     */
    static final double BOLT_SPEED = 1.2;

    /**
     * 每 tick 发射的魔力弹数;与速度共同决定弹链间距(speed/count 格)。
     */
    private static final int BOLTS_PER_TICK = 2;

    /**
     * 出膛横向散布速度,弹链略微发散再被追踪拉回,更有弹幕感。
     */
    private static final double BOLT_SPREAD = 0.12;

    private final int casterId;

    private final int targetId;

    private final float baseTime;

    protected TransferStreamEmitter(ClientLevel level, double x, double y, double z, TransferStreamOption option) {
        super(level, x, y, z);
        this.lifetime = PACKET_INTERVAL;
        this.casterId = option.casterId();
        this.targetId = option.targetId();
        this.baseTime = option.time();
        // 第 0 帧立即铺设;后续帧在 tick() 里补
        emitFrame(0);
    }

    @Override
    public void tick() {
        if (++this.age >= PACKET_INTERVAL) {
            this.remove();
            return;
        }
        emitFrame(this.age);
    }

    /**
     * 铺设第 n 帧(n=0..间隔-1):光束几何实时重算,相位按时基 +n。
     */
    private void emitFrame(int n) {
        BeamGeometry g = resolveBeam(this.level, this.casterId, this.targetId);
        if (g == null) {
            return;
        }
        double t = this.baseTime + n;

        // —— 彩色魔力弹:逐颗连续发射,同 tick 的几颗沿弹道错开半步;
        // 颜色按发射序号轮转彩虹盘,追踪逻辑在 HomingBolt 里 ——
        for (int i = 0; i < BOLTS_PER_TICK; i++) {
            Vec3 muzzle = g.start().add(g.dir().scale(BOLT_SPEED * i / BOLTS_PER_TICK));
            Vec3 vel = g.dir().scale(BOLT_SPEED)
                .add(g.u().scale((this.random.nextDouble() - 0.5) * BOLT_SPREAD))
                .add(g.v().scale((this.random.nextDouble() - 0.5) * BOLT_SPREAD));
            float serial = (float) (t * BOLTS_PER_TICK + i);
            this.level.addParticle(
                new TransferStreamOption(WsbParticles.MANA_BOLT.get(), this.casterId, this.targetId, serial, 0),
                muzzle.x, muzzle.y, muzzle.z, vel.x, vel.y, vel.z);
        }

        // —— 金十字星芒补种:每槽位每 STAR_LIFE tick 生一颗(相邻槽位错相),
        // 出生后星芒自行绕轴公转并播 7 帧闪烁,位置逐 tick 从实时光束重算 ——
        int slot = 0;
        for (double d = HELIX_STEP * 0.5; d < g.distance(); d += HELIX_STEP, slot++) {
            if (Math.floorMod((int) t + slot * STAR_STAGGER, STAR_LIFE) != 0) {
                continue;
            }
            double angle = t * HELIX_OMEGA + d * HELIX_WIND;
            for (int arm = 0; arm < 2; arm++) {
                double a = angle + arm * Math.PI;
                Vec3 pos = g.start().add(g.dir().scale(d))
                    .add(g.u().scale(Math.cos(a) * HELIX_RADIUS))
                    .add(g.v().scale(Math.sin(a) * HELIX_RADIUS));
                this.level.addParticle(
                    new TransferStreamOption(WsbParticles.GOLD_CROSS.get(), this.casterId, this.targetId,
                        (float) t, (float) (arm == 0 ? d : -d)),
                    pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }
    }

    /**
     * 从本地实体解析光束几何。与服务端相同的端点定义:施法者手部 → 目标胸口。
     *
     * @return 实体缺失或距离过近时返回 null
     */
    static BeamGeometry resolveBeam(ClientLevel level, int casterId, int targetId) {
        if (!(level.getEntity(casterId) instanceof LivingEntity caster)
            || !(level.getEntity(targetId) instanceof LivingEntity target)) {
            return null;
        }
        Vec3 start = caster.getEyePosition().add(caster.getForward().scale(0.4)).subtract(0, 0.2, 0);
        Vec3 end = target.getEyePosition().subtract(0, 0.35, 0);
        Vec3 diff = end.subtract(start);
        double distance = diff.length();
        if (distance < 0.5) {
            return null;
        }
        Vec3 dir = diff.scale(1.0 / distance);
        // 光束轴的正交基(轴近竖直时改用 X 轴避免叉积退化)
        Vec3 aux = Math.abs(dir.y) > 0.98 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 u = dir.cross(aux).normalize();
        Vec3 v = dir.cross(u);
        return new BeamGeometry(start, end, dir, distance, u, v);
    }

    /**
     * 光束几何:起点、终点、单位轴向、长度与正交基。
     */
    record BeamGeometry(Vec3 start, Vec3 end, Vec3 dir, double distance, Vec3 u, Vec3 v) {}

    /**
     * 无贴图,直接实例化,注册走 registerSpecial。
     */
    public static final class Provider implements ParticleProvider<TransferStreamOption> {
        @Override
        public Particle createParticle(TransferStreamOption option, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return new TransferStreamEmitter(level, x, y, z, option);
        }
    }
}
