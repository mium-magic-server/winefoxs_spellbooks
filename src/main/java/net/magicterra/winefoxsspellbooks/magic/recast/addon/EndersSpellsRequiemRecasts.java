package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import io.redspace.ironsspellbooks.capabilities.magic.MultiTargetEntityCastData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.Optional;
import java.util.UUID;
import net.magicterra.winefoxsspellbooks.magic.recast.AddonReflection;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Enders Spells Requiem 的 recast 收尾。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public final class EndersSpellsRequiemRecasts {
    private EndersSpellsRequiemRecasts() {
    }

    public static final String MOD_ID = "ess_requiem";

    private static final String SOUND_REGISTRY = "net.ender.ess_requiem.registries.GGSoundRegistry";

    private static final String PALE_FLAME = "net.ender.ess_requiem.entity.spells.pale_flame.PaleFlame";

    private static final Class<?>[] PALE_FLAME_CTOR = {LivingEntity.class, boolean.class};

    /**
     * 苍白火焰的伤害相对法术强度的倍率。
     */
    private static final float PALE_FLAME_DAMAGE_MULTIPLIER = 2.0f;

    /**
     * 向自检登记本模组的反射目标。
     */
    public static void declareReflection() {
        AddonReflection.declareClass(MOD_ID, SOUND_REGISTRY);
        AddonReflection.declareClass(MOD_ID, PALE_FLAME);
        AddonReflection.declareMethod(MOD_ID, PALE_FLAME, "<init>", PALE_FLAME_CTOR);
        AddonReflection.declareMethod(MOD_ID, PALE_FLAME, "setDamage", float.class);
    }

    /**
     * TWILIGHT_ASSAULT（暮光突袭）：对所有标记目标结算伤害，并在每个目标脚下点一簇苍白火焰。
     */
    public static void twilightAssault(RecastContext ctx) {
        ctx.addCooldown();
        if (ctx.result().isFailure()) {
            return;
        }
        if (!(ctx.castData() instanceof MultiTargetEntityCastData targetingData)) {
            return;
        }

        LivingEntity caster = ctx.caster();
        ServerLevel level = ctx.level();
        float damage = ctx.spellPower();

        // 播放声音：拿不到模组音效就退回原版凋灵射击声
        Vec3 origin = ctx.muzzle();
        level.playSound(null, origin.x, origin.y, origin.z, paleFlameStartSound(), SoundSource.PLAYERS, 2.0f, 1.0f);

        for (UUID uuid : targetingData.getTargets()) {
            if (!(level.getEntity(uuid) instanceof LivingEntity target)) {
                continue;
            }
            DamageSources.applyDamage(target, damage, ctx.spell().getDamageSource(caster));
            spawnPaleFlame(level, caster, target, damage * PALE_FLAME_DAMAGE_MULTIPLIER);
        }
    }

    private static SoundEvent paleFlameStartSound() {
        Optional<Object> holder = AddonReflection.staticField(SOUND_REGISTRY, "PALE_FLAME_START");
        if (holder.isPresent() && holder.get() instanceof DeferredHolder<?, ?> deferred
            && deferred.get() instanceof SoundEvent sound) {
            return sound;
        }
        return SoundEvents.WITHER_SHOOT;
    }

    private static void spawnPaleFlame(ServerLevel level, LivingEntity caster, LivingEntity target, float damage) {
        Optional<Object> created = AddonReflection.construct(PALE_FLAME, PALE_FLAME_CTOR, caster, false);
        if (created.isEmpty()) {
            return;
        }
        Object paleFlame = created.get();
        AddonReflection.invoke(paleFlame, "setDamage", new Class<?>[] {float.class}, damage);

        Entity flameEntity = (Entity) paleFlame;
        flameEntity.moveTo(target.position());
        flameEntity.setYRot(target.getYRot());
        flameEntity.setXRot(target.getXRot());
        level.addFreshEntity(flameEntity);
    }
}
