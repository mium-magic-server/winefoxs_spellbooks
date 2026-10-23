package net.magicterra.winefoxsspellbooks.magic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 把没有攻击者的状态伤害归到施加该状态的施法上。
 * <p>
 * 燃烧、冰冻、中毒、凋零的伤害源都不带攻击者，{@link MaidSpellMemory#onDamage} 无从归属：暴风雪只叠冰冻刻、 伤害全来自原版冰冻，会被学成零伤害；火焰弹、火球的燃烧与毒箭的中毒也会被低估。
 * <p>
 * 这里只记录「谁施加了状态」：燃烧与冰冻在目标的燃烧刻／冰冻刻上升的那一刻，找本刻直接命中目标、 或载体正接触目标的观测中施法；中毒与凋零看效果的来源实体是否为某次施法的载体。候选恰好一个才认定， 多个或没有（阳光、细雪等环境来源）都清除归属——无法证明属于哪一次就不记，与载体归因的原则一致。 之后同类状态伤害只在该施法的观测窗内计入。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-23
 */
public final class StatusDamageTracker {
    private StatusDamageTracker() {
    }

    private enum Status {FIRE, FREEZE, POISON, WITHER}

    private record Owner(MaidSpellMemory memory, MaidSpellMemory.CastRecord record) {
    }

    private static final class Watch {
        final Set<MaidSpellMemory> memories = Collections.newSetFromMap(new WeakHashMap<>());

        final Map<Status, Owner> owners = new EnumMap<>(Status.class);

        int fireTicks;

        int frozenTicks;

        Watch(LivingEntity victim) {
            fireTicks = victim.getRemainingFireTicks();
            frozenTicks = victim.getTicksFrozen();
        }
    }

    private static final Map<LivingEntity, Watch> WATCHED = new WeakHashMap<>();

    /**
     * 开观测窗时登记目标；只在服务端调用。
     */
    static void watch(LivingEntity victim, MaidSpellMemory memory) {
        if (victim.level().isClientSide) return;
        WATCHED.computeIfAbsent(victim, Watch::new).memories.add(memory);
    }

    /**
     * 每个服务器刻结束时调用：检测燃烧、冰冻的施加，并清理已无观测中施法的目标。
     */
    public static void tick() {
        for (Iterator<Map.Entry<LivingEntity, Watch>> it = WATCHED.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<LivingEntity, Watch> entry = it.next();
            LivingEntity victim = entry.getKey();
            Watch watch = entry.getValue();
            if (victim.isRemoved() || !hasOpenCast(victim, watch)) {
                it.remove();
                continue;
            }
            long gameTime = victim.level().getGameTime();
            int fire = victim.getRemainingFireTicks();
            if (fire > 0 && fire > watch.fireTicks) assign(victim, watch, Status.FIRE, onsetOwner(victim, watch, gameTime));
            watch.fireTicks = fire;
            int frozen = victim.getTicksFrozen();
            if (frozen > watch.frozenTicks) assign(victim, watch, Status.FREEZE, onsetOwner(victim, watch, gameTime));
            watch.frozenTicks = frozen;
        }
    }

    /**
     * 服务器停止时调用。弱引用键在这里不起作用：值里的 {@link Owner} 经观测记录强引用着目标本身， 而停服后 {@link #tick} 不再执行、施法者也不再关窗，表项会把整个旧世界留在内存里。
     */
    public static void clear() {
        WATCHED.clear();
    }

    /**
     * 状态效果加到观测目标身上时调用；来源实体须是某次观测中施法的载体。
     */
    public static void onEffectAdded(LivingEntity victim, Holder<MobEffect> effect, @Nullable Entity source) {
        Status status = effect.is(MobEffects.POISON) ? Status.POISON : effect.is(MobEffects.WITHER) ? Status.WITHER : null;
        Watch watch = WATCHED.get(victim);
        if (status == null || watch == null) return;
        Owner owner = null;
        for (Owner candidate : openCasts(victim, watch)) {
            if (source == null || !candidate.record().isSource(source)) continue;
            if (owner != null) {
                owner = null;
                break;
            }
            owner = candidate;
        }
        assign(victim, watch, status, owner);
    }

    /**
     * 没有攻击者的伤害到达时调用。
     *
     * @return 是否已计入某次施法
     */
    public static boolean onUnattributedDamage(LivingEntity victim, DamageSource source, float amount, long gameTime) {
        Watch watch = WATCHED.get(victim);
        if (watch == null) return false;
        Status status = source.is(DamageTypes.ON_FIRE) ? Status.FIRE
                                                       : source.is(DamageTypes.FREEZE) ? Status.FREEZE
                                                                                       : source.is(DamageTypes.WITHER) ? Status.WITHER
                                                                                                                       : source.is(DamageTypes.MAGIC) && victim.hasEffect(MobEffects.POISON) ? Status.POISON : null;
        Owner owner = status == null ? null : watch.owners.get(status);
        return owner != null && owner.memory().creditStatusDamage(owner.record(), victim, amount, gameTime);
    }

    @Nullable
    private static Owner onsetOwner(LivingEntity victim, Watch watch, long gameTime) {
        Owner owner = null;
        for (Owner candidate : openCasts(victim, watch)) {
            MaidSpellMemory.CastRecord record = candidate.record();
            if (!record.hitAt(gameTime) && !record.carrierTouches(victim)) continue;
            if (owner != null) return null;
            owner = candidate;
        }
        return owner;
    }

    private static void assign(LivingEntity victim, Watch watch, Status status, @Nullable Owner owner) {
        if (owner == null) {
            watch.owners.remove(status);
        } else {
            watch.owners.put(status, owner);
        }
    }

    private static boolean hasOpenCast(LivingEntity victim, Watch watch) {
        for (MaidSpellMemory memory : watch.memories) {
            if (memory.hasOpenCastOn(victim)) return true;
        }
        return false;
    }

    private static List<Owner> openCasts(LivingEntity victim, Watch watch) {
        List<Owner> result = new ArrayList<>();
        for (MaidSpellMemory memory : watch.memories) {
            for (MaidSpellMemory.CastRecord record : memory.openCastsOn(victim)) result.add(new Owner(memory, record));
        }
        return result;
    }
}
