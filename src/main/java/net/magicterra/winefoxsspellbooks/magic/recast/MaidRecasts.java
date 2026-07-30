package net.magicterra.winefoxsspellbooks.magic.recast;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * 女仆版的 recast（二段咏唱）管理器。
 * <p>
 * 与玩家版 {@link PlayerRecasts} 的两点差别：
 * <ul>
 *   <li><b>不同步给客户端</b>——女仆的 recast 没有 UI，三个 {@code sync*} 方法全部留空；</li>
 *   <li><b>收尾动作要自己实现</b>——Iron's 把收尾写在玩家路径上（依赖 {@code ServerPlayer}），
 *       女仆走不到，故派发到 {@link RecastHandlers}。</li>
 * </ul>
 * <p>
 * <b>本类只负责生命周期</b>（计时、超时清理、派发），具体某个法术结束时做什么在
 * {@code magic/recast/} 下按来源分文件：{@link IronsCoreRecasts} 管本体，{@code addon/} 下按模组一类。
 * <p>
 * 用反射读写 {@link PlayerRecasts#recastLookup} 与 {@link RecastInstance#remainingTicks} 是无奈之举：
 * Iron's 把它们设成 private 且没留 protected 访问器，而女仆需要按自己的节奏推进计时。
 * 字段句柄在类加载时解析一次并缓存，不是每次调用都查。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 18:26
 */
public class MaidRecasts extends PlayerRecasts {
    private static final Field RECAST_LOOKUP_FIELD =
        FieldUtils.getDeclaredField(PlayerRecasts.class, "recastLookup", true);

    private static final Field REMAINING_TICKS_FIELD =
        FieldUtils.getDeclaredField(RecastInstance.class, "remainingTicks", true);

    protected final LivingEntity maid;

    /**
     * {@link PlayerRecasts#recastLookup} 的引用，构造时反射取一次。
     * <p>
     * 该字段在父类里是 final、实例存活期间不换引用，故构造时反射取一次即可。
     */
    private final Map<String, RecastInstance> recastLookup;

    public MaidRecasts(LivingEntity maid) {
        this.maid = maid;
        this.recastLookup = readRecastLookup();
    }

    // 玩家不可见 Recast
    @Override
    public void syncAllToPlayer() {
    }

    @Override
    public void syncToPlayer(RecastInstance recastInstance) {
    }

    @Override
    public void syncRemoveToPlayer(String spellId) {
    }

    @Override
    public void removeRecast(RecastInstance recastInstance, RecastResult recastResult) {
        recastLookup.remove(recastInstance.getSpellId());
        triggerRecastComplete(recastInstance, recastResult);
    }

    private void triggerRecastComplete(RecastInstance recastInstance, RecastResult recastResult) {
        AbstractSpell spell = SpellRegistry.getSpell(recastInstance.getSpellId());
        if (!(maid.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        RecastHandlers.dispatch(new RecastContext(
            maid, serverLevel, spell, recastInstance, recastResult, recastInstance.getCastData()));
    }

    /**
     * 推进所有 recast 的计时，到期或召唤物已全灭的直接结算。
     *
     * @param actualTicks 本次推进的 tick 数；同时也是调用间隔（每 {@code actualTicks} tick 才真正走一次）
     */
    @Override
    public void tick(int actualTicks) {
        if (maid == null || maid.level().getGameTime() % actualTicks != 0) {
            return;
        }
        if (!(maid.level() instanceof ServerLevel level)) {
            return;
        }

        // 先收集再移除：removeRecast 会改动 recastLookup，边遍历边删会 ConcurrentModificationException
        List<RecastInstance> expired = new ArrayList<>();
        for (RecastInstance instance : recastLookup.values()) {
            setRemainingTicks(instance, instance.getTicksRemaining() - actualTicks);
            if (instance.getTicksRemaining() <= 0 || summonsAllGone(level, instance.getCastData())) {
                expired.add(instance);
            }
        }
        expired.forEach(instance -> removeRecast(instance, RecastResult.TIMEOUT));
    }

    /**
     * 召唤类 recast 的额外结束条件：召唤物一个不剩时立刻结算，不必等计时走完 （否则「二段咏唱收回召唤物」会挂在一个空壳上）。
     */
    private static boolean summonsAllGone(ServerLevel level, ICastDataSerializable castData) {
        if (!(castData instanceof SummonedEntitiesCastData summonData)) {
            return false;
        }
        for (var uuid : summonData.getSummons()) {
            if (level.getEntity(uuid) != null) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, RecastInstance> readRecastLookup() {
        try {
            return (Map<String, RecastInstance>) FieldUtils.readField(RECAST_LOOKUP_FIELD, this);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Iron's Spellbooks changed PlayerRecasts#recastLookup", e);
        }
    }

    private static void setRemainingTicks(RecastInstance recastInstance, int remainingTicks) {
        try {
            FieldUtils.writeField(REMAINING_TICKS_FIELD, recastInstance, remainingTicks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Iron's Spellbooks changed RecastInstance#remainingTicks", e);
        }
    }
}
