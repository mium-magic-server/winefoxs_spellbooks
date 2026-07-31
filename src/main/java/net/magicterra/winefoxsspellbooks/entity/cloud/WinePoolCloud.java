package net.magicterra.winefoxsspellbooks.entity.cloud;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;

/**
 * 狐酿炸弹落点铺开的酒池:自家女仆泡在其中持续回魔,敌对生物反复刷新微醺。
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

    /**
     * 池中女仆回魔比例:每次重涂(10 tick)回 2.5% 最大魔力,约合每秒 5%。
     */
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
     * 父类会豁免 owner 的盟友,但酒池要对自家女仆生效(回魔),故显式放行;其余仍走父类判定。
     * <p>
     * 自家女仆判定必须排在 owner 豁免之前:女仆自己扔的炸弹,owner 就是她本人,
     * 先判 owner 会把造池的那个女仆挡在自己的池外(只有她一个不回魔)。
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        Entity owner = getOwner();
        if (WinefoxHexHelper.isMaidOwnedBy(target, owner)) {
            return target.canBeHitByProjectile();
        }
        if (target == owner) {
            return false;
        }
        return super.canHitEntity(target);
    }

    /**
     * 半径 2.5 的池面约每 tick 冒 1 颗泡,泡自身寿命 30~55 tick,池面常年浮着十来颗。
     */
    @Override
    public float getParticleCount() {
        return 0.4f;
    }

    /**
     * 酒泡:从池面升起、摇摆、破裂,肥皂泡观感(上浮逻辑在粒子自身)。
     */
    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.of(WsbParticles.WINE_BUBBLE.get());
    }
}
