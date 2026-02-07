package net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.ShufflingList;

/**
 * 召唤女仆随机行为选择器
 * <p>
 * 带条件的随机行为选择器，仅在条件满足时执行。
 * 由于 GateBehavior.tryStart 是 final 方法，使用组合模式实现。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-10
 */
public class SummonedMaidRunOne extends Behavior<SummonedEntityMaid> {

    private final ShufflingList<BehaviorControl<? super SummonedEntityMaid>> behaviors;
    private final Predicate<SummonedEntityMaid> enableCondition;

    public SummonedMaidRunOne(List<Pair<? extends BehaviorControl<? super SummonedEntityMaid>, Integer>> behaviors,
                              Predicate<SummonedEntityMaid> enableCondition) {
        super(ImmutableMap.of());
        this.behaviors = new ShufflingList<>();
        for (Pair<? extends BehaviorControl<? super SummonedEntityMaid>, Integer> pair : behaviors) {
            this.behaviors.add(pair.getFirst(), pair.getSecond());
        }
        this.enableCondition = enableCondition;
    }

    public SummonedMaidRunOne(List<Pair<? extends BehaviorControl<? super SummonedEntityMaid>, Integer>> behaviors) {
        this(behaviors, maid -> true);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, SummonedEntityMaid maid) {
        // 与原版 MaidRunOne 保持一致的条件检查
        // 召唤女仆不会乞讨和睡觉，但保留条件检查以保持一致性
        return enableCondition.test(maid) && !maid.isBegging() && !maid.isSleeping();
    }

    @Override
    protected void start(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        this.behaviors.shuffle();
        for (BehaviorControl<? super SummonedEntityMaid> behavior : this.behaviors) {
            if (behavior.tryStart(level, maid, gameTime)) {
                break;
            }
        }
    }

    @Override
    protected void tick(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        for (BehaviorControl<? super SummonedEntityMaid> behavior : this.behaviors) {
            if (behavior.getStatus() == Status.RUNNING) {
                behavior.tickOrStop(level, maid, gameTime);
                break;
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        for (BehaviorControl<? super SummonedEntityMaid> behavior : this.behaviors) {
            if (behavior.getStatus() == Status.RUNNING) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void stop(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        for (BehaviorControl<? super SummonedEntityMaid> behavior : this.behaviors) {
            if (behavior.getStatus() == Status.RUNNING) {
                behavior.doStop(level, maid, gameTime);
            }
        }
    }
}
