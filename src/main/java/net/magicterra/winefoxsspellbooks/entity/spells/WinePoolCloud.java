package net.magicterra.winefoxsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;

/**
 * 狐酿炸弹落点铺开的酒池:自家女仆泡在其中持续回魔,敌对生物反复刷新微醺。
 * 结构同 {@link TipsyMistCloud},差异集中在 {@link #applyEffect}(回魔/微醺双通道)
 * 与 {@link #canHitEntity}(女仆需被显式放行,不能沿用父类的"owner 盟友豁免")。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-06
 */
public class WinePoolCloud extends AoeEntity {

    public static final EntityType<WinePoolCloud> TYPE = EntityType.Builder
        .<WinePoolCloud>of(WinePoolCloud::new, MobCategory.MISC)
        .sized(4.0f, 1.2f)
        .clientTrackingRange(64)
        .build("wine_pool");

    /** 池中女仆回魔比例:每次重涂(10 tick)回 2.5% 最大魔力,约合每秒 5%。 */
    private static final float MANA_RATIO_PER_APPLY = 0.025f;

    public WinePoolCloud(EntityType<WinePoolCloud> type, Level level) {
        super(type, level);
        this.setCircular();
    }

    public WinePoolCloud(Level level) {
        this(TYPE, level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        if (WinefoxHexHelper.isMaidOwnedBy(target, getOwner())) {
            // 女仆真实法力存于 MagicMaidAdapter,须经 MaidMagicEntity 读写;
            // Iron's 的 MagicData.getPlayerMagicData 对女仆返回的是无人读取的 attachment,写入无效。
            // 魔力紊乱期间禁止一切回魔(与 MaidMagicManager.regenMana 的门禁一致)
            if (target instanceof MaidMagicEntity maidMagic
                && !target.hasEffect(WsbEffects.MANA_DISRUPTION)) {
                double max = MaidMagicManager.getMaxMana(target);
                maidMagic.winefoxsSpellbooks$setMana(
                    (float) Math.min(maidMagic.winefoxsSpellbooks$getMana() + max * MANA_RATIO_PER_APPLY, max));
            }
            return;
        }
        if (WinefoxHexHelper.isHexEnemy(target)) {
            target.addEffect(new MobEffectInstance(WsbEffects.TIPSY, WinefoxHexHelper.TIPSY_REAPPLY_TICKS, 0));
        }
    }

    /**
     * 父类判定会豁免 owner 的盟友,但酒池要对自家女仆生效(回魔),不能套用该豁免;
     * 施法者自家女仆单独显式放行,只保留 owner 本人排除与 Projectile 基础可命中判定,
     * 其余实体(含其他玩家的女仆)仍走父类完整判定链(owner/盟友豁免 + 存活可选取)。
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == getOwner()) {
            return false;
        }
        if (WinefoxHexHelper.isMaidOwnedBy(target, getOwner())) {
            return target.canBeHitByProjectile();
        }
        return super.canHitEntity(target);
    }

    @Override
    public float getParticleCount() {
        return 0.3f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.of(WinefoxHexHelper.SCHOOL_MIST);
    }
}
