package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import java.util.Objects;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.entity.MagicMaidDelegate;
import net.magicterra.winefoxsspellbooks.entity.adapter.MagicMaidAdapter;
import net.magicterra.winefoxsspellbooks.magic.MaidAllyHelper;
import net.magicterra.winefoxsspellbooks.magic.MaidSummonManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为 EntityMaid 挂上施法能力与 {@code MagicMaidAdapter}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-20 23:45
 */
@Mixin(EntityMaid.class)
public abstract class MaidEntityMixin extends PathfinderMob implements MagicMaidDelegate {
    @Shadow
    @Nullable
    public abstract LivingEntity getOwner();

    @Shadow
    public boolean guiOpening;

    @Shadow
    protected abstract void completeUsingItem();

    @Unique
    public boolean hasUsedSingleAttack;

    @Unique
    private MagicMaidAdapter magicAdapter;

    protected MaidEntityMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
    public void init(EntityType<EntityMaid> type, Level world, CallbackInfo ci) {
        EntityMaid self = (EntityMaid) (Object) this;
        magicAdapter = new MagicMaidAdapter(self);
        magicAdapter.getMagicData().setSyncedData(new SyncedSpellData(self));
    }

    /**
     * 覆写 {@link MagicMaidDelegate} 的默认实现：真·女仆的这个标志要随实体 NBT 存档 （{@code usedSpecial} 键），所以存在女仆自己身上而不是适配器里。
     */
    @Override
    public boolean getHasUsedSingleAttack() {
        return hasUsedSingleAttack;
    }

    @Override
    public void setHasUsedSingleAttack(boolean hasUsedSingleAttack) {
        this.hasUsedSingleAttack = hasUsedSingleAttack;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity.level().isClientSide) {
            return super.isAlliedTo(entity);
        }

        if (Objects.equals(getOwner(), entity)) {
            return true;
        }
        if (entity instanceof TamableAnimal tamableAnimal) {
            LivingEntity owner = tamableAnimal.getOwner();
            if (Objects.equals(getOwner(), owner) || getOwner() != null && owner != null && getOwner().isAlliedTo(owner)) {
                return true;
            }
        }
        if (entity instanceof IMagicSummon) {
            return MaidAllyHelper.isAllied(this, entity) || super.isAlliedTo(entity);
        }
        return super.isAlliedTo(entity);
    }

    @Inject(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;backCurrentHandItemStack()V"))
    protected void beforeBackCurrentHandItemStack(CallbackInfo ci) {
        magicAdapter.completeUsingItem();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void afterAddAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        CompoundTag magicData = new CompoundTag();
        magicAdapter.getMagicData().saveNBTData(magicData, level.registryAccess());
        pCompound.put("magicData", magicData);
        pCompound.putBoolean("usedSpecial", hasUsedSingleAttack);
        pCompound.putBoolean("isDrinkingPotion", isDrinkingPotion());
        pCompound.putBoolean("usedItemOffhand", getUsedItemHand() == InteractionHand.OFF_HAND);
        pCompound.putInt("useItemRemaining", useItemRemaining);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void afterReadAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        MagicData magicData = magicAdapter.getMagicData();
        magicData.setSyncedData(new SyncedSpellData(self()));
        magicData.loadNBTData(pCompound.getCompound("magicData"), level.registryAccess());
        if (magicData.getSyncedData().isCasting()) {
            magicAdapter.setRecreateSpell(true);
        }
        hasUsedSingleAttack = pCompound.getBoolean("usedSpecial");
        if (pCompound.getBoolean("isDrinkingPotion")) {
            useItemRemaining = pCompound.getInt("useItemRemaining");
            boolean isUsedItemOffhand = pCompound.getBoolean("usedItemOffhand");
            setLivingEntityFlag(2, isUsedItemOffhand);
            setLivingEntityFlag(1, true);
            useItem = getItemInHand(this.getUsedItemHand());
            if (useItemRemaining <= 0 || useItem.isEmpty()) {
                completeUsingItem();
            }
        }
    }

    // 两个钩子都不在这里判端：MaidSummonManager 里各自的入口已经先要一个 ServerLevel 才动召唤表，
    // 而召唤女仆走的是另一条调用路径（SummonedEntityMaid.onRemovedFromLevel），只有被调方拦得住两边
    @Inject(method = "onAddedToLevel", at = @At("TAIL"))
    public void afterAddedToLevel(CallbackInfo ci) {
        MaidSummonManager.onMaidPlaced(self());
    }

    @Inject(method = "onRemovedFromLevel", at = @At("TAIL"))
    public void afterRemovedFromLevel(CallbackInfo ci) {
        MaidSummonManager.onMaidRemoved(self());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/TamableAnimal;tick()V"))
    protected void beforeAnimalTick(CallbackInfo ci) {
        if (level.isClientSide) {
            return;
        }
        magicAdapter.tickMagicLogic(level.getServer().getTickCount());
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    protected void afterCustomServerAiStep(CallbackInfo ci) {
        if (guiOpening) {
            cancelCast();
            getNavigation().stop();
            getMoveControl().strafe(0, 0);
            return;
        }

        magicAdapter.aiStepMagicLogic();
    }

    // ==================== 施法委托 ====================

    /**
     * 这些方法本可全部依赖 {@link MagicMaidDelegate} 的接口默认实现，但 Java 方法解析规则是
     * 「祖先类的具体方法 > 接口默认方法」：一旦有第三方 mixin 往 {@link PathfinderMob}
     * 这类祖先类里塞了同名的具体方法（例如 irons_spells_js 的 PathfinderMobMixin 给
     * 所有 PathfinderMob 加了一份 IMagicEntity 空实现），EntityMaid 上的调用就会被解析到那份
     * 无关实现，而不是转发到 {@link #magicAdapter}。写成本类的具体覆写能保证解析结果不受
     * 祖先类被第三方注入了什么影响。
     */
    @Override
    public MagicData getMagicData() {
        return magicAdapter.getMagicData();
    }

    @Override
    public void setSyncedSpellData(SyncedSpellData syncedSpellData) {
        magicAdapter.setSyncedSpellData(syncedSpellData);
    }

    @Override
    public boolean isCasting() {
        return magicAdapter.isCasting();
    }

    @Override
    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        magicAdapter.initiateCastSpell(spell, spellLevel);
    }

    @Override
    public void cancelCast() {
        magicAdapter.cancelCast();
    }

    @Override
    public void castComplete() {
        magicAdapter.castComplete();
    }

    @Override
    public void notifyDangerousProjectile(Projectile projectile) {
        magicAdapter.notifyDangerousProjectile(projectile);
    }

    @Override
    public boolean setTeleportLocationBehindTarget(int distance) {
        return magicAdapter.setTeleportLocationBehindTarget(distance);
    }

    @Override
    public void setBurningDashDirectionData() {
        magicAdapter.setBurningDashDirectionData();
    }

    @Override
    public boolean isDrinkingPotion() {
        return magicAdapter.isDrinkingPotion();
    }

    @Override
    public void startDrinkingPotion() {
        magicAdapter.startDrinkingPotion();
    }

    /**
     * {@link MagicMaidDelegate} 唯一要求实现的方法。
     */
    @Override
    public MagicMaidAdapter winefoxsSpellbooks$getMagicMaidAdapter() {
        return magicAdapter;
    }
}
