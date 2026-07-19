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
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.spells.FoxshadeWispProjectile;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 酒杯狐影 Foxshade Wisps 法术（recast 逐发版）。
 * <p>
 * 玩家/女仆每按一次施法键放出 1 发追踪狐影弹（{@link FoxshadeWispProjectile}），最多 6 发（1 初始 + 5 recast）。
 * 各发侧翼包抄打出酒狐巫法伤害；第 6 发为 finisher，弹体命中时触发残月爆发。
 * <p>
 * 首发扣法力，后续 recast 由 Iron's {@code castSpell} 自动跳过扣费；超时未续发则自动进入冷却。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-21
 */
public class FoxshadeWispsSpell extends AbstractSpell {

    /** 额外重铸次数：1 次初始 + 5 次 recast = 6 发。 */
    private static final int RECAST_COUNT = 5;
    /** recast 窗口（ticks）：超时未续发则进冷却、不触发爆发。 */
    private static final int RECAST_TTL = 80;
    /** 伤害系数：每发伤害 = getSpellPower(level, caster) * 此系数。 */
    private static final float DAMAGE_MULTIPLIER = 3.5f;
    /** 目标搜索半径（格）。 */
    private static final double SEARCH_RADIUS = 16.0;
    /** 女仆 AI 施法距离（格）：取比 {@link #SEARCH_RADIUS} 小的值，保证进入施法点后必能锁到目标；
     * 数据生成引用同一常量。 */
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
        // totalRecasts 传 RECAST_COUNT+1=6，令 RecastInstance 内 remainingRecasts=5(剩余数从 5 递减到 1)。
        if (!alreadyHasRecast) {
            recasts.addRecast(new RecastInstance(this.getSpellId(), spellLevel,
                RECAST_COUNT + 1, RECAST_TTL, castSource, this.getEmptyCastData()),
                playerMagicData);
        }

        float power = this.getSpellPower(spellLevel, entity);
        // 剩余 recast 数：首发视作 6；之后 R∈[5..1]。序号=6-R∈[0..5]，最后一发（R==1）为 finisher
        int remaining = alreadyHasRecast ? recasts.getRemainingRecastsForSpell(this) : RECAST_COUNT + 1;
        int shotIndex = RECAST_COUNT + 1 - remaining;

        // 目标选取：玩家沿视线锁定准星指向的敌人（LOS）；女仆/无锁定时退回半径内最近敌对生物
        LivingEntity homingTarget = acquireTarget(serverLevel, entity);

        Vec3 eye = entity.getEyePosition();
        FoxshadeWispProjectile wisp = new FoxshadeWispProjectile(level, entity);
        wisp.setDamage(power * DAMAGE_MULTIPLIER);
        wisp.moveTo(eye.x, eye.y - 0.1, eye.z, entity.getYRot(), entity.getXRot());
        if (homingTarget != null) {
            wisp.setHomingTarget(homingTarget);
        }
        // 左右交替包抄：偶数发（0,2,4）→ +1（右），奇数发（1,3,5）→ -1（左）
        wisp.setFlankSign((shotIndex % 2 == 0) ? 1 : -1);
        // 第 6 发（最后一发）为 finisher：命中触发残月爆发
        if (remaining == 1) {
            wisp.setFinisher(power * 5.0f);
        }
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
     * 选取追踪目标。
     * <p>
     * 玩家施法：沿准星视线 {@link RaycastBuilder 射线}（含方块遮挡判定、对实体 0.5 包围盒膨胀辅助瞄准、
     * 半径 {@link #SEARCH_RADIUS}）锁定指向的敌对生物——即「视线自动锁定」。
     * 女仆施法或视线未锁中时，退回半径 {@link #SEARCH_RADIUS} 内最近的敌对生物（{@link WinefoxHexHelper#isHexEnemy}）。
     *
     * @param level  服务端世界
     * @param caster 施法者
     * @return 追踪目标；附近无可锁敌人时返回 {@code null}（弹体直线前射）
     * @author Gardel &lt;gardel741@outlook.com&gt;
     * @since 2026-06-22
     */
    @Nullable
    private LivingEntity acquireTarget(ServerLevel level, LivingEntity caster) {
        // 玩家：视线锁定（遇方块阻断；对敌人 0.5 膨胀辅助瞄准）
        if (caster instanceof Player) {
            HitResult hit = RaycastBuilder.begin(level, caster)
                .range((float) SEARCH_RADIUS)
                .checkForBlocks(true)
                .bbInflation(0.5f)
                .filter(e -> e instanceof LivingEntity le && WinefoxHexHelper.isHostileTo(le, caster))
                .build();
            if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity locked) {
                return locked;
            }
        }
        // 退回：半径内最近敌对生物
        return level.getEntitiesOfClass(LivingEntity.class,
                caster.getBoundingBox().inflate(SEARCH_RADIUS),
                e -> WinefoxHexHelper.isHostileTo(e, caster))
            .stream()
            .min(Comparator.comparingDouble(caster::distanceToSqr))
            .orElse(null);
    }
}
