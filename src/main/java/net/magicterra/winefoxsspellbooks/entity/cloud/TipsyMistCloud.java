package net.magicterra.winefoxsspellbooks.entity.cloud;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;

/**
 * 醉狐之雾的粉色雾团实体:落地生成圆形领域,持续期内按 Iron's {@link AoeEntity} 默认节奏 (每 10 tick 一次)对雾内非自家女仆的实体重新灌上微醺。只补充命中过滤与视觉粒子, tick/checkHits/尺寸同步等一律沿用父类。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-06
 */
public class TipsyMistCloud extends AoeEntity {

    public static final EntityType<TipsyMistCloud> TYPE = EntityType.Builder
        .<TipsyMistCloud>of(TipsyMistCloud::new, MobCategory.MISC)
        .sized(4.0f, 1.2f)
        .clientTrackingRange(64)
        .build("tipsy_mist_cloud");

    /**
     * 微醺叠加等级;由施法法术等级决定,默认 0(等价于 Amplifier I)。
     */
    private int tipsyAmplifier = 0;

    public TipsyMistCloud(EntityType<TipsyMistCloud> type, Level level) {
        super(type, level);
        this.setCircular();
    }

    public TipsyMistCloud(Level level) {
        this(TYPE, level);
    }

    public void setTipsyAmplifier(int amplifier) {
        this.tipsyAmplifier = amplifier;
    }

    @Override
    public void applyEffect(LivingEntity target) {
        target.addEffect(new MobEffectInstance(WsbEffects.TIPSY, WinefoxHexHelper.TIPSY_REAPPLY_TICKS, tipsyAmplifier));
    }

    /**
     * 在父类判定(存活/可选取/owner 及其盟友排除)基础上,额外把施法者自家女仆挡在雾外; 其余实体(含中立/被动生物、其他玩家的女仆)一律放行。
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        return !WinefoxHexHelper.isMaidOwnedBy(target, getOwner()) && super.canHitEntity(target);
    }

    /**
     * 水汽斑点小而淡,密度比原学派粉雾开大一档才够"雾"。
     */
    @Override
    public float getParticleCount() {
        return 0.8f;
    }

    /**
     * 淡白水汽斑点:升腾飘摆,模仿水蒸气(运动逻辑在粒子自身)。
     */
    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.of(WsbParticles.MIST_SPECK.get());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TipsyAmplifier", this.tipsyAmplifier);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.tipsyAmplifier = tag.getInt("TipsyAmplifier");
    }
}
