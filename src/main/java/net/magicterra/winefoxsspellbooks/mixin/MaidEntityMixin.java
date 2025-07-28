package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.fire.BurningDashSpell;
import java.util.Objects;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.bauble.SpellBookAwareSlotItemHandler;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicData;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.wrapper.EntityHandsInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    //private static final EntityDataAccessor<SyncedSpellData> DATA_SPELL = SynchedEntityData.defineId(AbstractSpellCastingMob.class, SyncedSpellData.SYNCED_SPELL_DATA);
    @Unique
    private static final EntityDataAccessor<Boolean> DATA_CANCEL_CAST = SynchedEntityData.defineId(MaidEntityMixin.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> DATA_DRINKING_POTION = SynchedEntityData.defineId(MaidEntityMixin.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Float> DATA_MANA = SynchedEntityData.defineId(MaidEntityMixin.class, EntityDataSerializers.FLOAT);

    @Unique
    private final MagicData playerMagicData = new MaidMagicData((EntityMaid) (Object) this);

    @Unique
    private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(IronsSpellbooks.id("potion_slowdown"), -0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    @Unique
    private @Nullable SpellData castingSpell;

    @Unique
    private int drinkTime;

    @Unique
    public boolean hasUsedSingleAttack;

    @Unique
    private boolean recreateSpell;

    @Unique
    private AbstractSpell instantCastSpellType = SpellRegistry.none();

    @Unique
    private boolean cancelCastAnimation = false;

    protected MaidEntityMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }


    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private static void createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        AttributeSupplier.Builder builder = cir.getReturnValue()
            .add(AttributeRegistry.MAX_MANA)
            .add(AttributeRegistry.MANA_REGEN)
            .add(AttributeRegistry.COOLDOWN_REDUCTION);
        cir.setReturnValue(builder);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
    public void init(Level worldIn, CallbackInfo ci) {
        EntityMaid self = (EntityMaid) (Object) this;
        playerMagicData.setSyncedData(new SyncedSpellData(self));
        self.noCulling = true;
    }

    public boolean getHasUsedSingleAttack() {
        return hasUsedSingleAttack;
    }

    @Override
    public void setHasUsedSingleAttack(boolean hasUsedSingleAttack) {
        this.hasUsedSingleAttack = hasUsedSingleAttack;
    }

    public MagicData getMagicData() {
        return playerMagicData;
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    protected void afterDefineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        //pBuilder.define(DATA_SPELL, new SyncedSpellData(-1));
        builder.define(DATA_CANCEL_CAST, false);
        builder.define(DATA_DRINKING_POTION, false);
        builder.define(DATA_MANA, 1.0f);
    }

    public boolean isDrinkingPotion() {
        return entityData.get(DATA_DRINKING_POTION);
    }

    @Unique
    protected void setDrinkingPotion(boolean drinkingPotion) {
        this.entityData.set(DATA_DRINKING_POTION, drinkingPotion);
    }

    @Override
    public boolean canBeLeashed() {
        // 是否能被拴绳拴住
        return false;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
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
        if (!level.isClientSide) {
            EntityMaid maid = (EntityMaid) (Object) this;
            // 对手部进行处理：如果没有空的手部，那就取副手
            InteractionHand eanHand = InteractionHand.OFF_HAND;
            for (InteractionHand hand : InteractionHand.values()) {
                if (maid.getItemInHand(hand).isEmpty()) {
                    eanHand = hand;
                    break;
                }
            }
            ItemStack itemInHand = maid.getItemInHand(eanHand);
            ItemStack handStack = itemInHand.copy();
            ItemStack potionStack = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
            maid.setItemInHand(eanHand, potionStack);
            itemInHand = maid.getItemInHand(eanHand);
            if (!handStack.isEmpty()) {
                maid.memoryHandItemStack(handStack);
            }
            maid.startUsingItem(eanHand);

            setDrinkingPotion(true);
            drinkTime = itemInHand.getUseDuration(maid);
            AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attributeinstance.removeModifier(SPEED_MODIFIER_DRINKING);
            attributeinstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
        }
    }

    @Unique
    private void finishDrinkingPotion() {
        setDrinkingPotion(false);
//        this.heal(Math.min(Math.max(10, getMaxHealth() / 10), getMaxHealth() / 4));
        this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING);
        if (!this.isSilent()) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }
    }

    @Inject(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;backCurrentHandItemStack()V"))
    protected void beforeBackCurrentHandItemStack(CallbackInfo ci) {
        EntityMaid maid = (EntityMaid) (Object) this;
        EntityHandsInvWrapper handsInvWrapper = maid.getHandsInvWrapper();
        for (int i = 0; i < handsInvWrapper.getSlots(); i++) {
            ItemStack itemStack = handsInvWrapper.getStackInSlot(i);
            if (itemStack.is(Items.GLASS_BOTTLE) && isDrinkingPotion()) {
                handsInvWrapper.setStackInSlot(i, ItemStack.EMPTY);
                break;
            }
        }
    }

    @Inject(method = "onSyncedDataUpdated", at = @At("TAIL"))
    public void afterOnSyncedDataUpdated(EntityDataAccessor<?> pKey, CallbackInfo ci) {
        if (!level.isClientSide) {
            return;
        }

        if (pKey.id() == DATA_CANCEL_CAST.id()) {
            if (WinefoxsSpellbooks.DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("ASCM.onSyncedDataUpdated.1 this.isCasting:{}, playerMagicData.isCasting:{} isClient:{}", isCasting(), playerMagicData == null ? "null" : playerMagicData.isCasting(), this.level.isClientSide());
            }
            cancelCast();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void afterAddAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        playerMagicData.getSyncedData().saveNBTData(pCompound, level.registryAccess());
        pCompound.putBoolean("usedSpecial", hasUsedSingleAttack);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void afterReadAdditionalSaveData(CompoundTag pCompound, CallbackInfo ci) {
        super.readAdditionalSaveData(pCompound);
        var syncedSpellData = new SyncedSpellData(this);
        syncedSpellData.loadNBTData(pCompound, level.registryAccess());
        if (syncedSpellData.isCasting()) {
            this.recreateSpell = true;
        }
        playerMagicData.setSyncedData(syncedSpellData);
        hasUsedSingleAttack = pCompound.getBoolean("usedSpecial");

        if (level.isClientSide) {
            return;
        }
        for (int i = 0; i < getMaidBauble().getSlots(); i++) {
            ItemStack stackInSlot = getMaidBauble().getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                SpellBookAwareSlotItemHandler.onBookInstall(self(), stackInSlot);
            }
        }
    }

    @Inject(method = "onEquipItem", at = @At("HEAD"))
    public void onEquipItem(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci) {
        if (!level.isClientSide) {
            return;
        }
        if (oldItem.isEmpty() && !newItem.isEmpty()) {
            ItemAttributeModifiers attributeModifiers = newItem.getAttributeModifiers();
            var map = attributeModifiersToMap(attributeModifiers);
            if (!map.isEmpty()) {
                getAttributes().addTransientAttributeModifiers(map);
            }
        } else if (!oldItem.isEmpty() && newItem.isEmpty()) {
            ItemAttributeModifiers attributeModifiers = oldItem.getAttributeModifiers();
            var map = attributeModifiersToMap(attributeModifiers);
            if (!map.isEmpty()) {
                getAttributes().removeAttributeModifiers(map);
            }
        }
    }

    @Unique
    private static Multimap<Holder<Attribute>, AttributeModifier> attributeModifiersToMap(ItemAttributeModifiers attributeModifiers) {
        Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
        for (ItemAttributeModifiers.Entry entry : attributeModifiers.modifiers()) {
            map.put(entry.attribute(), entry.modifier());
        }
        return map;
    }

    public void cancelCast() {
        if (isCasting()) {
            if (level.isClientSide) {
                cancelCastAnimation = true;
            } else {
                //Need to ensure we pass a different value if we want the data to sync
                entityData.set(DATA_CANCEL_CAST, !entityData.get(DATA_CANCEL_CAST));
            }

            castComplete();
        }

    }

    public void castComplete() {
        if (!level.isClientSide) {
            if (castingSpell != null) {
                castingSpell.getSpell().onServerCastComplete(level, castingSpell.getLevel(), this, playerMagicData, false);
            }
        } else {
            playerMagicData.resetCastingState();
        }

        castingSpell = null;
    }

    public void setSyncedSpellData(SyncedSpellData syncedSpellData) {
        if (!level.isClientSide) {
            return;
        }

        var isCasting = playerMagicData.isCasting();
        playerMagicData.setSyncedData(syncedSpellData);
        castingSpell = playerMagicData.getCastingSpell();

        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("ASCM.setSyncedSpellData playerMagicData:{}, priorIsCastingState:{}, spell:{}", playerMagicData, isCasting, castingSpell);
        }

        if (castingSpell == null) {
            return;
        }

        if (!playerMagicData.isCasting() && isCasting) {
            castComplete();
        } else if (playerMagicData.isCasting() && !isCasting)/* if (syncedSpellData.getCastingSpellType().getCastType() == CastType.CONTINUOUS)*/ {
            var spell = playerMagicData.getCastingSpell().getSpell();

            initiateCastSpell(spell, playerMagicData.getCastingSpellLevel());

            if (castingSpell.getSpell().getCastType() == CastType.INSTANT) {
                instantCastSpellType = castingSpell.getSpell();
                castingSpell.getSpell().onClientPreCast(level, castingSpell.getLevel(), this, InteractionHand.MAIN_HAND, playerMagicData);
                castComplete();
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/TamableAnimal;tick()V"))
    protected void beforeAnimalTick(CallbackInfo ci) {
        EntityMaid maid = (EntityMaid) self();
        if (level.isClientSide) {
            return;
        }
        boolean doManaRegen = level.getServer().getTickCount() % MaidMagicManager.MANA_REGEN_TICKS == 0;
        if (doManaRegen) {
            MaidMagicManager.regenMaidMana(maid, playerMagicData);
            entityData.set(DATA_MANA, playerMagicData.getMana());
        }
        playerMagicData.getPlayerCooldowns().tick(1);
        playerMagicData.getPlayerRecasts().tick(2);
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    protected void afterCustomServerAiStep(CallbackInfo ci) {
        if (recreateSpell) {
            recreateSpell = false;
            var syncedSpellData = playerMagicData.getSyncedData();
            var spell = SpellRegistry.getSpell(syncedSpellData.getCastingSpellId());
            this.initiateCastSpell(spell, syncedSpellData.getCastingSpellLevel());
            //setSyncedSpellData(syncedSpellData);
        }

        if (isDrinkingPotion()) {
            if (drinkTime-- <= 0) {
                finishDrinkingPotion();
            } else if (drinkTime % 4 == 0) {
                if (!this.isSilent()) {
                    this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_DRINK, this.getSoundSource(), 1.0F, Utils.random.nextFloat() * 0.1F + 0.9F);
                }
            }
        }

        if (castingSpell == null) {
            return;
        }

        if (playerMagicData.isCasting()) {
            playerMagicData.handleCastDuration();
            var spell = castingSpell.getSpell();
            if ((spell.getCastType() == CastType.LONG && !isUsingItem()) || spell.getCastType() == CastType.INSTANT) {
                if (playerMagicData.getCastDurationRemaining() <= 0) {
                    castSpell(spell, playerMagicData.getCastingSpellLevel(), true);
                    castComplete();
                }
            } else if (spell.getCastType() == CastType.CONTINUOUS) {
                if ((playerMagicData.getCastDurationRemaining() + 1) % MaidMagicManager.CONTINUOUS_CAST_TICK_INTERVAL == 0) {
                    if (playerMagicData.getCastDurationRemaining() < MaidMagicManager.CONTINUOUS_CAST_TICK_INTERVAL || (playerMagicData.getCastSource().consumesMana() && playerMagicData.getMana() - spell.getManaCost(playerMagicData.getCastingSpellLevel()) * 2 < 0)) {
                        castSpell(spell, playerMagicData.getCastingSpellLevel(), true);
                        castComplete();
                    } else {
                        castSpell(spell, playerMagicData.getCastingSpellLevel(), false);
                    }
                }
            }

            if (playerMagicData.isCasting()) {
                spell.onServerCastTick(level, castingSpell.getLevel(), this, playerMagicData);
            }
        }

        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("ASCM.customServerAiStep.1");
        }

        this.forceLookAtTarget(getTarget());
    }

    @Unique
    private void castSpell(AbstractSpell spell, int spellLevel, boolean triggerCooldown) {
        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("AbstractSpell.castSpell isClient:{}, spell{}({})", level.isClientSide, spell.getSpellId(), spellLevel);
        }

        var mobRecasts = playerMagicData.getPlayerRecasts();
        var mobAlreadyHasRecast = mobRecasts.hasRecastForSpell(spell.getSpellId());


        if (!mobAlreadyHasRecast) {
            float manaCost = getManaCost(spell, spellLevel);
            var newMana = Math.max(playerMagicData.getMana() - manaCost, 0);
            playerMagicData.setMana(newMana);
        }
        spell.onCast(level, spellLevel, this, CastSource.MOB, playerMagicData);

        //If onCast just added a recast then don't decrement it

        EntityMaid maid = (EntityMaid) self();
        var mobHasRecastsLeft = mobRecasts.hasRecastForSpell(spell);
        if (mobAlreadyHasRecast && mobHasRecastsLeft) {
            mobRecasts.decrementRecastCount(spell);
        } else if (!mobHasRecastsLeft && triggerCooldown) {
            MaidMagicManager.addCooldown(maid, spell, CastSource.MOB);
        }
    }

    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("ASCM.initiateCastSpell: spellType:{} spellLevel:{}, isClient:{}", spell.getSpellId(), spellLevel, level.isClientSide);
        }

        if (spell == SpellRegistry.none()) {
            castingSpell = null;
            return;
        }

        if (level.isClientSide) {
            cancelCastAnimation = false;
        }

        castingSpell = new SpellData(spell, spellLevel);

        if (getTarget() != null) {
            forceLookAtTarget(getTarget());
        }

        if (isUsingItem()) {
            stopUsingItem();
        }

        if (!level.isClientSide && !(canCast(spell, spellLevel) && castingSpell.getSpell().checkPreCastConditions(level, spellLevel, this, playerMagicData))) {
            if (WinefoxsSpellbooks.DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("ASCM.precastfailed: spellType:{} spellLevel:{}, isClient:{}", spell.getSpellId(), spellLevel, level.isClientSide);
            }

            castingSpell = SpellData.EMPTY;
            return;
        }

        if (spell == SpellRegistry.TELEPORT_SPELL.get() || spell == SpellRegistry.FROST_STEP_SPELL.get()) {
            setTeleportLocationBehindTarget(10);
        } else if (spell == SpellRegistry.BLOOD_STEP_SPELL.get()) {
            setTeleportLocationBehindTarget(3);
        } else if (spell == SpellRegistry.BURNING_DASH_SPELL.get()) {
            setBurningDashDirectionData();
        }

        playerMagicData.initiateCast(castingSpell.getSpell(), castingSpell.getLevel(), castingSpell.getSpell().getEffectiveCastTime(castingSpell.getLevel(), this), CastSource.MOB, SpellSelectionManager.MAINHAND);
        playerMagicData.setPlayerCastingItem(getMainHandItem());

        if (!level.isClientSide) {
            castingSpell.getSpell().onServerPreCast(level, castingSpell.getLevel(), this, playerMagicData);
        }
    }

    /**
     * 检查冷却和魔力值
     */
    @Unique
    public boolean canCast(AbstractSpell spell, int spellLevel) {
        var playerMana = playerMagicData.getMana();

        boolean hasEnoughMana = playerMana - getManaCost(spell, spellLevel) >= 0;
        boolean isSpellOnCooldown = playerMagicData.getPlayerCooldowns().isOnCooldown(spell);
        boolean hasRecastForSpell = playerMagicData.getPlayerRecasts().hasRecastForSpell(spell.getSpellId());
        if (isSpellOnCooldown) {
            // 冷却中
            return false;
        }
        if (!hasRecastForSpell && !hasEnoughMana) {
            // 魔力不足
            return false;
        }
        return true;
    }

    @Unique
    private int getManaCost(AbstractSpell spell, int level) {
        return spell.getManaCost(level);
    }

    @Override
    public int winefoxsSpellbooks$getManaCost(AbstractSpell spell, int level) {
        return getManaCost(spell, level);
    }

    public void notifyDangerousProjectile(Projectile projectile) {
    }

    public boolean isCasting() {
        return playerMagicData.isCasting();
    }

    public boolean setTeleportLocationBehindTarget(int distance) {
        var target = getTarget();
        boolean valid = false;
        if (target != null) {
            var rotation = target.getLookAngle().normalize().scale(-distance);
            var pos = target.position();
            var teleportPos = rotation.add(pos);

            for (int i = 0; i < 24; i++) {
                Vec3 randomness = Utils.getRandomVec3(.15f * i).multiply(1, 0, 1);
                teleportPos = Utils.moveToRelativeGroundLevel(level, target.position().subtract(new Vec3(0, 0, distance / (float) (i / 7 + 1)).yRot(-(target.getYRot() + i * 45) * Mth.DEG_TO_RAD)).add(randomness), 5);
                teleportPos = new Vec3(teleportPos.x, teleportPos.y + .1f, teleportPos.z);
                var reposBB = this.getBoundingBox().move(teleportPos.subtract(this.position()));
                //WinefoxsSpellbooks.LOGGER.debug("setTeleportLocationBehindTarget attempt to teleport to {}:", reposBB.getCenter());
                if (!level.collidesWithSuffocatingBlock(this, reposBB.inflate(-.05f))) {
                    //WinefoxsSpellbooks.LOGGER.debug("\n\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n\nsetTeleportLocationBehindTarget: {} {} {} empty. teleporting\n\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n\n", reposBB.minX, reposBB.minY, reposBB.minZ);
                    valid = true;
                    break;
                }
                //WinefoxsSpellbooks.LOGGER.debug("fail");

            }
            if (valid) {
                if (WinefoxsSpellbooks.DEBUG) {
                    //WinefoxsSpellbooks.LOGGER.debug("ASCM.setTeleportLocationBehindTarget: valid, pos:{}, isClient:{}", teleportPos, level.isClientSide());
                }
                playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(teleportPos));
            } else {
                if (WinefoxsSpellbooks.DEBUG) {
                    //WinefoxsSpellbooks.LOGGER.debug("ASCM.setTeleportLocationBehindTarget: invalid, pos:{}, isClient:{}", teleportPos, level.isClientSide());
                }
                playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(this.position()));

            }
        } else {
            if (WinefoxsSpellbooks.DEBUG) {
                //WinefoxsSpellbooks.LOGGER.debug("ASCM.setTeleportLocationBehindTarget: no target, isClient:{}", level.isClientSide());
            }
            playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(this.position()));
        }
        return valid;
    }

    public void setBurningDashDirectionData() {
        playerMagicData.setAdditionalCastData(new BurningDashSpell.BurningDashDirectionOverrideCastData());
    }

    @Unique
    private void forceLookAtTarget(LivingEntity target) {
        if (target != null) {
            double d0 = target.getX() - this.getX();
            double d2 = target.getZ() - this.getZ();
            double d1 = target.getEyeY() - this.getEyeY();

            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            float f = (float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
            float f1 = (float) (-(Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
            this.setXRot(f1 % 360);
            this.setYRot(f % 360);
        }
    }

    @Override
    public SpellData winefoxsSpellbooks$getCastingSpell() {
        return castingSpell;
    }

    @Override
    public AbstractSpell winefoxsSpellbooks$getInstantCastSpellType() {
        return instantCastSpellType;
    }

    @Override
    public void winefoxsSpellbooks$setInstantCastSpellType(AbstractSpell instantCastSpellType) {
        this.instantCastSpellType = instantCastSpellType;
    }

    @Override
    public boolean winefoxsSpellbooks$getCancelCastAnimation() {
        return cancelCastAnimation;
    }

    @Override
    public void winefoxsSpellbooks$setCancelCastAnimation(boolean cancelCastAnimation) {
        this.cancelCastAnimation = cancelCastAnimation;
    }

    @Override
    public float winefoxsSpellbooks$getMana() {
        return entityData.get(DATA_MANA);
    }
}
