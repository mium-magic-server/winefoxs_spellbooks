package net.magicterra.winefoxsspellbooks.entity.spells;

import com.github.tartaricacid.touhoulittlemaid.api.animation.IMagicCastingState;
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.entity.info.ServerCustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.util.ParseI18n;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.adapter.MagicMaidAdapter;
import net.magicterra.winefoxsspellbooks.entity.ai.brain.SummonedMaidBrain;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidEquipmentRandomizer;
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidEquipmentRandomizer.CategorizedSpells;
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidEquipmentRandomizer.EquipmentResult;
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidEquipmentRandomizer.SpellWithLevel;
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidLoadoutManager;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadout;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.magic.MaidSummonManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * 召唤出的女仆
 * <p>
 * 实现了 IMagicEntity, IMagicSummon, IMaid 三个接口，使用 MagicMaidAdapter 委托施法逻辑。 参考 {@link SummonedSkeleton} 实现召唤物生命周期。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-17 01:23
 */
public class SummonedEntityMaid extends PathfinderMob implements MaidMagicEntity, IMagicEntity, IMagicSummon, IMaid {
    public static final EntityType<SummonedEntityMaid> TYPE = EntityType.Builder.of(SummonedEntityMaid::new, MobCategory.CREATURE)
        .sized(0.6f, 1.5f).clientTrackingRange(10).build("summoned_maid");

    private static final EntityDataAccessor<String> DATA_MODEL_ID = SynchedEntityData.defineId(SummonedEntityMaid.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_AIR_FORCE = SynchedEntityData.defineId(SummonedEntityMaid.class, EntityDataSerializers.BOOLEAN);

    /**
     * 默认女仆模型 ID
     */
    private static final String DEFAULT_MODEL_ID = "touhou_little_maid:hakurei_reimu";

    /**
     * 召唤等级 NBT 标签
     */
    private static final String TAG_SUMMON_LEVEL = "SummonLevel";

    /**
     * 飞行模式 NBT 标签
     */
    private static final String TAG_AIR_FORCE = "AirForce";
    /**
     * 编队索引 NBT 标签
     */
    private static final String TAG_SUMMON_INDEX = "SummonIndex";

    public final ItemStack[] handItemsForAnimation = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

    /**
     * 施法逻辑适配器
     */
    private final MagicMaidAdapter magicAdapter;

    /**
     * 召唤术等级，影响法术等级范围
     */
    private int summonLevel = 1;

    /**
     * 召唤顺序索引，用于编队
     */
    private int summonIndex;

    /**
     * 扫帚女仆初始化标记
     */
    private boolean airForceInitialized;

    /**
     * 预选的装备配置（由 SummonMaidSpell 设置，用于 broomMode 决策）
     */
    @Nullable
    private MaidLoadout preSelectedLoadout;

    public SummonedEntityMaid(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
        this.lookControl = new LookControl(this) {
            @Override
            protected boolean resetXRotOnTick() {
                // 有战斗目标时不重置 XRot（保持瞄准）
                // 没有战斗目标时重置 XRot 为 0（水平看）
                return !hasCombatTarget();
            }
        };
        // 在 defineSynchedData 之后初始化 adapter
        this.magicAdapter = new MagicMaidAdapter(this);
        this.magicAdapter.getMagicData().setSyncedData(new SyncedSpellData(this));
    }

    @Override
    protected void defineSynchedData(@Nonnull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MODEL_ID, DEFAULT_MODEL_ID);
        builder.define(DATA_AIR_FORCE, false);
    }

    /**
     * 创建召唤女仆的属性
     * <p>
     * 继承 EntityMaid 的基础属性，并添加魔法属性 (MAX_MANA, MANA_REGEN, COOLDOWN_REDUCTION)
     *
     * @return 属性构建器
     */
    public static AttributeSupplier.Builder createAttributes() {
        return EntityMaid.createAttributes();
    }

    // ==================== Brain 系统 ====================

    @Nonnull
    @Override
    protected Brain.Provider<SummonedEntityMaid> brainProvider() {
        return Brain.provider(SummonedMaidBrain.getMemoryTypes(), SummonedMaidBrain.getSensorTypes());
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    protected Brain<SummonedEntityMaid> makeBrain(@Nonnull com.mojang.serialization.Dynamic<?> dynamic) {
        Brain<SummonedEntityMaid> brain = (Brain<SummonedEntityMaid>) super.makeBrain(dynamic);
        SummonedMaidBrain.registerBrainGoals(brain, this);
        return brain;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public Brain<SummonedEntityMaid> getBrain() {
        return (Brain<SummonedEntityMaid>) super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("summonedMaidBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();

        // 更新活动状态
        if (isAirForce() && this.isPassenger() && this.getVehicle() instanceof SummonedMaidBroom) {
            this.getBrain().setActiveActivityIfPossible(Activity.RIDE);
        } else {
            this.getBrain().setActiveActivityIfPossible(
                this.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? Activity.FIGHT : Activity.IDLE
            );
        }

        // 处理施法逻辑（包含法术状态恢复）
        magicAdapter.aiStepMagicLogic();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        magicAdapter.tickMagicLogic(this.level().getServer().getTickCount());

        // 骑乘关系的恢复由扫帚负责（SummonedMaidBroom.tryRestoreRiding）
        // 这里不再尝试恢复，避免冲突

        if (!airForceInitialized && isAirForce() && this.isPassenger() && this.getVehicle() instanceof SummonedMaidBroom broom) {
            // 初始化飞行高度与扫帚绑定
            Vec3 targetPos = this.position().add(0.0, Config.getFollowHeight(), 0.0);
            broom.setFlightTarget(targetPos);
            this.getBrain().setMemory(MaidCastingMemoryModuleTypes.BROOM_ENTITY.get(), broom);
            this.getBrain().setMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get(), false);
            airForceInitialized = true;
        }
    }

    public boolean hasCombatTarget() {
        return getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
            || getBrain().hasMemoryValue(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get());
    }

    /**
     * 骑乘时的 tick 处理
     * <p>
     * 仿照 AbstractSpellCastingMob.rideTick()，让乘客（女仆）控制载具（扫帚）的方向。
     * <p>
     * 有战斗目标时：扫帚朝向目标（使用女仆的 yBodyRot）
     * 无战斗目标时：扫帚朝向移动方向
     */
    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof SummonedMaidBroom broom) {
            // 检查是否有战斗目标
            boolean hasCombatTarget = hasCombatTarget();

            float targetYRot;
            if (hasCombatTarget) {
                // 有战斗目标：朝向目标（由 LookControl 控制的 yBodyRot）
                targetYRot = this.yBodyRot;
            } else {
                // 无战斗目标：朝向移动方向
                Vec3 motion = broom.getDeltaMovement();
                if (motion.horizontalDistanceSqr() > 0.001) {
                    // 有水平移动，计算移动方向的角度
                    targetYRot = (float) (Mth.atan2(motion.z, motion.x) * (180.0 / Math.PI)) - 90.0F;
                } else {
                    // 静止状态，保持当前方向
                    targetYRot = broom.getYRot();
                }
            }

            // 平滑插值到目标方向
            float currentYRot = broom.getYRot();
            float newYRot = Mth.rotLerp(0.2F, currentYRot, targetYRot);

            broom.setYRot(newYRot);
            broom.yRotO = currentYRot;
        }
    }

    // ==================== IMaid 接口实现 ====================

    @Override
    public String getModelId() {
        return entityData.get(DATA_MODEL_ID);
    }

    /**
     * 设置女仆模型 ID
     *
     * @param modelId 模型 ID
     */
    public void setModelId(String modelId) {
        this.entityData.set(DATA_MODEL_ID, modelId);
    }

    /**
     * 获取召唤等级
     *
     * @return 召唤术等级
     */
    public int getSummonLevel() {
        return summonLevel;
    }

    /**
     * 设置召唤等级
     *
     * @param level 召唤术等级
     */
    public void setSummonLevel(int level) {
        this.summonLevel = level;
    }

    /**
     * 是否为扫帚女仆
     */
    public boolean isAirForce() {
        return entityData.get(DATA_AIR_FORCE);
    }

    /**
     * 设置飞行状态
     *
     * @param airForce 是否飞行
     */
    public void setAirForce(boolean airForce) {
        this.entityData.set(DATA_AIR_FORCE, airForce);
    }

    /**
     * 获取编队索引
     */
    public int getSummonIndex() {
        return summonIndex;
    }

    /**
     * 设置编队索引
     *
     * @param summonIndex 索引
     */
    public void setSummonIndex(int summonIndex) {
        this.summonIndex = summonIndex;
    }

    /**
     * 切换飞行 -> 地面模式
     */
    public void switchToGroundMode() {
        setAirForce(false);
        airForceInitialized = false;
        this.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.BROOM_ENTITY.get());
        this.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get());
        this.getBrain().eraseMemory(MaidCastingMemoryModuleTypes.IS_HOVERING.get());
        this.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.getBrain().setActiveActivityIfPossible(Activity.IDLE);
    }

    /**
     * 设置预选的装备配置
     * <p>
     * 由 SummonMaidSpell 调用，在 finalizeSpawn 之前设置， 用于根据 broomMode 决定是否使用扫帚
     *
     * @param loadout 预选的装备配置
     */
    public void setPreSelectedLoadout(@Nullable MaidLoadout loadout) {
        this.preSelectedLoadout = loadout;
    }

    /**
     * 获取预选的装备配置
     *
     * @return 预选的装备配置，可能为 null
     */
    @Nullable
    public MaidLoadout getPreSelectedLoadout() {
        return preSelectedLoadout;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        if (super.getTarget() == null && hasCombatTarget()) {
            return getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
                .or(() -> getBrain().getMemory(MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()))
                    .orElse(null);
        }
        return super.getTarget();
    }

    /**
     * 获取实体类型名称
     * <p>
     * 根据女仆模型显示 "召唤出的[模型名称]"
     *
     * @return 格式化后的实体名称
     */
    @Nonnull
    @Override
    protected Component getTypeName() {
        // 获取模型名称
        Component modelName = getModelName();
        // 返回 "召唤出的[模型名称]"
        return Component.translatable("entity.winefoxs_spellbooks.summoned_maid.with_model", modelName);
    }

    /**
     * 获取当前模型的显示名称
     * <p>
     * 参考 EntityMaid 的实现，从 ServerCustomPackLoader 获取模型信息
     *
     * @return 模型名称组件
     */
    private Component getModelName() {
        Optional<MaidModelInfo> info = ServerCustomPackLoader.SERVER_MAID_MODELS.getInfo(getModelId());
        return info.map(maidModelInfo -> ParseI18n.parse(maidModelInfo.getName()))
            .orElseGet(() -> Component.literal(getModelId()));
    }

    @Override
    public Mob asEntity() {
        return this;
    }

    @Override
    public ItemStack[] getHandItemsForAnimation() {
        return handItemsForAnimation;
    }

    /**
     * 获取女仆当前任务
     * <p>
     * 召唤女仆不使用正常任务系统，返回空闲任务
     *
     * @return 空闲任务
     */
    @Override
    public IMaidTask getTask() {
        return TaskManager.getIdleTask();
    }

    /**
     * 女仆是否处于坐姿
     * <p>
     * 召唤女仆永远不会坐下
     *
     * @return 始终返回 false
     */
    @Override
    public boolean isMaidInSittingPose() {
        return false;
    }

    /**
     * 女仆是否正在乞讨
     * <p>
     * 召唤女仆不会乞讨
     *
     * @return 始终返回 false
     */
    @Override
    public boolean isBegging() {
        return false;
    }

    // ==================== IMagicEntity 接口实现 (委托给 MagicMaidAdapter) ====================

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
    public boolean getHasUsedSingleAttack() {
        return magicAdapter.getHasUsedSingleAttack();
    }

    @Override
    public void setHasUsedSingleAttack(boolean bool) {
        magicAdapter.setHasUsedSingleAttack(bool);
    }

    @Override
    public void startDrinkingPotion() {
        magicAdapter.startDrinkingPotion();
    }

    // ==================== IMagicSummon 接口实现 ====================

    @Override
    public void onUnSummon() {
        if (!this.level().isClientSide) {
            // 生成消散粒子效果
            MaidMagicManager.spawnParticles(
                this.level(),
                ParticleTypes.POOF,
                this.getX(), this.getY() + this.getBbHeight() / 2, this.getZ(),
                25, 0.4, 0.8, 0.4, 0.03, false
            );
            this.discard();
        }
    }

    @Override
    public void die(@Nonnull DamageSource damageSource) {
        this.onDeathHelper();
        super.die(damageSource);
    }

    /**
     * 实体被移除时的处理
     * <p>
     * 只有在实体真正被销毁时才调用 onRemovedHelper， 卸载到区块或随玩家卸载时不应该触发清理逻辑
     */
    @Override
    public void onRemovedFromLevel() {
        Entity.RemovalReason reason = this.getRemovalReason();
        // 只有在非卸载情况下才调用 onRemovedHelper
        // UNLOADED_TO_CHUNK: 区块卸载
        // UNLOADED_WITH_PLAYER: 玩家下线时实体卸载
        if (reason != null
            && reason != Entity.RemovalReason.UNLOADED_TO_CHUNK
            && reason != Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            this.onRemovedHelper(this);
        }
        super.onRemovedFromLevel();
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        if (this.shouldIgnoreDamage(source)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isAlliedTo(@Nonnull Entity entity) {
        // 检查召唤者
        Entity summoner = this.getSummoner();
        if (summoner != null && Objects.equals(summoner, entity)) {
            return true;
        }

        // 检查召唤者的同盟
        if (summoner != null && summoner.isAlliedTo(entity)) {
            return true;
        }

        // 使用 IMagicSummon 的 helper 方法
        if (this.isAlliedHelper(entity)) {
            return true;
        }

        // 检查是否属于同一召唤链（支持嵌套召唤）
        if (MaidSummonManager.isSameSummonChain(this, entity)) {
            return true;
        }

        // 检查顶层召唤者之间的同盟关系（支持其他模组的自定义同盟逻辑）
        if (summoner != null) {
            Entity entityRoot = MaidSummonManager.getEffectiveRoot(entity);
            if (entityRoot != null && entityRoot != entity && summoner.isAlliedTo(entityRoot)) {
                return true;
            }
        }

        return super.isAlliedTo(entity);
    }

    // ==================== NBT 序列化 ====================

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("ModelId", getModelId());
        tag.putInt(TAG_SUMMON_LEVEL, summonLevel);
        tag.putBoolean(TAG_AIR_FORCE, isAirForce());
        tag.putInt(TAG_SUMMON_INDEX, summonIndex);
        // 保存魔法数据
        magicAdapter.getMagicData().saveNBTData(tag, level().registryAccess());
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ModelId")) {
            setModelId(tag.getString("ModelId"));
        }
        if (tag.contains(TAG_SUMMON_LEVEL)) {
            summonLevel = tag.getInt(TAG_SUMMON_LEVEL);
        }
        if (tag.contains(TAG_AIR_FORCE)) {
            setAirForce(tag.getBoolean(TAG_AIR_FORCE));
            airForceInitialized = false;
        }
        if (tag.contains(TAG_SUMMON_INDEX)) {
            summonIndex = tag.getInt(TAG_SUMMON_INDEX);
        }
        // 恢复魔法数据
        MagicData magicData = magicAdapter.getMagicData();
        magicData.setSyncedData(new SyncedSpellData(this));
        magicData.loadNBTData(tag, level().registryAccess());

        // 如果正在施法，标记恢复
        if (magicData.getSyncedData().isCasting()) {
            magicAdapter.setRecreateSpell(true);
        }
    }

    // ==================== 渲染辅助接口 ====================
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
    public int winefoxsSpellbooks$getManaCost(AbstractSpell spell, int level) {
        return magicAdapter.winefoxsSpellbooks$getManaCost(spell, level);
    }

    @Override
    public MaidSpellDataHolder winefoxsSpellbooks$getSpellDataHolder() {
        return magicAdapter.winefoxsSpellbooks$getSpellDataHolder();
    }

    // ==================== 其他方法 ====================

    /**
     * 完成生成时的初始化
     * <p>
     * 使用 MaidEquipmentRandomizer 随机选择女仆模型、装备和法术
     */
    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(@Nonnull ServerLevelAccessor level, @Nonnull DifficultyInstance difficulty,
                                        @Nonnull MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData);

        // 使用 loadout 系统随机生成装备
        applyRandomLoadout();

        return spawnData;
    }

    /**
     * 应用随机装备配置
     * <p>
     * 从 MaidLoadoutManager 获取配置并使用 MaidEquipmentRandomizer 随机生成装备。 如果 preSelectedLoadout 已设置，则使用该配置而不是重新随机选择。
     */
    private void applyRandomLoadout() {
        try {
            // 1. 使用预选配置或随机选择
            MaidLoadout loadout = preSelectedLoadout != null
                                  ? preSelectedLoadout
                                  : MaidLoadoutManager.getInstance().selectLoadout(random);

            if (loadout == null) {
                // 没有可用配置时，使用灵梦模型，不分配装备和法术
                WinefoxsSpellbooks.LOGGER.warn("SummonedEntityMaid: No loadout configuration available, using default Reimu model");
                setModelId(DEFAULT_MODEL_ID);
                return;
            }

            // 2. 获取可用的女仆模型
            Set<ResourceLocation> availableModels = getAvailableModelsAsResourceLocations();

            // 3. 使用随机化器生成装备
            EquipmentResult result = MaidEquipmentRandomizer.randomize(
                loadout,
                summonLevel,
                random,
                availableModels
            );

            // 4. 应用结果
            applyEquipmentResult(result);

            if (WinefoxsSpellbooks.DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("SummonedEntityMaid: Applied loadout {} at level {}",
                    loadout.id(), summonLevel);
            }
        } catch (Exception e) {
            // 发生异常时，使用灵梦模型，不分配装备和法术
            WinefoxsSpellbooks.LOGGER.error("SummonedEntityMaid: Failed to apply random loadout", e);
            setModelId(DEFAULT_MODEL_ID);
        }
    }

    /**
     * 获取可用女仆模型并转换为 ResourceLocation 集合
     */
    private Set<ResourceLocation> getAvailableModelsAsResourceLocations() {
        try {
            var serverModels = ServerCustomPackLoader.SERVER_MAID_MODELS;
            Set<String> modelIds = serverModels.getModelIdSet();
            return modelIds.stream()
                .map(ResourceLocation::parse)
                .collect(java.util.stream.Collectors.toSet());
        } catch (Exception e) {
            WinefoxsSpellbooks.LOGGER.warn("SummonedEntityMaid: Failed to get available models", e);
            return Set.of();
        }
    }

    /**
     * 应用装备结果到女仆
     */
    private void applyEquipmentResult(EquipmentResult result) {
        // 应用模型
        if (result.modelId() != null) {
            setModelId(result.modelId().toString());
        }

        // 应用武器
        if (!result.weapon().isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, result.weapon());
        }

        // 应用护甲
        for (Map.Entry<EquipmentSlot, ItemStack> entry : result.armor().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                setItemSlot(entry.getKey(), entry.getValue());
            }
        }

        // 设置装备不掉落
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setDropChance(slot, 0.0f);
        }

        // 应用法术
        applySpells(result.spells());

        // 设置初始法力值
        magicAdapter.winefoxsSpellbooks$setMana((float) getAttributeValue(AttributeRegistry.MAX_MANA));
    }

    /**
     * 应用法术到女仆
     * <p>
     * 法术已在 MaidEquipmentRandomizer 中按类别分类，直接应用即可
     */
    private void applySpells(CategorizedSpells spells) {
        MaidSpellDataHolder holder = magicAdapter.winefoxsSpellbooks$getSpellDataHolder();

        holder.updateAttackSpells(toSpellDataList(spells.attack()));
        holder.updateDefenseSpells(toSpellDataList(spells.defense()));
        holder.updateMovementSpells(toSpellDataList(spells.movement()));
        holder.updateSupportSpells(toSpellDataList(spells.support()));
        holder.updatePositiveEffectSpells(toSpellDataList(spells.positive()));
        holder.updateNegativeEffectSpells(toSpellDataList(spells.negativeEffect()));
        holder.updateSupportEffectSpells(toSpellDataList(spells.supportOther()));

        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("SummonedEntityMaid: Applied spells - attack:{}, defense:{}, movement:{}, support:{}, positive:{}, negative:{}, supportOther:{}",
                spells.attack().size(), spells.defense().size(), spells.movement().size(),
                spells.support().size(), spells.positive().size(), spells.negativeEffect().size(), spells.supportOther().size());
        }
    }

    /**
     * 将 SpellWithLevel 列表转换为 SpellData 列表
     */
    private static List<SpellData> toSpellDataList(List<SpellWithLevel> spells) {
        return spells.stream()
            .map(s -> new SpellData(s.spell(), s.level()))
            .toList();
    }

    /**
     * 处理玩家右键点击事件
     * <p>
     * 召唤女仆不响应右键点击，禁用 GUI 打开
     *
     * @param player 点击的玩家
     * @param hand   使用的手
     * @return 始终返回 PASS，不做任何处理
     */
    @Nonnull
    @Override
    public InteractionResult mobInteract(@Nonnull Player player, @Nonnull InteractionHand hand) {
        // 召唤女仆不可交互，禁用 GUI 打开
        return InteractionResult.PASS;
    }


    /**
     * 获取魔法适配器
     *
     * @return 魔法适配器实例
     */
    public MagicMaidAdapter getMagicAdapter() {
        return magicAdapter;
    }
}
