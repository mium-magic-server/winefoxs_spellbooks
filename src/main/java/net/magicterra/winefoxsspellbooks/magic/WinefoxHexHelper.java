package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.damage.WsbDamageTypes;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * Winefox Hex 学派胶水：常量 + 跨系统判定的单一事实源。 工具类（私有构造 + final），以静态方法/常量扩展，不通过继承。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
public final class WinefoxHexHelper {
    private WinefoxHexHelper() {
    }

    public static final int BREWSURGE_DURATION = 200;    // 10s 滚动刷新（引爆计层触发较稀有，窗口放宽）

    public static final int BREWSURGE_MAX_STACKS = 3;    // amplifier 上限 = 2

    public static final int BRAND_DURATION = 240;         // 12s（240 ticks）残月烙印持续时间

    // -----------------------------------------------------------------------
    // 微醺 Tipsy 共享常量：效果侧踉跄抖动与 TipsyAimHandler 弹道打偏须同量级，
    // 雾团/酒池的重涂时长也须一致，故集中于此单一事实源
    // -----------------------------------------------------------------------

    /**
     * 每 tick yaw 抖动/弹道偏转标准差(度);高斯 → 典型 ±30°。
     */
    public static final double TIPSY_YAW_DEG = 30.0;

    /**
     * pitch 抖动/偏转相对 yaw 的衰减比例(约 30%)。
     */
    public static final double TIPSY_PITCH_RATIO = 0.3;

    /**
     * AoE 雾团/酒池每次命中刷新的微醺时长(3s);离开区域后效果自然到期,不会无限续杯。
     */
    public static final int TIPSY_REAPPLY_TICKS = 60;

    public static final ResourceLocation WINEFOX_HEX_ID =
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "winefox_hex");

    /**
     * 该 SchoolType 是否本学派（按注册 id，schools 为单例）。
     */
    public static boolean isWinefoxHexSchool(SchoolType school) {
        return school != null && WINEFOX_HEX_ID.equals(school.getId());
    }

    /**
     * 学派主色 #FF5FA0（粉）。学派文本、粒子、弹体拖尾、药水染色共用此常量。 色相约 336°，刻意避开紫向；任何学派配色都从这里取，不要另写字面量。
     */
    public static final int SCHOOL_COLOR = 0xFF5FA0;

    /**
     * 学派主色的归一化 RGB（{@link DustParticleOptions} 用，该粒子不吃 alpha）。
     */
    public static final Vector3f SCHOOL_COLOR_VEC = new Vector3f(
        (SCHOOL_COLOR >> 16 & 0xFF) / 255f, (SCHOOL_COLOR >> 8 & 0xFF) / 255f, (SCHOOL_COLOR & 0xFF) / 255f);

    /**
     * 造一枚指定颜色的 {@code ENTITY_EFFECT} 粒子选项，alpha 自动补满。
     * <p>
     * <b>必须走这里，不要直接 {@code ColorParticleOption.create(ENTITY_EFFECT, rgb)}</b>：该重载吃的是
     * <b>ARGB</b>（{@code getAlpha()} 取的是高 8 位），而 {@code SpellParticle.MobEffectProvider} 会把它
     * 灌进 {@code setAlpha()} 并锁成 {@code originalAlpha}——传六位 RGB 得到 alpha=0，粒子全程完全透明。 本模组的颜色常量一律是六位 RGB（药水染色、文本 {@code Style#withColor} 都要求不带 alpha）， 故统一在此补 {@code 0xFF} 高位。
     *
     * @param rgb 六位 RGB（0xRRGGBB）
     */
    public static ColorParticleOption effectParticle(int rgb) {
        return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000 | rgb);
    }

    /**
     * 蜜金符箓尘色 #F5C77A（符箓金线），归一化 RGB。
     */
    private static final Vector3f RUNE_GOLD_COLOR = new Vector3f(0xF5 / 255f, 0xC7 / 255f, 0x7A / 255f);

    private static final DustParticleOptions RUNE_GOLD = new DustParticleOptions(RUNE_GOLD_COLOR, 0.8f);

    /**
     * 在施法者周身发射一批本学派的施法环绕粒子。可一次性爆发（瞬发法术）， 也可按 tick 反复调用（持续法术）。仅服务端调用——通过 {@link ServerLevel#sendParticles} 广播给附近所有玩家。
     * <p>
     * 两类粒子（count=0 → 速度 = (dx,dy,dz)，单粒子方向运动）：
     * <ul>
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

        for (int i = 0; i < 2; i++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double rad = r * (0.4 + rng.nextDouble() * 0.5);
            double px = cx + Math.cos(angle) * rad;
            double pz = cz + Math.sin(angle) * rad;
            double py = cy + h * (0.1 + rng.nextDouble() * 0.3);
            level.sendParticles(ParticleTypes.FLAME, px, py, pz, 0,
                (rng.nextDouble() - 0.5) * 0.01, 0.03 + rng.nextDouble() * 0.03, (rng.nextDouble() - 0.5) * 0.01, 1.0);
        }

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
     * 瞬发法术的一次性施法粒子爆发：连发三批 {@link #spawnCastParticles}。 持续法术仍应按 tick 调用 {@link #spawnCastParticles}。
     */
    public static void spawnCastBurst(ServerLevel level, LivingEntity caster) {
        for (int i = 0; i < 3; i++) {
            spawnCastParticles(level, caster);
        }
    }

    // -----------------------------------------------------------------------
    // 残月烙印引爆系统常量
    // -----------------------------------------------------------------------

    /**
     * 引爆基础伤害；实际伤害 = 本值 + 挂印时的 bouquet，再乘女仆/玩家分支倍率。
     */
    public static final float DETONATE_BASE = 10.0f;

    /**
     * 女仆引爆伤害倍率
     */
    public static final float MAID_MULT = 1.5f;

    /**
     * 女仆引爆半径（普通）
     */
    public static final int MAID_RADIUS = 4;

    /**
     * 女仆引爆半径（≥3 层 Brewsurge 时）
     */
    public static final int MAID_RADIUS_SURGE = 5;

    /**
     * 玩家分支引爆半径
     */
    public static final int PLAYER_RADIUS = 3;

    /**
     * 烙印自然过期弱自爆半径
     */
    public static final float WEAK_SELF_RADIUS = 2.0f;

    /**
     * 烙印自然过期弱自爆伤害
     */
    public static final float WEAK_SELF_DAMAGE = 3.0f;

    /**
     * 判断伤害源是否为酒狐巫法类型。 检测键为伤害类型 ResourceKey（非 instanceof SpellDamageSource）， 这样 /damage … winefox_hex_magic 命令注入也能命中。
     */
    public static boolean isWinefoxHexDamage(DamageSource source) {
        return source != null && source.is(WsbDamageTypes.WINEFOX_HEX_MAGIC);
    }

    // -----------------------------------------------------------------------
    // 敌我判定三档，语义不同不可互换：
    //   学派敌我 isHostileTo    —— instanceof Enemy，用于「自动挑目标」（女仆索敌等）
    //   指向目标 isAimTarget    —— 非自己人即可，用于「已由施法者指定了目标」（准星锁定、出手命中）
    //   阵营敌我 isFactionHostile —— 非自己人即可，用于 AoE 无差别波及
    // isAimTarget 与 isFactionHostile 判据同源，差别是前者额外排除打不疼的目标（无敌标记、创造玩家）。
    // -----------------------------------------------------------------------

    /**
     * 【学派敌我】是否为「敌对」实体，判据 {@code instanceof Enemy}。
     */
    public static boolean isHexEnemy(LivingEntity entity) {
        return entity instanceof Enemy;
    }

    /**
     * 【学派敌我】「存活的敌对目标且不是自己」——<b>自动目标选取</b>的统一谓词 （女仆无指派时的索敌兜底等共用，避免各处手写三连判定）。
     * <p>
     * 只用在「由代码替施法者挑目标」的场合：判据收紧到 {@code Enemy}，女仆才不会自作主张 去点村民的猪和铁傀儡。施法者已经自己指定了目标（准星、AI 指派、出手命中）时请用 {@link #isAimTarget}，AoE 无差别波及请用 {@link #isFactionHostile}。
     */
    public static boolean isHostileTo(LivingEntity candidate, @Nullable Entity self) {
        return candidate != self && candidate.isAlive() && isHexEnemy(candidate) && !isOwnMount(candidate, self);
    }

    /**
     * 候选者是否为施法者（直接或间接）骑着的坐骑——召唤女仆的扫帚、女仆或玩家骑的马等。
     * <p>
     * 骑乘施法时坐骑既不是敌人也不是要照顾的队友：扫帚与女仆同阵营，不排除的话会被当作队友挂增益； 玩家骑的马与玩家不同阵营，不排除的话会被自己的 AoE 波及。 只排除坐骑本身，同乘一辆载具的其他乘客照常处理。
     *
     * @param candidate 候选实体
     * @param self      施法者，可为 {@code null}
     */
    public static boolean isOwnMount(Entity candidate, @Nullable Entity self) {
        return self != null && candidate != self && candidate.hasIndirectPassenger(self);
    }

    /**
     * 【指向目标】「存活、打得疼、不是自己人」——<b>施法者已自行指定目标</b>时的统一谓词。
     * <p>
     * 与 {@link #isHostileTo} 的区别在于不看 {@code Enemy}：玩家把准星套在猪或铁傀儡上、 或直接一刀砍上去，就是明确要打它，这时再按「是不是敌对生物」拦一道只会让法术失效。 自家女仆、召唤物与队友仍由阵营判定挡住，不会被误锁误咬。
     *
     * @param candidate 施法者指向的目标
     * @param self      施法者，可为 {@code null}（无归因时视作谁都不是自己人）
     */
    public static boolean isAimTarget(LivingEntity candidate, @Nullable Entity self) {
        return isFactionHostile(candidate, self) && !isDamageImmune(candidate);
    }

    /**
     * 是否根本挨不了打——挡在身前也不该吸走准星锁定，否则烙印、狐火弹全打在空处。
     * <p>
     * 两类要各查一遍：带 {@code Invulnerable} 标记的实体走 {@link Entity#isInvulnerable}； 创造模式玩家的免伤原版压根不在那里，而是 {@code Player#hurt} 里现查 {@code abilities.invulnerable}， 只问前者会漏。 {@code Entity#isAttackable} 帮不上忙：原版只有经验球、掉落物这些非 {@link LivingEntity} 覆写它，恒返回 {@code true}。
     */
    private static boolean isDamageImmune(LivingEntity candidate) {
        return candidate.isInvulnerable()
            || (candidate instanceof Player player && player.getAbilities().invulnerable);
    }

    /**
     * 【阵营敌我】「存活、不是施法者本人或其坐骑、且不与施法者同阵营」——<b>AoE 波及</b>的统一谓词。
     * <p>
     * {@code attacker} 允许为 {@code null}（环境学派伤害 / {@code /damage} 无 {@code by} 注入的测试）。此时必须短路，不能落到 {@code isAlliedTo(null)}—— {@code Entity#isAlliedTo} 会对参数调 {@code getTeam()}，传 null 直接 NPE 崩 tick。 无归因时视作「谁都不是自己人」，全部波及。
     *
     * @param candidate AoE 范围内的候选受害者
     * @param attacker  伤害归因实体，可为 {@code null}
     */
    public static boolean isFactionHostile(LivingEntity candidate, @Nullable Entity attacker) {
        return candidate != attacker
            && candidate.isAlive()
            && (attacker == null || !isSameFaction(candidate, attacker))
            && !isOwnMount(candidate, attacker);
    }

    /**
     * 同阵营判定，任一成立即视作自己人：
     * <ul>
     *   <li>同一召唤链顶层——主人、姐妹女仆、各自的召唤物之间没有记分板队伍关系，只能靠召唤链认亲；</li>
     *   <li>两个方向的 {@code isAlliedTo}——原版实现是 {@code getTeam().isAlliedTo(other.getTeam())}，
     *       无队伍的一侧恒返回 {@code false}，只问一个方向会漏判队伍关系。</li>
     * </ul>
     * 注：{@code EntityAllyMixin} 修复后，同一召唤链内的 {@code isAlliedTo} 已双向成立，上面两个方向的查询对召唤链而言属冗余，
     * 只为其他模组可能存在的非对称队伍逻辑兜底；开销仅两次虚调用，保留不动。
     * 召唤链只比对缓存的顶层 UUID，是三者里最便宜的，放最前面短路。
     */
    private static boolean isSameFaction(LivingEntity candidate, Entity attacker) {
        return MaidAllyHelper.isAllied(candidate, attacker)
            || candidate.isAlliedTo(attacker)
            || attacker.isAlliedTo(candidate);
    }

    /**
     * 是否挂着残月烙印——「优先打挂印目标」这条规则的单一判定入口。
     */
    public static boolean isBranded(LivingEntity entity) {
        return entity.hasEffect(WsbEffects.CRESCENT_BRAND);
    }

    /**
     * 构造射线检测用的实体谓词：补齐内置判定、收窄到 {@link LivingEntity}，调用方只给敌我判据。
     * <p>
     * {@code RaycastBuilder.filter} 是<b>替换</b>而非叠加——一旦自带谓词，内置的
     * {@link Utils#canHitWithRaycast}（可拾取、存活、非旁观者）就没了，得自己补回来。
     */
    public static Predicate<Entity> raycastFilter(Predicate<LivingEntity> valid) {
        return candidate -> Utils.canHitWithRaycast(candidate)
            && candidate instanceof LivingEntity living
            && valid.test(living);
    }

    /**
     * 射线检测最常用的判据：只认 {@code self} 的敌人，自家女仆与中立生物一概穿过去。
     */
    public static Predicate<Entity> hostileRaycastFilter(LivingEntity self) {
        return raycastFilter(target -> isHostileTo(target, self));
    }

    /**
     * 准星锁定用的射线判据：套住谁就是谁，只让自己人穿过去。 判据见 {@link #isAimTarget}。
     */
    public static Predicate<Entity> aimRaycastFilter(LivingEntity self) {
        return raycastFilter(target -> isAimTarget(target, self));
    }

    /**
     * 按施法者分流的射线判据——法术的 {@code onCast} 是玩家与女仆共用的入口，两者选目标的方式不同：
     * <ul>
     *   <li>玩家有准星，套住谁就是谁 → {@link #aimRaycastFilter}；</li>
     *   <li>女仆等由 AI 代为出手，此时射线只是「朝交战对象拉一条线」，只认那一个目标：
     *       不这么收紧的话，恰好站在中间的村民、牲畜会先被射线命中，法术全落在它们身上。</li>
     * </ul>
     * 指派目标不再复查 {@code Enemy}：女仆在 TLM 攻击列表里把猪设成敌对、或主人先动手打了猪时，
     * 猪本来就会被写进 {@code ATTACK_TARGET}，法术必须打得到它。没有指派目标时才退回 {@link #hostileRaycastFilter}。
     */
    public static Predicate<Entity> castRaycastFilter(LivingEntity self) {
        if (self instanceof Player) {
            return aimRaycastFilter(self);
        }
        LivingEntity assigned = assignedTarget(self);
        return assigned == null
               ? hostileRaycastFilter(self)
               : raycastFilter(target -> target == assigned && isAimTarget(target, self));
    }

    /**
     * AI 当前指派的交战对象；没有（或已死、就是自己）时返回 {@code null}。
     * <p>
     * 女仆一类 brain 生物记在 {@link MemoryModuleType#ATTACK_TARGET} 里，其余 {@link Mob} 读 {@code getTarget()}。 {@code hasMemoryValue} 是必要的前置：记忆模块没注册时 {@code getMemory} 直接抛 ISE。
     */
    @Nullable
    private static LivingEntity assignedTarget(LivingEntity self) {
        if (!(self instanceof Mob mob)) {
            return null;
        }
        LivingEntity assigned = mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                                ? mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)
                                : mob.getTarget();
        return assigned != null && assigned != self && assigned.isAlive() ? assigned : null;
    }

    /**
     * 女仆索敌（可见表）：挂着残月烙印的目标优先，其余取最近。
     * <p>
     * {@link MemoryModuleType#NEAREST_VISIBLE_LIVING_ENTITIES} 自带视线判定，但其
     * {@code Sensor.isEntityTargetable} 把距离锁死在 16 格。索敌半径可配置的场合请改用
     * {@link #findNearbyTargetPreferBranded}。
     *
     * @param mob   索敌的女仆（真·TLM 女仆或召唤女仆均可）
     * @param valid 目标有效性判定（各调用点不同）
     * @return 优先挂印、其次最近的有效目标；都没有时 empty
     */
    public static Optional<? extends LivingEntity> findVisibleTargetPreferBranded(
        Mob mob, Predicate<LivingEntity> valid) {
        return mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            // findAll 在自己的谓词之外还叠一层记忆化视线判定，故传恒真取「可见候选」，
            // 有效性判定交给 pickPreferBranded 按需求值
            .flatMap(entities -> pickPreferBranded(entities.findAll(candidate -> true), valid));
    }

    /**
     * 女仆索敌（近邻表）：挂着残月烙印的目标优先，其余取最近。
     * <p>
     * 候选取自 {@link MemoryModuleType#NEAREST_LIVING_ENTITIES}——该表按任务自报的搜索半径填充，
     * 不带视线判定，故 {@code valid} 需自行包含可见性检查。
     *
     * @param mob   索敌的女仆
     * @param valid 目标有效性判定（含可见性）
     * @return 优先挂印、其次最近的有效目标；都没有时 empty
     */
    public static Optional<? extends LivingEntity> findNearbyTargetPreferBranded(
        Mob mob, Predicate<LivingEntity> valid) {
        return mob.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
            .flatMap(entities -> pickPreferBranded(entities, valid));
    }

    /**
     * 从「已按距离排序」的候选序列里挑目标：单趟遍历，第一个通过的是最近的， 遇到挂印目标立即短路返回。
     * <p>
     * 定下最近目标之后就只有挂印者还能改变结果，故此后只对挂印者求值 {@code valid}—— 调用方的谓词可能带射线视线判定，逐个扫完整张近邻表会很贵。
     */
    private static Optional<LivingEntity> pickPreferBranded(Iterable<LivingEntity> ordered,
                                                            Predicate<LivingEntity> valid) {
        LivingEntity nearest = null;
        for (LivingEntity candidate : ordered) {
            boolean branded = isBranded(candidate);
            if ((nearest != null && !branded) || !valid.test(candidate)) {
                continue;
            }
            if (branded) {
                return Optional.of(candidate);
            }
            nearest = candidate;
        }
        return Optional.ofNullable(nearest);
    }

    /**
     * 弹体伤害归因实体：优先 owner（施法者，引爆系统据此区分女仆/玩家分支）， owner 缺失（已卸载/下线）时退回弹体自身，保证伤害类型仍为学派伤害。
     */
    public static Entity projectileAttribution(Projectile projectile) {
        Entity owner = projectile.getOwner();
        return owner != null ? owner : projectile;
    }

    /**
     * 以学派伤害源打出一次 AoE 爆发：{@code setIFrames(0)} 绕过受击无敌帧， 保证同一次爆发能命中多个目标、且不被同 tick 直击刚触发的无敌帧吞掉。 过滤器由调用方提供（存活/敌我/归因排除等判定各站点不同）。
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
     *   <li>法术召唤的战斗女仆 {@link net.magicterra.winefoxsspellbooks.entity.summon.SummonedEntityMaid}
     *       （{@link IMaid} + Iron's {@link IMagicSummon}，{@code getSummoner() != null}）。</li>
     * </ul>
     * 两类均归玩家所有，故引爆时都应走女仆分支（×1.5 / AoE+1 / 连锁）。
     * 只判定「有没有主人」，不判定主人是谁；需要区分归属时用 {@link #isMaidOwnedBy}。
     * <p>
     * 归属判定走 {@link MaidAllyHelper#claimsOwner} 的 UUID 口径而非解析主人实体： 主人下线时实体解析必然返回 null，而女仆在强加载区块里还在打，那时不该被当成野生生物。
     */
    public static boolean isOwnedMaid(Entity attacker) {
        return isMaid(attacker) && MaidAllyHelper.claimsOwner(attacker);
    }

    /**
     * 是否为本模组认的女仆：TLM 雇佣女仆，或法术召唤的战斗女仆。
     */
    private static boolean isMaid(@Nullable Entity entity) {
        return entity instanceof EntityMaid || (entity instanceof IMaid && entity instanceof IMagicSummon);
    }

    /**
     * 取女仆的主人：雇佣女仆（{@link EntityMaid}）取 {@code getOwner()}， 召唤女仆（{@link IMaid} + {@link IMagicSummon}）取 {@code getSummoner()}； 非女仆或无主时返回 {@code null}。
     * <p>
     * 返回的是主人<b>实体</b>，主人下线或未加载时为 {@code null}——调用方拿它是为了对主人施加效果， 主人不在场本就无从施加。只问「有没有主人」请用 {@link #isOwnedMaid}。
     */
    @Nullable
    private static Entity maidOwner(Entity maid) {
        return isMaid(maid) ? MaidAllyHelper.getSummonerOrOwner(maid) : null;
    }

    /**
     * 目标是否为「owner 阵营的自家女仆」：目标是有主人的女仆，且与 owner 共享同一召唤链顶层 （{@link MaidAllyHelper#isAllied}，兼容玩家直接施法与女仆代姐妹施法两种情形）。 增益类效果（披风、酒池回魔、豁免微醺等）须用本方法而非 {@link #isOwnedMaid}， 否则会把其他玩家的女仆也当成自家人。
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

    /**
     * 引爆归因的「指挥者」：攻击者为玩家时即其本人，为自家女仆时取其主人玩家；其余返回 {@code null}。
     */
    @Nullable
    public static Player commanderOf(@Nullable Entity attacker) {
        if (attacker instanceof Player p) {
            return p;
        }
        return attacker == null ? null : maidCommander(attacker);
    }

    /**
     * 为指挥者叠一层酒涌（{@link #BREWSURGE_DURATION 10s} 滚动刷新，至多 {@link #BREWSURGE_MAX_STACKS 3} 层）。仅由烙印引爆调用——酒涌是「引爆的余韵」， 施法本身不计层。
     */
    public static void addBrewsurgeStack(Player commander) {
        var cur = commander.getEffect(WsbEffects.BREWSURGE);
        int next = Math.min((cur == null ? -1 : cur.getAmplifier()) + 1, BREWSURGE_MAX_STACKS - 1);
        commander.addEffect(new MobEffectInstance(WsbEffects.BREWSURGE, BREWSURGE_DURATION, next));
    }
}
