package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
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
import net.magicterra.winefoxsspellbooks.entity.summon.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.summon.SummonedMaidBroom;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.magicterra.winefoxsspellbooks.registry.WsbSounds;
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
 * 参照 {@link io.redspace.ironsspellbooks.spells.blood.RaiseDeadSpell} 实现， 召唤 {@link SummonedEntityMaid} 实体来协助施法者战斗。
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
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
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

    /**
     * 咏唱音：Iron's 治愈之环的 {@code loop.cloud_of_regen}。
     */
    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.CLOUD_OF_REGEN_LOOP.get());
    }

    /**
     * 到场音：衣袍翻动在先，0.55 s 后落一记单击钟。
     */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(WsbSounds.SUMMON_MAID_ARRIVE.get());
    }

    /**
     * 获取召唤女仆数量
     *
     * @param spellLevel 法术等级
     * @param caster     施法者
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
                SummonedEntityMaid maid = new SummonedEntityMaid(SummonedEntityMaid.TYPE, world);
                maid.setSummonLevel(spellLevel);

                // 预选装备配置（用于 broomMode 决策）
                MaidLoadout loadout = MaidLoadoutManager.getInstance().selectLoadout(world.random);
                maid.setPreSelectedLoadout(loadout);

                boolean isAirForce = determineAirForce(loadout, defaultAirForceChance, world);

                // 围绕施法者圆形分布
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

                maid.setPos(spawn.x, spawn.y, spawn.z);
                maid.setYRot(entity.getYRot());

                // 设置飞行状态（在 finalizeSpawn 之前设置，以便 randomSpells 能正确过滤近战法术）
                if (isAirForce) {
                    maid.setAirForce(true);
                    maid.setSummonIndex(airForceIndex++);
                }

                maid.finalizeSpawn(
                    serverWorld,
                    world.getCurrentDifficultyAt(maid.getOnPos()),
                    MobSpawnType.MOB_SUMMONED,
                    null
                );

                applySpellPowerAttributes(maid, spellLevel, entity);

                maid.setOldPosAndRot();

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

            WinefoxHexHelper.spawnCastBurst(serverWorld, entity);
            // 到场音由 getCastFinishSound() 统一播放，不在此手写
        }
    }

    /**
     * 根据 loadout 的 broomMode 决定女仆是否飞行
     *
     * @param loadout       装备配置（可能为 null）
     * @param defaultChance 默认飞行概率（从 Config 计算）
     * @param world         世界实例（用于随机数）
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
     * 按法术强度给女仆的最大魔力与魔力恢复加成。
     *
     * @param maid       召唤的女仆
     * @param spellLevel 法术等级
     * @param caster     施法者
     */
    private void applySpellPowerAttributes(SummonedEntityMaid maid, int spellLevel, LivingEntity caster) {
        float spellPower = this.getSpellPower(spellLevel, caster);

        // 最高等级无加成时的基础法术强度，作为归一化参考值
        float maxBasePower = this.baseSpellPower + this.spellPowerPerLevel * (this.getMaxLevel() - 1);

        float normalizedPower = spellPower / maxBasePower;

        // 最大魔力：最高 +500%
        double maxManaBonus = normalizedPower * 5.0;

        // 魔力恢复：基础 1% → 最高 10%
        double manaRegenBonus = normalizedPower * 9.0;

        AttributeInstance maxManaAttr = maid.getAttribute(AttributeRegistry.MAX_MANA);
        if (maxManaAttr != null) {
            maxManaAttr.addPermanentModifier(new AttributeModifier(
                MAX_MANA_MODIFIER_ID,
                maxManaBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

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
