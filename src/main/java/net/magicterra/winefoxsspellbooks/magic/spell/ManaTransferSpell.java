package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import java.util.List;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 魔力转移法术
 * <p>
 * 持续施法，将施法者的魔力转移到目标生物（玩家或 IMagicEntity）。 使用射线检测目标，命中后执行魔力转移逻辑。 施法者消耗的魔力略大于目标获得的魔力（20% 损耗），最大持续时间 5 秒（100 ticks）。
 * <p>
 * 持续施法的魔力消耗由系统自动处理：每 10 tick 调用一次 {@link #onCast}， 系统在调用前会扣除 {@link #getManaCost} 的魔力。 卷轴显示的每秒消耗 = {@link #getManaCost} * 2。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-02-18 11:46
 */
public class ManaTransferSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "mana_transfer");

    /**
     * 传输效率：目标获得的魔力 = 系统扣除的魔力 / 此系数 1.2 表示 20% 损耗
     */
    private static final double TRANSFER_EFFICIENCY_DIVISOR = 1.2;

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
        .setMaxLevel(10)
        .setCooldownSeconds(15.0)
        .build();

    public ManaTransferSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.castTime = 100; // 5 秒持续时间
        this.baseManaCost = 15;
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
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
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.HOLY_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.HOLY_CAST.get());
    }

    /**
     * 获取每次 onCast 调用时目标获得的魔力量
     * <p>
     * 系统每 10 tick 扣除 getManaCost(level) 魔力后调用 onCast， 目标获得的魔力 = getManaCost(level) / 1.2（有 20% 损耗）。
     */
    public double getTransferAmount(int spellLevel) {
        return getManaCost(spellLevel) / TRANSFER_EFFICIENCY_DIVISOR;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        // 每秒消耗 = getManaCost * 2（系统对持续施法每秒调用2次）
        int manaCostPerSecond = getManaCost(spellLevel) * 2;
        double transferPerSecond = getTransferAmount(spellLevel) * 2;
        return List.of(
            Component.translatable("ui.winefoxs_spellbooks.mana_transfer_rate",
                String.format("%.1f", transferPerSecond)),
            Component.translatable("ui.winefoxs_spellbooks.mana_transfer_cost",
                manaCostPerSecond)
        );
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return null;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        // 系统已经在调用 onCast 前扣除了 getManaCost(level) 的魔力
        // 这里只需要给目标加魔力
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return;
        }

        // 使用射线检测获取目标实体
        var raycast = Utils.raycastForEntity(level, entity, 32.0F, true, 0.35F);
        if (raycast.getType() != EntityHitResult.Type.ENTITY) {
            return;
        }

        var hitEntity = ((EntityHitResult) raycast).getEntity();
        if (!(hitEntity instanceof LivingEntity targetEntity)) {
            return;
        }

        // 验证目标：必须是玩家或 IMagicEntity
        if (!isValidTarget(targetEntity)) {
            return;
        }

        // 给予目标魔力（扣除 20% 损耗后的量）
        double transferAmount = getTransferAmount(spellLevel);
        MagicData targetMagicData = MagicData.getPlayerMagicData(targetEntity);
        double targetMaxMana = getMaxMana(targetEntity);
        double newTargetMana = Math.min(targetMagicData.getMana() + transferAmount, targetMaxMana);
        targetMagicData.setMana((float) newTargetMana);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 使用射线检测获取目标实体（用于粒子效果）
        var raycast = Utils.raycastForEntity(level, entity, 32.0F, true, 0.35F);
        if (raycast.getType() != EntityHitResult.Type.ENTITY) {
            return;
        }

        var hitEntity = ((EntityHitResult) raycast).getEntity();
        if (!(hitEntity instanceof LivingEntity targetEntity)) {
            return;
        }

        if (!isTargetNeedsSupport(targetEntity)) {
            return;
        }

        // 每 tick 都生成粒子效果
        Vec3 start = entity.getEyePosition();
        Vec3 end = targetEntity.getEyePosition();
        Vec3 particleDirection = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        // 沿射线生成紫色星形粒子
        for (int i = 0; i < 8; i++) {
            Vec3 particlePos = start.add(particleDirection.scale(distance * i / 8.0));
            serverLevel.sendParticles(
                ParticleTypes.WITCH,
                particlePos.x, particlePos.y, particlePos.z,
                2,
                0.05, 0.05, 0.05,
                0.02
            );
        }

        // 在目标位置生成额外粒子
        serverLevel.sendParticles(
            ParticleTypes.ENCHANT,
            end.x, end.y, end.z,
            10,
            0.3, 0.3, 0.3,
            0.1
        );
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        if (!isTargetNeedsSupport(target)) {
            return true;
        }
        return super.shouldAIStopCasting(spellLevel, mob, target);
    }

    public boolean isTargetNeedsSupport(LivingEntity target) {
        if (!isValidTarget(target)) {
            return false;
        }
        MagicData targetMagicData = MagicData.getPlayerMagicData(target);
        double targetMaxMana = getMaxMana(target);
        return targetMagicData.getMana() < targetMaxMana;
    }

    /**
     * 验证目标是否有效（玩家或 IMagicEntity）
     */
    private boolean isValidTarget(LivingEntity target) {
        return target instanceof Player || target instanceof IMagicEntity;
    }

    /**
     * 获取实体的最大魔力值
     */
    private double getMaxMana(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(AttributeRegistry.MAX_MANA);
        return attribute != null ? attribute.getValue() : 100.0;
    }
}
