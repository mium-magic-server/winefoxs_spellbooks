package net.magicterra.winefoxsspellbooks.entity.ai.goal;

import net.magicterra.winefoxsspellbooks.entity.SummonedMaidPresence;
import net.magicterra.winefoxsspellbooks.entity.summon.SummonedEntityMaid;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;

/**
 * 「躲开召唤女仆」目标：苦力怕见到召唤女仆会退避。
 * <p>
 * 与直接用 {@link AvoidEntityGoal} 的区别只有 {@link #canUse()} 前的一道
 * {@link SummonedMaidPresence} 闸门。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public class AvoidSummonedMaidGoal extends AvoidEntityGoal<SummonedEntityMaid> {

    public AvoidSummonedMaidGoal(PathfinderMob mob, float maxDistance, double walkSpeed, double sprintSpeed) {
        super(mob, SummonedEntityMaid.class, maxDistance, walkSpeed, sprintSpeed);
    }

    @Override
    public boolean canUse() {
        // 本维度没有召唤女仆时直接否决，省掉父类的实体查询
        if (!SummonedMaidPresence.isPresentIn(this.mob.level())) {
            return false;
        }
        return super.canUse();
    }
}
