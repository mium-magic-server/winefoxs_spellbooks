package net.magicterra.winefoxsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSpells;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 酒杯狐影弹（Foxshade Wisp）— 女仆可打出的酒狐巫法（Winefox Hex）追踪弹。
 * <p>
 * 复用 Iron's {@link AbstractMagicProjectile} 基类，天生具备实体追踪（{@link #setHomingTarget}）、
 * 300t 自动消散与命中检测。本类只需：命中生物时打出一发<b>酒狐巫法学派伤害</b>
 * （以施法者为归因 → 引爆系统据此区分女仆 ×1.5 / 玩家 ×1.0），并补足基类的抽象渲染/音效钩子。
 * <p>
 * 打出的是真正的 WH 学派伤害（伤害 type = {@code winefoxs_spellbooks:winefox_hex_magic}），
 * 因此命中带「残月烙印」的敌人会触发引爆——它是女仆侧最主要的 WH 伤害源。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
public class FoxshadeWispProjectile extends AbstractMagicProjectile implements GeoEntity {

    /** 紫粉主色调：拖尾/命中粒子用，与狐头模型配色一致。 */
    private static final int FOXSHADE_COLOR = WinefoxHexHelper.SCHOOL_COLOR;

    /** 侧翼弧线：距目标 ≥ 此距离时用满侧偏（格）。 */
    private static final double ARC_DIST = 12.0;
    /** 近距收敛：距目标 ≤ 此距离时取消侧偏并加大转向，直扑命中而非绕圈（格）。 */
    private static final double CONVERGE_DIST = 3.0;
    /** 远距巡航转角（每 tick 朝期望方向插值比例）。 */
    private static final double TURN_CRUISE = 0.18;
    /** 近距收敛转角：贴近目标时加大转向，确保命中。 */
    private static final double TURN_CONVERGE = 0.5;

    /**
     * 同步数据：finisher（第 6 发）命中时的残月爆发伤害；0 表示普通弹、不爆发。
     * 服务端在 {@link #setFinisher} 写入并决定 AoE 伤害量；客户端渲染器经
     * {@link #isFinisher()} 读取，决定弹体缩放（finisher 0.85 / 普通 0.55）。
     */
    private static final EntityDataAccessor<Float> DATA_BURST_DAMAGE =
        SynchedEntityData.defineId(FoxshadeWispProjectile.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    /** 世界上方向,供侧向向量叉积用。 */
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);

    /** 侧翼包抄方向：+1 向右、-1 向左；0 表示直线（无侧偏）。由施法逻辑分配。 */
    private int flankSign = 0;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> {
            state.setAnimation(IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static final EntityType<FoxshadeWispProjectile> TYPE =
        EntityType.Builder.<FoxshadeWispProjectile>of(FoxshadeWispProjectile::new, MobCategory.MISC)
            .sized(0.3f, 0.3f)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build("foxshade_wisp");

    public FoxshadeWispProjectile(EntityType<? extends FoxshadeWispProjectile> entityType, Level level) {
        super(entityType, level);
        // 狐影弹靠追踪飞行，不受重力下坠（与 Iron's 自家追踪弹一致）
        this.setNoGravity(true);
    }

    /** 施法时构造：绑定施法者为 owner（用于伤害归因与友伤过滤）。 */
    public FoxshadeWispProjectile(Level level, LivingEntity owner) {
        this(TYPE, level);
        this.setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BURST_DAMAGE, 0.0f);
    }

    /**
     * 将此弹体标记为 finisher（第 6 发），命中时在半径 3 内打出 WH 学派 AoE。
     *
     * @param burstDamage AoE 伤害值；须大于 0 才会爆发
     * @author Gardel &lt;gardel741@outlook.com&gt;
     * @since 2026-06-21
     */
    public void setFinisher(float burstDamage) {
        this.entityData.set(DATA_BURST_DAMAGE, burstDamage);
    }

    /**
     * 返回此弹体是否为 finisher（第 6 发）。
     *
     * @return {@code true} 时渲染器会放大显示，命中时触发残月爆发
     * @author Gardel &lt;gardel741@outlook.com&gt;
     * @since 2026-06-21
     */
    public boolean isFinisher() {
        return this.entityData.get(DATA_BURST_DAMAGE) > 0.0f;
    }

    // 基类只持久化 Damage/homingTarget/Age 等；侧翼方向与 finisher 状态需自行存取，
    // 否则存档重载后终结弹退化为普通直线弹
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FlankSign", this.flankSign);
        tag.putFloat("BurstDamage", this.entityData.get(DATA_BURST_DAMAGE));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.flankSign = Integer.signum(tag.getInt("FlankSign"));
        this.setFinisher(tag.getFloat("BurstDamage"));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) {
            this.discard();
            return;
        }
        Entity attribution = WinefoxHexHelper.projectileAttribution(this);
        var spell = WsbSpells.FOXSHADE_WISPS_SPELL.get();
        if (result.getEntity() instanceof LivingEntity target) {
            // 学派伤害源：causingEntity = 施法者（attribution），用于引爆归因
            DamageSources.applyDamage(target, this.getDamage(), spell.getDamageSource(this, attribution));
        }
        // finisher：在命中点打出半径 3 的 WH 学派 AoE；带残月烙印者经 WinefoxDetonationHandler 自动引爆。
        // 直击目标若为敌对也在 AoE 内，同 tick 吃「直击 + 爆发」双份——精准命中终结弹的奖励（有意设计）
        float burstDamage = this.entityData.get(DATA_BURST_DAMAGE);
        if (burstDamage > 0.0f) {
            WinefoxHexHelper.applyAoeBurst(this.level(), this.getBoundingBox().inflate(3.0), burstDamage,
                spell.getDamageSource(this, attribution), le -> WinefoxHexHelper.isHostileTo(le, attribution));
            this.impactParticles(this.getX(), this.getY(), this.getZ());
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    public float getSpeed() {
        return 0.6f;
    }

    @Override
    public void trailParticles() {
        Vec3 v = this.getDeltaMovement();
        this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FOXSHADE_COLOR),
            this.getX(), this.getY(), this.getZ(),
            -v.x * 0.1, -v.y * 0.1, -v.z * 0.1);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FOXSHADE_COLOR),
                x, y, z, 12, 0.2, 0.2, 0.2, 0.0);
        }
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    /**
     * 设置侧翼包抄方向。
     *
     * @param sign 正数向右包抄，负数向左包抄，0 表示直线
     * @author Gardel &lt;gardel741@outlook.com&gt;
     * @since 2026-06-21
     */
    public void setFlankSign(int sign) {
        this.flankSign = Integer.signum(sign);
    }

    /**
     * 依「指向目标的向量及其模长」计算侧翼期望朝向：远距强侧偏、近距收向目标。
     * 出膛初速与逐 tick 追踪共用此朝向，保证弧线连续无折点；toTarget/dist 由调用方
     * 一次算好传入，避免逐 tick 重复求包围盒中心。
     *
     * @return 侧翼期望单位向量；目标过近（方向不可解）时返回 {@code null}
     * @author Gardel &lt;gardel741@outlook.com&gt;
     * @since 2026-06-21
     */
    private Vec3 flankDesiredDir(Vec3 toTarget, double dist) {
        if (dist < 1.0e-4) {
            return null;
        }
        Vec3 dirToTarget = toTarget.scale(1.0 / dist);
        // 水平侧向单位向量（dir × up）；flankSign 决定左右
        Vec3 perp = dirToTarget.cross(UP);
        if (perp.lengthSqr() < 1.0e-6) {
            perp = new Vec3(1.0, 0.0, 0.0); // 近垂直时退回固定侧向，避免零向量
        } else {
            perp = perp.normalize();
        }
        // 侧偏权重 t：≥ARC_DIST 满侧偏(t=1)；≤CONVERGE_DIST 取消侧偏(t=0，纯指向目标→收弧命中)；中间线性
        double t = Mth.clamp((dist - CONVERGE_DIST) / (ARC_DIST - CONVERGE_DIST), 0.0, 1.0);
        return dirToTarget.add(perp.scale(this.flankSign * 0.9 * t)).normalize();
    }

    /**
     * 出膛即带侧翼初速：沿「侧翼期望朝向」射出，使弧线在出膛时即张开。
     * 须在 {@link #setHomingTarget}/{@link #setFlankSign} 之后调用。
     *
     * @param target 追踪目标
     * @return {@code true} 已带侧偏射出；{@code false} 表示目标方向不可解，调用方应退回直线 {@link #shoot}
     * @author Gardel &lt;gardel741@outlook.com&gt;
     * @since 2026-06-22
     */
    public boolean shootFlanking(Entity target) {
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(this.position());
        Vec3 desired = this.flankDesiredDir(toTarget, toTarget.length());
        if (desired == null) {
            return false;
        }
        this.shoot(desired);
        return true;
    }

    /**
     * 侧翼扫弯包抄：远距离时偏向侧方飘出，接近时强力收向目标，画出一道大弧。
     *
     * @author Gardel &lt;gardel741@outlook.com&gt;
     * @since 2026-06-21
     */
    @Override
    protected void handleEntityHoming() {
        Entity target = this.getHomingTarget();
        if (target == null) {
            return; // 无目标：保持当前直线前射
        }
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(this.position());
        double dist = toTarget.length();
        Vec3 desired = this.flankDesiredDir(toTarget, dist);
        if (desired == null) {
            return;
        }
        // 贴近目标时加大转向，直扑命中而非绕圈；远距用巡航转角保持弧线平滑
        double turn = dist <= CONVERGE_DIST ? TURN_CONVERGE : TURN_CRUISE;
        Vec3 cur = this.getDeltaMovement();
        Vec3 curDir = cur.lengthSqr() < 1.0e-6 ? desired : cur.normalize();
        Vec3 newDir = curDir.add(desired.subtract(curDir).scale(turn)).normalize();
        this.setDeltaMovement(newDir.scale(this.getSpeed()));
    }
}
