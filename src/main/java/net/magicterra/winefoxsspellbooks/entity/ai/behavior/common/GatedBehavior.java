package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

/**
 * 给现成的行为加一道开关：条件不满足时不启动，运行中条件失效则停下
 * <p>
 * 用于在不改写别家行为的前提下限定其生效场合，例如万法皆通任务里的接近行为只在本模组没有选中法术时运行。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class GatedBehavior<E extends LivingEntity> implements BehaviorControl<E> {
    private final BehaviorControl<? super E> delegate;
    private final Predicate<? super E> gate;

    public GatedBehavior(BehaviorControl<? super E> delegate, Predicate<? super E> gate) {
        this.delegate = delegate;
        this.gate = gate;
    }

    @Override
    public Behavior.Status getStatus() {
        return delegate.getStatus();
    }

    @Override
    public boolean tryStart(ServerLevel level, E entity, long gameTime) {
        return gate.test(entity) && delegate.tryStart(level, entity, gameTime);
    }

    @Override
    public void tickOrStop(ServerLevel level, E entity, long gameTime) {
        if (!gate.test(entity)) {
            doStop(level, entity, gameTime);
            return;
        }
        delegate.tickOrStop(level, entity, gameTime);
    }

    @Override
    public void doStop(ServerLevel level, E entity, long gameTime) {
        if (delegate.getStatus() == Behavior.Status.RUNNING) {
            delegate.doStop(level, entity, gameTime);
        }
    }

    @Override
    public String debugString() {
        return "Gated(" + delegate.debugString() + ")";
    }
}
