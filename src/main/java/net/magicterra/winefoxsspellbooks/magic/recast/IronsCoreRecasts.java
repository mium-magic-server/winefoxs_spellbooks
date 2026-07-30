package net.magicterra.winefoxsspellbooks.magic.recast;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MultiTargetEntityCastData;
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
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Iron's Spellbooks 本体法术的 recast 收尾，照原实现重写为静态方法，上下文取自 {@link RecastContext}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public final class IronsCoreRecasts {
    private IronsCoreRecasts() {
    }

    /**
     * 默认收尾：只挂冷却。适用于没有特殊收尾动作的法术。
     */
    public static void defaultFinish(RecastContext ctx) {
        ctx.addCooldown();
    }

    /**
     * 炽焰追踪弹幕：对所有标记目标各射一发追踪小火球。
     */
    public static void flamingBarrage(RecastContext ctx) {
        ctx.addCooldown();
        if (!(ctx.spell() instanceof FlamingBarrageSpell spell)) {
            return;
        }
        LivingEntity caster = ctx.caster();
        var level = ctx.level();
        Vec3 origin = ctx.muzzle();
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 2.0f, 1.0f);
        if (ctx.castData() instanceof MultiTargetEntityCastData targetingData) {
            targetingData.getTargets().forEach(uuid -> {
                if (!(level.getEntity(uuid) instanceof LivingEntity target)) {
                    return;
                }
                SmallMagicFireball fireball = new SmallMagicFireball(level, caster);
                fireball.setPos(origin.subtract(0, fireball.getBbHeight(), 0));
                var vec = target.getBoundingBox().getCenter().subtract(caster.getEyePosition()).normalize();
                var inaccuracy = (float) Mth.clampedLerp(.2f, 1.4f,
                    target.position().distanceToSqr(caster.position()) / (32 * 32));
                fireball.shoot(vec.scale(.75f), inaccuracy);
                fireball.setDamage(spell.getSpellPower(ctx.spellLevel(), caster));
                fireball.setHomingTarget(target);
                level.addFreshEntity(fireball);
            });
        }
    }

    /**
     * 召唤类法术：二段咏唱即收回召唤物。
     */
    public static void summon(RecastContext ctx) {
        LivingEntity caster = ctx.caster();
        if (ctx.result() == RecastResult.COUNTERSPELL) {
            //ignore counterspell
            IMagicEntity magicEntity = (IMagicEntity) caster;
            magicEntity.getMagicData().getPlayerRecasts().forceAddRecast(ctx.instance());
        } else if (ctx.result() != RecastResult.TIMEOUT) { // timeouts are handled by summon manager
            if (ctx.castData() instanceof SummonedEntitiesCastData summonedEntitiesCastData) {
                summonedEntitiesCastData.getSummons().forEach(uuid -> {
                    var toRemove = ctx.level().getEntity(uuid);
                    if (toRemove instanceof IMagicSummon summon) {
                        summon.onUnSummon();
                    } else if (toRemove != null) {
                        toRemove.discard();
                    }
                });
            }
        } else if (ItemRegistry.GREATER_CONJURERS_TALISMAN.get().isEquippedBy(caster)) {
            return;
        }
        ctx.addCooldown();
    }

    /**
     * 雷霆步：传送到标记球体处，沿途电击路径上的实体。
     */
    public static void thunderStep(RecastContext ctx) {
        ctx.addCooldown();
        if (!(ctx.spell() instanceof ThunderStepSpell spell)) {
            return;
        }
        LivingEntity caster = ctx.caster();
        var serverlevel = ctx.level();
        if (ctx.castData() instanceof MultiTargetEntityCastData targetData && !targetData.getTargets().isEmpty()) {
            Entity orb = serverlevel.getEntity(targetData.getTargets().getFirst());
            if (orb == null) {
                return;
            }
            if (!ctx.result().isFailure()) {
                Vec3 dest = TeleportSpell.solveTeleportDestination(serverlevel, caster, orb.blockPosition(), orb.position());
                Vec3 travel = dest.subtract(caster.position());
                if (travel.lengthSqr() < 32 * 32) {
                    zapEntitiesBetween(spell, caster, ctx.spellLevel(), dest);
                    for (int i = 0; i < 7; i++) {
                        Vec3 random1 = Utils.getRandomVec3(0.5f).multiply(caster.getBbWidth(), caster.getBbHeight(), caster.getBbWidth());
                        Vec3 random2 = Utils.getRandomVec3(0.8f).multiply(caster.getBbWidth(), caster.getBbHeight(), caster.getBbWidth());
                        float yOffset = i / 7f * caster.getBbHeight();
                        Vec3 midpoint = caster.position().add(travel.scale(0.5f)).add(random2);
                        serverlevel.sendParticles(new ZapParticleOption(random1.add(caster.getX(), caster.getY() + yOffset, caster.getZ())),
                            midpoint.x, midpoint.y, midpoint.z, 1, 0, 0, 0, 0);
                        serverlevel.sendParticles(new ZapParticleOption(random1.scale(-1f).add(dest.x, dest.y + yOffset, dest.z)),
                            midpoint.x, midpoint.y, midpoint.z, 1, 0, 0, 0, 0);
                    }
                }

                if (caster.isPassenger()) {
                    caster.stopRiding();
                }
                Utils.handleSpellTeleport(spell, caster, dest);
                caster.resetFallDistance();
            }
            orb.discard();
        }
    }

    /**
     * 火墙术：在锚点之间拉起火墙。
     */
    public static void wallOfFire(RecastContext ctx) {
        if (!(ctx.spell() instanceof WallOfFireSpell spell)) {
            ctx.addCooldown();
            return;
        }
        LivingEntity caster = ctx.caster();
        if (!ctx.result().isFailure()) {
            var level = ctx.level();
            var fireWallData = (WallOfFireSpell.FireWallData) ctx.instance().getCastData();
            if (fireWallData.anchorPoints.size() == 1) {
                spell.addAnchor(fireWallData, level, caster, ctx.instance());
            }

            if (!fireWallData.anchorPoints.isEmpty()) {
                WallOfFireEntity fireWall = new WallOfFireEntity(level, caster, fireWallData.anchorPoints,
                    spell.getSpellPower(ctx.spellLevel(), caster));
                Vec3 origin = fireWallData.anchorPoints.get(0);
                for (int i = 1; i < fireWallData.anchorPoints.size(); i++) {
                    origin = origin.add(fireWallData.anchorPoints.get(i));
                }
                origin = origin.scale(1 / (float) fireWallData.anchorPoints.size());
                fireWall.setPos(origin);
                level.addFreshEntity(fireWall);
            }
        }
        ctx.addCooldown();
    }

    private static void zapEntitiesBetween(ThunderStepSpell spell, LivingEntity caster, int spellLevel, Vec3 blockEnd) {
        Vec3 start = caster.getEyePosition();
        Vec3 end = blockEnd.add(0, caster.getEyeHeight(), 0);
        AABB range = caster.getBoundingBox().expandTowards(end.subtract(start));
        List<? extends Entity> entities = caster.level().getEntities(caster, range);
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
