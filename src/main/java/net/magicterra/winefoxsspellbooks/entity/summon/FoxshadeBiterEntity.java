package net.magicterra.winefoxsspellbooks.entity.summon;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSounds;
import net.magicterra.winefoxsspellbooks.registry.WsbSpells;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 狐影伏击的落地狐狸 — 复用狐影 littlefox 模型的寻路生物。
 * <p>
 * 由 {@link net.magicterra.winefoxsspellbooks.event.FoxshadeAmbushHandler} 在目标附近生成： 用原版地面导航跑向指定目标，贴近时腾身一跃、落点撞咬，打出酒狐巫法学派伤害；按法术等级连咬 1～4 口，两口之间贴着目标跟走，咬完停留片刻后消散。 咬击以召唤者（owner）归因 → 引爆系统据此区分女仆 ×1.5 / 玩家 ×1.0；命中带残月烙印者自动引爆。
 * <p>
 * 短寿命瞬时召唤物：无敌（不可被击杀打断）、咬完或追咬超时即消散、够不到目标（飞行/隔坑）则自然到期。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-23
 */
public class FoxshadeBiterEntity extends PathfinderMob implements GeoEntity {

    /**
     * 每次追咬的最长时长(ticks)：自现身或上一口起算，跑不到目标（飞行/隔坑）则到点自然消散，不无限追。
     */
    private static final int MAX_LIFETIME = 100;

    /**
     * 最后一口咬中后停留(ticks)再消散：让扑咬收尾看得清，不瞬间消失。
     */
    private static final int POOF_DELAY = 15;

    /**
     * 两口之间的间隔(ticks)：长于受击无敌时间的一半（10 tick），否则后一口会被吞掉。
     */
    private static final int BITE_INTERVAL = 12;

    /**
     * 导航速度倍率：狐狸跑得比常规怪快。
     */
    private static final double MOVE_SPEED_MULT = 1.3;

    /**
     * 触发起跳的水平距离(格)。
     */
    private static final double LEAP_TRIGGER_DIST = 3.0;

    /**
     * 起跳水平冲量与竖直冲量。
     */
    private static final double LEAP_HORIZONTAL = 0.55;

    private static final double LEAP_VERTICAL = 0.48;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation RUNNING = RawAnimation.begin().thenLoop("running");

    private int lifetime = 0;

    private boolean hasBitten = false;

    /**
     * 还能咬几口；咬完最后一口后进入消散倒计时。
     */
    private int bitesRemaining = 1;

    /**
     * 距下一口还要等多少 tick；期间贴着目标跟走，不起跳也不咬。
     */
    private int biteCooldown = 0;

    /**
     * 已咬口数，供扑咬 AI 判断「换了一口」时重新给起跳机会。
     */
    private int bitesDone = 0;

    /**
     * 咬中后的消散倒计时；-1 表示尚未咬中。
     */
    private int poofCountdown = -1;

    private float biteDamage = 1.0f;
    @Nullable
    private LivingEntity biteTarget;
    @Nullable
    private Entity owner;

    public static final EntityType<FoxshadeBiterEntity> TYPE =
        EntityType.Builder.of(FoxshadeBiterEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.4f)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build("foxshade_biter");

    public FoxshadeBiterEntity(EntityType<? extends FoxshadeBiterEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 4.0)
            .add(Attributes.MOVEMENT_SPEED, 0.45)
            .add(Attributes.FOLLOW_RANGE, 24.0)
            .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapBiteGoal(this));
    }

    /**
     * 装配一只扑咬狐：绑定召唤者、目标、每口伤害与口数，并立即朝目标转身。
     *
     * @param owner  召唤者（伤害归因；引爆女仆/玩家分支据此判定）
     * @param target 扑咬目标
     * @param damage 每口伤害
     * @param bites  连咬口数（至少 1）
     */
    public void setup(Entity owner, LivingEntity target, float damage, int bites) {
        this.owner = owner;
        this.biteTarget = target;
        this.biteDamage = damage;
        this.bitesRemaining = Math.max(1, bites);
        this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        // 现身狐鸣：四变体随机，音高再抖 ±10% 与 Iron's 施法音一致，避免连续伏击听出重复
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            WsbSounds.FOXSHADE_BITER_SPAWN.get(), this.getSoundSource(),
            1.0f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        // 最后一口已咬中：走消散倒计时，停留 POOF_DELAY tick 收尾后消失
        if (this.poofCountdown >= 0) {
            if (--this.poofCountdown < 0) {
                poof();
            }
            return;
        }
        if (this.biteCooldown > 0) {
            --this.biteCooldown;
        }
        // 目标消失 / 超时：直接消散
        if (++this.lifetime > MAX_LIFETIME || this.biteTarget == null || !this.biteTarget.isAlive()) {
            poof();
        }
    }

    /**
     * 落点撞咬：打出一发酒狐巫法学派伤害（以 owner 归因）。还有口数则隔 {@link #BITE_INTERVAL} tick 再咬，
     * 追咬时限重新起算；咬完最后一口进入消散倒计时（停留 {@link #POOF_DELAY} tick）。
     */
    private void bite() {
        if (this.hasBitten || this.biteCooldown > 0 || this.biteTarget == null) {
            return;
        }
        this.bitesDone++;
        if (--this.bitesRemaining <= 0) {
            this.hasBitten = true;
        } else {
            this.biteCooldown = BITE_INTERVAL;
            this.lifetime = 0;
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            WsbSounds.FOXSHADE_BITER_BITE.get(), this.getSoundSource(),
            1.0f, 0.9f + this.random.nextFloat() * 0.2f);
        Entity attribution = this.owner != null ? this.owner : this;
        // 复用狐影弹的学派伤害源：directEntity = 本狐（供伏击处理器识别、免二次触发），causingEntity = 召唤者
        var spell = WsbSpells.FOXSHADE_WISPS_SPELL.get();
        DamageSources.applyDamage(this.biteTarget, this.biteDamage, spell.getDamageSource(this, attribution));
        if (this.hasBitten) {
            this.poofCountdown = POOF_DELAY;
        }
    }

    /**
     * 消散：撒一小簇学派粉粒子后移除自身。
     */
    private void poof() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                WinefoxHexHelper.effectParticle(WinefoxHexHelper.SCHOOL_COLOR),
                this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ(),
                10, 0.2, 0.2, 0.2, 0.0);
        }
        this.discard();
    }

    // 瞬时召唤物：无敌，避免被杂兵打断扑咬；生命周期完全由 tick 的超时/咬击控制。
    // 但 BYPASSES_INVULNERABILITY 必须放行：/kill 走的正是 hurt(genericKill, MAX_VALUE)，
    // 一并挡掉的话管理员没有任何手段清除它，而它又不会随距离消失
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    /**
     * 不写进存档：owner / biteTarget / biteDamage 都只活在内存里，读档后 {@code biteTarget == null}，
     * 第一帧就走 {@link #poof()}——玩家重进世界会在早已结束的战斗点看到一簇凭空冒出的粒子。 一次伏击总共只有 {@link #MAX_LIFETIME} tick，本就活不过一次存档，索性不存。
     */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "running", 0, state -> {
            state.setAnimation(RUNNING);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * 扑咬 AI：跑向目标，贴近时腾身一跃，落点进入咬击范围即撞咬一口。
     */
    private static class LeapBiteGoal extends Goal {
        private final FoxshadeBiterEntity fox;

        private boolean leaped = false;

        /**
         * 上次给起跳机会时的已咬口数；咬过一口后复位 {@link #leaped}，下一口可以再扑一次。
         */
        private int leapedForBite = -1;

        LeapBiteGoal(FoxshadeBiterEntity fox) {
            this.fox = fox;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.fox.biteTarget != null && this.fox.biteTarget.isAlive() && !this.fox.hasBitten;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        /**
         * 每次激活都重新给一次起跳机会。
         * <p>
         * {@code leaped} 是「本次接近只跳一次」的闸门，不复位的话被同优先级组的 {@link FloatGoal} 抢过 一次 {@code JUMP}（趟过水/岩浆）后，本目标重新激活时它还是 {@code true}，狐从此只能贴地走——
         * 而 {@code STEP_HEIGHT} 只有 1.0，遇到矮台或水沟对岸就再也够不着目标。
         */
        @Override
        public void start() {
            this.leaped = false;
        }

        @Override
        public void tick() {
            LivingEntity target = this.fox.biteTarget;
            if (target == null) {
                return;
            }
            this.fox.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (this.leapedForBite != this.fox.bitesDone) {
                this.leapedForBite = this.fox.bitesDone;
                this.leaped = false;
            }
            if (this.fox.biteCooldown > 0) {
                // 两口之间：贴着目标跟走（被击退的目标也追得上），不起跳不咬
                this.fox.getNavigation().moveTo(target, MOVE_SPEED_MULT);
                return;
            }

            double reach = this.fox.getBbWidth() * 0.5 + target.getBbWidth() * 0.5 + 0.8;
            double dist3dSqr = this.fox.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (dist3dSqr <= reach * reach) {
                this.fox.bite();
                return;
            }

            this.fox.getNavigation().moveTo(target, MOVE_SPEED_MULT);

            // 贴近且落地时腾身一跃，扑向目标；跃起后进入咬击范围由上方判定收尾
            double dx = target.getX() - this.fox.getX();
            double dz = target.getZ() - this.fox.getZ();
            double horizSqr = dx * dx + dz * dz;
            if (!this.leaped && this.fox.onGround() && horizSqr <= LEAP_TRIGGER_DIST * LEAP_TRIGGER_DIST) {
                double len = Math.sqrt(horizSqr);
                if (len > 1.0e-3) {
                    this.fox.setDeltaMovement(
                        dx / len * LEAP_HORIZONTAL, LEAP_VERTICAL, dz / len * LEAP_HORIZONTAL);
                    this.fox.hasImpulse = true;
                    this.leaped = true;
                }
            }
        }
    }
}
