package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.api.animation.IMagicCastingState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import java.util.Objects;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.adapter.MagicMaidAdapter;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.magic.MaidSummonManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 魔法女仆，参考 AbstractSpellCastingMob
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-20 23:45
 * @see io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob
 */
@Mixin(EntityMaid.class)
public abstract class MaidEntityMixin extends PathfinderMob implements IMagicEntity, MaidMagicEntity {
    @Shadow @Nullable public abstract LivingEntity getOwner();

    @Shadow public abstract BaubleItemHandler getMaidBauble();

    @Shadow public boolean guiOpening;

    @Shadow protected abstract void completeUsingItem();

    @Unique
    private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(IronsSpellbooks.id("potion_slowdown"), -0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

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

    public boolean getHasUsedSingleAttack() {
        return hasUsedSingleAttack;
    }

    @Override
    public void setHasUsedSingleAttack(boolean hasUsedSingleAttack) {
        this.hasUsedSingleAttack = hasUsedSingleAttack;
    }

    public MagicData getMagicData() {
        return magicAdapter.getMagicData();
    }

    public boolean isDrinkingPotion() {
        return magicAdapter.isDrinkingPotion();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (Objects.equals(getOwner(), entity)) {
            // 女仆和主人是相同的队伍
            return true;
        }
        if (entity instanceof TamableAnimal tamableAnimal) {
            LivingEntity owner = tamableAnimal.getOwner();
            if (Objects.equals(getOwner(), owner) || getOwner() != null && owner != null && getOwner().isAlliedTo(owner)) {
                // 主人相同，或都没有主人，或者主人是同一个队伍，认为是相同的队伍
                return true;
            }
        }
        if (entity instanceof IMagicSummon magicSummon) {
            // 召唤物
            Entity owner = magicSummon.getSummoner();
            if (owner instanceof TamableAnimal tamableAnimal) {
                // 召唤者是女仆
                LivingEntity ownerOfOwner = tamableAnimal.getOwner(); // 该女仆的主人
                if (Objects.equals(getOwner(), ownerOfOwner) || getOwner() != null && ownerOfOwner != null && getOwner().isAlliedTo(ownerOfOwner)) {
                    return true;
                }
            }
            if (Objects.equals(getOwner(), owner) || getOwner() != null && owner != null && getOwner().isAlliedTo(owner)) {
                // 召唤者相同，或都没有主人，或者召唤者和主人是同一个队伍，认为是相同的队伍
                return true;
            }
        }
        return super.isAlliedTo(entity);
    }

    public void startDrinkingPotion() {
        magicAdapter.startDrinkingPotion();
    }

    @Inject(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;backCurrentHandItemStack()V"))
    protected void beforeBackCurrentHandItemStack(CallbackInfo ci) {
        magicAdapter.completeUsingItem();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void afterAddAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        magicAdapter.getMagicData().saveNBTData(pCompound, level.registryAccess());
        pCompound.putBoolean("usedSpecial", hasUsedSingleAttack);
        pCompound.putBoolean("isDrinkingPotion", isDrinkingPotion());
        pCompound.putBoolean("usedItemOffhand", getUsedItemHand() == InteractionHand.OFF_HAND);
        pCompound.putInt("useItemRemaining", useItemRemaining);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void afterReadAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        MagicData magicData = magicAdapter.getMagicData();
        magicData.setSyncedData(new SyncedSpellData(self()));
        magicData.loadNBTData(pCompound, level.registryAccess());
        if (magicData.getSyncedData().isCasting()) {
            magicAdapter.setRecreateSpell(true);
        }
        hasUsedSingleAttack = pCompound.getBoolean("usedSpecial");
        // 恢复喝药水状态：通过设置 useItem 和 useItemRemaining 来恢复
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

    @Inject(method = "onAddedToLevel", at = @At("TAIL"))
    public void afterAddedToLevel(CallbackInfo ci) {
        if (level.isClientSide) {
            return;
        }

        MaidSummonManager.onMaidPlaced(self());
    }

    @Inject(method = "onRemovedFromLevel", at = @At("TAIL"))
    public void afterRemovedFromLevel(CallbackInfo ci) {
        MaidSummonManager.onMaidRemoved(self());
    }

    public void cancelCast() {
        magicAdapter.cancelCast();
    }

    public void castComplete() {
        magicAdapter.castComplete();
    }

    public void setSyncedSpellData(SyncedSpellData syncedSpellData) {
        magicAdapter.setSyncedSpellData(syncedSpellData);
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

    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        magicAdapter.initiateCastSpell(spell, spellLevel);
    }

    @Override
    public int winefoxsSpellbooks$getManaCost(AbstractSpell spell, int level) {
        return magicAdapter.winefoxsSpellbooks$getManaCost(spell, level);
    }

    public void notifyDangerousProjectile(Projectile projectile) {
        magicAdapter.notifyDangerousProjectile(projectile);
    }

    public boolean isCasting() {
        return magicAdapter.isCasting();
    }

    public boolean setTeleportLocationBehindTarget(int distance) {
        return magicAdapter.setTeleportLocationBehindTarget(distance);
    }

    public void setBurningDashDirectionData() {
        magicAdapter.setBurningDashDirectionData();
    }

    @Override
    public IMagicCastingState winefoxSpellbooks$getMagicCastingState() {
        return magicAdapter.winefoxSpellbooks$getMagicCastingState();
    }

    @Override
    public boolean winefoxsSpellbooks$getCancelCastAnimation() {
        return magicAdapter.winefoxsSpellbooks$getCancelCastAnimation();
    }

    @Override
    public void winefoxsSpellbooks$setCancelCastAnimation(boolean cancelCastAnimation) {
        magicAdapter.winefoxsSpellbooks$setCancelCastAnimation(cancelCastAnimation);
    }

    @Override
    public float winefoxsSpellbooks$getMana() {
        return magicAdapter.winefoxsSpellbooks$getMana();
    }

    @Override
    public void winefoxsSpellbooks$setMana(float mana) {
        magicAdapter.winefoxsSpellbooks$setMana(mana);
    }

    @Override
    public MaidSpellDataHolder winefoxsSpellbooks$getSpellDataHolder() {
        return magicAdapter.winefoxsSpellbooks$getSpellDataHolder();
    }
}
