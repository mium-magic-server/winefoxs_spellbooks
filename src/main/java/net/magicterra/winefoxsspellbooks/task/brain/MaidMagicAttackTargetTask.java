package net.magicterra.winefoxsspellbooks.task.brain;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

/**
 * 行为控制器
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-20 23:31
 */
public class MaidMagicAttackTargetTask extends Behavior<EntityMaid> {
    protected LivingEntity target;
    protected final double speedModifier;
    protected final int spellAttackIntervalMin;
    protected final int spellAttackIntervalMax;
    protected float spellcastingRange;
    protected float spellcastingRangeSqr;

    protected boolean hasLineOfSight;
    protected int seeTime = 0;
    protected int strafeTime;
    protected boolean strafingClockwise;
    protected int spellAttackDelay = -1;
    protected int projectileCount;

    protected AbstractSpell singleUseSpell = SpellRegistry.none();
    protected int singleUseDelay;
    protected int singleUseLevel;

    protected boolean isFlying;
    protected boolean allowFleeing; // 允许走位
    protected boolean allowMovement;
    protected int fleeCooldown;

    protected final ArrayList<SpellData> attackSpells = new ArrayList<>();
    protected final ArrayList<SpellData> defenseSpells = new ArrayList<>();
    protected final ArrayList<SpellData> movementSpells = new ArrayList<>();
    protected final ArrayList<SpellData> supportSpells = new ArrayList<>();
    protected ArrayList<SpellData> lastSpellCategory = attackSpells;
    protected SpellData currentSpell = SpellData.EMPTY;

    protected float minSpellQuality = .1f;
    protected float maxSpellQuality = .4f;

    protected boolean drinksPotions;
    protected final PathfinderMob mob;
    protected final IMagicEntity spellCastingMob;

    public MaidMagicAttackTargetTask(IMagicEntity abstractSpellCastingMob, boolean allowMovement) {
        super(ImmutableMap.of(
            MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
            MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
            MemoryModuleType.PATH, MemoryStatus.REGISTERED,
            MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ), 1200);

        this.spellCastingMob = abstractSpellCastingMob;
        if (abstractSpellCastingMob instanceof PathfinderMob m) {
            this.mob = m;
        } else {
            throw new IllegalStateException("Unable to add " + this.getClass().getSimpleName() + "to entity, must extend PathfinderMob.");
        }

        this.speedModifier = 1;
        this.spellAttackIntervalMin = 10; // 攻击间隔 0.5s
        this.spellAttackIntervalMax = 60; // 最长 3s
        this.spellcastingRange = 10;
        this.spellcastingRangeSqr = spellcastingRange * spellcastingRange;
        this.allowFleeing = false;
        this.allowMovement = allowMovement;

        this.setDrinksPotions();
    }

    public MaidMagicAttackTargetTask setSpells(List<SpellData> attackSpells, List<SpellData> defenseSpells, List<SpellData> movementSpells, List<SpellData> supportSpells) {
        this.attackSpells.clear();
        this.defenseSpells.clear();
        this.movementSpells.clear();
        this.supportSpells.clear();

        this.attackSpells.addAll(attackSpells);
        this.defenseSpells.addAll(defenseSpells);
        this.movementSpells.addAll(movementSpells);
        this.supportSpells.addAll(supportSpells);

        return this;
    }

    public MaidMagicAttackTargetTask setSpellQuality(float minSpellQuality, float maxSpellQuality) {
        this.minSpellQuality = minSpellQuality;
        this.maxSpellQuality = maxSpellQuality;
        return this;
    }

    public MaidMagicAttackTargetTask setSingleUseSpell(AbstractSpell abstractSpell, int minDelay, int maxDelay, int minLevel, int maxLevel) {
        this.singleUseSpell = abstractSpell;
        this.singleUseDelay = Utils.random.nextIntBetweenInclusive(minDelay, maxDelay);
        this.singleUseLevel = Utils.random.nextIntBetweenInclusive(minLevel, maxLevel);
        return this;
    }

    public MaidMagicAttackTargetTask setIsFlying() {
        isFlying = true;
        return this;
    }

    public MaidMagicAttackTargetTask setDrinksPotions() {
        drinksPotions = true;
        return this;
    }

    public MaidMagicAttackTargetTask setAllowFleeing(boolean allowFleeing) {
        this.allowFleeing = allowFleeing;
        return this;
    }

    public MaidMagicAttackTargetTask setAllowMovement(boolean allowMovement) {
        this.allowMovement = allowMovement;
        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid owner) {
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        owner.setTarget(target);
        this.target = target;
        if (target == null) {
            return false;
        }
        boolean condition = (owner.canSee(target) || seeTime > -50) && target.isAlive();
        if (!condition) {
            owner.setTarget(null);
            this.target = null;
            return false;
        }

        List<SpellData> attackSpells = new ArrayList<>();
        List<SpellData> defenseSpells = new ArrayList<>();
        List<SpellData> movementSpells = new ArrayList<>();
        List<SpellData> supportSpells = new ArrayList<>();
        boolean found = false;
        // 检查主手、盔甲栏、饰品栏法术 TODO 优化使用缓存
        boolean spellBookLoaded = false;
        IItemHandler invWrapper = new CombinedInvWrapper(owner.getArmorInvWrapper(), new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, owner.getMainHandItem())), owner.getMaidBauble());
        for (int i = 0; i < invWrapper.getSlots(); i++) {
            ItemStack stack = invWrapper.getStackInSlot(i);
            Item item = stack.getItem();
            if (item instanceof ISpellbook) {
                // 如果饰品栏放入多个魔法书，只有一本能生效（和玩家保持一致）
                if (spellBookLoaded) {
                    continue;
                } else {
                    spellBookLoaded = true;
                }
            }
            if (!stack.isEmpty() && ISpellContainer.isSpellContainer(stack)) {
                ISpellContainer spellContainer = ISpellContainer.get(stack);
                List<SpellSlot> activeSpells = spellContainer.getActiveSpells();
                for (SpellSlot spellSlot : activeSpells) {
                    AbstractSpell spell = spellSlot.getSpell();
                    int level = getLevelFor(owner, spell, spellSlot.getLevel());
                    SpellData spellData = new SpellData(spell, level);
                    if (MaidSpellRegistry.isAttackSpell(spell)) {
                        attackSpells.add(spellData);
                        found = true;
                    } else if (MaidSpellRegistry.isDefenseSpell(spell)) {
                        defenseSpells.add(spellData);
                        found = true;
                    } else if (MaidSpellRegistry.isMovementSpell(spell)) {
                        movementSpells.add(spellData);
                        found = true;
                    } else if (MaidSpellRegistry.isSupportSpell(spell)) {
                        supportSpells.add(spellData);
                        found = true;
                    }
                }
            }
        }
        condition = found;
        setSpells(attackSpells, defenseSpells, movementSpells, supportSpells);
        return condition;
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        return !entityIn.guiOpening && entityIn.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(worldIn, entityIn);
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        this.mob.setAggressive(true);
        initialCurrentSpell();
    }

    @Override
    protected void tick(ServerLevel worldIn, EntityMaid owner, long gameTime) {
        if (target == null) {
            return;
        }

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }

//        //search for projectiles around the mob
//        if (mob.tickCount % 3 == 0) {
//            projectileCount = mob.level.getEntitiesOfClass(Projectile.class, mob.getBoundingBox().inflate(24), (projectile) -> projectile.getOwner() != mob && !projectile.isOnGround()).size();
//        }

        //default mage movement
        if (allowMovement) {
            doMovement(distanceSquared);
        }

        //do attacks
        if (mob.getLastHurtByMobTimestamp() == mob.tickCount - 1) {
            spellAttackDelay = (int) (Mth.lerp(.6f, spellAttackDelay, 0) + 1);
        }

        //default attack timer
        handleAttackLogic(distanceSquared);

        singleUseDelay--;
    }

    @Override
    protected void stop(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
        target = null;
        seeTime = 0;
        spellAttackDelay = -1;
        mob.setAggressive(false);
        mob.getMoveControl().strafe(0, 0);
        mob.getNavigation().stop();
        entityIn.setTarget(null);
        if (spellCastingMob.isCasting()) {
            spellCastingMob.cancelCast();
        }
//        PlayerRecasts playerRecasts = spellCastingMob.getMagicData().getPlayerRecasts();
//        for (RecastInstance activeRecast : playerRecasts.getActiveRecasts()) {
//            spellCastingMob.initiateCastSpell(SpellRegistry.getSpell(activeRecast.getSpellId()), activeRecast.getSpellLevel());
//        }
    }

    protected void handleAttackLogic(double distanceSquared) {
        if (seeTime < -50) {
            return;
        }
        if (--this.spellAttackDelay == 0) {
            resetSpellAttackTimer(distanceSquared);
            if (!spellCastingMob.isCasting() && !spellCastingMob.isDrinkingPotion()) {
                doSpellAction();
            }

        } else if (this.spellAttackDelay < 0) {
            resetSpellAttackTimer(distanceSquared);
        }
        if (spellCastingMob.isCasting()) {
            var spellData = spellCastingMob.getMagicData().getCastingSpell();
            if (target.isDeadOrDying() || spellData.getSpell().shouldAIStopCasting(spellData.getLevel(), mob, target)) {
                spellCastingMob.cancelCast();
            }
        }
    }

    public boolean isActing() {
        return spellCastingMob.isCasting() || spellCastingMob.isDrinkingPotion();
    }

    protected void resetSpellAttackTimer(double distanceSquared) {
        float f = (float) Math.sqrt(distanceSquared) / this.spellcastingRange;
        this.spellAttackDelay = Math.max(1, Mth.floor(f * (float) (this.spellAttackIntervalMax - this.spellAttackIntervalMin) + (float) this.spellAttackIntervalMin));
    }

    protected void doMovement(double distanceSquared) {
        double speed = (spellCastingMob.isCasting() ? .75f : 1f) * movementSpeed();
        mob.lookAt(target, 30, 30);
        // 保持距离, 拉近距离, 或者四处走位
        float fleeDist = .275f;
        float ss = getStrafeMultiplier();
        if (allowFleeing && (!spellCastingMob.isCasting() && spellAttackDelay > 10) && --fleeCooldown <= 0 && distanceSquared < spellcastingRangeSqr * (fleeDist * fleeDist)) {
            // 尝试远离敌人
            Vec3 flee = DefaultRandomPos.getPosAway(this.mob, 16, 7, target.position());
            if (flee != null) {
                this.mob.getNavigation().moveTo(flee.x, flee.y, flee.z, speed * 1.5);
            } else {
                mob.getMoveControl().strafe(-(float) speed * ss, (float) speed * ss);
            }
        } else if (distanceSquared < spellcastingRangeSqr && seeTime >= 5) {
            //irons_spellbooks.LOGGER.debug("WizardAttackGoal.tick.1: distanceSquared: {},attackRadiusSqr: {}, seeTime: {}, attackTime: {}", distanceSquared, attackRadiusSqr, seeTime, attackTime);
            // 在敌人附近
            this.mob.getNavigation().stop();
            if (++strafeTime > 25) {
                if (mob.getRandom().nextDouble() < .1) {
                    strafingClockwise = !strafingClockwise;
                    strafeTime = 0;
                }
            }
            float strafeForward = (distanceSquared * 6 < spellcastingRangeSqr ? -1 : .5f) * .2f * (float) speedModifier; // 前后走位矢量
            int strafeDir = strafingClockwise ? 1 : -1; // 左右走位方向
            mob.getMoveControl().strafe(strafeForward * ss, (float) speed * strafeDir * ss);
            if (mob.horizontalCollision && mob.getRandom().nextFloat() < .1f) {
                tryJump();
            }
        } else {
            // 太远了，拉近距离
            if (mob.tickCount % 5 == 0) {
                if (isFlying) {
                    this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY() + 2, target.getZ(), speedModifier);
                } else {
                    this.mob.getNavigation().moveTo(this.target, speedModifier);
                }
            }
        }
    }

    protected double movementSpeed() {
        return speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    protected void tryJump() {
        //mob.getJumpControl().jump();
        Vec3 nextBlock = new Vec3(mob.xxa, 0, mob.zza).normalize();
        //IronsSpellbooks.LOGGER.debug("{}", nextBlock);

        BlockPos blockpos = BlockPos.containing(mob.position().add(nextBlock));
        BlockState blockstate = this.mob.level.getBlockState(blockpos);
        VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level, blockpos);
        //IronsSpellbooks.LOGGER.debug("{}", mob.getDeltaMovement());
        //IronsSpellbooks.LOGGER.debug("{}", blockstate.getBlock().getName().getString());
        if (!voxelshape.isEmpty() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
            BlockPos blockposAbove = blockpos.above();
            BlockState blockstateAbove = this.mob.level.getBlockState(blockposAbove);
            VoxelShape voxelshapeAbove = blockstateAbove.getCollisionShape(this.mob.level, blockposAbove);
            if (voxelshapeAbove.isEmpty()) {
                this.mob.getJumpControl().jump();
                //boost to get over the edge
                mob.setXxa(mob.xxa * 5);
                mob.setZza(mob.zza * 5);
            }

        }
    }

    protected void doSpellAction() {
        if (!spellCastingMob.getHasUsedSingleAttack() && singleUseSpell != SpellRegistry.none() && singleUseDelay <= 0) {
            // 施放固有技能
            spellCastingMob.setHasUsedSingleAttack(true);
            spellCastingMob.initiateCastSpell(singleUseSpell, singleUseLevel);
            fleeCooldown = 7 + singleUseSpell.getCastTime(singleUseLevel);
        } else {
            // 获取咒语类型
            var spellData = currentSpell;
            var spell = spellData.getSpell();
            int spellLevel = spellData.getLevel();
            spellLevel = Math.max(spellLevel, 1);

            PlayerRecasts playerRecasts = spellCastingMob.getMagicData().getPlayerRecasts();
            boolean hasRecastForSpell = playerRecasts.hasRecastForSpell(spell);

            //Make sure cast is valid. if not, try again shortly
            if (currentSpell != SpellData.EMPTY && !hasRecastForSpell && !spell.shouldAIStopCasting(spellLevel, mob, target)) {
                spellCastingMob.initiateCastSpell(spell, spellLevel);
                fleeCooldown = 7 + spell.getCastTime(spellLevel);
            } else {
                spellAttackDelay = 5;
            }
            initialCurrentSpell();
        }
    }

    protected void initialCurrentSpell() {
        currentSpell = getNextSpellType();
        spellcastingRange = MaidSpellRegistry.getSpellRange(currentSpell.getSpell());
        spellcastingRangeSqr = spellcastingRange * spellcastingRange;
    }

    protected SpellData getNextSpellType() {
        NavigableMap<Integer, ArrayList<SpellData>> weightedSpells = new TreeMap<>();
        int attackWeight = getAttackWeight();
        int defenseWeight = getDefenseWeight() - (lastSpellCategory == defenseSpells ? 100 : 0);
        int movementWeight = getMovementWeight() - (lastSpellCategory == movementSpells ? 50 : 0);
        int supportWeight = getSupportWeight() - (lastSpellCategory == supportSpells ? 100 : 0);
        int total = 0;

        if (!attackSpells.isEmpty() && attackWeight > 0) {
            total += attackWeight;
            weightedSpells.put(total, attackSpells);
        }
        if (!defenseSpells.isEmpty() && defenseWeight > 0) {
            total += defenseWeight;
            weightedSpells.put(total, defenseSpells);
        }
        if (!movementSpells.isEmpty() && movementWeight > 0) {
            total += movementWeight;
            weightedSpells.put(total, movementSpells);
        }
        if ((!supportSpells.isEmpty() || drinksPotions) && supportWeight > 0) {
            total += supportWeight;
            weightedSpells.put(total, supportSpells);
        }

        if (total > 0) {
            int seed = mob.getRandom().nextInt(total);
            var spellList = weightedSpells.higherEntry(seed).getValue();
            lastSpellCategory = spellList;
            //IronsSpellbooks.LOGGER.debug("WizardAttackGoal.getNextSpell weights: A:{} D:{} M:{} S:{} ({}/{})", attackWeight, defenseWeight, movementWeight, supportWeight, seed, total);
            if (drinksPotions && spellList == supportSpells) {
                if (supportSpells.isEmpty() || mob.getRandom().nextFloat() < .5f) {
                    //IronsSpellbooks.LOGGER.debug("Drinking Potion");
                    spellCastingMob.startDrinkingPotion();
                    return SpellData.EMPTY;
                }
            }
            MaidMagicEntity maid = (MaidMagicEntity) mob;
            PlayerRecasts playerRecasts = spellCastingMob.getMagicData().getPlayerRecasts();
            List<SpellData> filtered = spellList.stream()
                .filter(spellData -> !playerRecasts.hasRecastForSpell(spellData.getSpell())
                    || spellData.getSpell().equals(SpellRegistry.FLAMING_BARRAGE_SPELL.get())
                    || spellData.getSpell().equals(SpellRegistry.WALL_OF_FIRE_SPELL.get())) // 排除重新施法会消失的
                .filter(spellData -> spellCastingMob.getMagicData().getMana() - maid.winefoxsSpellbooks$getManaCost(spellData.getSpell(), spellData.getLevel()) >= 0) // 考虑剩余魔力
                .filter(spellData -> !spellCastingMob.getMagicData().getPlayerCooldowns().isOnCooldown(spellData.getSpell())) // 排除冷却中的
                .toList();
            if (filtered.isEmpty()) {
                return SpellData.EMPTY;
            }
            return filtered.get(mob.getRandom().nextInt(filtered.size()));
        } else {
            //IronsSpellbooks.LOGGER.debug("WizardAttackGoal.getNextSpell weights: A:{} D:{} M:{} S:{} (no spell)", attackWeight, defenseWeight, movementWeight, supportWeight);
            return SpellData.EMPTY;
        }
    }

    protected int getAttackWeight() {
        //We want attack to be a common action in any circumstance, but the more "confident" we are the more likely we are to attack (we have health or our target is weak)
        int baseWeight = 80;
        if (!hasLineOfSight || target == null) {
            return 0;
        }

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) ((1 - targetHealth) * baseWeight * .75f);

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        int distanceWeight = (int) (1 - (distanceSquared / spellcastingRangeSqr) * -60);

        return baseWeight + targetHealthWeight + distanceWeight;
    }

    protected int getDefenseWeight() {
        //We want defensive spells to be used when we feel "threatened", meaning we aren't confident, or we're actively being attacked
        int baseWeight = -20;

        if (target == null) {
            return baseWeight;
        }

        //https://www.desmos.com/calculator/tqs7dudcmv
        //https://www.desmos.com/calculator/7skhcvpic0
        float x = mob.getHealth();
        float m = mob.getMaxHealth();
        //int healthWeight = (int) (50 * (Math.pow(-(x / m) * (x - m), 3) / Math.pow(m / 2, 3)) * 8);
        int healthWeight = (int) (50 * (-(x * x * x) / (m * m * m) + 1));

        float targetHealth = target.getHealth() / target.getMaxHealth();
        int targetHealthWeight = (int) (1 - targetHealth) * -35;

        //this count be finicky due to the fact that projectiles don't stick around for long, so it might be easy to miss them
        int threatWeight = projectileCount * 95;

        return baseWeight + healthWeight + targetHealthWeight + threatWeight;
    }

    protected int getMovementWeight() {
        if (target == null) {
            return 0;
        }
        //We want to move if we're in a disadvantageous spot, or we need a better angle on our target

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double distancePercent = Mth.clamp(distanceSquared / spellcastingRangeSqr, 0, 1);

        int distanceWeight = (int) ((distancePercent) * 50);

        int losWeight = hasLineOfSight ? 0 : 80;

        float healthInverted = 1 - mob.getHealth() / mob.getMaxHealth();
        float distanceInverted = (float) (1 - distancePercent);
        int runWeight = (int) (400 * healthInverted * healthInverted * distanceInverted * distanceInverted);

        return distanceWeight + losWeight + runWeight;
    }

    protected int getSupportWeight() {
        //We want to support/buff ourselves if we are weak
        int baseWeight = -15;

        if (target == null) {
            return baseWeight;
        }

        float health = 1 - mob.getHealth() / mob.getMaxHealth();
        int healthWeight = (int) (200 * health);

        //If our target is close we should probably not drink a potion right in front of them
        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        double distancePercent = Mth.clamp(distanceSquared / spellcastingRangeSqr, 0, 1);
        int distanceWeight = (int) ((1 - distancePercent) * -75);

        return baseWeight + healthWeight + distanceWeight;
    }

    public float getStrafeMultiplier(){
        return 1.2f;
    }

    protected int getLevelFor(EntityMaid maid, AbstractSpell spell, int level) {
        int addition = 0;
        if (maid != null) {
            IItemHandler invWrapper = new CombinedInvWrapper(maid.getArmorInvWrapper(), new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, maid.getMainHandItem())), maid.getMaidBauble());
            for (int i = 0; i < invWrapper.getSlots(); i++) {
                ItemStack stackInSlot = invWrapper.getStackInSlot(i);
                AffinityData affinityData = AffinityData.getAffinityData(stackInSlot);
                if (affinityData == AffinityData.NONE) {
                    continue;
                }
                addition += affinityData.getBonusFor(spell);
            }
        }
        var levelEvent = new ModifySpellLevelEvent(spell, maid, level, level + addition);
        NeoForge.EVENT_BUS.post(levelEvent);
        return levelEvent.getLevel();
    }
}
