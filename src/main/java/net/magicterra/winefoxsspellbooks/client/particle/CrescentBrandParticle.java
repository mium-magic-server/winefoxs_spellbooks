package net.magicterra.winefoxsspellbooks.client.particle;

import java.util.HashMap;
import java.util.Map;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.particle.CrescentBrandOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.joml.Vector3f;

/**
 * 残月烙印粒子族。{@link CrescentBrandOption#mode()} 选形态:
 * <ul>
 *   <li>0 落印:胸口浮现的印记,1.5 倍快速收缩「压」上去后驻留淡出;</li>
 *   <li>1 绕身:绕目标躯干公转的常驻印记,呼吸脉动,临期(&lt;3s)脉动加速变亮,
 *       每 10 tick 滴一粒血色尘;位置逐 tick 实体跟踪;</li>
 *   <li>2 碎片:引爆甩出的月牙残片,弹道下坠自旋,渐缩淡出;</li>
 *   <li>3 闪光:引爆瞬间的白金亮月,快速放大淡出。</li>
 * </ul>
 * 印记为魔法视觉,全亮度渲染保证黑暗中可读。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-21
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = WinefoxsSpellbooks.MODID)
public final class CrescentBrandParticle {

    /**
     * 绕身公转角速度(弧度/tick),约 3 秒一圈。
     */
    private static final double ORBIT_OMEGA = Math.PI * 2 / 60;

    /**
     * 临期加速阈值(tick):剩余低于 3 秒脉动加速、颜色转亮。
     */
    private static final int URGENT_TICKS = 60;

    /**
     * 滴落血尘。
     */
    private static final DustParticleOptions BLOOD_DRIP =
        new DustParticleOptions(new Vector3f(200 / 255f, 32 / 255f, 58 / 255f), 0.6f);

    private CrescentBrandParticle() {
    }

    /**
     * 世界卸载（退回主菜单、切维度）时清空绕身单例表。
     * <p>
     * {@link Orbit#ACTIVE} 与 {@link Orbit#activeLevel} 都是静态的，只在「另一个世界又来了绕身包」
     * 时才换表；不清的话断线后旧 {@code ClientLevel} 会一直被静态字段吊住不回收。
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) {
            Orbit.forget();
        }
    }

    /**
     * amplifier 尺寸微增:每级 +5%,封顶 +25%,高级印视觉更「重」但不花哨。
     */
    private static float sizeScale(int amplifier) {
        return 1.0f + Math.min(amplifier, 5) * 0.05f;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<CrescentBrandOption> {
        @Override
        public Particle createParticle(CrescentBrandOption option, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            return switch (option.mode()) {
                case CrescentBrandOption.MODE_STAMP -> new Stamp(level, x, y, z, option, sprites);
                case CrescentBrandOption.MODE_ORBIT -> Orbit.spawnOrRefresh(level, x, y, z, option, sprites);
                case CrescentBrandOption.MODE_SHARD -> new Shard(level, x, y, z, xd, yd, zd, sprites);
                case CrescentBrandOption.MODE_FLASH -> new Flash(level, x, y, z, sprites);
                default -> null;
            };
        }
    }

    /**
     * 共有基底:无重力无摩擦、全亮度、帧 0 完整残月。
     */
    private abstract static class Base extends TextureSheetParticle {
        Base(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
            super(level, x, y, z);
            this.friction = 1.0f;
            this.gravity = 0.0f;
            this.hasPhysics = false;
            this.setSprite(sprites.get(0, 3));
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
         * 被烙印实体的胸口锚点。
         */
        static Vec3 chestOf(Entity entity) {
            return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.65, entity.getZ());
        }

        /**
         * 瞬移到目标位置并同步渲染插值用的旧坐标。 出生帧必须用它:sendParticles 的出生点在实体脚底,若等首个 tick 才归位, 粒子会在脚底闪现一帧再跳上来。
         */
        void snapTo(Vec3 pos) {
            this.setPos(pos.x, pos.y, pos.z);
            this.xo = pos.x;
            this.yo = pos.y;
            this.zo = pos.z;
        }
    }

    /**
     * 落印:1.5 倍快速收缩压上胸口,驻留后淡出;位置跟踪实体。
     */
    private static final class Stamp extends Base {
        private final int entityId;

        private final float baseSize;

        Stamp(ClientLevel level, double x, double y, double z, CrescentBrandOption option, SpriteSet sprites) {
            super(level, x, y, z, sprites);
            this.entityId = option.brandedEntityId();
            this.baseSize = 0.42f * sizeScale(option.amplifier());
            this.quadSize = this.baseSize * 1.5f;
            this.lifetime = 18;
            if (level.getEntity(this.entityId) instanceof Entity entity) {
                snapTo(chestOf(entity));
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (this.removed) {
                return;
            }
            if (this.level.getEntity(this.entityId) instanceof Entity entity) {
                Vec3 pos = chestOf(entity);
                this.setPos(pos.x, pos.y, pos.z);
            }
            // 前 8 tick 从 1.5 倍收缩到原大(盖章),尾段 4 tick 淡出
            float press = Math.min(this.age / 8.0f, 1.0f);
            this.quadSize = this.baseSize * (1.5f - 0.5f * press);
            int remaining = this.lifetime - this.age;
            this.alpha = remaining < 4 ? remaining / 4.0f : 1.0f;
        }
    }

    /**
     * 绕身:公转 + 呼吸脉动 + 临期加速 + 滴血;寿命 20 tick,服务端同频补发接续。
     */
    private static final class Orbit extends Base {
        private final int entityId;

        /**
         * 无补发续命时的余命(tick):比补发周期 20 长 4,包迟到也不早亡。
         */
        private static final int ORBIT_LIFE = 24;

        /**
         * 出生淡入/自然消亡淡出的 tick 数。
         */
        private static final int FADE_TICKS = 4;

        /**
         * 呼吸基础角频率(弧度/tick),约 25 tick 一次呼吸;临期经变频积分平滑升到 2 倍。
         */
        private static final double PULSE_FREQ = 0.25;

        /**
         * 多目标错相用的黄金角(弧度),不同实体的印记不齐步转。
         */
        private static final double GOLDEN_ANGLE = 2.399963;

        /**
         * 每实体常驻单例:补发包只给现存粒子续命,不新建——不共存就没有交叠混合的闪烁。
         */
        private static final Map<Integer, Orbit> ACTIVE = new HashMap<>();

        /**
         * 判定僵尸单例的静默阈值(tick)。
         */
        private static final int STALE_TICKS = 3;

        /**
         * {@link #ACTIVE} 所属的世界,换世界即整表作废。
         */
        private static ClientLevel activeLevel;

        /**
         * 呼吸相位积分器:每 tick 按当前频率累加,变频全程连续。
         */
        private double pulsePhase;

        private int remaining0;

        /**
         * 上次 tick 时的世界时刻。
         */
        private long lastTickTime;

        /**
         * 丢掉整张表与世界引用：世界卸载时调用，避免静态字段吊住已弃用的 {@link ClientLevel}。
         */
        static void forget() {
            ACTIVE.clear();
            activeLevel = null;
        }

        /**
         * 已有存活单例则续命并返回 null(不生成新粒子),否则新建。
         * <p>
         * 不能只看 {@link Particle#isAlive()}:{@code ParticleEngine} 在换世界
         * ({@code setLevel} → {@code clearParticles})和队列溢出时都是直接丢引用,不调 {@code remove()},
         * 被丢掉的粒子会永远停在「存活」上,把该实体的印记堵死。故换世界整表清空,
         * 同世界内则按「多久没刷新过 {@link #lastTickTime}」判僵尸。
         */
        static Particle spawnOrRefresh(ClientLevel level, double x, double y, double z,
                                       CrescentBrandOption option, SpriteSet sprites) {
            if (activeLevel != level) {
                ACTIVE.clear();
                activeLevel = level;
            }
            Orbit existing = ACTIVE.get(option.brandedEntityId());
            if (existing != null && existing.isAlive()
                && level.getGameTime() - existing.lastTickTime <= STALE_TICKS) {
                existing.refresh(option.remainingTicks());
                return null;
            }
            return new Orbit(level, x, y, z, option, sprites);
        }

        private Orbit(ClientLevel level, double x, double y, double z, CrescentBrandOption option, SpriteSet sprites) {
            super(level, x, y, z, sprites);
            this.entityId = option.brandedEntityId();
            this.remaining0 = option.remainingTicks();
            this.quadSize = 0.34f * sizeScale(option.amplifier());
            this.lifetime = ORBIT_LIFE;
            this.alpha = 0.0f;
            this.pulsePhase = this.entityId * GOLDEN_ANGLE;
            this.lastTickTime = level.getGameTime();
            // 出生帧就落到轨道上,避免在脚底(sendParticles 出生点)闪现
            if (level.getEntity(this.entityId) instanceof Entity entity) {
                snapTo(orbitPos(entity));
            }
            ACTIVE.put(this.entityId, this);
        }

        /**
         * 补发续命:校准剩余时长,余命重置为 ORBIT_LIFE;服务端停发(烙印消失)则自然消亡。
         */
        private void refresh(int remaining) {
            this.remaining0 = remaining + this.age;
            this.lifetime = this.age + ORBIT_LIFE;
        }

        @Override
        public void remove() {
            super.remove();
            if (ACTIVE.get(this.entityId) == this) {
                ACTIVE.remove(this.entityId);
            }
        }

        /**
         * 轨道位置:共享世界钟计角,同目标任何时刻角度唯一;黄金角错相,多目标不齐步。
         */
        private Vec3 orbitPos(Entity entity) {
            double angle = -this.level.getGameTime() * ORBIT_OMEGA + this.entityId * GOLDEN_ANGLE;
            double radius = entity.getBbWidth() * 0.5 + 0.45;
            return chestOf(entity).add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        }

        @Override
        public void tick() {
            super.tick();
            if (this.removed) {
                return;
            }
            this.lastTickTime = this.level.getGameTime();
            if (!(this.level.getEntity(this.entityId) instanceof Entity entity) || !entity.isAlive()) {
                this.remove();
                return;
            }
            Vec3 pos = orbitPos(entity);
            this.setPos(pos.x, pos.y, pos.z);
            // 呼吸:单一正弦,相位逐 tick 积分;临期(<3s)频率经权重 u 平滑升到 2 倍,曲线处处连续
            float remaining = this.remaining0 - this.age;
            float u = Math.clamp((URGENT_TICKS - remaining) / 20.0f, 0.0f, 1.0f);
            this.pulsePhase += PULSE_FREQ * (1.0 + u);
            float pulse = 0.62f + 0.33f * (0.5f + 0.5f * (float) Math.sin(this.pulsePhase));
            // 出生淡入;仅当补发停止(烙印已消失)才走到淡出段
            float envelope = Math.min(1.0f,
                Math.min(this.age, this.lifetime - this.age) / (float) FADE_TICKS);
            this.alpha = pulse * envelope;
            // 滴落血尘
            if (this.age % 10 == 0) {
                this.level.addParticle(BLOOD_DRIP, pos.x, pos.y - 0.1, pos.z, 0, -0.04, 0);
            }
        }
    }

    /**
     * 碎片:弹道下坠 + 自旋 + 渐缩淡出,随机取三种残片帧之一。
     */
    private static final class Shard extends Base {
        Shard(ClientLevel level, double x, double y, double z,
              double xd, double yd, double zd, SpriteSet sprites) {
            super(level, x, y, z, sprites);
            this.xd = xd;
            this.yd = yd;
            this.zd = zd;
            this.quadSize = 0.16f + this.random.nextFloat() * 0.08f;
            this.lifetime = 12 + this.random.nextInt(7);
            this.roll = (float) (this.random.nextDouble() * Math.PI * 2);
            this.oRoll = this.roll;
            this.setSprite(sprites.get(1 + this.random.nextInt(3), 3));
        }

        @Override
        public void tick() {
            super.tick();
            if (this.removed) {
                return;
            }
            this.yd -= 0.035;
            this.oRoll = this.roll;
            this.roll += 0.35f;
            int remaining = this.lifetime - this.age;
            if (remaining < 5) {
                this.alpha = remaining / 5.0f;
                this.quadSize *= 0.92f;
            }
        }
    }

    /**
     * 闪光:白金亮月,5 tick 内放大 2 倍并淡出。
     */
    private static final class Flash extends Base {
        private static final float BASE_SIZE = 0.75f;

        Flash(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
            super(level, x, y, z, sprites);
            this.quadSize = BASE_SIZE;
            this.lifetime = 5;
            this.setColor(1.0f, 0.92f, 0.76f);
        }

        @Override
        public void tick() {
            super.tick();
            if (this.removed) {
                return;
            }
            float t = this.age / (float) this.lifetime;
            this.quadSize = BASE_SIZE * (1.0f + t);
            this.alpha = 1.0f - t;
        }
    }
}
