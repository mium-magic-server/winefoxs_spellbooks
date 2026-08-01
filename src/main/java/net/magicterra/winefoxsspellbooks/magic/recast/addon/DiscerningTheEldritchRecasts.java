package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.magic.recast.AddonReflection;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastContext;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Discerning the Eldritch 的 recast 收尾。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-08-01
 */
public final class DiscerningTheEldritchRecasts {
    private DiscerningTheEldritchRecasts() {
    }

    public static final String MOD_ID = "discerning_the_eldritch";

    private static final String CRYSTAL_CARVE =
        "net.acetheeldritchking.discerning_the_eldritch.entity.spells.crystal_carve.CrystalCarveEntity";

    /**
     * CrystalCarveEntity 只有 {@code (EntityType, Level)} 与 {@code (Level, boolean)} 两个构造器， 后者的 boolean 是「镜像」——左右手挥砍方向，与归属无关。
     */
    private static final Class<?>[] CRYSTAL_CARVE_CTOR = {Level.class, boolean.class};

    /**
     * {@code setIsFinal(boolean)}：登记自检与实际调用共用同一份，改名时不会只改到一边。
     */
    private static final AddonProjectiles.Setter SET_IS_FINAL =
        new AddonProjectiles.Setter("setIsFinal", boolean.class);

    /**
     * 冰霜武器标签：主手武器在此标签内时收尾伤害额外放大。 标签只认 id，用 {@link TagKey} 直接构造即可，不必碰附属的类。
     */
    private static final TagKey<Item> FROZEN_WEAPONS = TagKey.create(Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "frozen_weapons"));

    /**
     * 冻伤层数的数据附件 id。附件本身在 NeoForge 注册表里，按 id 查得到就用，查不到（模组没装）按 0 层算。
     */
    private static final ResourceLocation FROSTBITE_LEVEL =
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "frostbite_level");

    /**
     * 收尾横扫的判定半径。
     */
    private static final float SWEEP_RADIUS = 2.15f;

    /**
     * 判定盒中心相对施法者前推的距离。
     */
    private static final float SWEEP_DISTANCE = 1.2f;

    /**
     * 主手持冰霜武器时的伤害倍率。
     */
    private static final float FROZEN_WEAPON_MULTIPLIER = 1.5f;

    /**
     * 命中时的雪花粒子数量。
     */
    private static final int SNOWFLAKE_COUNT = 50;

    /**
     * 登记本模组的收尾动作与反射目标。
     */
    public static void contribute(RecastRegistrar registrar) {
        AddonReflection.declareClass(MOD_ID, CRYSTAL_CARVE);
        AddonReflection.declareMethod(MOD_ID, CRYSTAL_CARVE, "<init>", CRYSTAL_CARVE_CTOR);
        AddonProjectiles.declare(MOD_ID, CRYSTAL_CARVE, SET_IS_FINAL);

        registrar.register("crystalline_carver", DiscerningTheEldritchRecasts::crystallineCarver);
    }

    /**
     * CRYSTALLINE_CARVER（晶棱裂割）：最后一刀横扫身前，对视线内的敌人结算伤害。
     * <p>
     * 伤害 = （法术强度的一半 + 武器伤害 + 目标的冻伤层数）×（主手冰霜武器则再乘 1.5）， 冻伤层数只在目标身上挂着 Iron's 的
     * {@code CHILLED} 时才计入。
     */
    public static void crystallineCarver(RecastContext ctx) {
        ctx.addCooldown();
        if (!ctx.result().isSuccess()) {
            return;
        }

        LivingEntity caster = ctx.caster();
        ServerLevel level = ctx.level();
        Vec3 hitLocation = caster.position()
            .add(0.0, caster.getBbHeight() * 0.3f, 0.0)
            .add(caster.getForward().multiply(SWEEP_DISTANCE, 0.35f, SWEEP_DISTANCE));

        // 附属自己的 getDamage：法术强度打对折再加上武器伤害
        float baseDamage = (ctx.spellPower() * 0.5f) + Utils.getWeaponDamage(caster);
        boolean frozenWeapon = caster.getMainHandItem().is(FROZEN_WEAPONS);
        DamageSource damageSource = ctx.spell().getDamageSource(caster);
        AABB sweepBox = AABB.ofSize(hitLocation, SWEEP_RADIUS * 2.0f, SWEEP_RADIUS, SWEEP_RADIUS * 2.0f);
        AttachmentType<?> frostbiteCounter = NeoForgeRegistries.ATTACHMENT_TYPES.get(FROSTBITE_LEVEL);

        // AoE 波及走【阵营敌我】的统一谓词，勿手写
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, sweepBox,
            e -> WinefoxHexHelper.isFactionHostile(e, caster))) {
            // 冻伤层数在收尾时一次性吃掉：无论这一刀最终打没打中，进了判定盒就清零
            int frostbite = takeFrostbiteLevel(frostbiteCounter, target);
            if (!isInSweep(level, caster, target)) {
                continue;
            }

            float damage = baseDamage + (target.hasEffect(MobEffectRegistry.CHILLED) ? frostbite : 0);
            if (frozenWeapon) {
                damage *= FROZEN_WEAPON_MULTIPLIER;
            }
            if (DamageSources.applyDamage(target, damage, damageSource)) {
                spawnSnowflakes(level, target);
                EnchantmentHelper.doPostAttackEffects(level, target, damageSource);
            }
        }

        spawnFinalSwipe(level, caster, hitLocation);
    }

    /**
     * 判定盒是方的、实际判定是球形，还要求施法者到目标之间没有遮挡。
     */
    private static boolean isInSweep(ServerLevel level, LivingEntity caster, LivingEntity target) {
        return caster.distanceToSqr(target) < (SWEEP_RADIUS * SWEEP_RADIUS)
            && Utils.hasLineOfSight(level, caster.getEyePosition(), target.getBoundingBox().getCenter(), true);
    }

    /**
     * 读走目标的冻伤层数并清零。附件查不到、或值不是整数时按 0 层处理，收尾照常结算基础伤害。
     *
     * @param type {@link NeoForgeRegistries#ATTACHMENT_TYPES} 里查出的附件类型，模组没装时为 {@code null}；
     *             整轮横扫查一次传进来即可，不必每个目标各查一遍
     */
    private static int takeFrostbiteLevel(AttachmentType<?> type, LivingEntity target) {
        // 先问 hasData：直接 getData 会顺手把附件按默认值建出来，白白给每个路人挂一份
        if (type == null || !target.hasData(type)) {
            return 0;
        }
        // 用通配符接住再 instanceof：写死 AttachmentType<Integer> 的话，附属改了值类型
        // 会在 getData 的隐式转型上抛 CCE，轮不到下面这行兜底
        if (!(target.getData(type) instanceof Integer level)) {
            return 0;
        }
        // 上一行已确认存的是整数，这里的转型是安全的
        @SuppressWarnings("unchecked")
        AttachmentType<Integer> counter = (AttachmentType<Integer>) type;
        target.setData(counter, 0);
        return level;
    }

    private static void spawnSnowflakes(ServerLevel level, LivingEntity target) {
        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE,
            target.getX(), target.getY() + (target.getBbHeight() * 0.5f), target.getZ(),
            SNOWFLAKE_COUNT,
            target.getBbWidth() * 0.5f, target.getBbHeight() * 0.5f, target.getBbWidth() * 0.5f,
            0.03, false);
    }

    /**
     * 生成收尾那道剑气。它继承 Iron's 的 AoeEntity，但 {@code applyEffect} 是空实现—— 纯表现层，不必设归属与伤害，拿不到类也只是少一段特效。
     */
    private static void spawnFinalSwipe(ServerLevel level, LivingEntity caster, Vec3 hitLocation) {
        if (!(AddonReflection.construct(CRYSTAL_CARVE, CRYSTAL_CARVE_CTOR, level, false)
            .orElse(null) instanceof AoeEntity swipe)) {
            return;
        }
        swipe.moveTo(hitLocation);
        swipe.setYRot(caster.getYRot());
        // 不设 final 标记的话播的是普通挥砍，收尾看不出区别
        AddonProjectiles.invoke(swipe, SET_IS_FINAL, true);
        level.addFreshEntity(swipe);
    }
}
