package net.magicterra.winefoxsspellbooks.entity.adapter;

import com.github.tartaricacid.touhoulittlemaid.api.animation.IMagicCastingState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.fire.BurningDashSpell;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.client.animation.MagicCastingAnimateStateHolder;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicData;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.registry.InitAttachments;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.wrapper.EntityHandsInvWrapper;

/**
 * 魔法女仆适配器
 * <br />
 * 使用细节: <br />
 * 使用方需要在 Entity 对应的 Override 方法调用此类的: <br />
 * {@link net.magicterra.winefoxsspellbooks.entity.adapter.MagicMaidAdapter#completeUsingItem()} <br />
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-21 00:45
 */
public class MagicMaidAdapter implements MaidMagicEntity, IMagicEntity {
    private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(IronsSpellbooks.id("potion_slowdown"), -0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private final LivingEntity delegatedEntity;

    private final MagicData maidMagicData;

    private final MagicCastingAnimateStateHolder magicCastingAnimateStateHolder;

    private SpellData castingSpell = SpellData.EMPTY;

    /**
     * 是否需要恢复法术状态
     */
    private boolean recreateSpell = false;

    private boolean cancelCastAnimation = false;

    private boolean isDrinking = false;

    private boolean hasUsedSingleAttack;


    public MagicMaidAdapter(LivingEntity delegatedEntity) {
        this.delegatedEntity = delegatedEntity;
        this.maidMagicData = new MaidMagicData(delegatedEntity);
        this.magicCastingAnimateStateHolder = new MagicCastingAnimateStateHolder(IMagicCastingState.CastingPhase.NONE);
    }

    @Override
    public MagicData getMagicData() {
        return maidMagicData;
    }

    @Override
    public void setSyncedSpellData(SyncedSpellData syncedSpellData) {
        if (!delegatedEntity.level.isClientSide) {
            return;
        }

        magicCastingAnimateStateHolder.updateState(delegatedEntity, syncedSpellData);

        var isCasting = maidMagicData.isCasting();
        maidMagicData.setSyncedData(syncedSpellData);
        castingSpell = maidMagicData.getCastingSpell();

        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("MagicMaidAdapter.setSyncedSpellData playerMagicData:{}, priorIsCastingState:{}, spell:{}", maidMagicData, isCasting, castingSpell);
        }

        if (castingSpell == SpellData.EMPTY) {
            return;
        }

        if (!maidMagicData.isCasting() && isCasting) {
            castComplete();
        } else if (maidMagicData.isCasting() && !isCasting) {
            var spell = maidMagicData.getCastingSpell().getSpell();

            initiateCastSpell(spell, maidMagicData.getCastingSpellLevel());

            if (castingSpell.getSpell().getCastType() == CastType.INSTANT) {
                castingSpell.getSpell().onClientPreCast(delegatedEntity.level, castingSpell.getLevel(), delegatedEntity, InteractionHand.MAIN_HAND, maidMagicData);
                castComplete();
            }
        }
    }

    @Override
    public boolean isCasting() {
        return maidMagicData.isCasting();
    }

    @Override
    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("MagicMaidAdapter.initiateCastSpell: spellType:{} spellLevel:{}, isClient:{}", spell.getSpellId(), spellLevel, delegatedEntity.level.isClientSide);
        }

        if (spell == SpellRegistry.none()) {
            castingSpell = SpellData.EMPTY;
            return;
        }

        if (delegatedEntity.level.isClientSide) {
            cancelCastAnimation = false;
        }

        castingSpell = new SpellData(spell, spellLevel);

        LivingEntity target = getTargetFromBrain();
        if (target != null) {
            delegatedEntity.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            delegatedEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
        }

        if (delegatedEntity.isUsingItem()) {
            delegatedEntity.stopUsingItem();
        }

        if (!delegatedEntity.level.isClientSide && !(canCast(spell, spellLevel) && castingSpell.getSpell().checkPreCastConditions(delegatedEntity.level, spellLevel, delegatedEntity, maidMagicData))) {
            if (WinefoxsSpellbooks.DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("MagicMaidAdapter.precastfailed: spellType:{} spellLevel:{}, isClient:{}", spell.getSpellId(), spellLevel, delegatedEntity.level.isClientSide);
            }

            castingSpell = SpellData.EMPTY;
            return;
        }

        if (spell == SpellRegistry.TELEPORT_SPELL.get() || spell == SpellRegistry.FROST_STEP_SPELL.get() || spell.getSpellId().equals("discerning_the_eldritch:otherworldly_presence")) {
            setTeleportLocationBehindTarget(10);
        } else if (spell == SpellRegistry.BLOOD_STEP_SPELL.get()) {
            setTeleportLocationBehindTarget(3);
        } else if (spell == SpellRegistry.BURNING_DASH_SPELL.get()) {
            setBurningDashDirectionData();
        }

        maidMagicData.initiateCast(castingSpell.getSpell(), castingSpell.getLevel(), castingSpell.getSpell().getEffectiveCastTime(castingSpell.getLevel(), delegatedEntity), CastSource.MOB, SpellSelectionManager.MAINHAND);
        maidMagicData.setPlayerCastingItem(delegatedEntity.getMainHandItem());

        if (!delegatedEntity.level.isClientSide) {
            castingSpell.getSpell().onServerPreCast(delegatedEntity.level, castingSpell.getLevel(), delegatedEntity, maidMagicData);
        }
    }

    @Override
    public void cancelCast() {
        if (isCasting()) {
            if (delegatedEntity.level.isClientSide) {
                cancelCastAnimation = true;
            }
            castComplete();
        }
    }

    @Override
    public void castComplete() {
        if (!delegatedEntity.level.isClientSide) {
            if (castingSpell != SpellData.EMPTY) {
                castingSpell.getSpell().onServerCastComplete(delegatedEntity.level, castingSpell.getLevel(), delegatedEntity, maidMagicData, false);
            }
        } else {
            maidMagicData.resetCastingState();
        }

        castingSpell = SpellData.EMPTY;
    }

    @Override
    public void notifyDangerousProjectile(Projectile projectile) {

    }

    @Override
    public boolean setTeleportLocationBehindTarget(int distance) {
        var target = getTargetFromBrain();
        boolean valid = false;
        if (target != null) {
            var rotation = target.getLookAngle().normalize().scale(-distance);
            var pos = target.position();
            var teleportPos = rotation.add(pos);

            for (int i = 0; i < 24; i++) {
                Vec3 randomness = Utils.getRandomVec3(.15f * i).multiply(1, 0, 1);
                teleportPos = Utils.moveToRelativeGroundLevel(delegatedEntity.level, target.position().subtract(new Vec3(0, 0, distance / (float) (i / 7 + 1)).yRot(-(target.getYRot() + i * 45) * Mth.DEG_TO_RAD)).add(randomness), 5);
                teleportPos = new Vec3(teleportPos.x, teleportPos.y + .1f, teleportPos.z);
                var reposBB = delegatedEntity.getBoundingBox().move(teleportPos.subtract(delegatedEntity.position()));
                if (!delegatedEntity.level.collidesWithSuffocatingBlock(delegatedEntity, reposBB.inflate(-.05f))) {
                    valid = true;
                    break;
                }
            }
            if (valid) {
                maidMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(teleportPos));
            } else {
                maidMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(delegatedEntity.position()));
            }
        } else {
            maidMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(delegatedEntity.position()));
        }
        return valid;
    }

    @Override
    public void setBurningDashDirectionData() {
        maidMagicData.setAdditionalCastData(new BurningDashSpell.BurningDashDirectionOverrideCastData());
    }

    @Override
    @SuppressWarnings("removal")
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return delegatedEntity.getItemBySlot(pSlot);
    }

    @Override
    public boolean isDrinkingPotion() {
        return isDrinking;
    }

    @Override
    public boolean getHasUsedSingleAttack() {
        return hasUsedSingleAttack;
    }

    @Override
    public void setHasUsedSingleAttack(boolean hasUsedSingleAttack) {
        this.hasUsedSingleAttack = hasUsedSingleAttack;
    }

    @Override
    public void startDrinkingPotion() {
        if (!delegatedEntity.level.isClientSide) {
            isDrinking = true;
            if (delegatedEntity instanceof EntityMaid maid) {
                // 对手部进行处理：如果没有空的手部，那就取副手
                InteractionHand eatHand = InteractionHand.OFF_HAND;
                for (InteractionHand hand : InteractionHand.values()) {
                    if (maid.getItemInHand(hand).isEmpty()) {
                        eatHand = hand;
                        break;
                    }
                }
                ItemStack itemInHand = maid.getItemInHand(eatHand);
                ItemStack handStack = itemInHand.copy();
                ItemStack potionStack = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
                maid.setItemInHand(eatHand, potionStack);
                // itemInHand = maid.getItemInHand(eatHand);
                if (!handStack.isEmpty()) {
                    maid.memoryHandItemStack(handStack);
                }
                maid.startUsingItem(eatHand);
            } else {
                ItemStack potionStack = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
                delegatedEntity.setItemInHand(InteractionHand.OFF_HAND, potionStack);
                delegatedEntity.startUsingItem(InteractionHand.OFF_HAND);
            }
            AttributeInstance attributeinstance = delegatedEntity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeinstance != null) {
                attributeinstance.removeModifier(SPEED_MODIFIER_DRINKING);
                attributeinstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
            }
        }
    }

    /**
     * 需要使用方调用此事件，注意要在恢复女仆隐藏物品栏之前
     */
    public void completeUsingItem() {
        if (delegatedEntity instanceof EntityMaid maid) {
            EntityHandsInvWrapper handsInvWrapper = maid.getHandsInvWrapper();
            for (int i = 0; i < handsInvWrapper.getSlots(); i++) {
                ItemStack itemStack = handsInvWrapper.getStackInSlot(i);
                if (isDrinkingPotion()) {
                    if (itemStack.is(Items.GLASS_BOTTLE)) {
                        handsInvWrapper.setStackInSlot(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }
        finishDrinkingPotion();
    }

    private void finishDrinkingPotion() {
        isDrinking = false;
        delegatedEntity.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING);
        if (!delegatedEntity.isSilent()) {
            delegatedEntity.level.playSound(null, delegatedEntity.getX(), delegatedEntity.getY(), delegatedEntity.getZ(), SoundEvents.WITCH_DRINK, delegatedEntity.getSoundSource(), 1.0F, 0.8F + delegatedEntity.getRandom().nextFloat() * 0.4F);
        }
    }

    // ==================== tick 和 customServerAiStep 通用逻辑 ====================

    /**
     * 处理 tick 中的通用魔法逻辑
     * <br />
     * 包括: 魔力恢复、冷却更新、重铸更新
     * <br />
     * 使用方需要在服务端 tick 中调用此方法
     *
     * @param serverTickCount 服务器 tick 计数 (通常是 level.getServer().getTickCount())
     */
    public void tickMagicLogic(int serverTickCount) {
        if (delegatedEntity.level.isClientSide) {
            return;
        }

        // 魔力恢复
        boolean doManaRegen = serverTickCount % MaidMagicManager.MANA_REGEN_TICKS == 0;
        if (doManaRegen) {
            MaidMagicManager.regenMana(delegatedEntity, maidMagicData);
            delegatedEntity.setData(InitAttachments.MAID_MANA, maidMagicData.getMana());
        }

        // 冷却更新
        maidMagicData.getPlayerCooldowns().tick(1);
        maidMagicData.getPlayerRecasts().tick(2);
    }

    /**
     * 处理 customServerAiStep 中的通用施法逻辑
     * <br />
     * 包括: 法术状态恢复、施法处理
     * <br />
     * 使用方需要在服务端 customServerAiStep 中调用此方法
     */
    public void aiStepMagicLogic() {
        if (delegatedEntity.level.isClientSide) {
            return;
        }

        // 恢复法术状态
        if (recreateSpell) {
            recreateSpell = false;
            SyncedSpellData syncedSpellData = maidMagicData.getSyncedData();
            var spell = SpellRegistry.getSpell(syncedSpellData.getCastingSpellId());
            this.initiateCastSpell(spell, syncedSpellData.getCastingSpellLevel());
        }

        // 处理施法逻辑
        handleCastingLogic();
    }

    /**
     * 处理施法逻辑
     */
    private void handleCastingLogic() {
        if (castingSpell == SpellData.EMPTY) {
            return;
        }

        if (maidMagicData.isCasting()) {
            maidMagicData.handleCastDuration();
            var spell = castingSpell.getSpell();

            // 施法期间强制朝向目标（仿照 AbstractSpellCastingMob.forceLookAtTarget）
            LivingEntity target = getTargetFromBrain();
            forceLookAtTarget(target);

            if ((spell.getCastType() == CastType.LONG && !delegatedEntity.isUsingItem()) || spell.getCastType() == CastType.INSTANT) {
                if (maidMagicData.getCastDurationRemaining() <= 0) {
                    castSpell(spell, maidMagicData.getCastingSpellLevel(), true);
                    castComplete();
                }
            } else if (spell.getCastType() == CastType.CONTINUOUS) {
                if ((maidMagicData.getCastDurationRemaining() + 1) % MaidMagicManager.CONTINUOUS_CAST_TICK_INTERVAL == 0) {
                    if (maidMagicData.getCastDurationRemaining() < MaidMagicManager.CONTINUOUS_CAST_TICK_INTERVAL ||
                        (maidMagicData.getCastSource().consumesMana() &&
                         maidMagicData.getMana() - getManaCost(spell, maidMagicData.getCastingSpellLevel()) * 2 < 0)) {
                        castSpell(spell, maidMagicData.getCastingSpellLevel(), true);
                        castComplete();
                    } else {
                        castSpell(spell, maidMagicData.getCastingSpellLevel(), false);
                    }
                }
            }

            if (maidMagicData.isCasting()) {
                spell.onServerCastTick(delegatedEntity.level, castingSpell.getLevel(), delegatedEntity, maidMagicData);
            }
        }
    }

    /**
     * 强制朝向目标
     * <p>
     * 仿照 AbstractSpellCastingMob.forceLookAtTarget()，直接设置实体的 XRot 和 YRot。
     * 这对于需要精确瞄准的投射物法术至关重要。
     * <p>
     * 同时更新 LOOK_TARGET 记忆，确保与 Brain 系统保持一致。
     *
     * @param target 目标实体
     */
    private void forceLookAtTarget(LivingEntity target) {
        if (target == null) {
            return;
        }
        double dx = target.getX() - delegatedEntity.getX();
        double dz = target.getZ() - delegatedEntity.getZ();
        double dy = target.getEyeY() - delegatedEntity.getEyeY();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        float yRot = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float xRot = (float) (-(Mth.atan2(dy, horizontalDist) * Mth.RAD_TO_DEG));

        delegatedEntity.setXRot(xRot % 360.0F);
        delegatedEntity.setYRot(yRot % 360.0F);

        // 同时更新 LOOK_TARGET 记忆，确保与 Brain 系统保持一致
        // 使用 setMemoryInternal 避免记忆未注册时抛出异常
        if (delegatedEntity instanceof Mob mob) {
            Brain<?> brain = mob.getBrain();
            if (brain.checkMemory(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED)) {
                brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
            }
        }
    }

    /**
     * 执行法术
     *
     * @param spell 要执行的法术
     * @param spellLevel 法术等级
     * @param triggerCooldown 是否触发冷却
     */
    public void castSpell(AbstractSpell spell, int spellLevel, boolean triggerCooldown) {
        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("MagicMaidAdapter.castSpell isClient:{}, spell{}({})",
                delegatedEntity.level.isClientSide, spell.getSpellId(), spellLevel);
        }

        var mobRecasts = maidMagicData.getPlayerRecasts();
        var mobAlreadyHasRecast = mobRecasts.hasRecastForSpell(spell.getSpellId());

        if (!mobAlreadyHasRecast) {
            float manaCost = getManaCost(spell, spellLevel);
            var newMana = Math.max(maidMagicData.getMana() - manaCost, 0);
            winefoxsSpellbooks$setMana(newMana);
        }

        spell.onCast(delegatedEntity.level, spellLevel, delegatedEntity, CastSource.MOB, maidMagicData);

        var mobHasRecastsLeft = mobRecasts.hasRecastForSpell(spell);
        if (mobAlreadyHasRecast && mobHasRecastsLeft) {
            mobRecasts.decrementRecastCount(spell);
        } else if (!mobHasRecastsLeft && triggerCooldown) {
            MaidMagicManager.addCooldown(delegatedEntity, spell, CastSource.MOB);
        }
    }

    public void setRecreateSpell(boolean recreateSpell) {
        this.recreateSpell = recreateSpell;
    }

    public boolean isRecreateSpell() {
        return recreateSpell;
    }

    protected final LivingEntity getTargetFromBrain() {
        Brain<?> brain = delegatedEntity.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        } else {
            return null;
        }
    }

    /**
     * 检查冷却和魔力值
     */
    protected boolean canCast(AbstractSpell spell, int spellLevel) {
        var maidMana = maidMagicData.getMana();
        var manaCost = getManaCost(spell, spellLevel);

        boolean hasEnoughMana = maidMana - manaCost >= 0;
        boolean isSpellOnCooldown = maidMagicData.getPlayerCooldowns().isOnCooldown(spell);
        boolean hasRecastForSpell = maidMagicData.getPlayerRecasts().hasRecastForSpell(spell.getSpellId());
        if (isSpellOnCooldown) {
            // 冷却中
            if (WinefoxsSpellbooks.DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("MagicMaidAdapter.canCast: spellType:{} spellLevel:{}, isCooldown:true", spell.getSpellId(), spellLevel);
            }
            return false;
        }
        if (!hasRecastForSpell && !hasEnoughMana) {
            // 魔力不足
            if (WinefoxsSpellbooks.DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("MagicMaidAdapter.canCast: spellType:{} spellLevel:{}, maidMana:{}, manaCost:{}", spell.getSpellId(), spellLevel, maidMana, manaCost);
            }
            return false;
        }
        return true;
    }

    private int getManaCost(AbstractSpell spell, int level) {
        return spell.getManaCost(level);
    }

    @Override
    public IMagicCastingState winefoxSpellbooks$getMagicCastingState() {
        return magicCastingAnimateStateHolder;
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
        return delegatedEntity.getData(InitAttachments.MAID_MANA);
    }

    @Override
    public void winefoxsSpellbooks$setMana(float mana) {
        maidMagicData.setMana(mana);
        delegatedEntity.setData(InitAttachments.MAID_MANA, maidMagicData.getMana());
    }

    @Override
    public int winefoxsSpellbooks$getManaCost(AbstractSpell spell, int level) {
        return getManaCost(spell, level);
    }

    @Override
    public MaidSpellDataHolder winefoxsSpellbooks$getSpellDataHolder() {
        return delegatedEntity.getData(InitAttachments.MAID_SPELL_DATA);
    }
}
