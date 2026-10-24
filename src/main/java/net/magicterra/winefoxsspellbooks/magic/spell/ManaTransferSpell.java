package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import java.util.List;
import javax.annotation.Nullable;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.magic.particle.TransferStreamOption;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 魔力转移法术
 * <p>
 * 持续施法，用射线锁定目标（玩家或 IMagicEntity）并向其转移魔力，最大持续 5 秒。
 * <p>
 * 系统每 10 tick 调一次 {@link #onCast}，调用前已扣除 {@link #getManaCost} 的魔力。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-02-18 11:46
 */
public class ManaTransferSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID,
        "mana_transfer");

    /**
     * 传输效率：目标获得的魔力 = 系统扣除的魔力 / 此系数。1.2 即扣除的比送达的多 20%（送达约为扣除的 83%）
     */
    private static final double TRANSFER_EFFICIENCY_DIVISOR = 1.2;

    /**
     * 目标锁定射线的最大距离。
     */
    private static final float RAYCAST_RANGE = 32.0f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
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

    /**
     * 咏唱音：Iron's 治愈之环的 {@code loop.cloud_of_regen}。
     */
    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.CLOUD_OF_REGEN_LOOP.get());
    }

    /**
     * 本法术是 CONTINUOUS，{@code onCast} 每 10 tick 触发一次，故这条音在 5 秒里播 10 遍，
     * 听感是持续层而非收尾音——刻意保留。
     */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.HOLY_CAST.get());
    }

    /**
     * 每次 {@link #onCast} 目标获得的魔力量。
     */
    public double getTransferAmount(int spellLevel) {
        return getManaCost(spellLevel) / TRANSFER_EFFICIENCY_DIVISOR;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource,
                       MagicData playerMagicData) {
        // 系统已在调用前扣除 getManaCost(level)，这里只给目标加魔力
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return;
        }

        LivingEntity targetEntity = raycastLivingTarget(level, entity);
        if (targetEntity == null) {
            return;
        }

        double transferAmount = getTransferAmount(spellLevel);
        double targetMaxMana = getMaxMana(targetEntity);
        MaidMagicManager.setMana(targetEntity,
            (float) Math.min(MaidMagicManager.getMana(targetEntity) + transferAmount, targetMaxMana));

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 施法环绕粒子与传输流同频:5 tick 一批,节流网络开销
        if (playerMagicData.getCastDurationRemaining() % 5 != 0) {
            return;
        }
        WinefoxHexHelper.spawnCastParticles(serverLevel, entity);

        LivingEntity targetEntity = raycastLivingTarget(level, entity);
        if (targetEntity == null || !isTargetNeedsSupport(targetEntity)) {
            return;
        }

        // 传输流 5 tick 一个粒子包:包内带施法者/目标实体 ID 与动画时基,
        // 客户端逐帧从本地实体实时重算光束端点并铺设弹幕与螺旋,转头/移动不滞后
        Vec3 start = entity.getEyePosition().add(entity.getForward().scale(0.4)).subtract(0, 0.2, 0);
        Vec3 end = targetEntity.getEyePosition().subtract(0, 0.35, 0);
        if (end.subtract(start).length() < 0.5) {
            return;
        }
        // castDurationRemaining 递减,取负得单调递增的动画时基
        int time = -playerMagicData.getCastDurationRemaining();
        serverLevel.sendParticles(
            new TransferStreamOption(WsbParticles.TRANSFER_STREAM.get(),
                entity.getId(), targetEntity.getId(), time, 0),
            start.x, start.y, start.z, 1, 0, 0, 0, 0);
    }

    /**
     * 沿施法者视线锁定一个可补魔的非敌对生物；未命中时返回 {@code null}。
     * <p>
     * 射线必须自带敌我过滤：本法术的合法目标涵盖所有 {@link IMagicEntity}，不筛的话视线上先经过的 敌对施法怪会被当成目标，变成女仆自己扣蓝给敌人回蓝。
     */
    @Nullable
    private LivingEntity raycastLivingTarget(Level level, LivingEntity caster) {
        var raycast = RaycastBuilder.begin(level, caster)
            .range(RAYCAST_RANGE)
            .checkForBlocks(true)
            .bbInflation(0.35f)
            .filter(WinefoxHexHelper.raycastFilter(
                candidate -> isValidTarget(candidate) && !WinefoxHexHelper.isHostileTo(candidate, caster)
                    && !WinefoxHexHelper.isOwnMount(candidate, caster)))
            .build();
        if (raycast.getType() != EntityHitResult.Type.ENTITY) {
            return null;
        }
        return ((EntityHitResult) raycast).getEntity() instanceof LivingEntity living ? living : null;
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
        return MaidMagicManager.getMana(target) < getMaxMana(target);
    }

    /**
     * 验证目标是否有效（玩家或 IMagicEntity）
     */
    private boolean isValidTarget(LivingEntity target) {
        return target instanceof Player || target instanceof IMagicEntity;
    }

    /**
     * 获取实体的最大魔力值。
     * <p>
     * 上限一律走 {@link MaidMagicManager#getMaxMana}，自行读属性会与魔力条、回魔门禁的口径不一致。
     * 没有该属性的实体读值会抛 IAE，故先问一句再取。
     */
    private static double getMaxMana(LivingEntity entity) {
        return entity.getAttributes().hasAttribute(AttributeRegistry.MAX_MANA)
            ? MaidMagicManager.getMaxMana(entity) : 100.0;
    }
}
