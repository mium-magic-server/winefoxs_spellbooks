package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.loadout.MaidLoadoutManager;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.BroomMode;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadout;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 召唤女仆法术
 * <p>
 * 参照 {@link io.redspace.ironsspellbooks.spells.blood.RaiseDeadSpell} 实现，
 * 召唤 {@link SummonedEntityMaid} 实体来协助施法者战斗。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-02 23:00
 */
public class SummonMaidSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "summon_maid");

    /**
     * 女仆最大魔力属性修改器 ID
     */
    private static final ResourceLocation MAX_MANA_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "summoned_maid_max_mana");

    /**
     * 女仆魔力恢复属性修改器 ID
     */
    private static final ResourceLocation MANA_REGEN_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "summoned_maid_mana_regen");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.UNCOMMON)
        .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
        .setMaxLevel(6)
        .setCooldownSeconds(150.0)
        .build();

    public SummonMaidSpell() {
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 30;
        this.baseManaCost = 50;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    /**
     * 获取召唤女仆数量
     *
     * @param spellLevel 法术等级
     * @param caster 施法者
     * @return 召唤数量
     */
    public int getSummonCount(int spellLevel, LivingEntity caster) {
        return Mth.ceil(getSpellPower(spellLevel, caster));
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
            Component.translatable("ui.irons_spellbooks.summon_count", getSummonCount(spellLevel, caster))
        );
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2; // 支持 2 次重铸
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (SummonManager.recastFinishedHelper(serverPlayer, recastInstance, recastResult, castDataSerializable)) {
            super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        }
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SummonedEntitiesCastData();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(world instanceof ServerLevel serverWorld)) {
            super.onCast(world, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        PlayerRecasts recasts = playerMagicData.getPlayerRecasts();

        // 检查是否已有该法术的重铸记录
        if (!recasts.hasRecastForSpell(this)) {
            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
            int summonTime = 12000; // 召唤持续时间 600 秒（10 分钟）
            int count = getSummonCount(spellLevel, entity);
            float radius = 1.5f + 0.185f * count; // 召唤半径随数量增加

            // 默认飞行概率（从 Config 获取）
            float defaultAirForceChance = Config.getAirForceBaseChance() + Config.getAirForceChancePerLevel() * spellLevel;
            defaultAirForceChance = Mth.clamp(defaultAirForceChance, 0.0f, 1.0f);

            int airForceIndex = 0;

            for (int i = 0; i < count; ++i) {
                // 创建召唤女仆实体
                SummonedEntityMaid maid = new SummonedEntityMaid(SummonedEntityMaid.TYPE, world);
                maid.setSummonLevel(spellLevel);

                // 预选装备配置（用于 broomMode 决策）
                MaidLoadout loadout = MaidLoadoutManager.getInstance().selectLoadout(world.random);
                maid.setPreSelectedLoadout(loadout);

                // 根据 broomMode 决定是否飞行
                boolean isAirForce = determineAirForce(loadout, defaultAirForceChance, world);

                // 计算生成位置（围绕施法者圆形分布）
                float yrot = (float) (2 * Math.PI) / count * i + entity.getYRot() * ((float) Math.PI / 180);
                Vec3 spawn = Utils.moveToRelativeGroundLevel(
                    world,
                    entity.getEyePosition().add(new Vec3(
                        radius * Mth.cos(yrot),
                        0.0,
                        radius * Mth.sin(yrot)
                    )),
                    10
                );

                // 设置位置和朝向
                maid.setPos(spawn.x, spawn.y, spawn.z);
                maid.setYRot(entity.getYRot());

                // 设置飞行状态（在 finalizeSpawn 之前设置，以便 randomSpells 能正确过滤近战法术）
                if (isAirForce) {
                    maid.setAirForce(true);
                    maid.setSummonIndex(airForceIndex++);
                }

                // 初始化实体生成数据（此时 isAirForce 已确定，randomSpells 会根据此状态过滤近战法术）
                maid.finalizeSpawn(
                    serverWorld,
                    world.getCurrentDifficultyAt(maid.getOnPos()),
                    MobSpawnType.MOB_SUMMONED,
                    null
                );

                // 根据法术强度修改女仆的魔力属性
                applySpellPowerAttributes(maid, spellLevel, entity);

                maid.setOldPosAndRot();

                // 触发召唤事件
                Mob summonedMaid = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(entity, maid, this.spellId, spellLevel)).getCreature();

                if (summonedMaid instanceof SummonedEntityMaid summonedEntityMaid && isAirForce) {
                    SummonedMaidBroom broom = SummonedMaidBroom.createForMaid(serverWorld, summonedEntityMaid, entity);
                    summonedEntityMaid.startRiding(broom, true);
                    serverWorld.addFreshEntityWithPassengers(broom);
                    SummonManager.initSummon(entity, broom, summonTime, summonedEntitiesCastData);
                } else {
                    serverWorld.addFreshEntity(summonedMaid);
                }

                SummonManager.initSummon(entity, summonedMaid, summonTime, summonedEntitiesCastData);
            }

            RecastInstance recastInstance = new RecastInstance(
                this.getSpellId(),
                spellLevel,
                this.getRecastCount(spellLevel, entity),
                summonTime,
                castSource,
                summonedEntitiesCastData
            );
            recasts.addRecast(recastInstance, playerMagicData);

            // 播放完成音效
            world.playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundRegistry.HOLY_CAST.get(),
                entity.getSoundSource(),
                2.0f,
                0.9f + Utils.random.nextFloat() * 0.2f
            );
        }
    }

    /**
     * 根据 loadout 的 broomMode 决定女仆是否飞行
     *
     * @param loadout 装备配置（可能为 null）
     * @param defaultChance 默认飞行概率（从 Config 计算）
     * @param world 世界实例（用于随机数）
     * @return 是否飞行
     */
    private boolean determineAirForce(@Nullable MaidLoadout loadout, float defaultChance, Level world) {
        if (loadout == null) {
            // 没有配置时使用默认概率
            return world.random.nextFloat() < defaultChance;
        }

        BroomMode broomMode = loadout.broomMode();
        return switch (broomMode) {
            case ALWAYS -> true;
            case NEVER -> false;
            case DEFAULT -> world.random.nextFloat() < defaultChance;
        };
    }

    /**
     * 根据法术强度修改女仆的魔力属性
     * <p>
     * 最大魔力和魔力恢复速度基于召唤法术的强度进行加成。
     *
     * @param maid 召唤的女仆
     * @param spellLevel 法术等级
     * @param caster 施法者
     */
    private void applySpellPowerAttributes(SummonedEntityMaid maid, int spellLevel, LivingEntity caster) {
        // 获取法术强度（基于施法者属性和法术等级）
        float spellPower = this.getSpellPower(spellLevel, caster);

        // 计算最高等级无加成时的基础法术强度作为参考值
        // baseSpellPower=2, spellPowerPerLevel=1, maxLevel=6
        // 最高等级基础强度 = 2 + 1 * (6-1) = 7
        float maxBasePower = this.baseSpellPower + this.spellPowerPerLevel * (this.getMaxLevel() - 1);

        // 归一化法术强度（相对于最高等级基础强度的比例）
        // 施法者属性加成会使 spellPower 超过 maxBasePower
        float normalizedPower = spellPower / maxBasePower;

        // 最大魔力加成：最高时获得 +500% 加成
        // 公式：(normalizedPower * 5) 作为乘法加成
        double maxManaBonus = normalizedPower * 5.0;

        // 魔力恢复加成：最高时获得 10% 恢复速度
        // 基础恢复速度是 1%，目标是 10%，所以需要 +900%
        double manaRegenBonus = normalizedPower * 9.0;

        // 应用最大魔力加成（乘法修改器）
        AttributeInstance maxManaAttr = maid.getAttribute(AttributeRegistry.MAX_MANA);
        if (maxManaAttr != null) {
            maxManaAttr.addPermanentModifier(new AttributeModifier(
                MAX_MANA_MODIFIER_ID,
                maxManaBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        // 应用魔力恢复加成（乘法修改器）
        AttributeInstance manaRegenAttr = maid.getAttribute(AttributeRegistry.MANA_REGEN);
        if (manaRegenAttr != null) {
            manaRegenAttr.addPermanentModifier(new AttributeModifier(
                MANA_REGEN_MODIFIER_ID,
                manaRegenBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("SummonMaidSpell: Applied attributes - spellPower={}, normalized={}, maxManaBonus=+{}%, manaRegenBonus=+{}%, finalMaxMana={}, finalManaRegen={}",
                spellPower, normalizedPower, maxManaBonus * 100, manaRegenBonus * 100,
                maxManaAttr != null ? maxManaAttr.getValue() : "N/A",
                manaRegenAttr != null ? manaRegenAttr.getValue() : "N/A");
        }
    }
}
