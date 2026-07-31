package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.magicterra.winefoxsspellbooks.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

/**
 * 女仆战斗中使用药水
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-02 19:43
 */
public class DrinkPotionsTask extends Behavior<Mob> {
    private final float fleeSpeed;

    private final int drinkCooldown;

    private int lastDrinkTick = 0;

    public DrinkPotionsTask(float fleeSpeed, int drinkCooldown) {
        super(ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT // 只在攻击时使用，避免索敌时候消耗太多药水
        ), 1200);

        this.fleeSpeed = fleeSpeed;
        this.drinkCooldown = drinkCooldown;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
        if (!(owner instanceof IMagicEntity magicEntity)) {
            return false;
        }
        if (!owner.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            // 非战斗状态
            return false;
        }
        if (!Config.getDrinkPotionInBattle()) {
            // 配置关闭使用药水
            return false;
        }
        if (magicEntity.isCasting()) {
            // 正在施法
            return false;
        }
        if (magicEntity.isDrinkingPotion()) {
            // 正在喝药
            return false;
        }
        int tickPast = level.getServer().getTickCount() - lastDrinkTick;
        if (tickPast <= drinkCooldown) {
            // 冷却中
            return false;
        }
        float maxHealth = owner.getMaxHealth();
        float health = owner.getHealth();
        float healthThreshold = (float) (maxHealth * 0.6);
        // 小于 60% 血量启动喝药行为
        return health < healthThreshold;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        IMagicEntity magicEntity = (IMagicEntity) entity;
        return magicEntity.isDrinkingPotion();
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) {
            return;
        }
        IMagicEntity magicEntity = (IMagicEntity) entity;
        magicEntity.startDrinkingPotion();

        setFleeWalkTarget(entity, target);
    }

    /**
     * 从自己脚下沿「目标 → 自己」方向后撤 10 格。
     * <p>
     * 撤离方向要用两者的相对位置算，不能用目标的朝向取反：交战中的目标正盯着自己，反向即背离自己，
     * 落点会落到目标的另一侧，喝药时反而径直穿过它的近战范围。
     */
    private void setFleeWalkTarget(Mob mob, LivingEntity target) {
        // 只取水平分量，高度差交给寻路；完全重叠时退回自身朝向，避免归一化出 NaN
        Vec3 away = mob.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
        Vec3 fleeVector = (away.lengthSqr() < 1.0E-4 ? mob.getForward() : away).normalize().scale(10);
        BlockPos fleePos = mob.blockPosition().offset(Mth.floor(fleeVector.x), 0, Mth.floor(fleeVector.z));
        mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(fleePos, fleeSpeed, 0));
    }

    @Override
    protected void tick(ServerLevel level, Mob owner, long gameTime) {
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) {
            return;
        }
        setFleeWalkTarget(owner, target);
    }

    @Override
    protected void stop(ServerLevel level, Mob entity, long gameTime) {
        lastDrinkTick = level.getServer().getTickCount();
    }
}
