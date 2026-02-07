package net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon;

import java.util.Map;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * 扫帚女仆下车检测行为
 * <p>
 * 检测扫帚状态，必要时触发下车并切换为行走模式。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-11
 */
public class SummonedMaidDismountTask extends Behavior<SummonedEntityMaid> {

    public SummonedMaidDismountTask() {
        super(Map.of(
            MaidCastingMemoryModuleTypes.BROOM_ENTITY.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, SummonedEntityMaid maid) {
        return maid.isAirForce();
    }

    @Override
    protected void tick(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        // 检查扫帚是否有效
        if (!maid.isPassenger()) {
            // 女仆不再骑乘，切换为行走
            maid.switchToGroundMode();
            return;
        }

        if (!(maid.getVehicle() instanceof SummonedMaidBroom broom)) {
            // 骑乘的不是扫帚，跳过检查
            return;
        }

        // 检查扫帚是否被销毁或无效
        if (!broom.isAlive() || broom.isRemoved()) {
            maid.stopRiding();
            maid.switchToGroundMode();
            return;
        }

        // 检查扫帚是否接触流体（扫帚会自行销毁，这里做双重检查）
        if (broom.isInFluidType()) {
            maid.stopRiding();
            maid.switchToGroundMode();
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, SummonedEntityMaid maid, long gameTime) {
        return maid.isAirForce();
    }
}
