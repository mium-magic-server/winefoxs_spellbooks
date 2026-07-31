package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import io.redspace.ironsspellbooks.capabilities.magic.MultiTargetEntityCastData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import java.util.Comparator;
import java.util.Optional;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.projectile.FoxshadeWispProjectile;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.magicterra.winefoxsspellbooks.registry.WsbSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 酒杯狐影 Foxshade Wisps 法术（recast 逐发版）。
 * <p>
 * 玩家/女仆每按一次施法键放出 1 发追踪狐影弹（{@link FoxshadeWispProjectile}），最多 3 发（1 初始 + 2 recast）。 各发侧翼包抄打出酒狐巫法伤害，<b>三发完全等价</b>——命中即引爆目标身上的残月烙印。
 * <p>
 * 首发扣法力，后续 recast 由 Iron's {@code castSpell} 自动跳过扣费；超时未续发则自动进入冷却。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-21
 */
public class FoxshadeWispsSpell extends AbstractSpell {

    /**
     * 额外重铸次数：1 次初始 + 2 次 recast = 3 发。
     */
    private static final int RECAST_COUNT = 2;

    /**
     * recast 窗口（ticks）：超时未续发则进冷却。
     */
    private static final int RECAST_TTL = 80;

    /**
     * 伤害系数：每发伤害 = getSpellPower(level, caster) * 此系数。
     */
    private static final float DAMAGE_MULTIPLIER = 2.0f;

    /**
     * 目标搜索半径（格）。
     */
    private static final double SEARCH_RADIUS = 16.0;

    /**
     * 女仆 AI 施法距离（格）：取比 {@link #SEARCH_RADIUS} 小的值，保证进入施法点后必能锁到目标； 数据生成引用同一常量。
     */
    public static final float MAID_CAST_RANGE = (float) SEARCH_RADIUS - 4.0f;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "foxshade_wisps");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.UNCOMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
        .setMaxLevel(8)
        .setCooldownSeconds(15.0)
        .build();

    public FoxshadeWispsSpell() {
        this.baseManaCost = 45;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
     * 发射音：0.4-0.5 s 短「咻」，三变体随机 + 引擎 ±10% 音高抖动。
     * <p>
     * 本法术逐发 recast，此钩子每发触发一次，所以素材必须短——长了连发会糊成一片。
     */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(WsbSounds.FOXSHADE_WISPS_FIRE.get());
    }

    @Override
    public int getMinLevel() {
        // 本法术从 3 级起步，1-2 级不存在（DefaultConfig 无 setMinLevel，须覆写）
        return 3;
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return RECAST_COUNT;
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        // 各发自行就近选目标，无需跨发持久数据；用空 MultiTargetEntityCastData 作载体，
        // 避开 MaidRecasts.tick() 对 SummonedEntitiesCastData（召唤物数=0 → 立即回收）的特判。
        return new MultiTargetEntityCastData();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        PlayerRecasts recasts = playerMagicData.getPlayerRecasts();
        boolean alreadyHasRecast = recasts.hasRecastForSpell(this);

        // 首发：注册 recast 实例（之后每按一次施法键由 castSpell 自动 decrement）。
        // totalRecasts 传 RECAST_COUNT+1=3，令 RecastInstance 内 remainingRecasts=2(剩余数从 2 递减到 1)。
        if (!alreadyHasRecast) {
            recasts.addRecast(new RecastInstance(this.getSpellId(), spellLevel,
                    RECAST_COUNT + 1, RECAST_TTL, castSource, this.getEmptyCastData()),
                playerMagicData);
        }

        float power = this.getSpellPower(spellLevel, entity);
        // 剩余 recast 数：首发视作 3；之后 R∈[2..1]。序号=3-R∈[0..2]，只用来定左右交替方向
        int remaining = alreadyHasRecast ? recasts.getRemainingRecastsForSpell(this) : RECAST_COUNT + 1;
        int shotIndex = RECAST_COUNT + 1 - remaining;

        LivingEntity homingTarget = acquireTarget(serverLevel, entity);

        Vec3 eye = entity.getEyePosition();
        FoxshadeWispProjectile wisp = new FoxshadeWispProjectile(level, entity);
        wisp.setDamage(power * DAMAGE_MULTIPLIER);
        wisp.moveTo(eye.x, eye.y - 0.1, eye.z, entity.getYRot(), entity.getXRot());
        if (homingTarget != null) {
            wisp.setHomingTarget(homingTarget);
        }
        // 左右交替包抄：偶数发（0,2）→ +1（右），奇数发（1）→ -1（左）
        wisp.setFlankSign((shotIndex % 2 == 0) ? 1 : -1);
        // 出膛即带侧翼初速：有目标时沿侧翼期望朝向射出，弧线出膛即张开；无目标退回直线前射
        if (homingTarget == null || !wisp.shootFlanking(homingTarget)) {
            wisp.shoot(entity.getLookAngle());
        }
        serverLevel.addFreshEntity(wisp);

        // 施法主粒子:每发一小批,连发时形成持续环绕
        WinefoxHexHelper.spawnCastParticles(serverLevel, entity);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData); // 播放完成音效
    }

    /**
     * 选取追踪目标：玩家沿准星视线锁定；女仆认 AI 已指派的攻击目标，其次退回可见近邻表（挂印优先）。
     *
     * @return 无可锁目标时 {@code null}（弹体直线前射）
     */
    @Nullable
    private LivingEntity acquireTarget(ServerLevel level, LivingEntity caster) {
        if (caster instanceof Player) {
            HitResult hit = RaycastBuilder.begin(level, caster)
                .range((float) SEARCH_RADIUS)
                .checkForBlocks(true)
                .bbInflation(0.5f)
                .filter(WinefoxHexHelper.raycastFilter(target -> WinefoxHexHelper.isHostileTo(target, caster)))
                .build();
            if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity locked) {
                return locked;
            }
        } else if (caster instanceof Mob mob) {
            // 女仆已被 AI 指向某个目标（SpellCastingTask 也是照它转身的），弹体必须跟着打同一个，
            // 否则会绕开当前交战对象去点更近的无关怪。
            // hasMemoryValue 是必要的前置：记忆模块没注册时 getMemory 直接抛 ISE
            Brain<?> brain = mob.getBrain();
            if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                // 交战对象由 AI 选定，这里不再套一遍敌我判定（否则被命令打玩家时反而不追踪）
                LivingEntity assigned = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
                if (assigned != caster && assigned.isAlive()) {
                    return assigned;
                }
            }
            // 没有指派目标时退回可见近邻表：该表自带视线判定，不会挑中墙后的目标
            if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) {
                return WinefoxHexHelper.findVisibleTargetPreferBranded(mob,
                        e -> WinefoxHexHelper.isHostileTo(e, caster))
                    .orElse(null);
            }
        }
        // 无脑（brain 未注册相关记忆）的施法者兜底：半径内自行扫一遍，视线判定要自己补。
        // 排序在前、视线在后：hasLineOfSight 每次都是一趟方块射线，先排好序就只用验到第一个通过的，
        // 不必给整片候选都投一次射线。比较器写成「未挂印(true) 排在挂印(false) 之后」再比距离。
        return level.getEntitiesOfClass(LivingEntity.class,
                caster.getBoundingBox().inflate(SEARCH_RADIUS),
                e -> WinefoxHexHelper.isHostileTo(e, caster))
            .stream()
            .sorted(Comparator.comparing((LivingEntity e) -> !WinefoxHexHelper.isBranded(e))
                .thenComparingDouble(caster::distanceToSqr))
            .filter(caster::hasLineOfSight)
            .findFirst()
            .orElse(null);
    }
}
