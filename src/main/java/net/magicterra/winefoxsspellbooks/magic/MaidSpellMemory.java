package net.magicterra.winefoxsspellbooks.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/**
 * 女仆对自己攻击法术的实战记忆：每个法术一次完整施放平均打出多少伤害、伤害多久才到账。
 * <p>
 * 取代人工标注的静态伤害表——三十余个附属的法术各自结算伤害，读源码估出来的数字既不准、 又跟不上附属更新；而女仆亲手打出来的数字天然包含了命中率、目标护甲、她自己的法术强度加成。
 * <p>
 * 工作方式：法术真正生效（{@code onCast} 首次执行）时 {@link #recordCast} 开一个「观测窗」，窗口期内命中该次施法目标的伤害由 {@link net.magicterra.winefoxsspellbooks.event.SpellDamageLearningHandler} 归到这次施法上； 窗口关闭后把总伤害折算回 1 级、并入该法术的滑动平均。只统计对施法时攻击目标造成的伤害： 规划器求的是「放倒这一个目标要多久」，溅射到旁人的伤害对它没有意义。
 * <p>
 * 统计量随女仆存档（附件 {@code maid_spell_memory}），女仆越打越熟；观测窗只在内存里，不落盘。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-06
 */
public class MaidSpellMemory {
    /**
     * 法术生效后（持续施法则是引导结束后）继续观测的刻数。持续型、召唤型法术只算这段时间内打出的伤害——规划器关心的 本来就是「近期能打多少」，一只召唤物一分钟后才慢慢磨出来的伤害不该算进击杀时间。
     */
    public static final int LEARN_WINDOW_TICKS = 200;

    /**
     * 受击后伤害被拦截的刻数：原版 invulnerableTime 设为 20，大于 10 时新伤害只计超出上次的部分。
     */
    public static final int INVULNERABLE_WINDOW_TICKS = 10;

    /**
     * 在途弹体到达估计的最远刻数；更远的不作为时序依据。
     */
    private static final int MAX_ARRIVAL_ESTIMATE_TICKS = 20;

    /**
     * 滑动平均的有效样本数上限。样本数到顶后新样本按 1/16 的权重并入，旧经验逐渐让位： 换了新法术书、法术强度加成变了、对手换了一批，记忆都能跟上。
     */
    private static final int MAX_SAMPLES = 16;

    /**
     * 观测次数的上限，只为防止计数无限增长；到这个量级时探索加成早已可以忽略。
     */
    private static final int MAX_CASTS = 1024;

    /**
     * 单个法术的实战统计
     *
     * @param meanDamage     折算到 1 级的单次施放平均总伤害；{@code samples} 为 0 时是截断施法给出的伤害下界
     * @param meanDelayTicks 伤害加权的平均到账时刻（相对法术生效），衡量这个法术「多快见效」
     * @param samples        已并入的样本数，封顶 {@link #MAX_SAMPLES}
     * @param casts          观测过的施法次数（含被丢弃的截断样本），封顶 {@link #MAX_CASTS}，用于衰减探索加成
     * @param delaySamples   提供有效到账延迟的样本数；零伤害不参与，旧存档从下一次有效命中重新建立延迟均值
     * @param damageSamples  最近最多 16 次完整观测的 1 级总伤害（含未命中的零），旧记忆为空，不由均值反推
     * @param censoredDamage 最近完整观测之后的正伤害下界，避免此前只打空的法术在补刀命中后仍被永久判为零
     */
    public record SpellStats(float meanDamage, float meanDelayTicks, int samples, int casts, int delaySamples,
                             List<Float> damageSamples, float censoredDamage) {
        private static final Codec<List<Float>> DAMAGE_SAMPLES_CODEC = Codec.FLOAT.listOf().validate(values ->
            values.size() <= MAX_SAMPLES && values.stream().allMatch(v -> Float.isFinite(v) && v >= 0f)
            ? DataResult.success(values) : DataResult.error(() -> "Invalid spell damage samples"));

        public SpellStats {
            damageSamples = List.copyOf(damageSamples);
        }

        public SpellStats(float meanDamage, float meanDelayTicks, int samples, int casts, int delaySamples,
                          List<Float> damageSamples) {
            this(meanDamage, meanDelayTicks, samples, casts, delaySamples, damageSamples, 0f);
        }

        public static final Codec<SpellStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("damage").forGetter(SpellStats::meanDamage),
            Codec.FLOAT.fieldOf("delay").forGetter(SpellStats::meanDelayTicks),
            Codec.INT.fieldOf("samples").forGetter(SpellStats::samples),
            Codec.INT.fieldOf("casts").forGetter(SpellStats::casts),
            Codec.INT.optionalFieldOf("delay_samples", 0).forGetter(SpellStats::delaySamples),
            DAMAGE_SAMPLES_CODEC.optionalFieldOf("damage_samples", List.of()).forGetter(SpellStats::damageSamples),
            Codec.floatRange(0f, Float.MAX_VALUE).optionalFieldOf("censored_damage", 0f).forGetter(SpellStats::censoredDamage)
        ).apply(instance, SpellStats::new));
    }

    public static final Codec<MaidSpellMemory> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, SpellStats.CODEC)
        .xmap(MaidSpellMemory::fromStats, memory -> memory.stats);

    private final Map<ResourceLocation, SpellStats> stats = new HashMap<>();

    /**
     * 观测窗口还开着的施法，不落盘。同时开着的通常只有寥寥几个，线性扫描即可。
     */
    private final List<CastRecord> openCasts = new ArrayList<>();

    /**
     * 当前目标的近期飞行失败，只在内存保留；不把旧目标／未结算施法当成新条件下的证据。
     */
    private final Map<ResourceLocation, FlightFeedback> flightFeedback = new HashMap<>();

    private static MaidSpellMemory fromStats(Map<ResourceLocation, SpellStats> stats) {
        MaidSpellMemory memory = new MaidSpellMemory();
        memory.stats.putAll(stats);
        return memory;
    }

    /**
     * 查询某个法术的实战统计
     *
     * @param spell 法术
     * @return 统计量；从未观测过时为 null
     */
    @Nullable
    public SpellStats getStats(AbstractSpell spell) {
        return stats.get(spell.getSpellResource());
    }

    /**
     * 当前目标的困难弹道用近期完整观测估值，避免静态训练的命中率淹没实战中的连续失手。 至少两次完整未命中且当前收益低于总体经验时才复核；未决、截断、别的目标和不同距离的观测不混入。 目标停下或证据过期后回退总体经验，不改写持久化统计。
     */
    @Nullable
    public SpellStats getPlanningStats(AbstractSpell spell, LivingEntity caster, @Nullable LivingEntity target,
                                       long gameTime) {
        SpellStats baseline = getStats(spell);
        FlightFeedback feedback = flightFeedback.get(spell.getSpellResource());
        if (target == null || feedback == null || feedback.target != target || !target.isAlive()
            || feedback.completedFlights.size() < 2) return baseline;
        CastRecord latest = feedback.completedFlights.getLast();
        double distance = caster.getEyePosition().distanceTo(target.getEyePosition());
        if (!latest.difficultFlight(distance)) return baseline;
        List<Float> outcomes = new ArrayList<>();
        float total = 0f;
        float delay = 0f;
        int positives = 0;
        for (CastRecord record : feedback.completedFlights) {
            if (gameTime - record.startTime - record.castTicks > LEARN_WINDOW_TICKS * 2L
                || distance < record.flightDistance * .5 || distance > record.flightDistance * 2d) continue;
            float damage = record.damage / record.levelScale;
            outcomes.add(damage);
            total += damage;
            if (damage > 0f) {
                delay += (float) (record.weightedDelay / record.damage);
                positives++;
            }
        }
        if (outcomes.size() - positives < 2 || baseline == null || baseline.samples() <= 0
            || total / outcomes.size() >= baseline.meanDamage()) return baseline;
        return new SpellStats(total / outcomes.size(), positives > 0 ? delay / positives
                                                                     : baseline.meanDelayTicks(), outcomes.size(), baseline.casts(), positives, outcomes);
    }

    /**
     * 已到账伤害提供的 1 级下界；仅供短期补刀判断，不提前写入完整样本或推断未命中。
     */
    public float getPendingDamageLowerBound(AbstractSpell spell) {
        float lowerBound = 0f;
        for (CastRecord record : openCasts) {
            if (record.spell == spell) {
                lowerBound = Math.max(lowerBound, record.damage / record.levelScale);
            }
        }
        return lowerBound;
    }

    /**
     * 已观测到至少指定 1 级伤害的到账时刻上界；没有符合的未结算观测时返回正无穷。
     */
    public float getPendingDamageDelayTicks(AbstractSpell spell, float requiredDamage) {
        float delay = Float.POSITIVE_INFINITY;
        for (CastRecord record : openCasts) {
            if (record.spell == spell && record.damage > 0 && record.damage / record.levelScale >= requiredDamage) {
                delay = Math.min(delay, record.lastDamageTime - record.startTime);
            }
        }
        return delay;
    }

    /**
     * 当前目标上仍有未回报的施法，且已经超过合理到账宽限、目标运动足以偏离弹道时，暂缓重复投资。 这不是未命中样本：慢弹道仍继续观测，完整学习窗口与归因规则不变。 历史到账延迟放宽到三倍再加十刻，持续施法从引导结束后计算。
     */
    public boolean isAwaitingFeedback(AbstractSpell spell, LivingEntity target, long gameTime) {
        return hasUnproductiveFlights(spell, target, gameTime, 1);
    }

    /**
     * 同一目标上两次困难飞行的完整观测均为零时，旧的正收益历史也需要临时复核。 未完成施法不算失败；目标停下、后续确认命中或近期证据到期后自动恢复。
     */
    public boolean hasRepeatedUnproductiveFlight(AbstractSpell spell, LivingEntity target, long gameTime) {
        FlightFeedback feedback = flightFeedback.get(spell.getSpellResource());
        return feedback != null && feedback.target == target && feedback.failures >= 2
            && gameTime - feedback.lastFailureTime <= LEARN_WINDOW_TICKS
            && feedback.lastFailure != null && feedback.lastFailure.difficultFlight();
    }

    /**
     * 当前目标上是否仍有该法术未结束观测的施法。
     */
    public boolean hasOpenCast(AbstractSpell spell, LivingEntity target) {
        for (CastRecord record : openCasts) {
            if (record.spell == spell && record.target == target) return true;
        }
        return false;
    }

    private boolean hasUnproductiveFlights(AbstractSpell spell, LivingEntity target, long gameTime, int required) {
        SpellStats known = getStats(spell);
        float grace = Math.max(20f, known != null && known.meanDamage() > 0f
                                    ? known.meanDelayTicks() * 3f + 10f : 0f);
        long lastPositiveCast = Long.MIN_VALUE;
        for (CastRecord record : openCasts) {
            if (record.spell == spell && record.target == target && record.damage > 0f)
                lastPositiveCast = Math.max(lastPositiveCast, record.startTime);
        }
        for (CastRecord record : openCasts) {
            if (record.spell == spell && record.target == target && record.damage <= 0f
                && record.startTime > lastPositiveCast
                && gameTime - record.startTime > record.castTicks + grace
                && record.carrierSpeed > 0d
                && record.targetSpeed * record.flightDistance > target.getBbWidth() * 2d * record.carrierSpeed
                && --required == 0) return true;
        }
        return false;
    }

    /**
     * 法术生效时调用，为本次施法开一个观测窗。只在 {@code onCast} 首次执行时调，而不是起手时： 读条中途被打断的施法根本没打出去，不该记成一次「零伤害」的样本。
     *
     * @param caster    施法者
     * @param spellData 施放的法术
     * @param target    施法时的攻击目标
     * @param gameTime  当前游戏刻
     * @param castTicks 生效后还要继续引导的刻数：持续施法为引导时长，瞬发与读条法术生效即结束，为 0
     * @return 这次施法的观测记录，持续施法后续每次 {@code onCast} 都拿它开载体登记范围
     */
    public CastRecord recordCast(LivingEntity caster, SpellData spellData, LivingEntity target, long gameTime,
                                 int castTicks) {
        expire(gameTime);
        CastRecord record = new CastRecord(spellData.getSpell(), levelScale(spellData, caster), target, gameTime,
            castTicks, caster.getEyePosition().distanceTo(target.getEyePosition()));
        openCasts.add(record);
        StatusDamageTracker.watch(target, this);
        FlightFeedback feedback = flightFeedback.computeIfAbsent(spellData.getSpell().getSpellResource(),
            ignored -> new FlightFeedback(target, gameTime));
        if (feedback.target != target) {
            feedback = new FlightFeedback(target, gameTime);
            flightFeedback.put(spellData.getSpell().getSpellResource(), feedback);
        }
        feedback.updatedAt = gameTime;
        return record;
    }

    /**
     * 法术实现抛异常、施法中止时调用，撤掉 {@link #recordCast} 开的观测窗。
     */
    public void abortCast(CastRecord record) {
        openCasts.remove(record);
    }

    /**
     * 优先使用载体的精确归属，其次是正在执行的 onCast。只有没有独立载体且候选唯一的法术伤害才允许兜底。 先确定施法再检查目标，避免旧弹射物溅射到新目标时被新施法抢走。
     */
    public boolean onDamage(DamageSource source, LivingEntity victim, float amount, LivingEntity owner,
                            long gameTime, @Nullable CastRecord currentCast) {
        expire(gameTime);
        CastRecord matched = null;
        for (int i = openCasts.size() - 1; i >= 0; i--) {
            CastRecord record = openCasts.get(i);
            if (record.isCarrier(source.getDirectEntity()) || record.isCarrier(source.getEntity())) {
                matched = record;
                break;
            }
        }
        if (matched == null && source.getEntity() == owner && source instanceof SpellDamageSource spellSource) {
            if (currentCast != null && openCasts.contains(currentCast) && currentCast.spell == spellSource.spell()) {
                matched = currentCast;
            } else {
                if (source.getDirectEntity() != null && source.getDirectEntity() != owner) {
                    // 独立载体没有有效观测窗（包括已到期的旧弹射物），不得转记到新施法。
                    return false;
                }
                for (CastRecord record : openCasts) {
                    if (record.spell == spellSource.spell()) {
                        if (matched != null) {
                            // 多次同法术仍在观测，无法证明属于哪一次；不能凭“最近”污染等级和延迟统计。
                            return false;
                        }
                        matched = record;
                    }
                }
            }
        }
        if (matched == null || matched.target != victim) {
            return false;
        }
        // 召唤物的攻击不算施法本身的命中：它们持续出手，算进去会让受击无敌窗口一直开着，女仆永远等不到出手时机
        if (!(source.getDirectEntity() instanceof LivingEntity direct) || direct == owner) {
            matched.lastDirectHitTime = gameTime;
        }
        credit(matched, victim, amount, gameTime);
        return true;
    }

    /**
     * 把没有攻击者的状态伤害（燃烧、冰冻、中毒、凋零）记到施加该状态的那次施法上，见 {@link StatusDamageTracker}。 观测窗已关闭或目标不符时不记。
     */
    public boolean creditStatusDamage(CastRecord record, LivingEntity victim, float amount, long gameTime) {
        expire(gameTime);
        if (!openCasts.contains(record) || record.target != victim) {
            return false;
        }
        credit(record, victim, amount, gameTime);
        return true;
    }

    /**
     * 自己的施法在目标身上的命中时刻：近 {@value #INVULNERABLE_WINDOW_TICKS} 刻内已命中的，以及在途弹体按距离／速度估计的到达刻。 两次命中相隔不足一个受击无敌窗口时，后到的伤害不超过前一次就会被吞掉。
     */
    public List<Long> hitTimes(LivingEntity target, long gameTime) {
        List<Long> result = new ArrayList<>();
        for (CastRecord record : openCasts) {
            if (record.target != target) continue;
            if (record.lastDirectHitTime != Long.MIN_VALUE
                && gameTime - record.lastDirectHitTime < INVULNERABLE_WINDOW_TICKS) result.add(record.lastDirectHitTime);
            long arrival = record.inFlightArrival(gameTime);
            if (arrival != Long.MIN_VALUE) result.add(arrival);
        }
        return result;
    }

    /**
     * 当前仍在观测、目标为 {@code victim} 的施法。
     */
    public List<CastRecord> openCastsOn(LivingEntity victim) {
        List<CastRecord> result = new ArrayList<>();
        for (CastRecord record : openCasts) {
            if (record.target == victim) result.add(record);
        }
        return result;
    }

    /**
     * 是否有目标为 {@code victim} 的施法仍在观测；不分配列表，供每 tick 的存在性检查使用。
     */
    public boolean hasOpenCastOn(LivingEntity victim) {
        for (CastRecord record : openCasts) {
            if (record.target == victim) return true;
        }
        return false;
    }

    private void credit(CastRecord matched, LivingEntity victim, float amount, long gameTime) {
        matched.damage += amount;
        if (amount > 0f) {
            FlightFeedback feedback = flightFeedback.get(matched.spell.getSpellResource());
            if (feedback != null && feedback.target == victim) {
                feedback.lastPositiveCast = Math.max(feedback.lastPositiveCast, matched.startTime);
                feedback.failures = 0;
                feedback.lastFailure = null;
                feedback.updatedAt = gameTime;
            }
        }
        matched.lastDamageTime = gameTime;
        matched.weightedDelay += amount * (double) (gameTime - matched.startTime);
        if (victim.isDeadOrDying()) {
            matched.truncated = true;
        }
    }

    /**
     * 关闭所有到期的观测窗，把结果并入统计。施法者每 tick 都会调一次（见 {@code MagicMaidAdapter#tickMagicLogic}）， 所以关窗时刻就是窗口到期的那一刻，此时目标已死即可认定死在窗口内。
     *
     * @param gameTime 当前游戏刻
     */
    public void expire(long gameTime) {
        for (Iterator<FlightFeedback> it = flightFeedback.values().iterator(); it.hasNext(); ) {
            FlightFeedback feedback = it.next();
            if (feedback.target.isRemoved() || feedback.target.isDeadOrDying()
                || gameTime - feedback.updatedAt > LEARN_WINDOW_TICKS * 2L) it.remove();
            else {
                if (feedback.lastFailure != null) feedback.lastFailure.observeFlight(gameTime);
                if (!feedback.completedFlights.isEmpty()) {
                    CastRecord latest = feedback.completedFlights.getLast();
                    if (latest != feedback.lastFailure) latest.observeFlight(gameTime);
                }
            }
        }
        if (openCasts.isEmpty()) {
            return;
        }
        Iterator<CastRecord> iterator = openCasts.iterator();
        while (iterator.hasNext()) {
            CastRecord record = iterator.next();
            record.observeFlight(gameTime);
            if (gameTime - record.startTime <= record.castTicks + LEARN_WINDOW_TICKS) {
                continue;
            }
            iterator.remove();
            if (record.target.isRemoved() || record.target.isDeadOrDying()) {
                // 目标中途死亡或消失，后续本该打出的伤害没机会打出来
                record.truncated = true;
            }
            FlightFeedback feedback = flightFeedback.get(record.spell.getSpellResource());
            if (feedback != null && feedback.target == record.target && !record.truncated && record.difficultFlight()) {
                if (feedback.completedFlights.size() >= 8) feedback.completedFlights.removeFirst();
                feedback.completedFlights.add(record);
            }
            if (feedback != null && feedback.target == record.target && !record.truncated
                && record.damage <= 0f && record.startTime > feedback.lastPositiveCast) {
                if (record.difficultFlight()) {
                    feedback.failures = Math.min(2, feedback.failures + 1);
                    feedback.lastFailure = record;
                    feedback.lastFailureTime = gameTime;
                } else {
                    feedback.failures = 0;
                    feedback.lastFailure = null;
                }
                feedback.updatedAt = gameTime;
            }
            learn(record);
        }
    }

    private void learn(CastRecord record) {
        ResourceLocation key = record.spell.getSpellResource();
        SpellStats old = stats.get(key);
        float sample = record.damage / record.levelScale;
        float delay = record.damage > 0 ? (float) (record.weightedDelay / record.damage) : record.castTicks;
        int casts = Math.min(MAX_CASTS, (old != null ? old.casts() : 0) + 1);
        boolean hasMean = old != null && old.samples() > 0;
        SpellStats updated;
        if (record.truncated && !hasMean) {
            // 截断样本只是一个下界，不能直接当均值——补刀打出的零头会被当成这个法术的真实伤害。
            // 但也不能丢：每发都把目标打死的法术会永远学不到任何东西。记成下界（samples 为 0），取历次最大值
            float lowerBound = old != null ? Math.max(old.meanDamage(), sample) : sample;
            float lowerBoundDelay = old != null && old.meanDamage() >= sample ? old.meanDelayTicks() : delay;
            updated = new SpellStats(lowerBound, lowerBoundDelay, 0, casts, 0, List.of(), lowerBound);
        } else if (record.truncated) {
            // 下界无论高低都不是一次完整施法；不能将暴击补刀或部分引导作为分布样本。
            updated = new SpellStats(old.meanDamage(), old.meanDelayTicks(), old.samples(), casts, old.delaySamples(),
                old.damageSamples(), Math.max(old.censoredDamage(), sample));
        } else if (!hasMean) {
            updated = new SpellStats(sample, delay, 1, casts, record.damage > 0 ? 1 : 0, List.of(sample));
        } else {
            int samples = Math.min(MAX_SAMPLES, old.samples() + 1);
            float meanDamage = old.meanDamage() + (sample - old.meanDamage()) / samples;
            int delaySamples = Math.min(MAX_SAMPLES, old.delaySamples() + (record.damage > 0 ? 1 : 0));
            // 到账延迟独立计数：先打空再命中的第一份延迟，也必须获得完整权重。
            float meanDelay = record.damage > 0
                              ? old.meanDelayTicks() + (delay - old.meanDelayTicks()) / delaySamples
                              : old.meanDelayTicks();
            List<Float> history = new ArrayList<>(old.damageSamples());
            if (history.size() >= MAX_SAMPLES) history.removeFirst();
            history.add(sample);
            updated = new SpellStats(meanDamage, meanDelay, samples, casts, delaySamples, history);
        }
        stats.put(key, updated);
        if (WinefoxsSpellbooks.DEBUG) {
            WinefoxsSpellbooks.LOGGER.debug("MaidSpellMemory learned {}: sample={} truncated={} -> {}",
                key, sample, record.truncated, updated);
        }
    }

    private static final class FlightFeedback {
        private final LivingEntity target;

        private int failures;

        private long updatedAt;

        private long lastFailureTime;

        private long lastPositiveCast = Long.MIN_VALUE;

        private CastRecord lastFailure;

        private final List<CastRecord> completedFlights = new ArrayList<>();

        private FlightFeedback(LivingEntity target, long gameTime) {
            this.target = target;
            updatedAt = gameTime;
        }
    }

    /**
     * 法术实际等级相对 1 级的强度倍率：统计量统一按 1 级存，换了更高等级的法术书也能沿用旧经验。
     * <p>
     * 取 Iron's 的法术威能之比：{@code getSpellPower} 里施法者属性、学派、配置三项修正在比值中同施法者下 完全抵消，剩下的正是纯粹的等级成长。绝大多数附属法术的伤害都直接正比于 spellPower， 少数不按 spellPower 结算的法术会有偏差，属于已知近似。
     */
    public static float levelScale(SpellData spellData, LivingEntity caster) {
        int level = spellData.getLevel();
        if (level <= 1) {
            return 1f;
        }
        AbstractSpell spell = spellData.getSpell();
        float basePower = spell.getSpellPower(1, caster);
        if (basePower <= 0) {
            return 1f;
        }
        return Math.max(1f, spell.getSpellPower(level, caster) / basePower);
    }

    /**
     * 一次被观测的施法，由 {@link #recordCast} 创建，对外只用作句柄。
     */
    public static final class CastRecord {
        private final AbstractSpell spell;

        private final float levelScale;

        private final LivingEntity target;

        private final long startTime;

        private final int castTicks;

        private final double flightDistance;

        private Vec3 lastTargetPosition;

        private long lastMotionTime;

        private double targetSpeed;

        private double carrierSpeed;

        /**
         * 这次施法在 {@code onCast} 期间生成的实体 id（弹射物、召唤物），它们打出的伤害归到这次施法
         */
        private final IntSet carriers = new IntOpenHashSet();

        private float damage;

        private long lastDamageTime;

        /**
         * 载体或 onCast 直接命中的时刻；状态伤害不更新它，避免把自己记成下一次状态的施加者。
         */
        private long lastDirectHitTime = Long.MIN_VALUE;

        private double weightedDelay;

        private boolean truncated;

        private CastRecord(AbstractSpell spell, float levelScale, LivingEntity target, long startTime, int castTicks,
                           double flightDistance) {
            this.spell = spell;
            this.levelScale = levelScale;
            this.target = target;
            this.startTime = startTime;
            this.castTicks = castTicks;
            this.flightDistance = flightDistance;
            lastTargetPosition = target.position();
            lastMotionTime = startTime;
        }

        /**
         * 登记一个在这次施法 {@code onCast} 期间生成的实体。
         */
        public void addCarrier(Entity entity) {
            carriers.add(entity.getId());
            observeCarrier(entity);
        }

        private void observeCarrier(Entity entity) {
            if (entity instanceof Projectile) {
                double speed = entity.getDeltaMovement().length();
                if (Double.isFinite(speed)) carrierSpeed = Math.max(carrierSpeed, speed);
            }
        }

        private void observeFlight(long gameTime) {
            long elapsed = gameTime - lastMotionTime;
            if (elapsed > 0) {
                Vec3 position = target.position();
                double speed = position.distanceTo(lastTargetPosition) / elapsed;
                // 半秒平滑实际位移；停下后快速恢复试射机会，不按僵尸等实体类型猜速度。
                targetSpeed += -Math.expm1(-elapsed / 10d) * (speed - targetSpeed);
                lastTargetPosition = position;
                lastMotionTime = gameTime;
            }
            for (int id : carriers) {
                Entity carrier = target.level().getEntity(id);
                if (carrier != null) observeCarrier(carrier);
            }
        }

        private boolean difficultFlight() {
            return difficultFlight(flightDistance);
        }

        private boolean difficultFlight(double distance) {
            return carrierSpeed > 0d && targetSpeed * distance > target.getBbWidth() * 2d * carrierSpeed;
        }

        /**
         * 未完成的引导只提供总伤害下界，不能当作完整施放的均值样本。
         */
        public void markTruncated() {
            truncated = true;
        }

        /**
         * 本刻或上一刻是否被这次施法直接命中。
         */
        public boolean hitAt(long gameTime) {
            return lastDirectHitTime >= gameTime - 1;
        }

        /**
         * 这次施法仍存活的载体是否正接触 {@code victim}。范围实体按半径判定，其余按碰撞箱外扩半格。
         */
        public boolean carrierTouches(LivingEntity victim) {
            for (int id : carriers) {
                Entity carrier = victim.level().getEntity(id);
                if (carrier == null || carrier.isRemoved()) continue;
                if (carrier instanceof AoeEntity aoe) {
                    Vec3 offset = victim.position().subtract(aoe.position());
                    double reach = aoe.getRadius() + victim.getBbWidth() * .5;
                    if (offset.horizontalDistanceSqr() <= reach * reach && Math.abs(offset.y) <= aoe.getBbHeight() + 2) {
                        return true;
                    }
                } else if (carrier.getBoundingBox().inflate(.5).intersects(victim.getBoundingBox())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 仍在飞向目标的弹体最早的到达刻（按当前距离和速度直线估计）；没有时返回 {@link Long#MIN_VALUE}。 范围实体、召唤物和背离目标的弹体不计。
         */
        private long inFlightArrival(long gameTime) {
            long earliest = Long.MIN_VALUE;
            for (int id : carriers) {
                Entity carrier = target.level().getEntity(id);
                if (!(carrier instanceof Projectile) || carrier instanceof AoeEntity || carrier.isRemoved()) continue;
                Vec3 velocity = carrier.getDeltaMovement();
                Vec3 offset = target.getBoundingBox().getCenter().subtract(carrier.position());
                double speed = velocity.length();
                if (speed < .05 || velocity.dot(offset) < .7 * speed * offset.length()) continue;
                double ticks = Math.ceil(Math.max(0, offset.length() - target.getBbWidth() * .5) / speed);
                if (ticks > MAX_ARRIVAL_ESTIMATE_TICKS) continue;
                long arrival = gameTime + (long) ticks;
                if (earliest == Long.MIN_VALUE || arrival < earliest) earliest = arrival;
            }
            return earliest;
        }

        /**
         * 状态效果的来源实体是否属于这次施法。
         */
        public boolean isSource(@Nullable Entity entity) {
            return isCarrier(entity);
        }

        private boolean isCarrier(@Nullable Entity entity) {
            return entity != null && carriers.contains(entity.getId());
        }
    }
}
