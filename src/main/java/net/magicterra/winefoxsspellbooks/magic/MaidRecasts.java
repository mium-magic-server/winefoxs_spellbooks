package net.magicterra.winefoxsspellbooks.magic;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.MultiTargetEntityCastData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.spells.fireball.SmallMagicFireball;
import io.redspace.ironsspellbooks.entity.spells.wall_of_fire.WallOfFireEntity;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.fire.FlamingBarrageSpell;
import io.redspace.ironsspellbooks.spells.fire.WallOfFireSpell;
import io.redspace.ironsspellbooks.spells.lightning.ThunderStepSpell;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * 管理重新释放的魔法，如召唤术
 * 支持 Iron's Spellbooks 核心法术和各种附属模组的 Recast 法术
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 18:26
 */
public class MaidRecasts extends PlayerRecasts {
    private static final Field recastLookupField = FieldUtils.getDeclaredField(PlayerRecasts.class, "recastLookup", true);
    private static final Field remainingTicksField = FieldUtils.getDeclaredField(RecastInstance.class, "remainingTicks", true);

    /**
     * 附属模组法术的 Recast 处理器注册表
     * Key: 法术的 ResourceLocation 字符串
     * Value: 处理函数 (recastInstance, recastResult) -> void
     */
    private final Map<String, BiConsumer<RecastInstance, RecastResult>> addonSpellHandlers = new HashMap<>();

    protected final LivingEntity maid;

    public MaidRecasts(LivingEntity maid) {
        this.maid = maid;
        registerAddonSpellHandlers();
    }

    /**
     * 注册附属模组法术的 Recast 处理器
     * 使用 ResourceLocation 字符串作为 key，避免直接依赖模组类
     */
    private void registerAddonSpellHandlers() {
        // ========== Fires Ender Expansion ==========
        // SCINTILLATING_STRIDE - 闪烁步伐：冲刺结束时造成范围伤害
        registerHandler("firesenderexpansion:scintillating_stride", this::onScintillatingStrideRecastFinished);
        // BINARY_STARS - 双星：发射追踪星体
        registerHandler("firesenderexpansion:binary_stars", this::onBinaryStarsRecastFinished);
        // HOLLOW_CRYSTAL - 空心水晶：蓄力发射水晶
        registerHandler("firesenderexpansion:hollow_crystal", this::onHollowCrystalRecastFinished);

        // ========== Cataclysm Spellbooks ==========
        // BONE_PIERCE - 骨刺穿透：用完所有 recast 时发射 8 个骨头
        registerHandler("cataclysm_spellbooks:piercing_bone", this::onBonePierceRecastFinished);
        // ABYSS_FIREBALL - 深渊火球：用完所有 recast 时发射深渊火球
        registerHandler("cataclysm_spellbooks:abyss_fireball", this::onAbyssFireballRecastFinished);

        // ========== Enders Spells Requiem ==========
        // TWILIGHT_ASSAULT - 暮光突袭：对标记目标造成伤害并生成苍白火焰
        registerHandler("ess_requiem:twilight_assault", this::onTwilightAssaultRecastFinished);
    }

    private void registerHandler(String spellId, BiConsumer<RecastInstance, RecastResult> handler) {
        addonSpellHandlers.put(spellId, handler);
    }

    // 玩家不可见 Recast
    @Override
    public void syncAllToPlayer() {
    }

    @Override
    public void syncToPlayer(RecastInstance recastInstance) {
    }

    @Override
    public void syncRemoveToPlayer(String spellId) {
    }

    public void removeRecast(RecastInstance recastInstance, RecastResult recastResult) {
        removeRecast(recastInstance, recastResult, true);
    }

    private void removeRecast(RecastInstance recastInstance, RecastResult recastResult, boolean doSync) {
        Map<String, RecastInstance> recastLookup = getRecastLookup();
        recastLookup.remove(recastInstance.getSpellId());
        if (doSync) {
            syncRemoveToPlayer(recastInstance.getSpellId());
        }
        triggerRecastComplete(recastInstance, recastResult);
    }

    private void triggerRecastComplete(RecastInstance recastInstance, RecastResult recastResult) {
        AbstractSpell spell = SpellRegistry.getSpell(recastInstance.getSpellId());
        onSpellRecastFinished(spell, recastInstance, recastResult,recastInstance.getCastData());
    }

    public void tick(int actualTicks) {
        Map<String, RecastInstance> recastLookup = getRecastLookup();
        if (maid != null && maid.level.getGameTime() % actualTicks == 0) {
            recastLookup.values()
                .stream()
                .filter(r -> {
                    setRecastInstanceRemainingTicks(r, r.getTicksRemaining() - actualTicks);
                    ICastDataSerializable castData = r.getCastData();
                    ServerLevel level = (ServerLevel) maid.level;
                    if (castData instanceof SummonedEntitiesCastData summonedEntitiesCastData) {
                        long count = summonedEntitiesCastData.getSummons().stream()
                            .map(level::getEntity)
                            .filter(Objects::nonNull)
                            .count();
                        if (count == 0) {
                            return true;
                        }
                    }
                    return r.getTicksRemaining() <= 0;
                })
                .toList()
                .forEach(recastInstance -> removeRecast(recastInstance, RecastResult.TIMEOUT));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, RecastInstance> getRecastLookup() {
        Map<String, RecastInstance> recastLookup;
        try {
            recastLookup = (Map<String, RecastInstance>) FieldUtils.readField(recastLookupField, this);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return recastLookup;
    }

    private static void setRecastInstanceRemainingTicks(RecastInstance recastInstance, int remainingTicks) {
        try {
            FieldUtils.writeField(remainingTicksField, recastInstance, remainingTicks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private void onSpellRecastFinished(AbstractSpell spell, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        String spellId = spell.getSpellId();

        // 1. 首先检查是否是 Iron's Spellbooks 核心法术（需要特殊处理）
        if (spell instanceof FlamingBarrageSpell flamingBarrageSpell) {
            onFlamingBarrageSpellRecastFinished(flamingBarrageSpell, recastInstance, recastResult, castDataSerializable);
            return;
        } else if (spell instanceof ThunderStepSpell thunderStepSpell) {
            onThunderStepSpellRecastFinished(thunderStepSpell, recastInstance, recastResult, castDataSerializable);
            return;
        } else if (spell instanceof WallOfFireSpell wallOfFireSpell) {
            onWallOfFireSpellRecastFinished(wallOfFireSpell, recastInstance, recastResult, castDataSerializable);
            return;
        }

        // 2. 检查是否是召唤法术（通用处理）
        if (MaidSpellRegistry.isSummonSpell(spell)) {
            onSummonXXSpellRecastFinished(spell, recastInstance, recastResult, castDataSerializable);
            return;
        }

        // 3. 检查是否有注册的附属模组处理器
        BiConsumer<RecastInstance, RecastResult> handler = addonSpellHandlers.get(spellId);
        if (handler != null) {
            try {
                handler.accept(recastInstance, recastResult);
            } catch (NoClassDefFoundError | Exception e) {
                // 模组未安装或处理失败，使用默认处理
                WinefoxsSpellbooks.LOGGER.debug("Addon spell handler failed for {}, using default handler: {}", spellId, e.getMessage());
                onDefaultRecastFinished(spell, recastInstance);
            }
            return;
        }

        // 4. 默认处理：只添加冷却
        onDefaultRecastFinished(spell, recastInstance);
    }

    /**
     * 默认的 Recast 完成处理：只添加冷却
     * 适用于未重写 onRecastFinished 的法术
     */
    private void onDefaultRecastFinished(AbstractSpell spell, RecastInstance recastInstance) {
        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());
    }

    private void onFlamingBarrageSpellRecastFinished(FlamingBarrageSpell spell, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());
        var level = maid.level;
        Vec3 origin = maid.getEyePosition().add(maid.getForward().normalize().scale(.2f));
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 2.0f, 1.0f);
        if (castDataSerializable instanceof MultiTargetEntityCastData targetingData) {
            targetingData.getTargets().forEach(uuid -> {
                var target = (LivingEntity) ((ServerLevel) maid.level).getEntity(uuid);
                if (target != null) {
                    SmallMagicFireball fireball = new SmallMagicFireball(level, maid);
                    fireball.setPos(origin.subtract(0, fireball.getBbHeight(), 0));
                    var vec = target.getBoundingBox().getCenter().subtract(maid.getEyePosition()).normalize();
                    var inaccuracy = (float) Mth.clampedLerp(.2f, 1.4f, target.position().distanceToSqr(maid.position()) / (32 * 32));
                    fireball.shoot(vec.scale(.75f), inaccuracy);
                    fireball.setDamage(spell.getSpellPower(recastInstance.getSpellLevel(), maid));
                    fireball.setHomingTarget(target);
                    level.addFreshEntity(fireball);
                }
            });
        }
    }

    private void onSummonXXSpellRecastFinished(AbstractSpell spell, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (recastResult == RecastResult.COUNTERSPELL) {
            //ignore counterspell
            IMagicEntity magicEntity = (IMagicEntity) maid;
            magicEntity.getMagicData().getPlayerRecasts().forceAddRecast(recastInstance);
        } else if (recastResult != RecastResult.TIMEOUT) { // timeouts are handled by summon manager
            if (castDataSerializable instanceof SummonedEntitiesCastData summonedEntitiesCastData) {
                var serverLevel = ((ServerLevel) maid.level);
                summonedEntitiesCastData.getSummons().forEach(uuid -> {
                    var toRemove = serverLevel.getEntity(uuid);
                    if (toRemove instanceof IMagicSummon summon) {
                        summon.onUnSummon();
                    } else if (toRemove != null) {
                        toRemove.discard();
                    }
                });
            }
        } else if (ItemRegistry.GREATER_CONJURERS_TALISMAN.get().isEquippedBy(maid)) {
            return;
        }
        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());
    }

    private void onThunderStepSpellRecastFinished(ThunderStepSpell spell, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());
        var serverlevel = ((ServerLevel) maid.level());
        if (castDataSerializable instanceof MultiTargetEntityCastData targetData && !targetData.getTargets().isEmpty()) {
            Entity orb = serverlevel.getEntity(targetData.getTargets().getFirst());
            if (orb == null) {
                return;
            }
            if (!recastResult.isFailure()) {

                Vec3 dest = TeleportSpell.solveTeleportDestination(serverlevel, maid, orb.blockPosition(), orb.position());
                Vec3 travel = dest.subtract(maid.position());
                if (travel.lengthSqr() < 32 * 32) {
                    thunderStepSpellZapEntitiesBetween(spell, maid, recastInstance.getSpellLevel(), dest);
                    for (int i = 0; i < 7; i++) {
                        Vec3 random1 = Utils.getRandomVec3(0.5f).multiply(maid.getBbWidth(), maid.getBbHeight(), maid.getBbWidth());
                        Vec3 random2 = Utils.getRandomVec3(0.8f).multiply(maid.getBbWidth(), maid.getBbHeight(), maid.getBbWidth());
                        float yOffset = i / 7f * maid.getBbHeight();
                        Vec3 midpoint = maid.position().add(travel.scale(0.5f)).add(random2);
                        serverlevel.sendParticles(new ZapParticleOption(random1.add(maid.getX(), maid.getY() + yOffset, maid.getZ())), midpoint.x, midpoint.y, midpoint.z, 1, 0, 0, 0, 0);
                        serverlevel.sendParticles(new ZapParticleOption(random1.scale(-1f).add(dest.x, dest.y + yOffset, dest.z)), midpoint.x, midpoint.y, midpoint.z, 1, 0, 0, 0, 0);
                    }
                }

                if (maid.isPassenger()) {
                    maid.stopRiding();
                }
                Utils.handleSpellTeleport(spell, maid, dest);
                maid.resetFallDistance();

            }
            orb.discard();
        }
    }

    private void onWallOfFireSpellRecastFinished(WallOfFireSpell spell, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (!recastResult.isFailure()) {
            var level = maid.level;
            var fireWallData = (WallOfFireSpell.FireWallData) recastInstance.getCastData();
            if (fireWallData.anchorPoints.size() == 1) {
                spell.addAnchor(fireWallData, level, maid, recastInstance);
            }

            if (fireWallData.anchorPoints.size() > 0) {
                WallOfFireEntity fireWall = new WallOfFireEntity(level, maid, fireWallData.anchorPoints, spell.getSpellPower(recastInstance.getSpellLevel(), maid));
                Vec3 origin = fireWallData.anchorPoints.get(0);
                for (int i = 1; i < fireWallData.anchorPoints.size(); i++) {
                    origin = origin.add(fireWallData.anchorPoints.get(i));
                }
                origin = origin.scale(1 / (float) fireWallData.anchorPoints.size());
                fireWall.setPos(origin);
                level.addFreshEntity(fireWall);
            }
        }
        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());
    }

    private static void thunderStepSpellZapEntitiesBetween(ThunderStepSpell spell, LivingEntity caster, int spellLevel, Vec3 blockEnd) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = blockEnd.add(0, caster.getEyeHeight(), 0);
        AABB range = caster.getBoundingBox().expandTowards(end.subtract(start));
        List<? extends Entity> entities = caster.level.getEntities(caster, range);
        for (Entity target : entities) {
            Vec3 height = new Vec3(0, caster.getEyeHeight(), 0);
            //Raycast from eyes and from feet. Rectangular zone of zapping.
            if (Utils.checkEntityIntersecting(target, start, end, 1f).getType() != HitResult.Type.MISS
                || Utils.checkEntityIntersecting(target, start.subtract(height), end.subtract(height), 1f).getType() != HitResult.Type.MISS) {
                DamageSources.applyDamage(target, spell.getSpellPower(spellLevel, caster), spell.getDamageSource(caster));
            }
        }
    }

    // ==================== 附属模组法术处理器 ====================

    /**
     * Fires Ender Expansion - SCINTILLATING_STRIDE (闪烁步伐)
     * Recast 完成时：生成粒子效果、播放声音、对范围内敌人造成伤害、移除效果
     */
    private void onScintillatingStrideRecastFinished(RecastInstance recastInstance, RecastResult recastResult) {
        AbstractSpell spell = SpellRegistry.getSpell(recastInstance.getSpellId());
        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());

        if (!recastResult.isSuccess()) {
            return;
        }

        ServerLevel level = (ServerLevel) maid.level();
        int spellLevel = recastInstance.getSpellLevel();
        float spellPower = spell.getSpellPower(spellLevel, maid);
        float damage = (spellPower * 0.5f) + 5.0f;
        float radius = 2.0f + (spellPower / 10.0f);

        // 生成粒子效果
        for (int i = 0; i < 20; i++) {
            level.sendParticles(
                (SimpleParticleType) ParticleRegistry.UNSTABLE_ENDER_PARTICLE.get(),
                maid.getX(), maid.getY() + 1.0, maid.getZ(),
                20, 0.0, 0.0, 0.0, 0.3
            );
        }

        // 生成冲击波粒子
        MagicManager.spawnParticles(
            level,
            new BlastwaveParticleOptions(((SchoolType) SchoolRegistry.ENDER.get()).getTargetingColor(), radius),
            maid.position().x, maid.position().y, maid.position().z,
            1, 0.0, 0.0, 0.0, 0.0, true
        );

        // 播放声音
        level.playSound(null, maid.position().x(), maid.position().y(), maid.position().z(),
            SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 15.0f, 1.0f);

        // 对范围内敌人造成伤害
        level.getEntitiesOfClass(LivingEntity.class, maid.getBoundingBox().inflate(radius))
            .stream()
            .filter(e -> e != maid && !e.isAlliedTo(maid))
            .forEach(e -> DamageSources.applyDamage(e, damage, spell.getDamageSource(maid)));

        // 尝试移除效果（如果模组已安装）
        tryRemoveEffect("firesenderexpansion:striding");
    }

    /**
     * Fires Ender Expansion - BINARY_STARS (双星)
     * Recast 完成时：向标记的目标发射追踪星体
     * 注意：由于需要创建模组特定的实体，此方法使用反射来避免硬依赖
     */
    private void onBinaryStarsRecastFinished(RecastInstance recastInstance, RecastResult recastResult) {
        AbstractSpell spell = SpellRegistry.getSpell(recastInstance.getSpellId());
        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());

        if (!recastResult.isSuccess()) {
            return;
        }

        ICastDataSerializable castData = recastInstance.getCastData();
        if (!(castData instanceof MultiTargetEntityCastData targetingData)) {
            return;
        }

        ServerLevel level = (ServerLevel) maid.level();
        int spellLevel = recastInstance.getSpellLevel();
        float spellPower = spell.getSpellPower(spellLevel, maid);
        float damage = (spellPower * 0.15f) + 5.0f;
        int duration = (int) spellPower + 100;

        List<UUID> targets = targetingData.getTargets();

        // 计算发射方向
        double cosPsi = Math.cos(Math.toRadians(maid.getYRot()));
        double sinPsi = Math.sin(Math.toRadians(maid.getYRot()));
        double cosTheta = Math.cos(Math.toRadians(maid.getXRot()));
        double sinTheta = Math.sin(Math.toRadians(maid.getXRot()));

        // 尝试使用反射创建星体实体
        try {
            for (int i = 0; i < targets.size(); i++) {
                Entity targetEntity = level.getEntity(targets.get(i));
                if (!(targetEntity instanceof LivingEntity target)) {
                    continue;
                }

                // 计算发射位置
                double offsetX = (i == 0 || targets.size() == 1) ? 1.0 : -1.0;
                Vec3 origin = maid.position().add(
                    (offsetX * cosPsi) - ((2.0 * sinTheta) * sinPsi),
                    2.0 * cosTheta,
                    (offsetX * sinPsi) + (2.0 * sinTheta * cosPsi)
                );

                // 使用反射创建星体实体
                String entityClassName = (i == 0 || targets.size() == 1)
                    ? "net.fireofpower.firesenderexpansion.entities.spells.BinaryStars.NovaStar.NovaStarEntity"
                    : "net.fireofpower.firesenderexpansion.entities.spells.BinaryStars.ObsidianStar.ObsidianStarEntity";

                createAndShootHomingProjectile(entityClassName, level, origin, target, damage, duration);
            }
        } catch (Exception e) {
            WinefoxsSpellbooks.LOGGER.debug("Failed to create Binary Stars entities: {}", e.getMessage());
        }
    }

    /**
     * Fires Ender Expansion - HOLLOW_CRYSTAL (空心水晶)
     * Recast 完成时：发射蓄力的水晶
     * 注意：原版使用 Timer 延迟执行，这里简化为立即执行
     */
    private void onHollowCrystalRecastFinished(RecastInstance recastInstance, RecastResult recastResult) {
        AbstractSpell spell = SpellRegistry.getSpell(recastInstance.getSpellId());
        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());

        if (!recastResult.isSuccess()) {
            return;
        }

        // 检查是否有效果（需要效果才能发射）
        if (!hasEffect("firesenderexpansion:hollow_crystal")) {
            return;
        }

        ServerLevel level = (ServerLevel) maid.level();
        Vec3 lookDir = maid.getLookAngle();

        try {
            // 使用反射创建 HollowCrystal 实体
            Class<?> hollowCrystalClass = Class.forName("net.fireofpower.firesenderexpansion.entities.spells.HollowCrystal.HollowCrystal");
            Object hollowCrystal = hollowCrystalClass
                .getConstructor(net.minecraft.world.level.Level.class, LivingEntity.class)
                .newInstance(level, maid);

            // 设置位置
            Entity entity = (Entity) hollowCrystal;
            double yOffset = maid.getEyeHeight() + (entity.getBoundingBox().getYsize() * 0.25) - 3.0;
            Vec3 pos = maid.position().add(0, yOffset, 0).add(maid.getForward().multiply(3.0, 3.0, 3.0));
            entity.setPos(pos);

            // 设置伤害（通过反射）
            try {
                hollowCrystalClass.getMethod("setDamage", float.class).invoke(hollowCrystal, getHollowCrystalDamage(spell));
            } catch (Exception ignored) {
            }

            // 设置移动方向
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5, 0.5, 0.5));

            // 发射（通过反射调用 shoot 方法）
            try {
                hollowCrystalClass.getMethod("shoot", Vec3.class).invoke(hollowCrystal, lookDir);
            } catch (Exception ignored) {
            }

            // 移除效果
            tryRemoveEffect("firesenderexpansion:hollow_crystal");

            // 添加到世界
            level.addFreshEntity(entity);

            // 播放声音
            level.playSound(null, maid, io.redspace.ironsspellbooks.registries.SoundRegistry.SONIC_BOOM.get(),
                SoundSource.PLAYERS, 3.0f, 1.0f);

        } catch (Exception e) {
            WinefoxsSpellbooks.LOGGER.debug("Failed to create Hollow Crystal entity: {}", e.getMessage());
        }
    }

    private float getHollowCrystalDamage(AbstractSpell spell) {
        // 尝试获取效果等级来计算伤害
        try {
            Class<?> effectRegistryClass = Class.forName("net.fireofpower.firesenderexpansion.registries.EffectRegistry");
            Object effectHolder = effectRegistryClass.getField("HOLLOW_CRYSTAL_EFFECT").get(null);
            var effect = maid.getEffect((net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>) effectHolder);
            if (effect != null) {
                return ((effect.getAmplifier() * 15.0f) * spell.getSpellPower(1, maid)) / 50.0f;
            }
        } catch (Exception ignored) {
        }
        return (15.0f * spell.getSpellPower(1, maid)) / 50.0f;
    }

    /**
     * Cataclysm Spellbooks - BONE_PIERCE (骨刺穿透)
     * 用完所有 recast 时：向 8 个方向发射骨头
     */
    private void onBonePierceRecastFinished(RecastInstance recastInstance, RecastResult recastResult) {
        AbstractSpell spell = SpellRegistry.getSpell(recastInstance.getSpellId());

        // 只有用完所有 recast 时才发射额外骨头
        if (recastResult == RecastResult.USED_ALL_RECASTS) {
            ServerLevel level = (ServerLevel) maid.level();
            spreadBoneShoot(level);
        }

        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());
    }

    /**
     * 向 8 个方向发射骨头（Cataclysm 的 Blazing_Bone_Entity）
     */
    private void spreadBoneShoot(ServerLevel level) {
        maid.playSound(SoundEvents.DROWNED_SHOOT, 1.0f, 0.75f);

        try {
            Class<?> blazingBoneClass = Class.forName("com.github.L_Ender.cataclysm.entity.projectile.Blazing_Bone_Entity");

            for (int i = 0; i < 8; i++) {
                float throwAngle = (float) ((i * Math.PI) / 4.0);
                double casterX = maid.getX() + Mth.cos(throwAngle);
                double casterY = maid.getY() + (maid.getBbHeight() * 0.62);
                double casterZ = maid.getZ() + Mth.sin(throwAngle);
                double angleX = Mth.cos(throwAngle);
                double angleZ = Mth.sin(throwAngle);

                // 创建骨头实体
                Object blazingBone = blazingBoneClass
                    .getConstructor(net.minecraft.world.level.Level.class, float.class, LivingEntity.class)
                    .newInstance(level, 3.0f, maid);

                Entity entity = (Entity) blazingBone;
                entity.moveTo(casterX, casterY, casterZ, i * 45.0f, maid.getXRot());

                // 设置无重力
                try {
                    blazingBoneClass.getMethod("setNoGravity", boolean.class).invoke(blazingBone, true);
                } catch (Exception ignored) {
                }

                // 发射
                try {
                    blazingBoneClass.getMethod("shoot", double.class, double.class, double.class, float.class, float.class)
                        .invoke(blazingBone, angleX, 0.2, angleZ, 0.5f, 1.0f);
                } catch (Exception ignored) {
                }

                level.addFreshEntity(entity);
            }
        } catch (Exception e) {
            WinefoxsSpellbooks.LOGGER.debug("Failed to create Blazing Bone entities: {}", e.getMessage());
        }
    }

    /**
     * Cataclysm Spellbooks - ABYSS_FIREBALL (深渊火球)
     * 用完所有 recast 时：发射深渊火球
     */
    private void onAbyssFireballRecastFinished(RecastInstance recastInstance, RecastResult recastResult) {
        AbstractSpell spell = SpellRegistry.getSpell(recastInstance.getSpellId());

        // 只有用完所有 recast 时才发射深渊火球
        if (recastResult == RecastResult.USED_ALL_RECASTS) {
            ServerLevel level = (ServerLevel) maid.level();
            shootAbyssFireball(level);
        }

        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());
    }

    /**
     * 发射深渊火球（Cataclysm 的 Ignis_Abyss_Fireball_Entity）
     */
    private void shootAbyssFireball(ServerLevel level) {
        try {
            Class<?> fireballClass = Class.forName("com.github.L_Ender.cataclysm.entity.projectile.Ignis_Abyss_Fireball_Entity");

            Object fireball = fireballClass
                .getConstructor(net.minecraft.world.level.Level.class, LivingEntity.class)
                .newInstance(level, maid);

            Entity entity = (Entity) fireball;
            entity.setPos(maid.position().add(0, maid.getEyeHeight() - (entity.getBoundingBox().getYsize() * 0.5), 0));

            // 发射
            try {
                fireballClass.getMethod("shootFromRotation", Entity.class, float.class, float.class, float.class, float.class, float.class)
                    .invoke(fireball, maid, maid.getXRot(), maid.getYHeadRot(), 0.0f, 1.0f, 1.0f);
            } catch (Exception ignored) {
            }

            level.addFreshEntity(entity);
        } catch (Exception e) {
            WinefoxsSpellbooks.LOGGER.debug("Failed to create Abyss Fireball entity: {}", e.getMessage());
        }
    }

    /**
     * Enders Spells Requiem - TWILIGHT_ASSAULT (暮光突袭)
     * Recast 完成时：对所有标记的目标造成伤害并生成苍白火焰
     */
    private void onTwilightAssaultRecastFinished(RecastInstance recastInstance, RecastResult recastResult) {
        AbstractSpell spell = SpellRegistry.getSpell(recastInstance.getSpellId());
        MaidMagicManager.addCooldown(maid, spell, recastInstance.getCastSource());

        if (recastResult.isFailure()) {
            return;
        }

        ICastDataSerializable castData = recastInstance.getCastData();
        if (!(castData instanceof MultiTargetEntityCastData targetingData)) {
            return;
        }

        ServerLevel level = (ServerLevel) maid.level();
        int spellLevel = recastInstance.getSpellLevel();
        float damage = spell.getSpellPower(spellLevel, maid);

        // 播放声音
        Vec3 origin = maid.getEyePosition().add(maid.getForward().normalize().scale(0.2));
        try {
            Class<?> soundRegistryClass = Class.forName("net.ender.ess_requiem.registries.GGSoundRegistry");
            Object soundHolder = soundRegistryClass.getField("PALE_FLAME_START").get(null);
            if (soundHolder instanceof net.neoforged.neoforge.registries.DeferredHolder<?, ?> holder) {
                level.playSound(null, origin.x, origin.y, origin.z,
                    (net.minecraft.sounds.SoundEvent) holder.get(), SoundSource.PLAYERS, 2.0f, 1.0f);
            }
        } catch (Exception ignored) {
            level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 2.0f, 1.0f);
        }

        // 对每个目标造成伤害并生成苍白火焰
        for (UUID uuid : targetingData.getTargets()) {
            Entity targetEntity = level.getEntity(uuid);
            if (!(targetEntity instanceof LivingEntity target)) {
                continue;
            }

            // 造成伤害
            DamageSources.applyDamage(target, damage, spell.getDamageSource(maid));

            // 尝试创建苍白火焰实体
            try {
                Class<?> paleFlameClass = Class.forName("net.ender.ess_requiem.entity.spells.pale_flame.PaleFlame");
                Object paleFlame = paleFlameClass
                    .getConstructor(LivingEntity.class, boolean.class)
                    .newInstance(maid, false);

                Entity flameEntity = (Entity) paleFlame;

                // 设置伤害
                try {
                    paleFlameClass.getMethod("setDamage", float.class).invoke(paleFlame, damage * 2.0f);
                } catch (Exception ignored) {
                }

                // 设置位置和朝向
                flameEntity.moveTo(target.position());
                flameEntity.setYRot(target.getYRot());
                flameEntity.setXRot(target.getXRot());

                level.addFreshEntity(flameEntity);
            } catch (Exception e) {
                WinefoxsSpellbooks.LOGGER.debug("Failed to create Pale Flame entity: {}", e.getMessage());
            }
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 使用反射创建并发射追踪投射物
     */
    private void createAndShootHomingProjectile(String className, ServerLevel level, Vec3 origin,
                                                 LivingEntity target, float damage, int duration) {
        try {
            Class<?> entityClass = Class.forName(className);
            Object projectile = entityClass
                .getConstructor(net.minecraft.world.level.Level.class, LivingEntity.class)
                .newInstance(level, maid);

            Entity entity = (Entity) projectile;
            entity.setPos(origin);

            // 计算发射方向
            Vec3 vec = target.getBoundingBox().getCenter().subtract(maid.getEyePosition()).normalize();

            // 调用 shoot 方法
            try {
                entityClass.getMethod("shoot", Vec3.class).invoke(projectile, vec.scale(0.75));
            } catch (Exception ignored) {
            }

            // 设置伤害
            try {
                entityClass.getMethod("setDamage", float.class).invoke(projectile, damage);
            } catch (Exception ignored) {
            }

            // 设置追踪目标
            try {
                entityClass.getMethod("setHomingTarget", LivingEntity.class).invoke(projectile, target);
            } catch (Exception ignored) {
            }

            // 设置持续时间
            try {
                entityClass.getMethod("setDuration", int.class).invoke(projectile, duration);
            } catch (Exception ignored) {
            }

            level.addFreshEntity(entity);
        } catch (Exception e) {
            WinefoxsSpellbooks.LOGGER.debug("Failed to create homing projectile {}: {}", className, e.getMessage());
        }
    }

    /**
     * 尝试移除指定的效果（通过 ResourceLocation）
     */
    private void tryRemoveEffect(String effectId) {
        try {
            ResourceLocation effectLoc = ResourceLocation.parse(effectId);
            var effectRegistry = maid.level().registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.MOB_EFFECT);
            var effect = effectRegistry.get(effectLoc);
            if (effect != null) {
                maid.removeEffect(effectRegistry.wrapAsHolder(effect));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 检查是否有指定的效果（通过 ResourceLocation）
     */
    private boolean hasEffect(String effectId) {
        try {
            ResourceLocation effectLoc = ResourceLocation.parse(effectId);
            var effectRegistry = maid.level().registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.MOB_EFFECT);
            var effect = effectRegistry.get(effectLoc);
            if (effect != null) {
                return maid.hasEffect(effectRegistry.wrapAsHolder(effect));
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
