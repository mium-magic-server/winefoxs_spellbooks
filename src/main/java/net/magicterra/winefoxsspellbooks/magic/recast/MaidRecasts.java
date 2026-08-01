package net.magicterra.winefoxsspellbooks.magic.recast;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import net.magicterra.winefoxsspellbooks.mixin.PlayerRecastsAccessor;
import net.magicterra.winefoxsspellbooks.mixin.RecastInstanceAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * 女仆版的 recast（二段咏唱）管理器。
 * <p>
 * 与玩家版 {@link PlayerRecasts} 的三点差别：
 * <ul>
 *   <li><b>不同步给客户端</b>——女仆的 recast 没有 UI，三个 {@code sync*} 方法全部留空；</li>
 *   <li><b>收尾动作要自己实现</b>——Iron's 把收尾写在玩家路径上（依赖 {@code ServerPlayer}），
 *       女仆走不到，故派发到 {@link RecastHandlers}；</li>
 *   <li><b>计时自己推</b>——父类的 {@link #tick} 只认 {@code ServerPlayer}，女仆得自己扣
 *       {@code remainingTicks}，靠 {@link RecastInstanceAccessor} 就地改字段。</li>
 * </ul>
 * <p>
 * 其余记账（入库、计次、序列化）刻意继续走父类，跟着 Iron's 一起变。
 * <p>
 * <b>本类只负责生命周期</b>（计时、超时清理、派发），具体某个法术结束时做什么在
 * {@code magic/recast/} 下按来源分文件：{@link IronsCoreRecasts} 管本体，{@code addon/} 下按模组一类。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 18:26
 */
public class MaidRecasts extends PlayerRecasts {
    protected final LivingEntity maid;

    public MaidRecasts(LivingEntity maid) {
        this.maid = maid;
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

    /**
     * 摘表 + 派发收尾。
     * <p>
     * 不能用父类的 {@code removeRecast(String)} 摘表：它带 {@code @OnlyIn(Dist.CLIENT)}，专服上会被剥掉。
     */
    @Override
    public void removeRecast(RecastInstance recastInstance, RecastResult recastResult) {
        ((PlayerRecastsAccessor) this).getRecastLookup().remove(recastInstance.getSpellId());
        triggerRecastComplete(recastInstance, recastResult);
    }

    /**
     * 清空所有 recast。
     * <p>
     * 必须重写：父类的实现走的是自己那个私有三参 {@code removeRecast}，绕过本类的收尾派发——冷却挂不上、
     * 召唤类 recast 不收召唤物，还会以 {@code serverPlayer == null} 调 {@code spell.onRecastFinished}，
     * 附属里直接解引用它的实现会在服务端 tick 里抛 NPE。
     */
    @Override
    public void removeAll(RecastResult recastResult) {
        // getAllRecasts 返回的已是快照，可以边遍历边让 removeRecast 改动底表
        getAllRecasts().forEach(instance -> removeRecast(instance, recastResult));
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
        if (maid == null || !hasRecastsActive()) {
            return;
        }
        if (!(maid.level() instanceof ServerLevel level) || level.getGameTime() % actualTicks != 0) {
            return;
        }

        // getAllRecasts 返回的已是快照，可以边遍历边让 removeRecast 改动底表
        for (RecastInstance instance : getAllRecasts()) {
            int remaining = instance.getTicksRemaining() - actualTicks;
            ((RecastInstanceAccessor) instance).setRemainingTicks(remaining);
            if (remaining <= 0 || summonsAllGone(level, instance.getCastData())) {
                removeRecast(instance, RecastResult.TIMEOUT);
            }
        }
    }

    /**
     * 召唤类 recast 的额外结束条件：召唤物一个不剩时立刻结算，不必等计时走完（否则「二段咏唱收回召唤物」会挂在一个空壳上）。
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
}
