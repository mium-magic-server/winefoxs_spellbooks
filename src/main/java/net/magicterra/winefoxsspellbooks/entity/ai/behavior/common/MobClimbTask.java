package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * 召唤女仆攀爬行为

 * <p>
 * 复制过来的: {@link com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidClimbTask}
 * <p>
 * 简化版的攀爬行为，适用于 PathfinderMob。
 *
 * @author tartaric_acid, Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-10
 */
public class MobClimbTask extends Behavior<PathfinderMob> {

    public MobClimbTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        // 初始化动量，将实体定格在方块中心，避免爬楼梯过程中摔死
        BlockPos currentPosition = mob.blockPosition().mutable();
        Vec3 centerPos = Vec3.atCenterOf(currentPosition);
        mob.moveTo(centerPos.x, currentPosition.getY(), centerPos.z);
        mob.setDeltaMovement(0, mob.getDeltaMovement().y(), 0);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
        return mob.onClimbable();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob mob, long gameTime) {
        return this.checkExtraStartConditions(level, mob);
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob mob, long gameTime) {
        Path path = mob.getNavigation().getPath();
        if (path == null || path.isDone()) {
            return;
        }

        // 获取基础信息
        int beGoNodeIndex = path.getNextNodeIndex();
        Node beGoNode = path.getNode(beGoNodeIndex);
        BlockPos mobFeetPos = mob.blockPosition();
        BlockState feetBlock = level.getBlockState(mobFeetPos);

        // 判断上行还是下行
        boolean up = true;
        if (beGoNodeIndex > 0) {
            Node currentNext = path.getNode(beGoNodeIndex - 1);
            Node pointNext = path.getNode(beGoNodeIndex);
            if (pointNext.y <= currentNext.y) {
                up = false;
            }
        }

        // 如果是下行，添加 shift 行为
        mob.setShiftKeyDown(!up);

        // 控制上行和下行楼梯的动量
        // 先给一个大点的 y 轴向量，再拉回一点，这样能连续下去
        // 原版的爬楼梯数值为 0.15，有些慢，加快点
        if (mobFeetPos.getY() <= beGoNode.y && up && feetBlock.isLadder(level, mobFeetPos, mob)) {
            mob.setDeltaMovement(0, 1, 0);
            mob.setDeltaMovement(0, 0.25, 0);
        } else {
            mob.setDeltaMovement(0, -1, 0);
            mob.setDeltaMovement(0, -0.25, 0);
        }

        // 对下行做出额外处理
        if (!up && beGoNode.y != mobFeetPos.getY()) {
            int nodeCount = path.getNodeCount();
            for (int i = 0; i < nodeCount; i++) {
                Node node = path.getNode(i);
                Node nextNode = path.getNode(Math.min(i + 1, nodeCount - 1));
                if (node.y == mobFeetPos.getY() && node.x == mobFeetPos.getX()
                    && node.z == mobFeetPos.getZ() && node.y == nextNode.y) {
                    beGoNodeIndex = i;
                    beGoNode = node;
                    path.setNextNodeIndex(i);
                    break;
                }
            }
        }

        // 控制到达楼梯节点顶部或底部向着平台进发
        if ((beGoNode.y - mobFeetPos.getY() >= 0 && beGoNode.y - mobFeetPos.getY() <= 1.2)
            && beGoNodeIndex + 1 < path.getNodeCount()) {
            Node currentNext = path.getNode(beGoNodeIndex);
            Node pointNext = path.getNode(beGoNodeIndex + 1);

            boolean beWalkSurface = pointNext.y == currentNext.y;
            if (beWalkSurface || pointNext == path.getEndNode() || mobFeetPos.getY() == currentNext.y) {
                // 给予当前坐标与水平节点的x、z方向的差值向量，
                // 让其向着那个水平节点进发，脱离楼梯等可爬行物体
                int x1 = pointNext.x - currentNext.x;
                int z1 = pointNext.z - currentNext.z;
                double y = mob.getDeltaMovement().y();
                mob.setDeltaMovement(0.2, 1, 0.2);
                mob.setDeltaMovement(x1 * 0.3, y + 0.012, z1 * 0.3);
                mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pointNext.asVec3()));
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob mob, long gameTime) {
        mob.setShiftKeyDown(false);
    }
}
