package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamEntity;
import io.redspace.ironsspellbooks.entity.spells.void_tentacle.VoidTentacle;
import io.redspace.ironsspellbooks.entity.spells.wisp.WispEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.magicterra.winefoxsspellbooks.magic.recast.AddonReflection;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastContext;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastRegistrar;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

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

    /**
     * 苍白火焰起手音在该音效注册表里的字段名。
     */
    private static final String PALE_FLAME_START_FIELD = "PALE_FLAME_START";

    private static final String PALE_FLAME = "net.ender.ess_requiem.entity.spells.pale_flame.PaleFlame";

    /**
     * PaleFlame 只有 {@code (EntityType, Level)} 与 {@code (Level, boolean)} 两个构造器， 后者的 boolean 是「镜像」而非归属，归属须构造后自行 {@code setOwner}。
     */
    private static final Class<?>[] PALE_FLAME_CTOR = {Level.class, boolean.class};

    /**
     * 苍白火焰的伤害相对法术强度的倍率。
     */
    private static final float PALE_FLAME_DAMAGE_MULTIPLIER = 2.0f;

    /**
     * 虚空触手的挥击伤害相对法术强度的倍率。构造器已按全额伤害设过一次，原法术随后又压到一半。
     */
    private static final float VOID_TENTACLE_DAMAGE_MULTIPLIER = 0.5f;

    /**
     * 夜幕之纱给施法者的隐身时长（tick）。
     */
    private static final int INVISIBILITY_TICKS = 300;

    /**
     * 附属自己的增益时长（tick），夜纱与光之壁垒同为 800。
     */
    private static final int BUFF_TICKS = 800;

    /**
     * 夜纱效果 id。效果在动态注册表里，按 id 查即可，不必碰模组的类。
     */
    private static final String NIGHT_VEIL_EFFECT = MOD_ID + ":night_veil";

    /**
     * 光之壁垒效果 id。
     */
    private static final String BASTION_OF_LIGHT_EFFECT = MOD_ID + ":bastion_of_light";

    /**
     * 鬼火的出生点距施法者眼前的距离。
     */
    private static final float WISP_SPAWN_DISTANCE = 2.0f;

    /**
     * 登记本模组的收尾动作与反射目标。
     */
    public static void contribute(RecastRegistrar registrar) {
        AddonReflection.declareField(MOD_ID, SOUND_REGISTRY, PALE_FLAME_START_FIELD);
        AddonReflection.declareClass(MOD_ID, PALE_FLAME);
        AddonReflection.declareMethod(MOD_ID, PALE_FLAME, "<init>", PALE_FLAME_CTOR);

        registrar.register("twilight_assault", EndersSpellsRequiemRecasts::twilightAssault);
        // 夜幕之纱的注册 id 是 nights_cover，不是这里的方法名 nightVeil
        registrar.register("nights_cover", EndersSpellsRequiemRecasts::nightVeil);
        registrar.register("overwhelming_light", EndersSpellsRequiemRecasts::overwhelmingLight);
    }

    /**
     * TWILIGHT_ASSAULT（暮光突袭）：对所有标记目标结算伤害，并在每个目标脚下点一簇苍白火焰。
     */
    public static void twilightAssault(RecastContext ctx) {
        if (!ctx.beginMultiTargetFinish()) {
            return;
        }

        LivingEntity caster = ctx.caster();
        ServerLevel level = ctx.level();
        float damage = ctx.spellPower();
        DamageSource damageSource = ctx.spell().getDamageSource(caster);

        // 播放声音：拿不到模组音效就退回原版凋灵射击声
        ctx.playAtMuzzle(paleFlameStartSound(), 2.0f);

        for (LivingEntity target : ctx.markedTargets()) {
            DamageSources.applyDamage(target, damage, damageSource);
            spawnPaleFlame(level, caster, target, damage * PALE_FLAME_DAMAGE_MULTIPLIER);
        }
    }

    /**
     * NIGHT_VEIL（夜幕之纱）：施法者隐入夜色（隐身 + 夜纱），对所有标记目标结算伤害， 并在每个目标处召出一条虚空触手和一簇苍白火焰。
     */
    public static void nightVeil(RecastContext ctx) {
        if (!ctx.beginMultiTargetFinish()) {
            return;
        }

        LivingEntity caster = ctx.caster();
        ServerLevel level = ctx.level();
        float damage = ctx.spellPower();
        DamageSource damageSource = ctx.spell().getDamageSource(caster);

        // 播放声音：拿不到模组音效就退回原版凋灵射击声
        ctx.playAtMuzzle(paleFlameStartSound(), 2.0f);

        for (LivingEntity target : ctx.markedTargets()) {
            // 增益写在目标循环内，与原法术一致：目标越多，隐身与夜纱被刷新的次数越多
            caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBILITY_TICKS));
            AddonEffects.add(caster, NIGHT_VEIL_EFFECT, BUFF_TICKS);
            MagicManager.spawnParticles(level, ParticleTypes.EXPLOSION_EMITTER,
                caster.getX(), caster.getY() + 0.25, caster.getZ(), 100, 0.03, 0.4, 0.03, 0.25, false);

            DamageSources.applyDamage(target, damage, damageSource);
            spawnVoidTentacle(level, caster, target, damage);
            spawnPaleFlame(level, caster, target, damage);
        }
    }

    /**
     * OVERWHELMING_LIGHT（圣光压顶）：施法者披上光之壁垒，对所有标记目标结算伤害， 并在每个目标处落下一道阳光柱，再朝其放出一团追踪鬼火。
     */
    public static void overwhelmingLight(RecastContext ctx) {
        if (!ctx.beginMultiTargetFinish()) {
            return;
        }

        LivingEntity caster = ctx.caster();
        ServerLevel level = ctx.level();
        float damage = ctx.spellPower();
        DamageSource damageSource = ctx.spell().getDamageSource(caster);

        ctx.playAtMuzzle(SoundRegistry.CLEANSE_CAST.get(), 2.0f);

        for (LivingEntity target : ctx.markedTargets()) {
            AddonEffects.add(caster, BASTION_OF_LIGHT_EFFECT, BUFF_TICKS);
            DamageSources.applyDamage(target, damage, damageSource);
            spawnSunbeam(level, caster, target, damage);
            spawnWisp(level, caster, target, damage);
        }
    }

    private static SoundEvent paleFlameStartSound() {
        return AddonReflection.holderValue(SOUND_REGISTRY, PALE_FLAME_START_FIELD, SoundEvent.class)
            .orElse(SoundEvents.WITHER_SHOOT);
    }

    private static void spawnPaleFlame(ServerLevel level, LivingEntity caster, LivingEntity target, float damage) {
        // PaleFlame 继承 Iron's 的 AoeEntity，是编译期可见的类型，伤害与归属直接调即可，无需反射。
        // 归属不能省：AoeEntity.canHitEntity 靠 owner 过滤友军，不设会连自家人一起烧
        if (!(AddonReflection.construct(PALE_FLAME, PALE_FLAME_CTOR, level, false)
            .orElse(null) instanceof AoeEntity flame)) {
            return;
        }
        flame.setDamage(damage);
        flame.setOwner(caster);
        flame.moveTo(target.position());
        flame.setYRot(target.getYRot());
        flame.setXRot(target.getXRot());
        level.addFreshEntity(flame);
    }

    private static void spawnVoidTentacle(ServerLevel level, LivingEntity caster, LivingEntity target, float damage) {
        // 虚空触手是 Iron's 本体的实体，编译期可见，无需反射
        VoidTentacle whip = new VoidTentacle(level, caster, damage);
        whip.setPos(target.position());
        whip.setYRot(target.getYRot());
        whip.setDamage(damage * VOID_TENTACLE_DAMAGE_MULTIPLIER);
        level.addFreshEntity(whip);
    }

    private static void spawnSunbeam(ServerLevel level, LivingEntity caster, LivingEntity target, float damage) {
        // 归属不能省：SunbeamEntity 继承 AoeEntity，靠 owner 过滤友军
        SunbeamEntity sunbeam = new SunbeamEntity(level);
        sunbeam.setOwner(caster);
        sunbeam.moveTo(target.position());
        sunbeam.setDamage(damage);
        level.addFreshEntity(sunbeam);
        // 阳光柱有 15 tick 的蓄力，落地先响一声给目标反应时间
        level.playSound(null, sunbeam.blockPosition(), SoundRegistry.SUNBEAM_WINDUP.get(),
            SoundSource.NEUTRAL, 3.5f, 1.0f);
    }

    private static void spawnWisp(ServerLevel level, LivingEntity caster, LivingEntity target, float damage) {
        WispEntity wisp = new WispEntity(level, caster, damage);
        wisp.setTarget(target);
        // 从施法者视线前方一点点出生，稍微下压避免直接卡在脸上
        wisp.setPos(Utils.getPositionFromEntityLookDirection(caster, WISP_SPAWN_DISTANCE).subtract(0.0, 0.2, 0.0));
        level.addFreshEntity(wisp);
    }
}
