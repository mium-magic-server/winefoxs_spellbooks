package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.damage.WsbDamageTypes;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * Winefox Hex 学派胶水：常量 + 跨系统判定的单一事实源。
 * 工具类（私有构造 + final），以静态方法/常量扩展，不通过继承。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
public final class WinefoxHexHelper {
    private WinefoxHexHelper() {}

    public static final int BREWSURGE_DURATION = 100;    // 5s 滚动刷新
    public static final int BREWSURGE_MAX_STACKS = 3;    // amplifier 上限 = 2

    public static final int BRAND_DURATION = 240;         // 12s（240 ticks）残月烙印持续时间

    // -----------------------------------------------------------------------
    // 微醺 Tipsy 共享常量：效果侧踉跄抖动与 TipsyAimHandler 弹道打偏须同量级，
    // 雾团/酒池的重涂时长也须一致，故集中于此单一事实源
    // -----------------------------------------------------------------------

    /** 每 tick yaw 抖动/弹道偏转标准差(度);高斯 → 典型 ±30°。 */
    public static final double TIPSY_YAW_DEG = 30.0;
    /** pitch 抖动/偏转相对 yaw 的衰减比例(约 30%)。 */
    public static final double TIPSY_PITCH_RATIO = 0.3;
    /** AoE 雾团/酒池每次命中刷新的微醺时长(3s);离开区域后效果自然到期,不会无限续杯。 */
    public static final int TIPSY_REAPPLY_TICKS = 60;

    public static final ResourceLocation WINEFOX_HEX_ID =
            ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "winefox_hex");

    /** 该 SchoolType 是否本学派（按注册 id，schools 为单例）。 */
    public static boolean isWinefoxHexSchool(SchoolType school) {
        return school != null && WINEFOX_HEX_ID.equals(school.getId());
    }

    // -----------------------------------------------------------------------
    // 视觉 · 施法主粒子：紫粉酒滴（向下凋谢）+ 暖橘狐火（向上飘）+ 偶发金色符箓
    // -----------------------------------------------------------------------

    /** 学派主色 #C846FF（紫粉）。学派文本、粒子、弹体拖尾、药水染色共用此常量。 */
    public static final int SCHOOL_COLOR = 0xC846FF;
    /** 学派主色的归一化 RGB（粒子用）。 */
    public static final Vector3f SCHOOL_COLOR_VEC = new Vector3f(
        (SCHOOL_COLOR >> 16 & 0xFF) / 255f, (SCHOOL_COLOR >> 8 & 0xFF) / 255f, (SCHOOL_COLOR & 0xFF) / 255f);

    /** 蜜金符箓尘色 #F5C77A（符箓金线），归一化 RGB。 */
    private static final Vector3f RUNE_GOLD_COLOR = new Vector3f(0xF5 / 255f, 0xC7 / 255f, 0x7A / 255f);
    private static final DustParticleOptions WINE_DROPLET = new DustParticleOptions(SCHOOL_COLOR_VEC, 1.3f);
    private static final DustParticleOptions RUNE_GOLD = new DustParticleOptions(RUNE_GOLD_COLOR, 0.8f);
    /** 学派色大号尘粒：醉狐之雾/酒池共用的雾团粒子。 */
    public static final DustParticleOptions SCHOOL_MIST = new DustParticleOptions(SCHOOL_COLOR_VEC, 2.0f);

    /**
     * 在施法者周身发射一批本学派的施法环绕粒子。可一次性爆发（瞬发法术），
     * 也可按 tick 反复调用（持续法术）。仅服务端调用——通过 {@link ServerLevel#sendParticles}
     * 广播给附近所有玩家。
     * <p>
     * 三类粒子（count=0 → 速度 = (dx,dy,dz)，单粒子方向运动）：
     * <ul>
     *   <li>紫粉酒滴：环绕上半身，向下凋谢；</li>
     *   <li>暖橘狐火（vanilla FLAME）：环绕脚边，向上飘；</li>
     *   <li>偶发蜜金符箓尘：腰际高度，缓慢上浮。</li>
     * </ul>
     *
     * @param level  施法者所在服务端世界
     * @param caster 施法者
     */
    public static void spawnCastParticles(ServerLevel level, LivingEntity caster) {
        RandomSource rng = caster.getRandom();
        double cx = caster.getX();
        double cy = caster.getY();
        double cz = caster.getZ();
        double h = caster.getBbHeight();
        double r = caster.getBbWidth() * 0.6;

        // 紫粉酒滴：上半身环绕，向下凋谢
        for (int i = 0; i < 3; i++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double rad = r * (0.6 + rng.nextDouble() * 0.5);
            double px = cx + Math.cos(angle) * rad;
            double pz = cz + Math.sin(angle) * rad;
            double py = cy + h * (0.6 + rng.nextDouble() * 0.4);
            level.sendParticles(WINE_DROPLET, px, py, pz, 0,
                (rng.nextDouble() - 0.5) * 0.01, -0.04 - rng.nextDouble() * 0.04, (rng.nextDouble() - 0.5) * 0.01, 1.0);
        }

        // 暖橘狐火：脚边环绕，向上飘
        for (int i = 0; i < 2; i++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double rad = r * (0.4 + rng.nextDouble() * 0.5);
            double px = cx + Math.cos(angle) * rad;
            double pz = cz + Math.sin(angle) * rad;
            double py = cy + h * (0.1 + rng.nextDouble() * 0.3);
            level.sendParticles(ParticleTypes.FLAME, px, py, pz, 0,
                (rng.nextDouble() - 0.5) * 0.01, 0.03 + rng.nextDouble() * 0.03, (rng.nextDouble() - 0.5) * 0.01, 1.0);
        }

        // 偶发金色符箓尘
        if (rng.nextFloat() < 0.25f) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double rad = r * 0.9;
            double px = cx + Math.cos(angle) * rad;
            double pz = cz + Math.sin(angle) * rad;
            double py = cy + h * (0.4 + rng.nextDouble() * 0.4);
            level.sendParticles(RUNE_GOLD, px, py, pz, 0, 0.0, 0.02, 0.0, 1.0);
        }
    }

    /**
     * 瞬发法术的一次性施法粒子爆发：连发三批 {@link #spawnCastParticles}。
     * 持续法术仍应按 tick 调用 {@link #spawnCastParticles}。
     */
    public static void spawnCastBurst(ServerLevel level, LivingEntity caster) {
        for (int i = 0; i < 3; i++) {
            spawnCastParticles(level, caster);
        }
    }

    // -----------------------------------------------------------------------
    // 残月烙印引爆系统常量
    // -----------------------------------------------------------------------

    public static final float DETONATE_BASE = 6.0f;
    /** 女仆引爆伤害倍率 */
    public static final float MAID_MULT = 1.5f;
    /** 女仆引爆半径（普通） */
    public static final int MAID_RADIUS = 4;
    /** 女仆引爆半径（≥3 层 Brewsurge 时） */
    public static final int MAID_RADIUS_SURGE = 5;
    /** 玩家分支引爆半径 */
    public static final int PLAYER_RADIUS = 3;
    /** 烙印自然过期弱自爆半径 */
    public static final float WEAK_SELF_RADIUS = 2.0f;
    /** 烙印自然过期弱自爆伤害 */
    public static final float WEAK_SELF_DAMAGE = 3.0f;

    /**
     * 判断伤害源是否为酒狐巫法类型。
     * 检测键为伤害类型 ResourceKey（非 instanceof SpellDamageSource），
     * 这样 /damage … winefox_hex_magic 命令注入也能命中。
     */
    public static boolean isWinefoxHexDamage(DamageSource source) {
        return source != null && source.is(WsbDamageTypes.WINEFOX_HEX_MAGIC);
    }

    /**
     * 敌我判定单一事实源:是否为「敌对」实体。复用代码既有约定 {@code instanceof Enemy}
     * (与 Foxshade Wisps 目标筛选、SummonedMaidBrain.isEnemyTarget 一致)。
     * 非 Enemy(女仆/玩家/中立动物)视为友方侧。供 Tipsy 等区分敌我表现的效果复用。
     */
    public static boolean isHexEnemy(LivingEntity entity) {
        return entity instanceof Enemy;
    }

    /**
     * 「存活的敌对目标且不是自己」——目标选取/AoE 过滤的统一谓词
     * （狐影弹选目标、finisher 爆发等共用，避免各处手写三连判定）。
     */
    public static boolean isHostileTo(LivingEntity candidate, @Nullable Entity self) {
        return candidate != self && candidate.isAlive() && isHexEnemy(candidate);
    }

    /**
     * 弹体伤害归因实体：优先 owner（施法者，引爆系统据此区分女仆/玩家分支），
     * owner 缺失（已卸载/下线）时退回弹体自身，保证伤害类型仍为学派伤害。
     */
    public static Entity projectileAttribution(Projectile projectile) {
        Entity owner = projectile.getOwner();
        return owner != null ? owner : projectile;
    }

    /**
     * 以学派伤害源打出一次 AoE 爆发：{@code setIFrames(0)} 绕过受击无敌帧，
     * 保证同一次爆发能命中多个目标、且不被同 tick 直击刚触发的无敌帧吞掉。
     * 过滤器由调用方提供（存活/敌我/归因排除等判定各站点不同）。
     */
    public static void applyAoeBurst(Level level, AABB box, float damage,
                                     SpellDamageSource source, Predicate<LivingEntity> filter) {
        source.setIFrames(0);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, filter)) {
            DamageSources.applyDamage(e, damage, source);
        }
    }

    /**
     * 攻击者是否为「有主人的女仆」。涵盖两类女仆：
     * <ul>
     *   <li>真·TLM 雇佣女仆 {@link EntityMaid}（{@code TamableAnimal} 主人，{@code getOwner() != null}）；</li>
     *   <li>法术召唤的战斗女仆 {@link net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid}
     *       （{@link IMaid} + Iron's {@link IMagicSummon}，{@code getSummoner() != null}）。</li>
     * </ul>
     * 两类均归玩家所有，故引爆时都应走女仆分支（×1.5 / AoE+1 / 连锁）。
     * 只判定「有没有主人」，不判定主人是谁；需要区分归属时用 {@link #isMaidOwnedBy}。
     */
    public static boolean isOwnedMaid(Entity attacker) {
        return maidOwner(attacker) != null;
    }

    /**
     * 取女仆的主人：雇佣女仆（{@link EntityMaid}）取 {@code getOwner()}，
     * 召唤女仆（{@link IMaid} + {@link IMagicSummon}）取 {@code getSummoner()}；
     * 非女仆或无主时返回 {@code null}。两类女仆的判定统一收口于此。
     */
    @Nullable
    private static Entity maidOwner(Entity maid) {
        if (maid instanceof EntityMaid m) {
            return m.getOwner();
        }
        if (maid instanceof IMaid && maid instanceof IMagicSummon summon) {
            return summon.getSummoner();
        }
        return null;
    }

    /**
     * 目标是否为「owner 阵营的自家女仆」：目标是有主人的女仆，且与 owner 共享同一召唤链顶层
     * （{@link MaidAllyHelper#isAllied}，兼容玩家直接施法与女仆代姐妹施法两种情形）。
     * 增益类效果（披风、酒池回魔、豁免微醺等）须用本方法而非 {@link #isOwnedMaid}，
     * 否则会把其他玩家的女仆也当成自家人。
     */
    public static boolean isMaidOwnedBy(Entity target, @Nullable Entity owner) {
        return owner != null && isOwnedMaid(target) && MaidAllyHelper.isAllied(target, owner);
    }

    /**
     * 取女仆「指挥官」（雇佣女仆的主人 / 召唤女仆的召唤者）若为玩家则返回之，否则 null。
     */
    private static Player maidCommander(Entity maid) {
        return maidOwner(maid) instanceof Player p ? p : null;
    }

    /**
     * 取女仆指挥官的 Brewsurge 层数（1-based；无指挥官或无效果时返回 0）。
     */
    public static int commanderBrewsurgeStacks(Entity maid) {
        Player commander = maidCommander(maid);
        if (commander == null) {
            return 0;
        }
        var e = commander.getEffect(WsbEffects.BREWSURGE);
        return e == null ? 0 : e.getAmplifier() + 1;
    }
}
