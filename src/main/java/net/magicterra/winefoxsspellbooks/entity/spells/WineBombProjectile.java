package net.magicterra.winefoxsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import java.util.List;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSpells;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 狐酿炸弹的投掷弹体:直击打出酒狐巫法学派伤害(可触发残月烙印引爆),
 * 落点(命中生物或方块均触发)铺开 {@link WinePoolCloud} 酒池。渲染借用
 * {@link ItemSupplier} 接口,以染色喷溅药水外观显示。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-06
 */
public class WineBombProjectile extends AbstractMagicProjectile implements ItemSupplier {

    public static final EntityType<WineBombProjectile> TYPE = EntityType.Builder
        .<WineBombProjectile>of(WineBombProjectile::new, MobCategory.MISC)
        .sized(0.35f, 0.35f)
        .clientTrackingRange(8)
        .build("wine_bomb");

    /** 落点酒池半径(格)。 */
    private static final float POOL_RADIUS = 2.5f;
    /** 酒池持续时间(5s)。 */
    private static final int POOL_DURATION_TICKS = 100;

    private static final DustParticleOptions WINE_DUST =
        new DustParticleOptions(WinefoxHexHelper.SCHOOL_COLOR_VEC, 1.0f);

    /** 渲染外观:紫粉染色的喷溅药水,不含实际药水效果。 */
    private static final ItemStack RENDER_STACK = createRenderStack();

    private static ItemStack createRenderStack() {
        ItemStack stack = new ItemStack(Items.SPLASH_POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(WinefoxHexHelper.SCHOOL_COLOR), List.of()));
        return stack;
    }

    public WineBombProjectile(EntityType<? extends WineBombProjectile> entityType, Level level) {
        super(entityType, level);
    }

    /** 施法时构造:绑定施法者为 owner(用于伤害归因与酒池的女仆判定)。 */
    public WineBombProjectile(Level level, LivingEntity owner) {
        this(TYPE, level);
        this.setOwner(owner);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result); // 基类已处理 impactParticles/impact 音效与 onHitEntity/onHitBlock 分发
        if (this.level().isClientSide) {
            return;
        }

        Entity owner = this.getOwner();
        if (result instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity victim
                && victim != owner) {
            var spell = WsbSpells.VINTNERS_BOMB_SPELL.get();
            DamageSources.applyDamage(victim, this.getDamage(),
                spell.getDamageSource(this, WinefoxHexHelper.projectileAttribution(this)));
        }

        // 落点铺酒池:命中生物或方块都触发
        WinePoolCloud pool = new WinePoolCloud(this.level());
        pool.setOwner(owner instanceof LivingEntity le ? le : null);
        pool.setRadius(POOL_RADIUS);
        pool.setDuration(POOL_DURATION_TICKS);
        pool.moveTo(Utils.moveToRelativeGroundLevel(this.level(), result.getLocation(), 3));
        this.level().addFreshEntity(pool);
        this.discard();
    }

    @Override
    public ItemStack getItem() {
        return RENDER_STACK;
    }

    @Override
    public float getSpeed() {
        return 1.0f;
    }

    @Override
    public void trailParticles() {
        Vec3 v = this.getDeltaMovement();
        this.level().addParticle(WINE_DUST, this.getX(), this.getY(), this.getZ(),
            -v.x * 0.1, -v.y * 0.1, -v.z * 0.1);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, x, y, z, 20, 0.3, 0.2, 0.3, 0.1);
            serverLevel.sendParticles(WINE_DUST, x, y, z, 15, 0.3, 0.2, 0.3, 0.0);
        }
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }
}
