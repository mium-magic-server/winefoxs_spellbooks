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

        // 朝相反方向逃跑
        // TODO 测试此功能
        Vec3 fleeVector = target.getForward().reverse().scale(10);

        BlockPos fleePos = target.blockPosition().offset(Mth.floor(fleeVector.x), Mth.floor(fleeVector.y), Mth.floor(fleeVector.z));
        entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(fleePos, fleeSpeed, 0));
    }

    @Override
    protected void tick(ServerLevel level, Mob owner, long gameTime) {
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) {
            return;
        }
        Vec3 fleeVector = target.getForward().reverse().scale(10);
        BlockPos fleePos = target.blockPosition().offset(Mth.floor(fleeVector.x), Mth.floor(fleeVector.y), Mth.floor(fleeVector.z));
        owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(fleePos, fleeSpeed, 0));
    }

    @Override
    protected void stop(ServerLevel level, Mob entity, long gameTime) {
        lastDrinkTick = level.getServer().getTickCount();
    }
}
