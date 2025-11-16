package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MultiTargetEntityCastData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.spells.fireball.SmallMagicFireball;
import io.redspace.ironsspellbooks.entity.spells.wall_of_fire.WallOfFireEntity;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.fire.FlamingBarrageSpell;
import io.redspace.ironsspellbooks.spells.fire.WallOfFireSpell;
import io.redspace.ironsspellbooks.spells.lightning.ThunderStepSpell;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
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
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 18:26
 */
public class MaidRecasts extends PlayerRecasts {
    private static final Field recastLookupField = FieldUtils.getDeclaredField(PlayerRecasts.class, "recastLookup", true);
    private static final Field remainingTicksField = FieldUtils.getDeclaredField(RecastInstance.class, "remainingTicks", true);

    protected final EntityMaid maid;

    public MaidRecasts(EntityMaid maid) {
        this.maid = maid;
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
        // TODO 兼容附属的 Recast
        if (spell instanceof FlamingBarrageSpell flamingBarrageSpell) {
            onFlamingBarrageSpellRecastFinished(flamingBarrageSpell, recastInstance, recastResult, castDataSerializable);
        } else if (spell instanceof ThunderStepSpell thunderStepSpell) {
            onThunderStepSpellRecastFinished(thunderStepSpell, recastInstance, recastResult, castDataSerializable);
        } else if (spell instanceof WallOfFireSpell wallOfFireSpell) {
            onWallOfFireSpellRecastFinished(wallOfFireSpell, recastInstance, recastResult, castDataSerializable);
        } else if (MaidSpellRegistry.isSummonSpell(spell)) {
            onSummonXXSpellRecastFinished(spell, recastInstance, recastResult, castDataSerializable);
        }
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
                    origin.add(fireWallData.anchorPoints.get(i));
                }
                origin.scale(1 / (float) fireWallData.anchorPoints.size());
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
}
