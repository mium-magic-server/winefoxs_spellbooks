package net.magicterra.winefoxsspellbooks.magic;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/**
 * 攻击法术的攒蓝/出手规划：以「最快放倒当前目标」为目标，在「用当前买得起的法术出手」与 「攒蓝等一个更强的法术」之间做决策。
 * <p>
 * 核心是一张最短击杀时间表 {@code T[h][b]}：目标还剩第 {@code h} 格血、女仆魔力在第 {@code b} 格时， 最少还要多少个回魔周期才能把目标打死。施法会让血量严格下降，等待会让魔力严格上升， 所以按「血量从低到高、魔力从高到低」的顺序一遍就能填完，不需要折扣因子或规划视界： 弱怪自然是「手上有什么打什么」，厚血目标才会算出「攒蓝换高性价比法术」更快。
 * <p>
 * 法术伤害来自女仆的实战记忆 {@link MaidSpellMemory}，没打过的法术用先验估计，并给一份按样本数递减的 乐观加成以提供尝试机会。积累完整伤害样本后按各个实测结果推演，探索加成不再冒充击杀伤害。
 * <p>
 * 每个 {@link net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellChooseTask} 持有一个专属实例 （该任务本就是每个女仆一份）。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-09-03
 */
public class SpellCastPlanner {
    /**
     * 魔力离散化的格数上限。正常情况下格宽取每周期回魔量（「等一个周期」正好前进一格）， 这个上限只是回魔相对魔力池极慢时的兜底，防止表被撑爆。
     */
    private static final int MAX_MANA_BUCKETS = 96;

    /**
     * 目标血量离散化的格数上限，格宽 = 当前血量 / 此值（至少 1 点血）。
     */
    private static final int MAX_HP_BUCKETS = 64;

    /**
     * 完全没有实战数据可参考时，每点魔力估计能打出的伤害。Iron's 原版攻击法术大致落在这个量级， 只用来给第一批法术起个头，打过几次就会被实测值取代。
     */
    private static final float DEFAULT_DAMAGE_PER_MANA = 0.5f;

    /**
     * 先验的初始样本权重，随观测次数继续衰减，避免已熟悉的高价法术仍被蓝耗先验主导。
     */
    private static final float PRIOR_WEIGHT = 1f;

    /**
     * 乐观探索加成：未知法术以先验为基准；有完整实测后改以实测为基准，并按 1/(1+n)^2 衰减。
     */
    private static final float EXPLORATION_BONUS = 1f;

    /**
     * 实测值参与「每点魔力伤害」换算前至少要有的样本数，免得一次失手就把所有先验带偏。
     */
    private static final int MIN_SAMPLES_FOR_RATIO = 2;

    /**
     * 「立刻出手」与「等待」的击杀时间相差不到这么多周期时，选立刻出手。
     * <p>
     * 魔力是瓶颈时，同一组法术换个先后顺序，总击杀时间往往完全一样（花掉的魔力总量相同）； 这时早出手更稳——伤害先落袋，目标还可能被队友或召唤物先一步打死。
     */
    private static final float CAST_NOW_TIE_EPOCHS = 0.5f;

    /**
     * 两个法术的击杀时间相差不到这么多周期时视为一样快，改比蓝耗。只用来吸收浮点误差。
     */
    private static final float TIE_EPSILON = 1e-3f;

    /**
     * 攒蓝预留在「算出能买得起」之后再多保留的刻数，留给选择任务的随机间隔。
     */
    private static final int RESERVE_SLACK_TICKS = 40;

    /**
     * 预留生效时女仆的血量下限比例：血量更低时保命优先，不拦防御/位移等法术花蓝。
     */
    private static final float RESERVE_MIN_HEALTH_RATIO = 0.5f;

    /**
     * 安全状态下，最多三发已知攻击可用预计三秒换取后续战斗的魔力。
     */
    private static final int CONSERVE_READY_DELAY_TICKS = 60;

    /**
     * 未验证的探索最多支出当前魔力的四分之一，至少容纳两发最便宜的可行攻击。
     */
    private static final float COLD_EXPLORATION_MANA_FRACTION = 0.25f;

    /**
     * DP 表的复用缓冲区，按最大尺寸一次分配。每个线程一份而不是每个规划器一份：规划器跟着女仆的 每个选择任务走，挂在实例上的话每只女仆都要常驻一张表；而规划只在服务端线程上同步执行、 不会重入，一个线程一份就够了。多出的一行余量兜住浮点除法把血量格数算成上限 + 1 的情况。
     */
    private static final ThreadLocal<DpBuffers> BUFFERS = ThreadLocal.withInitial(DpBuffers::new);

    private float reservedMana;

    private long reservedUntil = Long.MIN_VALUE;

    /**
     * 为之攒蓝的那个目标的实体 id，没有预留时为 -1。目标死了、换了，这笔预留就作废——否则回魔慢时预留能拖上几十秒， 期间新威胁来了也放不出防御、位移法术。存 id 而不是实体本身：规划器随女仆的选择任务常驻， 持有实体引用会把战斗结束后的最后一个目标一直钉在内存里。
     */
    private int reservedForId = -1;

    /**
     * 攒蓝时保存完整估值输入；候选数量相同不代表法术、等级、记忆或施法属性没有变化。
     */
    private float reservedAtTargetHp;

    private ReservationInputs reservationInputs;

    private float reservedLastMana;

    /**
     * 额外考虑马上结束冷却的法术：同时比较魔力消耗，以及长施法期间被阻塞的后续输出。 能更早完成实测击杀的法术无需等待。
     */
    public SpellData plan(Mob mob, List<SpellData> castableNow, List<SpellData> pending,
                          List<SpellData> cooling, LivingEntity target, long gameTime) {
        if (target != null && target.isAlive() && !castableNow.isEmpty()) {
            castableNow = outsideHitWindows(mob, castableNow, target, gameTime);
            if (castableNow.isEmpty()) return null;
        }
        if (target != null && target.isAlive() && safeToConserve(mob, target)) {
            MaidSpellMemory memory = mob.getData(WsbAttachments.MAID_SPELL_MEMORY);
            float maxMana = (float) MaidMagicManager.getMaxMana(mob);
            boolean awaitingFeedback = false;
            boolean awaitingLearnedFeedback = false;
            boolean alternative = false;
            boolean reachableAlternative = false;
            for (List<SpellData> group : List.of(castableNow, pending, cooling)) {
                for (SpellData data : group) {
                    if (!MaidMagicManager.hasRecast(mob, data)
                        && awaitingUnproductiveFeedback(memory, data, target, gameTime)) awaitingFeedback = true;
                    else {
                        boolean learnedNeedsReview = !MaidMagicManager.hasRecast(mob, data)
                            && awaitingLearnedFeedback(mob, memory, data, target, gameTime);
                        if (learnedNeedsReview) awaitingLearnedFeedback = true;
                        float cost = planPrice(mob, data, maxMana);
                        boolean failed = confirmedZero(memory, data.getSpell());
                        if (!failed && cost <= maxMana && (cost <= MaidMagicManager.getMana(mob)
                            || MaidMagicManager.getManaRegenPerInterval(mob) > 0f)) alternative = true;
                        if (!learnedNeedsReview && !failed && cost <= maxMana
                            && (cost <= MaidMagicManager.getMana(mob)
                            || MaidMagicManager.getManaRegenPerInterval(mob) > 0f)) reachableAlternative = true;
                    }
                }
            }
            // 试过的攻击在当前目标上全部确认打空时，预算只会把剩下的未知法术也挡住，冷启动就此卡在廉价飞弹上。
            boolean triedFailing = false;
            boolean triedHealthy = false;
            for (List<SpellData> group : List.of(castableNow, pending, cooling)) {
                for (SpellData data : group) {
                    MaidSpellMemory.SpellStats stats = memory.getStats(data.getSpell());
                    boolean tried = (stats != null && stats.samples() > 0) || memory.hasOpenCast(data.getSpell(), target);
                    if (!tried) continue;
                    boolean zero = confirmedZero(memory, data.getSpell());
                    if (zero || awaitingUnproductiveFeedback(memory, data, target, gameTime)
                        || memory.hasRepeatedUnproductiveFlight(data.getSpell(), target, gameTime)) triedFailing = true;
                    else triedHealthy = true;
                }
            }
            // 只有仍有其他攻击可选时才轮换；唯一攻击和免费二段咏唱不因反馈延迟被封住。
            // 两次完整失败和当前困难飞行共同复核旧经验；暂未到账的施法不能触发这条攒蓝路线。
            boolean recoveringLearned = awaitingLearnedFeedback && reachableAlternative;
            boolean recovering = (awaitingFeedback && alternative) || recoveringLearned;
            if (recovering) {
                castableNow = withFeedback(mob, memory, castableNow, target, gameTime, recoveringLearned);
                pending = withFeedback(mob, memory, pending, target, gameTime, recoveringLearned);
                cooling = withFeedback(mob, memory, cooling, target, gameTime, recoveringLearned);
                if (recoveringLearned) {
                    SpellData probe = fastestFeedback(mob, memory, castableNow, pending, cooling, maxMana);
                    if (probe != null) {
                        float price = planPrice(mob, probe, maxMana);
                        if (castableNow.contains(probe) && price <= MaidMagicManager.getMana(mob)) {
                            clearReservation();
                            return probe;
                        }
                        castableNow = List.of();
                        pending = price > MaidMagicManager.getMana(mob) ? List.of(probe) : List.of();
                        cooling = cooling.contains(probe) ? List.of(probe) : List.of();
                    }
                }
            }
            float cheapest = Float.POSITIVE_INFINITY;
            for (List<SpellData> group : List.of(castableNow, pending, cooling)) {
                for (SpellData data : group) {
                    // 已确认只会打空的廉价攻击不能阻止尝试其他法术。
                    if (confirmedZero(memory, data.getSpell())) continue;
                    cheapest = Math.min(cheapest, planPrice(mob, data, maxMana));
                }
            }
            if (Float.isFinite(cheapest) && !(triedFailing && !triedHealthy)) {
                // 反馈迟迟未到时先试最廉价的替代品，不能把换路重新解释成一次昂贵的乐观秒杀。
                float budget = Math.max(cheapest * (recovering ? 1f : 2f), Math.max(
                    MaidMagicManager.getMana(mob) * COLD_EXPLORATION_MANA_FRACTION,
                    MaidMagicManager.getManaRegenPerInterval(mob) * 2f));
                List<SpellData> readyBeforeBudget = castableNow;
                castableNow = withinExplorationBudget(mob, memory, castableNow, maxMana, budget);
                pending = withinExplorationBudget(mob, memory, pending, maxMana, budget);
                cooling = withinExplorationBudget(mob, memory, cooling, maxMana, budget);
                if (castableNow.isEmpty() && !readyBeforeBudget.isEmpty()
                    && MaidMagicManager.getMana(mob) + MaidMagicManager.getManaRegenPerInterval(mob) >= maxMana) {
                    // 预算把此刻可放的全部筛掉、且魔力已满（再等回魔就溢出）时，
                    // 若一律筛掉，这个昂贵法术永远拿不到实测，女仆满蓝也只会干等冷却。
                    // 放回其中最便宜且付得起的一个，由下面的估值与冷却中的法术比较后再决定放还是等。
                    // 魔力未满时不放回：便宜法术冷却很短，等它比试昂贵法术划算（NoAI 冷启动实测）。
                    SpellData probe = cheapestAffordable(mob, memory, readyBeforeBudget, maxMana);
                    if (probe != null) castableNow = List.of(probe);
                }
            }
        }
        SpellData selected = planNow(mob, castableNow, pending, target, gameTime);
        if (selected == null && castableNow.isEmpty() && target != null && target.isAlive()
            && safeToConserve(mob, target)) {
            float mana = MaidMagicManager.getMana(mob);
            float maxMana = (float) MaidMagicManager.getMaxMana(mob);
            MaidSpellMemory memory = mob.getData(WsbAttachments.MAID_SPELL_MEMORY);
            for (SpellData next : cooling) {
                int ticks = MaidMagicManager.getSoonReadyTicks(mob, next);
                float cost = planPrice(mob, next, maxMana);
                if (ticks > 0 && cost > 0f && cost <= mana && (reservedMana <= 0f || cost < reservedMana)) {
                    reservedMana = cost;
                    reservedUntil = gameTime + ticks + RESERVE_SLACK_TICKS;
                    reservedForId = target.getId();
                }
            }
            for (SpellData next : pending) {
                float cost = listPrice(mob, next, castTicks(mob, next), maxMana);
                float regen = MaidMagicManager.getManaRegenPerInterval(mob);
                boolean failed = confirmedZero(memory, next.getSpell());
                if (!failed && regen > 0f && cost > mana && cost <= maxMana && (reservedMana <= 0f || cost < reservedMana)) {
                    reservedMana = cost;
                    reservedUntil = gameTime + RESERVE_SLACK_TICKS;
                    reservedForId = target.getId();
                    reservedAtTargetHp = target.getHealth() + target.getAbsorptionAmount();
                    reservedLastMana = mana;
                    reservationInputs = new ReservationInputs(mob, memory,
                        castableNow, pending, new ManaState(mana, maxMana, regen));
                }
            }
        }
        selected = conserveReadySequence(mob, selected, castableNow, target);
        if (selected == null || cooling.isEmpty() || target == null || !target.isAlive()) {
            return selected;
        }
        MaidSpellMemory memory = mob.getData(WsbAttachments.MAID_SPELL_MEMORY);
        List<SpellData> alternatives = new ArrayList<>(cooling.size() + 1);
        alternatives.add(selected);
        alternatives.addAll(cooling);
        Estimates estimates = estimate(mob, memory, alternatives, (float) MaidMagicManager.getMaxMana(mob));
        float health = target.getHealth() + target.getAbsorptionAmount();
        MaidSpellMemory.SpellStats selectedStats = memory.getStats(selected.getSpell());
        float provenDamage = selectedStats != null && selectedStats.samples() > 0 ? estimates.finisherDamages[0] : 0;
        float mana = MaidMagicManager.getMana(mob);
        for (int i = 1; i < alternatives.size(); i++) {
            SpellData next = alternatives.get(i);
            int ticks = MaidMagicManager.getSoonReadyTicks(mob, next);
            float damage = estimates.damages[i];
            float cost = estimates.manaCosts[i];
            if (ticks <= 0 || cost > mana || damage <= 0 || cost <= 0) {
                continue;
            }
            int firstHitTicks = ticks + estimates.finishEpochs[i] * MaidMagicManager.MANA_REGEN_TICKS;
            int selectedFinishTicks = estimates.finishEpochs[0] * MaidMagicManager.MANA_REGEN_TICKS;
            long nextCycleTicks = (long) estimates.castEpochs[i] * MaidMagicManager.MANA_REGEN_TICKS
                + Math.max(0, MaidMagicManager.getEffectiveSpellCooldown(next.getSpell(), mob, CastSource.MOB))
                + Math.max(MaidMagicManager.MANA_REGEN_TICKS, Config.getMaxComboDelayTick());
            float required = Mth.ceil(health / damage) * cost;
            // 完整击杀路线用实测／下界伤害，探索奖励不能把已知要两发的目标算成一发。
            float finisherDamage = estimates.finisherDamages[i];
            int killShots = finisherDamage > 0 ? Mth.ceil(health / finisherDamage) : Integer.MAX_VALUE;
            float killMana = killShots * cost;
            long nextKillTicks = firstHitTicks + (killShots - 1L) * nextCycleTicks;
            MaidSpellMemory.SpellStats nextStats = memory.getStats(next.getSpell());
            float nextScale = MaidSpellMemory.levelScale(next, mob);
            float nextObserved = nextStats != null ? estimates.finisherDamages[i] : 0f;
            float pendingDelay = memory.getPendingDamageDelayTicks(next.getSpell(), health / nextScale);
            boolean hasPendingFinisher = Float.isFinite(pendingDelay);
            long budgetHitTicks = firstHitTicks;
            if (nextObserved < health && hasPendingFinisher) {
                int duration = castTicks(mob, next);
                float arrival = next.getSpell().getCastType() == CastType.CONTINUOUS
                                ? Math.max(duration, pendingDelay) : duration + pendingDelay;
                budgetHitTicks = ticks + Math.max(1L, (long) Math.ceil(arrival / MaidMagicManager.MANA_REGEN_TICKS))
                    * MaidMagicManager.MANA_REGEN_TICKS;
            }
            float savedMana = estimates.manaCosts[0] - cost;
            long addedTicks = Math.max(0L, budgetHitTicks - selectedFinishTicks);
            boolean safeToWait = safeToConserve(mob, target);
            // 不为抢不足一秒的补刀透支下一场战斗。必须有实测／已到账下界支持，不能用探索奖励冒充命中。
            boolean conservesFinisherMana = safeToWait && (nextObserved >= health || hasPendingFinisher)
                && addedTicks <= MaidMagicManager.MANA_REGEN_TICKS * 2
                && savedMana > MaidMagicManager.getManaRegenPerInterval(mob)
                * ((float) addedTicks / MaidMagicManager.MANA_REGEN_TICKS) + TIE_EPSILON;
            if (provenDamage >= health && estimates.manaCosts[0] <= mana
                && !conservesFinisherMana && (killMana > mana || selectedFinishTicks <= nextKillTicks)) {
                continue;
            }
            boolean savesMana = mana < estimates.manaCosts[0] + required
                && damage / cost > estimates.damages[0] / Math.max(1f, estimates.manaCosts[0]) + TIE_EPSILON;
            // 蓝够也不能忽略长施法占用的时间。保守地计入冷却、读条和一次选法延迟，
            // 只用当前蓝量支付后续施法；不借用未来回魔来虚构可打出的伤害。
            int occupiedTicks = estimates.castEpochs[0] * MaidMagicManager.MANA_REGEN_TICKS;
            long shots = firstHitTicks <= occupiedTicks
                         ? Math.min(1 + (occupiedTicks - firstHitTicks) / nextCycleTicks, Mth.floor(mana / cost)) : 0;
            boolean avoidsBlocking = shots > 0
                && Math.min(health, shots * damage) > Math.min(health, estimates.damages[0]) + TIE_EPSILON;
            boolean finishesSooner = killMana <= mana && nextKillTicks < selectedFinishTicks;
            if (savesMana || avoidsBlocking || finishesSooner || conservesFinisherMana) {
                reservedMana = cost;
                reservedUntil = gameTime + ticks + RESERVE_SLACK_TICKS;
                reservedForId = target.getId();
                return null;
            }
        }
        return selected;
    }

    /**
     * 去掉会落进自己上一发受击无敌窗口的候选：预计到达刻与已命中或在途命中相隔不足 {@link MaidSpellMemory#INVULNERABLE_WINDOW_TICKS} 时，后到的一发多半被吞掉，稍等几刻再放即可。 到达刻按读条时长加实测到账延迟估计，没有实测时按 0 计；持续施法跨越多个窗口，不在此列。
     */
    private static List<SpellData> outsideHitWindows(Mob mob, List<SpellData> castableNow, LivingEntity target,
                                                     long gameTime) {
        MaidSpellMemory memory = mob.getData(WsbAttachments.MAID_SPELL_MEMORY);
        List<Long> hits = memory.hitTimes(target, gameTime);
        if (hits.isEmpty()) return castableNow;
        List<SpellData> result = new ArrayList<>(castableNow.size());
        for (SpellData data : castableNow) {
            if (data.getSpell().getCastType() == CastType.CONTINUOUS) {
                result.add(data);
                continue;
            }
            MaidSpellMemory.SpellStats stats = memory.getStats(data.getSpell());
            float delay = stats != null && stats.samples() > 0 && stats.meanDamage() > 0f ? stats.meanDelayTicks() : 0f;
            float arrival = gameTime + castTicks(mob, data) + delay;
            boolean overlaps = false;
            for (long hit : hits) {
                if (Math.abs(arrival - hit) < MaidSpellMemory.INVULNERABLE_WINDOW_TICKS) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) result.add(data);
        }
        return result;
    }

    /** 该法术已有样本且实测与到账下界都为零：确认只会打空。 */
    private static boolean confirmedZero(MaidSpellMemory memory, AbstractSpell spell) {
        MaidSpellMemory.SpellStats stats = memory.getStats(spell);
        return stats != null && stats.samples() > 0 && stats.meanDamage() <= 0f
            && stats.censoredDamage() <= 0f && memory.getPendingDamageLowerBound(spell) <= 0f;
    }

    /** 计划使用的法术价格：二段咏唱免费，其余按标价。 */
    private static float planPrice(Mob mob, SpellData data, float maxMana) {
        return MaidMagicManager.hasRecast(mob, data) ? 0f : listPrice(mob, data, castTicks(mob, data), maxMana);
    }

    private static boolean safeToConserve(Mob mob, LivingEntity target) {
        return mob.getHealth() >= mob.getMaxHealth() * RESERVE_MIN_HEALTH_RATIO
            && (mob.getLastHurtByMob() == null || mob.getLastHurtByMob() == target);
    }

    private static List<SpellData> withFeedback(Mob mob, MaidSpellMemory memory, List<SpellData> spells,
                                                LivingEntity target, long gameTime, boolean includeLearned) {
        List<SpellData> result = new ArrayList<>(spells.size());
        for (SpellData data : spells) {
            if (MaidMagicManager.hasRecast(mob, data)
                || (!awaitingUnproductiveFeedback(memory, data, target, gameTime)
                && (!includeLearned || !awaitingLearnedFeedback(mob, memory, data, target, gameTime)))) result.add(data);
        }
        return result;
    }

    private static boolean awaitingUnproductiveFeedback(MaidSpellMemory memory, SpellData data,
                                                        LivingEntity target, long gameTime) {
        MaidSpellMemory.SpellStats stats = memory.getStats(data.getSpell());
        // 未知或暂时全零的重复探索使用单次反馈；正收益历史另需两次完整的困难飞行失败。
        return (stats == null || stats.samples() <= 0 || (stats.meanDamage() <= 0f && stats.censoredDamage() <= 0f))
            && memory.isAwaitingFeedback(data.getSpell(), target, gameTime);
    }

    private static boolean awaitingLearnedFeedback(Mob mob, MaidSpellMemory memory, SpellData data,
                                                   LivingEntity target, long gameTime) {
        MaidSpellMemory.SpellStats stats = memory.getStats(data.getSpell());
        return stats != null && stats.samples() > 0 && stats.meanDamage() > 0f
            && (memory.hasRepeatedUnproductiveFlight(data.getSpell(), target, gameTime)
            || memory.getPlanningStats(data.getSpell(), mob, target, gameTime) != stats);
    }

    /**
     * 旧经验失效后的试射优先获取更早的可信反馈，避免按旧性价比换到另一个慢弹道。
     */
    private static SpellData fastestFeedback(Mob mob, MaidSpellMemory memory, List<SpellData> ready,
                                             List<SpellData> pending, List<SpellData> cooling, float maxMana) {
        SpellData best = null;
        float bestTicks = Float.POSITIVE_INFINITY;
        float bestPrice = Float.POSITIVE_INFINITY;
        for (List<SpellData> group : List.of(ready, pending, cooling)) {
            for (SpellData data : group) {
                MaidSpellMemory.SpellStats stats = memory.getStats(data.getSpell());
                if (stats == null || stats.samples() <= 0 || stats.meanDamage() <= 0f) continue;
                int duration = castTicks(mob, data);
                float price = MaidMagicManager.hasRecast(mob, data) ? 0f : listPrice(mob, data, duration, maxMana);
                if (price > maxMana || (price > MaidMagicManager.getMana(mob)
                    && MaidMagicManager.getManaRegenPerInterval(mob) <= 0f)) continue;
                float ticks = Math.max(duration, stats.meanDelayTicks()
                    + (data.getSpell().getCastType() == CastType.CONTINUOUS ? 0 : duration))
                    + Math.max(0, MaidMagicManager.getSoonReadyTicks(mob, data));
                if (ticks < bestTicks || (ticks == bestTicks && price < bestPrice)) {
                    best = data;
                    bestTicks = ticks;
                    bestPrice = price;
                }
            }
        }
        return best;
    }

    private static List<SpellData> withinExplorationBudget(Mob mob, MaidSpellMemory memory,
                                                           List<SpellData> spells, float maxMana, float budget) {
        List<SpellData> result = new ArrayList<>(spells.size());
        for (SpellData data : spells) {
            MaidSpellMemory.SpellStats stats = memory.getStats(data.getSpell());
            // 配置值、完整观测、已到账下界和免费重铸均为独立证据，不受未知先验的探索预算限制。
            boolean evidence = MaidSpellCatalog.getSpellValue(data.getSpell()) > 0f || MaidMagicManager.hasRecast(mob, data)
                || (stats != null && (stats.samples() > 0 || stats.meanDamage() > 0f || stats.censoredDamage() > 0f))
                || memory.getPendingDamageLowerBound(data.getSpell()) > 0f;
            if (evidence || listPrice(mob, data, castTicks(mob, data), maxMana) <= budget) result.add(data);
        }
        return result;
    }

    /**
     * 当前付得起、且未确认只会打空的法术中最便宜的一个；没有则返回 null
     */
    @Nullable
    private static SpellData cheapestAffordable(Mob mob, MaidSpellMemory memory, List<SpellData> spells, float maxMana) {
        float mana = MaidMagicManager.getMana(mob);
        SpellData best = null;
        float bestPrice = Float.POSITIVE_INFINITY;
        for (SpellData data : spells) {
            if (confirmedZero(memory, data.getSpell())) continue;
            float price = planPrice(mob, data, maxMana);
            if (price <= mana && price < bestPrice) {
                best = data;
                bestPrice = price;
            }
        }
        return best;
    }

    private static SpellData conserveReadySequence(Mob mob, @Nullable SpellData selected,
                                                   List<SpellData> ready, @Nullable LivingEntity target) {
        if (selected == null || ready.size() < 2 || target == null || !target.isAlive()
            || !safeToConserve(mob, target)) return selected;
        MaidSpellMemory memory = mob.getData(WsbAttachments.MAID_SPELL_MEMORY);
        MaidSpellMemory.SpellStats selectedStats = memory.getStats(selected.getSpell());
        if (selectedStats == null || selectedStats.samples() <= 0) return selected;
        List<SpellData> alternatives = new ArrayList<>(ready.size() + 1);
        alternatives.add(selected);
        for (SpellData candidate : ready) if (candidate != selected) alternatives.add(candidate);
        Estimates estimates = estimate(mob, memory, alternatives, (float) MaidMagicManager.getMaxMana(mob));
        float hp = target.getHealth() + target.getAbsorptionAmount();
        float mana = MaidMagicManager.getMana(mob);
        float bestCost = estimates.manaCosts[0];
        SpellData best = selected;
        for (int i = 1; i < alternatives.size(); i++) {
            SpellData candidate = alternatives.get(i);
            MaidSpellMemory.SpellStats stats = memory.getStats(candidate.getSpell());
            float damage = estimates.finisherDamages[i];
            if (stats == null || stats.samples() <= 0 || damage <= 0) continue;
            int shots = Mth.ceil(hp / damage);
            float cost = shots * estimates.manaCosts[i];
            if (shots > 3 || cost > mana || cost >= bestCost || mana - estimates.manaCosts[0] >= cost) continue;
            // 冷却从实际生效后计算，不重复加入一次向上取整的施法周期。
            long cycle = (long) castTicks(mob, candidate)
                + Math.max(0, MaidMagicManager.getEffectiveSpellCooldown(candidate.getSpell(), mob, CastSource.MOB))
                + Math.max(MaidMagicManager.MANA_REGEN_TICKS, Config.getMaxComboDelayTick());
            long finish = (long) estimates.finishEpochs[i] * MaidMagicManager.MANA_REGEN_TICKS + (shots - 1L) * cycle;
            long added = Math.max(0L, finish - (long) estimates.finishEpochs[0] * MaidMagicManager.MANA_REGEN_TICKS);
            float saved = estimates.manaCosts[0] - cost;
            if (added <= CONSERVE_READY_DELAY_TICKS && saved > TIE_EPSILON
                + MaidMagicManager.getManaRegenPerInterval(mob) * ((float) added / MaidMagicManager.MANA_REGEN_TICKS)) {
                best = candidate;
                bestCost = cost;
            }
        }
        return best;
    }

    private SpellData planNow(Mob mob, List<SpellData> castableNow, List<SpellData> pending,
                              LivingEntity target, long gameTime) {
        ManaState mana = new ManaState(MaidMagicManager.getMana(mob), (float) MaidMagicManager.getMaxMana(mob),
            MaidMagicManager.getManaRegenPerInterval(mob));
        float targetHp = target != null ? target.getHealth() + target.getAbsorptionAmount() : 0f;
        MaidSpellMemory memory = mob.getData(WsbAttachments.MAID_SPELL_MEMORY);
        if (isStillSaving(mob, memory, target, targetHp, mana, castableNow, pending, gameTime)) {
            return null;
        }
        clearReservation();
        if (castableNow.isEmpty()) {
            // 下面「只剩一个候选就直接放」的捷径依赖候选里至少有一个现在起得了手
            return null;
        }
        // 候选按「现在就能放完整一发」「起得了手但蓝不够引导完」「还买不起」排列。估计按整段引导算，所以只有第一组
        // 能被当作立刻出手的选项。SpellChooseTask 已按整段引导费用筛出 castableNow，第二组通常为空，
        // 留作 listPrice 与 MaidMagicManager#getEffectiveManaCost 口径出现偏差时的兜底
        List<SpellData> candidates = new ArrayList<>(castableNow.size() + pending.size());
        List<SpellData> partial = new ArrayList<>();
        for (SpellData spellData : castableNow) {
            if (MaidMagicManager.hasRecast(mob, spellData)
                || listPrice(mob, spellData, castTicks(mob, spellData), mana.max) <= mana.current) {
                candidates.add(spellData);
            } else {
                partial.add(spellData);
            }
        }
        int affordableCount = candidates.size();
        candidates.addAll(partial);
        int usableCount = candidates.size();
        candidates.addAll(pending);
        Estimates estimates = estimate(mob, memory, candidates, mana.max);

        if (target == null || !target.isAlive()) {
            return mana.regen <= 0 ? bestByEfficiency(candidates, usableCount, estimates)
                                   : bestByDamage(candidates, usableCount, estimates);
        }
        if (candidates.size() == 1) {
            // 等下去拿不到任何新法术，只会把同一发往后推
            return candidates.getFirst();
        }

        SpellData finisher = immediateFinisher(candidates, affordableCount, estimates, targetHp);
        if (finisher != null) {
            return finisher;
        }
        Decision decision = solve(candidates.size(), estimates, targetHp, mana);
        if (decision.castIndex >= 0) {
            return candidates.get(decision.castIndex);
        }
        if (decision.savingForIndex < 0) {
            // 表里没有任何能打死目标的路线（比如回魔被封），打一发总比干站着强——引导不完的持续施法也行
            return mana.regen <= 0 ? bestByEfficiency(candidates, usableCount, estimates)
                                   : bestByDamage(candidates, usableCount, estimates);
        }
        // 攒蓝：给其他类别的法术划一条不许花过的线，免得攒下的蓝被防御、减益法术顺手花掉
        reservedMana = estimates.manaCosts[decision.savingForIndex];
        float missing = Math.max(0f, reservedMana - mana.current);
        long ticksToAfford = mana.regen > 0
                             ? (long) Math.ceil(missing / mana.regen) * MaidMagicManager.MANA_REGEN_TICKS : 0;
        MaidSpellMemory.SpellStats savingStats = memory.getStats(candidates.get(decision.savingForIndex).getSpell());
        if ((savingStats == null || savingStats.samples() < MIN_SAMPLES_FOR_RATIO)
            && (savingStats == null || savingStats.meanDamage()
            * MaidSpellMemory.levelScale(candidates.get(decision.savingForIndex), mob) < targetHp)
            && ticksToAfford > MaidSpellMemory.LEARN_WINDOW_TICKS) {
            // 未验证的高价先验不值得让整场战斗都在攒蓝；先兑现手上能打出的伤害并积累实测。
            SpellData fallback = bestByDamage(candidates, usableCount, estimates);
            if (fallback != null) {
                reservedMana = 0;
                return fallback;
            }
        }
        reservedUntil = gameTime + ticksToAfford + RESERVE_SLACK_TICKS;
        reservedForId = target.getId();
        reservedAtTargetHp = targetHp;
        reservedLastMana = mana.current;
        reservationInputs = new ReservationInputs(mob, memory, castableNow, pending, mana);
        return null;
    }

    /**
     * 上一轮的攒蓝结论是否仍然成立，成立就不必重新填表。DP 给出的本来就是一整条「等到 X 买得起再放」的路线， 照着走下去、估值输入完全相同时可以复用。任何血量变化、新记忆、候选替换、等级与属性变化、 回魔或上限变化以及额外耗蓝都要重新规划；不能仅凭候选数量和血量桶推断局面不变。
     */
    private boolean isStillSaving(Mob mob, MaidSpellMemory memory, @Nullable LivingEntity target, float targetHp,
                                  ManaState mana, List<SpellData> castable, List<SpellData> pending, long gameTime) {
        if (target == null || target.getId() != reservedForId || gameTime > reservedUntil
            || mana.current >= reservedMana || mana.current < reservedLastMana
            || targetHp != reservedAtTargetHp || reservationInputs == null
            || !reservationInputs.matches(mob, memory, castable, pending, mana)) {
            return false;
        }
        reservedLastMana = mana.current;
        return true;
    }

    private void clearReservation() {
        reservedMana = 0;
        reservedForId = -1;
        reservationInputs = null;
    }

    /**
     * 本轮选法时生效的攒蓝预留线；预留不适用时为 0。每个选法周期算一次，再对各法术调 {@link #wouldSpendReservedMana}。
     * <p>
     * 只在女仆自己血量充足、且没有被别的怪围攻时生效：快死的时候、或者有规划没算到的威胁时，保命比打得快重要。
     *
     * @param mob          施法者
     * @param attackTarget 当前攻击目标
     * @param gameTime     当前游戏刻
     * @return 预留的魔力量
     */
    public float activeReservation(Mob mob, @Nullable LivingEntity attackTarget, long gameTime) {
        if (reservedMana <= 0 || gameTime > reservedUntil || attackTarget == null
            || attackTarget.getId() != reservedForId || !attackTarget.isAlive()) {
            return 0f;
        }
        if (reservationInputs != null) {
            ManaState mana = new ManaState(MaidMagicManager.getMana(mob), (float) MaidMagicManager.getMaxMana(mob),
                MaidMagicManager.getManaRegenPerInterval(mob));
            if (mana.current < reservedLastMana || attackTarget.getHealth() + attackTarget.getAbsorptionAmount() != reservedAtTargetHp
                || !reservationInputs.matches(mob, mob.getData(WsbAttachments.MAID_SPELL_MEMORY),
                reservationInputs.castable, reservationInputs.pending, mana)) {
                clearReservation();
                return 0f;
            }
        }
        if (mob.getHealth() < mob.getMaxHealth() * RESERVE_MIN_HEALTH_RATIO) {
            return 0f;
        }
        if (mob.getLastHurtByMob() instanceof LivingEntity attacker && attacker != attackTarget) {
            // 近期（原版 100 刻内）被攒蓝目标以外的怪打了：规划只算了怎么尽快打死眼前这一个，
            // 没算别处来的威胁，这时该让防御、位移法术先保命
            return 0f;
        }
        return reservedMana;
    }

    /**
     * 施放这个法术是否会把攒下的蓝花过预留线。攻击类由规划器自己决定，治疗类（自愈、救队友）豁免。
     *
     * @param mob         施法者
     * @param spellData   待检查的法术
     * @param action      法术所属类别
     * @param reservation {@link #activeReservation} 的结果
     * @return 施放后魔力会跌破预留线时为 true
     */
    public static boolean wouldSpendReservedMana(Mob mob, SpellData spellData, MaidSpellAction action,
                                                 float reservation) {
        if (reservation <= 0 || action == MaidSpellAction.ATTACK
            || action == MaidSpellAction.SUPPORT || action == MaidSpellAction.SUPPORT_OTHER) {
            return false;
        }
        // 二段咏唱中途不扣蓝，花不掉预留
        float cost = MaidMagicManager.getEffectiveManaCost(mob, spellData);
        return cost > 0 && MaidMagicManager.getMana(mob) - cost < reservation;
    }

    /**
     * 一发就能打死、且一个周期内伤害就到账的法术：任何别的路线至少也要一个周期，它必然最优，不必填表。 多个都满足时取蓝耗最低的：一样快，省下的蓝留给下一个目标。
     *
     * @param affordableCount 候选表里前多少个现在就能完整放出
     */
    private static SpellData immediateFinisher(List<SpellData> candidates, int affordableCount, Estimates estimates,
                                               float targetHp) {
        int best = -1;
        for (int i = 0; i < affordableCount; i++) {
            if (estimates.finisherDamages[i] >= targetHp && estimates.finishEpochs[i] <= 1
                && (best < 0 || estimates.manaCosts[i] < estimates.manaCosts[best])) {
                best = i;
            }
        }
        return best >= 0 ? candidates.get(best) : null;
    }

    /**
     * 施放第 {@code i} 个法术的击杀时间 {@code t} 是否比当前最优动作更好：明显更快，或者一样快但蓝耗更低。 后者是为了「一发打死」这类结果完全相同的情况——不比蓝耗的话会按候选顺序挑，可能白白烧掉大招。 当前最优是等待（{@code bestAction} 为 -1）时，只有严格更快才算。
     */
    private static boolean isBetter(float t, int i, float best, int bestAction, Estimates estimates) {
        if (bestAction < 0) {
            return t < best;
        }
        if (t < best - TIE_EPSILON) {
            return true;
        }
        return t <= best + TIE_EPSILON && estimates.manaCosts[i] < estimates.manaCosts[bestAction];
    }

    /**
     * 最短击杀时间 DP。
     * <p>
     * <b>已知近似</b>：推演里不跟踪冷却，DP 会以为同一个法术可以连放。每次只执行第一步、下一轮重新规划，
     * 刚放过的法术进了冷却自然不在下一轮的候选里，但推演仍可能低估多次施法的耗时。
     *
     * @return 根状态的最优动作
     */
    private static Decision solve(int n, Estimates estimates, float targetHp, ManaState mana) {
        float bucketSize = Math.max(1f, mana.regen);
        int buckets = Mth.floor(Math.max(0f, mana.max) / bucketSize) + 1;
        if (buckets > MAX_MANA_BUCKETS) {
            buckets = MAX_MANA_BUCKETS;
            bucketSize = mana.max / (buckets - 1);
        }
        int topBucket = buckets - 1;
        // 魔力前进一格要等的周期数；回魔被封时等待没有意义
        float waitEpochsPerBucket = mana.regen > 0 ? bucketSize / mana.regen : Float.POSITIVE_INFINITY;

        float hpStep = Math.max(1f, targetHp / MAX_HP_BUCKETS);
        int hpBuckets = Mth.clamp(Mth.ceil(targetHp / hpStep), 1, MAX_HP_BUCKETS + 1);

        int[] costBuckets = new int[n];
        for (int i = 0; i < n; i++) {
            costBuckets[i] = Mth.ceil(estimates.manaCosts[i] / bucketSize);
            // 伤害拆成整数格 + 小数部分，castTime 里按小数部分在相邻两格之间插值：直接取整的话，
            // 高血量目标面前不到半格的法术会整个消失，刚过半格的又被高估近一倍
            float exact = estimates.damages[i] / hpStep;
            estimates.damageBuckets[i] = Mth.floor(exact);
            estimates.damageFracs[i] = exact - estimates.damageBuckets[i];
        }

        // 复用缓冲区不必清零：第 0 行（目标已死）从不写入、恒为 0，其余用到的格子在读之前都会先写
        DpBuffers buffers = BUFFERS.get();
        int[][] policy = buffers.policy;
        Grid grid = new Grid(buffers.time, bucketSize, topBucket, mana.max, mana.regen, hpStep);
        for (int h = 1; h <= hpBuckets; h++) {
            for (int b = topBucket; b >= 0; b--) {
                float best = b < topBucket ? waitEpochsPerBucket + grid.time[h][b + 1] : Float.POSITIVE_INFINITY;
                int bestAction = -1;
                float bucketMana = b * bucketSize;
                for (int i = 0; i < n; i++) {
                    if (costBuckets[i] > b || estimates.damages[i] <= 0) {
                        continue;
                    }
                    float t = castTime(i, h, bucketMana, estimates, grid);
                    if (isBetter(t, i, best, bestAction, estimates)) {
                        best = t;
                        bestAction = i;
                    }
                }
                grid.time[h][b] = best;
                policy[h][b] = bestAction;
            }
        }

        // 根状态用精确魔力值决策，不查桶化的表：魔力桶向下取整、蓝耗格向上取整，两边一起偏时
        // 会把「刚好放得起」的法术误判成买不起，女仆就会凭空多等一个回魔周期
        float bestCast = Float.POSITIVE_INFINITY;
        int castIndex = -1;
        for (int i = 0; i < n; i++) {
            if (estimates.manaCosts[i] > mana.current || estimates.damages[i] <= 0) {
                continue;
            }
            float t = castTime(i, hpBuckets, mana.current, estimates, grid);
            if (isBetter(t, i, bestCast, castIndex, estimates)) {
                bestCast = t;
                castIndex = i;
            }
        }

        int nextBucket = Math.min(topBucket, Mth.floor(mana.current / bucketSize) + 1);
        float bestWait = Float.POSITIVE_INFINITY;
        if (mana.regen > 0 && nextBucket * bucketSize > mana.current) {
            bestWait = (nextBucket * bucketSize - mana.current) / mana.regen + grid.time[hpBuckets][nextBucket];
        }

        if (castIndex >= 0 && bestCast <= bestWait + CAST_NOW_TIE_EPOCHS) {
            return new Decision(castIndex, -1);
        }
        if (bestWait == Float.POSITIVE_INFINITY) {
            return new Decision(castIndex, -1);
        }
        // 顺着策略表往后推，找出这次攒蓝是为了哪个法术
        int b = nextBucket;
        while (b < topBucket && policy[hpBuckets][b] < 0) {
            b++;
        }
        return new Decision(-1, policy[hpBuckets][b]);
    }

    /**
     * 当前状态下施放第 {@code i} 个法术后，到目标死亡还要多少个周期。
     * <p>
     * 有完整观测时，分别计算各个正伤害结果的后继时间再加权；未命中用几何重试的期望成本摊销。 无分布的旧记忆仍使用原估值，不能凭均值反推出不存在的暴击比例。
     * <p>
     * 伤害的小数格按期望处理：以小数部分为概率多打掉一格，在两个后继状态之间插值。整格为 0 的法术折算成 「连放若干发打掉一格」，魔力撑不住这么多发时视为此路不通。
     */
    private static float castTime(int i, int h, float mana, Estimates estimates, Grid grid) {
        DamageProfile profile = estimates.profiles[i];
        if (profile != null) {
            if (profile.hitProbability <= 0f) return Float.POSITIVE_INFINITY;
            float expected = 0f;
            for (int k = 0; k < profile.damages.length; k++) {
                float damage = profile.damages[k] / grid.hpStep;
                // 每个实测伤害分别进入后继状态；不能将均伤相加当作稳定击杀。
                float time = outcomeTime(i, h, mana, Mth.floor(damage), damage - Mth.floor(damage),
                    1f / profile.hitProbability, estimates, grid);
                expected += profile.probabilities[k] * time;
            }
            return expected;
        }
        int damage = estimates.damageBuckets[i];
        float frac = estimates.damageFracs[i];
        // 探索收益可以影响尝试顺序，但不能把实测不足一发的攻击变成终结状态。
        // 两个法术同时就绪时也必须应用，不能仅靠外层的短冷却补刀检查。
        float finisherBuckets = estimates.finisherDamages[i] / grid.hpStep;
        if (damage + frac >= h && finisherBuckets < h) {
            damage = Mth.floor(finisherBuckets);
            frac = finisherBuckets - damage;
        }
        return outcomeTime(i, h, mana, damage, frac, 1f, estimates, grid);
    }

    private static float outcomeTime(int i, int h, float mana, int damage, float frac, float casts,
                                     Estimates estimates, Grid grid) {
        if (damage == 0) {
            if (frac <= 0) {
                return Float.POSITIVE_INFINITY;
            }
            casts /= frac;
            damage = 1;
            frac = 0f;
        }
        // 后继血量只能在伤害到账后使用；中间一发同样不能在读条结束时提前扣血。
        // 未显式跟踪在途伤害，因此这里保守地串行等待，不虚构慢弹道接瞬发的击杀。
        float attemptEpochs = estimates.finishEpochs[i];
        float epochs = attemptEpochs * casts;
        float afterMana = mana - estimates.manaCosts[i] * casts + grid.regen * (epochs - attemptEpochs);
        float retryWait = 0f;
        if (afterMana < 0) {
            if (grid.regen <= 0) return Float.POSITIVE_INFINITY;
            // 未命中用几何重试的期望次数摊销时间和蓝耗。重试缺蓝需要真实等待，不能免费重试。
            // 这是未命中分支的近似；不把零伤害冒充任何正伤害。
            retryWait = -afterMana / grid.regen;
            afterMana = 0f;
        }
        afterMana = Math.min(grid.maxMana, afterMana + grid.regen * attemptEpochs);
        int afterBucket = Mth.clamp(Mth.floor(afterMana / grid.bucketSize), 0, grid.topBucket);
        // 最后一发打死时要算到伤害真正到账，召唤物、持续伤害可能比读条慢得多
        float killEpochs = retryWait + epochs;
        int remaining = h - damage;
        float t = remaining <= 0 ? killEpochs : retryWait + epochs + grid.time[remaining][afterBucket];
        if (frac > 0 && remaining > 1) {
            // 剩余血量仍为正时不能插值到第 0 行（死亡）。例如 12 HP 吃到 11.2 伤害仍需补刀，
            // 不能凭小数格虚构出 20% 的“一发击杀”，否则会偏爱高耗蓝的大法术。
            float tMore = retryWait + epochs + grid.time[remaining - 1][afterBucket];
            t = (1 - frac) * t + frac * tMore;
        }
        return t;
    }

    /**
     * 估计每个候选法术的实际蓝耗、伤害与耗时。
     * <p>
     * 伤害估计 = 先验与实测按样本数加权平均，再叠一份随观测次数衰减的乐观加成。先验优先取配置里手动给定的值； 否则用「蓝耗 × 每点魔力伤害」，后者取本轮候选里实测样本足够的法术的均值，没有时退回 {@link #DEFAULT_DAMAGE_PER_MANA}——女仆熟悉了几个法术之后，新法术的起点就按她自己的手感来定。
     */
    private static Estimates estimate(Mob mob, MaidSpellMemory memory, List<SpellData> candidates, float maxMana) {
        int n = candidates.size();
        Estimates estimates = new Estimates(n);
        float[] scales = new float[n];
        float[] listPrices = new float[n];
        MaidSpellMemory.SpellStats[] stats = new MaidSpellMemory.SpellStats[n];

        double ratioSum = 0;
        int ratioCount = 0;
        for (int i = 0; i < n; i++) {
            SpellData spellData = candidates.get(i);
            AbstractSpell spell = spellData.getSpell();
            int castTicks = castTicks(mob, spellData);
            boolean continuous = spell.getCastType() == CastType.CONTINUOUS;
            listPrices[i] = listPrice(mob, spellData, castTicks, maxMana);
            // 二段咏唱中途不扣蓝（见 MagicMaidAdapter#castSpell），但先验仍按标价估强弱
            estimates.manaCosts[i] = MaidMagicManager.hasRecast(mob, spellData) ? 0f : listPrices[i];
            estimates.castEpochs[i] = Math.max(1, Mth.ceil((float) castTicks / MaidMagicManager.MANA_REGEN_TICKS));

            scales[i] = MaidSpellMemory.levelScale(spellData, mob);
            stats[i] = planningStats(mob, memory, spell);
            // 实测的到账时间相对法术生效；持续施法起手即生效，读条法术要先等读条。下界记录（samples 为 0）的延迟同样可信
            float delayTicks = stats[i] != null && stats[i].meanDamage() > 0 ? stats[i].meanDelayTicks() : 0f;
            float finishTicks = (continuous ? 0 : castTicks) + delayTicks;
            estimates.finishEpochs[i] = Math.max(estimates.castEpochs[i],
                Mth.ceil(finishTicks / MaidMagicManager.MANA_REGEN_TICKS));

            if (stats[i] != null && stats[i].samples() >= MIN_SAMPLES_FOR_RATIO && stats[i].meanDamage() > 0) {
                ratioSum += stats[i].meanDamage() * scales[i] / listPrices[i];
                ratioCount++;
            }
        }
        float damagePerMana = ratioCount > 0 ? (float) (ratioSum / ratioCount) : DEFAULT_DAMAGE_PER_MANA;

        for (int i = 0; i < n; i++) {
            float configured = MaidSpellCatalog.getSpellValue(candidates.get(i).getSpell());
            float prior = configured > 0 ? configured * scales[i] : listPrices[i] * damagePerMana;
            if (configured <= 0 && candidates.get(i).getSpell().getCastType() == CastType.CONTINUOUS) {
                // 引导的蓝耗按整段累计，但伤害可能受射程、无敌帧和命中时间限制，不能把每次扣蓝
                // 都当作一发独立命中。未知引导只按间隔数的平方根增长；配置先验与实测不打折。
                int intervals = Math.max(1, Mth.ceil((float) castTicks(mob, candidates.get(i))
                    / MaidMagicManager.CONTINUOUS_CAST_TICK_INTERVAL));
                prior /= (float) Math.sqrt(intervals);
            }
            int samples = stats[i] != null ? stats[i].samples() : 0;
            // 探索加成按观测次数衰减，不按封顶的样本数：否则加成永远停在先验的 1/17，贵法术被长期高估
            int casts = stats[i] != null ? stats[i].casts() : 0;
            float observed = samples > 0 ? stats[i].meanDamage() * scales[i] : 0f;
            if (stats[i] != null && samples == 0) {
                // 只有截断施法给出的下界：每次都把目标打死了，真实伤害至少这么多
                prior = Math.max(prior, stats[i].meanDamage() * scales[i]);
            }
            // 完整观测到的零伤害同样是实战收益，不能把连续打空重新解释成“没有信息”。
            float priorWeight = PRIOR_WEIGHT / (1 + casts);
            float mean = (prior * priorWeight + observed * samples) / (priorWeight + samples);
            float explorationBase = samples > 0 ? observed : prior;
            estimates.damages[i] = mean + explorationBase * EXPLORATION_BONUS / ((1f + casts) * (1f + casts));
            // 有实测时补刀用已观测伤害（含可信下界），不让先验平滑把“已知能一发打死”改成两发。
            // 未知法术仍保留探索估值，避免仅因蓝耗高就把先验当成更可靠的一击必杀保证。
            estimates.finisherDamages[i] = stats[i] != null ? stats[i].meanDamage() * scales[i] : estimates.damages[i];
            estimates.profiles[i] = DamageProfile.from(stats[i], scales[i]);
            if (estimates.profiles[i] != null) {
                // 快速补刀必须在已观测的各个结果下都够伤害；均值不能充当一击必杀保证。
                estimates.finisherDamages[i] = estimates.profiles[i].minimumDamage;
            }
        }
        return estimates;
    }

    @Nullable
    private static MaidSpellMemory.SpellStats planningStats(Mob mob, MaidSpellMemory memory, AbstractSpell spell) {
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && safeToConserve(mob, target)
               ? memory.getPlanningStats(spell, mob, target, mob.level().getGameTime()) : memory.getStats(spell);
    }

    private static int castTicks(Mob mob, SpellData spellData) {
        return Math.max(0, spellData.getSpell().getEffectiveCastTime(spellData.getLevel(), mob));
    }

    /**
     * 法术一次完整施放的标价。持续施法每个间隔都扣一次蓝，整段引导的总价才是这一发的真实成本 （超过魔力上限时按上限算：攒满蓝就是能引导的最长一段）。
     */
    private static float listPrice(Mob mob, SpellData spellData, int castTicks, float maxMana) {
        AbstractSpell spell = spellData.getSpell();
        float cost = Math.max(1, ((MaidMagicEntity) mob).winefoxsSpellbooks$getManaCost(spell, spellData.getLevel()));
        if (spell.getCastType() == CastType.CONTINUOUS) {
            cost = MaidMagicManager.fullChannelManaCost(cost, castTicks, maxMana);
        }
        return cost;
    }

    /**
     * 没有目标或无路可走时的兜底：在前 {@code usableCount} 个（起得了手的）候选里取估计伤害最高的。
     */
    private static SpellData bestByDamage(List<SpellData> candidates, int usableCount, Estimates estimates) {
        SpellData best = null;
        float bestDamage = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < usableCount; i++) {
            if (estimates.damages[i] > bestDamage) {
                bestDamage = estimates.damages[i];
                best = candidates.get(i);
            }
        }
        return best;
    }

    private static SpellData bestByEfficiency(List<SpellData> candidates, int usableCount, Estimates estimates) {
        int best = -1;
        for (int i = 0; i < usableCount; i++) {
            if (best < 0 || estimates.damages[i] / Math.max(1f, estimates.manaCosts[i])
                > estimates.damages[best] / Math.max(1f, estimates.manaCosts[best])) {
                best = i;
            }
        }
        return best >= 0 ? candidates.get(best) : null;
    }

    /**
     * 本轮规划用到的魔力状况，在 {@link #plan} 开头读一次
     */
    private record ManaState(float current, float max, float regen) {
    }

    private record SpellInput(AbstractSpell spell, int level, float price, boolean recast, int castTicks,
                              int cooldown, float scale, float power, float configured,
                              MaidSpellMemory.SpellStats stats) {
    }

    /**
     * Only retained for an active mana-saving decision; holds no entity references.
     */
    private static final class ReservationInputs {
        private final List<SpellData> castable;

        private final List<SpellData> pending;

        private final List<SpellInput> castableValues;

        private final List<SpellInput> pendingValues;

        private final float maxMana;

        private final float regen;

        private ReservationInputs(Mob mob, MaidSpellMemory memory, List<SpellData> castable,
                                  List<SpellData> pending, ManaState mana) {
            this.castable = List.copyOf(castable);
            this.pending = List.copyOf(pending);
            maxMana = mana.max;
            regen = mana.regen;
            castableValues = inputs(mob, memory, castable, maxMana);
            pendingValues = inputs(mob, memory, pending, maxMana);
        }

        private boolean matches(Mob mob, MaidSpellMemory memory, List<SpellData> castable,
                                List<SpellData> pending, ManaState mana) {
            return mana.max == maxMana && mana.regen == regen
                && castableValues.equals(inputs(mob, memory, castable, maxMana))
                && pendingValues.equals(inputs(mob, memory, pending, maxMana));
        }

        private static List<SpellInput> inputs(Mob mob, MaidSpellMemory memory, List<SpellData> spells, float maxMana) {
            List<SpellInput> values = new ArrayList<>(spells.size());
            for (SpellData data : spells) {
                AbstractSpell spell = data.getSpell();
                int duration = castTicks(mob, data);
                values.add(new SpellInput(spell, data.getLevel(), listPrice(mob, data, duration, maxMana),
                    MaidMagicManager.hasRecast(mob, data), duration,
                    MaidMagicManager.getEffectiveSpellCooldown(spell, mob, CastSource.MOB),
                    MaidSpellMemory.levelScale(data, mob), spell.getSpellPower(data.getLevel(), mob),
                    MaidSpellCatalog.getSpellValue(spell), planningStats(mob, memory, spell)));
            }
            return values;
        }
    }

    /**
     * 一次 DP 求解里不变的表与离散化参数
     */
    private record Grid(float[][] time, float bucketSize, int topBucket, float maxMana, float regen, float hpStep) {
    }

    /**
     * @param castIndex      立刻施放的候选下标，-1 表示等待
     * @param savingForIndex 等待时是在为哪个候选攒蓝，-1 表示不适用
     */
    private record Decision(int castIndex, int savingForIndex) {
    }

    private static final class DpBuffers {
        private final float[][] time = new float[MAX_HP_BUCKETS + 2][MAX_MANA_BUCKETS];

        private final int[][] policy = new int[MAX_HP_BUCKETS + 2][MAX_MANA_BUCKETS];
    }

    private static final class Estimates {
        private final float[] manaCosts;

        private final float[] damages;

        private final float[] finisherDamages;

        private final DamageProfile[] profiles;

        private final int[] castEpochs;

        private final int[] finishEpochs;

        /**
         * 伤害折成血量格后的整数部分与小数部分，由 {@link #solve} 按当前目标血量填写
         */
        private final int[] damageBuckets;

        private final float[] damageFracs;

        private Estimates(int n) {
            manaCosts = new float[n];
            damages = new float[n];
            finisherDamages = new float[n];
            profiles = new DamageProfile[n];
            castEpochs = new int[n];
            finishEpochs = new int[n];
            damageBuckets = new int[n];
            damageFracs = new float[n];
        }
    }

    /**
     * 16 次完整观测（必要时加一次正下界退路），合并相同伤害；未命中单独摊销重试成本。
     */
    private record DamageProfile(float[] damages, float[] probabilities, float hitProbability, float minimumDamage) {
        @Nullable
        static DamageProfile from(@Nullable MaidSpellMemory.SpellStats stats, float scale) {
            if (stats == null || stats.samples() <= 0 || stats.damageSamples().isEmpty()) return null;
            List<Float> observations = stats.damageSamples();
            if (stats.censoredDamage() > 0f && observations.stream().noneMatch(damage -> damage > 0f)) {
                // 完整记录只有未命中，但后来确实打出正伤害：以一次下界提供保守退路，
                // 不把它并入完整均值／样本；下一次完整观测会清除该证据。
                observations = new ArrayList<>(observations);
                observations.add(stats.censoredDamage());
            }
            Map<Float, Integer> counts = new LinkedHashMap<>();
            int hits = 0;
            float minimum = Float.POSITIVE_INFINITY;
            for (float sample : observations) {
                minimum = Math.min(minimum, sample * scale);
                if (sample > 0) {
                    counts.merge(sample * scale, 1, Integer::sum);
                    hits++;
                }
            }
            float[] damages = new float[counts.size()], probabilities = new float[counts.size()];
            int index = 0;
            for (var entry : counts.entrySet()) {
                damages[index] = entry.getKey();
                probabilities[index++] = (float) entry.getValue() / hits;
            }
            return new DamageProfile(damages, probabilities, (float) hits / observations.size(), minimum);
        }
    }
}
