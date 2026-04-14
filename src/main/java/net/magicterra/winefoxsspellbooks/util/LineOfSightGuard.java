package net.magicterra.winefoxsspellbooks.util;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;

/**
 * 视线检查的安全包装。
 * 
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-04-14
 */
public final class LineOfSightGuard {
    private static final double MAX_LOS_RANGE_SQR = 128.0 * 128.0;

    private LineOfSightGuard() {
    }

    /**
     * 安全的视线检查。在调用 vanilla {@link LivingEntity#hasLineOfSight} 之前
     * 检查 chunk 加载状态，未加载时直接返回 false。
     */
    public static boolean hasLineOfSight(LivingEntity self, Entity target) {
        if (!isSafe(self, target)) {
            return false;
        }
        return self.hasLineOfSight(target);
    }

    /**
     * 安全的 {@link TargetingConditions#test} 包装。
     * 当目标处于未加载 chunk 时直接返回 false，避免内部 LOS 检查触发死锁。
     */
    public static boolean test(TargetingConditions conditions, LivingEntity self, LivingEntity target) {
        if (!isSafe(self, target)) {
            return false;
        }
        return conditions.test(self, target);
    }

    /**
     * 校验射线两端是否安全：同维度、距离 ≤ 128 格、两端 chunk 均已加载。
     */
    public static boolean isSafe(LivingEntity self, Entity target) {
        if (target == null || self == null) {
            return false;
        }
        if (target.level() != self.level()) {
            return false;
        }
        if (target.distanceToSqr(self) > MAX_LOS_RANGE_SQR) {
            return false;
        }
        Level level = self.level();
        int x1 = SectionPos.blockToSectionCoord(self.getBlockX());
        int z1 = SectionPos.blockToSectionCoord(self.getBlockZ());
        int x2 = SectionPos.blockToSectionCoord(target.getBlockX());
        int z2 = SectionPos.blockToSectionCoord(target.getBlockZ());
        // Bresenham 在 chunk 网格上扫描射线经过的每一个 chunk，任一未加载即视为看不到，
        // 避免 Level.clip 沿射线逐方块 getBlockState 时撞上未加载 chunk 触发 getChunkBlocking。
        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;
        int x = x1;
        int z = z1;
        while (true) {
            if (!level.hasChunk(x, z)) {
                return false;
            }
            if (x == x2 && z == z2) {
                return true;
            }
            int e2 = err * 2;
            if (e2 > -dz) {
                err -= dz;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                z += sz;
            }
        }
    }
}
