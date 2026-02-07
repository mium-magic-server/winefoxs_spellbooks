package net.magicterra.winefoxsspellbooks.entity.ai.brain;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidInteractWithDoor;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidSwimJumpTask;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.FollowSummonerBehavior;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.MagicMeleeAttack;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.MobClimbTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellAttackWalkToTarget;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellCastingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellChooseTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.SpellStrafingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.StartAttacking;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon.SummonedMaidDismountTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon.SummonedMaidFlyStrafingTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon.SummonedMaidFlyToSummoner;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon.SummonedMaidPilotBroomTask;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon.SummonedMaidRandomFlight;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.summon.SummonedMaidRunOne;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.schedule.Activity;

/**
 * 召唤女仆的 Brain 配置
 * <p>
 * 简化的 Brain 系统，仅包含战斗和跟随行为。
 * 不包含拾取、乞讨、回家等不需要的行为。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-12-29
 */
public class SummonedMaidBrain {

    /**
     * 跟随速度
     */
    private static final float FOLLOW_SPEED = 0.5f;

    /**
     * 停止跟随的距离
     */
    private static final int STOP_FOLLOW_DISTANCE = 2;

    /**
     * 允许的法术动作类型
     */
    private static final Set<MaidSpellAction> ALLOWED_ACTIONS = Set.of(
        MaidSpellAction.ATTACK,
        MaidSpellAction.DEFENSE,
        MaidSpellAction.MOVEMENT
    );

    /**
     * 获取最小 Memory 类型列表
     * <p>
     * 仅包含战斗必需的 Memory
     *
     * @return Memory 类型列表
     */
    public static ImmutableList<MemoryModuleType<?>> getMemoryTypes() {
        return ImmutableList.of(
            // 导航
            MemoryModuleType.PATH,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.DOORS_TO_CLOSE,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,

            // 目标选择
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,

            // 实体检测
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_HOSTILE,

            // 伤害追踪
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,

            // 法术施放
            MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(),
            MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(),
            MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(),

            // 飞行
            MaidCastingMemoryModuleTypes.BROOM_ENTITY.get(),
            MaidCastingMemoryModuleTypes.FLIGHT_TARGET.get(),
            MaidCastingMemoryModuleTypes.IS_HOVERING.get()
        );
    }

    /**
     * 获取最小 Sensor 类型列表
     *
     * @return Sensor 类型列表
     */
    public static ImmutableList<SensorType<? extends Sensor<? super LivingEntity>>> getSensorTypes() {
        return ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY
        );
    }

    /**
     * 注册 Brain 行为
     * <p>
     * 配置 CORE 和 FIGHT 活动的行为
     *
     * @param brain Brain 实例
     * @param maid  召唤女仆实例
     */
    public static void registerBrainGoals(Brain<SummonedEntityMaid> brain, SummonedEntityMaid maid) {
        // 注册 CORE 活动 - 基础行为
        registerCoreGoals(brain);

        // 注册 FIGHT 活动 - 战斗行为
        registerFightGoals(brain, maid);

        // 注册 RIDE 活动 - 飞行行为
        registerRideGoals(brain, maid);

        // 注册 IDLE 活动 - 空闲行为
        registerIdleGoals(brain);

        // 设置活动优先级
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
    }

    /**
     * 注册 CORE 活动行为
     */
    private static void registerCoreGoals(Brain<SummonedEntityMaid> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.<BehaviorControl<? super SummonedEntityMaid>>of(
            // 游泳
            new MaidSwimJumpTask(0.8f),
            // 攀爬
            new MobClimbTask(),
            // 看向目标
            new LookAtTargetSink(45, 90),
            // 开门
            MaidInteractWithDoor.create(),
            // 移动到目标
            new MoveToTargetSink(),
            // 跟随召唤者
            new FollowSummonerBehavior(FOLLOW_SPEED, STOP_FOLLOW_DISTANCE)
        ));
    }

    /**
     * 注册 FIGHT 活动行为
     */
    private static void registerFightGoals(Brain<SummonedEntityMaid> brain, SummonedEntityMaid maid) {
        ImmutableList.Builder<BehaviorControl<? super SummonedEntityMaid>> behaviors = ImmutableList.builder();
        behaviors.add(
            // 目标无效时停止攻击
            StopAttackingIfTargetInvalid.create(
                (target) -> !isValidHostileTarget(maid, target),
                (mob, target) -> {},
                true
            ),
            // 法术选择
            new SpellChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid, ALLOWED_ACTIONS),
            // 走向目标
            SpellAttackWalkToTarget.create((float) Config.getBattleWalkSpeed()),
            // 走位
            new SpellStrafingTask(Config.getStartSpellRange(), (float) Config.getBattleWalkSpeed()),
            // 施法
            new SpellCastingTask(maid)
        );
        if (Config.getMeleeAttackInMagicTask()) {
            behaviors.add(MagicMeleeAttack.create(20));
        }

        brain.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            behaviors.build(),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    /**
     * 注册 RIDE 活动行为（扫帚女仆飞行模式）
     * <p>
     * 模仿悦灵(Allay)的行为模式：
     * - 使用 SummonedMaidFlyToSummoner 保持与召唤者的距离
     * - 使用 SummonedMaidRandomFlight 进行随机飞行游走
     * - 使用 SetEntityLookTarget 随机看向（与地面模式一致：玩家、女仆、狼、猫、鹦鹉）
     */
    private static void registerRideGoals(Brain<SummonedEntityMaid> brain, SummonedEntityMaid maid) {
        brain.addActivityAndRemoveMemoryWhenStopped(
            Activity.RIDE,
            15,
            ImmutableList.of(
                // 目标无效时停止攻击
                StopAttackingIfTargetInvalid.create(
                    (target) -> !isValidHostileTarget(maid, target),
                    (mob, target) -> {},
                    true
                ),
                // 索敌 - 发现敌人开始攻击
                StartAttacking.create(
                    (mob) -> true,
                    SummonedMaidBrain::findNearestHostile
                ),
                // 法术选择
                new SpellChooseTask(Config.getStartSpellRange(), Config.getMaxComboDelayTick(), maid, ALLOWED_ACTIONS),
                // 施法
                new SpellCastingTask(maid),
                // 飞行走位（围绕目标扫射，设置 FLIGHT_TARGET）
                new SummonedMaidFlyStrafingTask(),
                // 飞行控制（将 WALK_TARGET 转换为飞行目标，处理悬停状态）
                new SummonedMaidPilotBroomTask(),
                // 跟随召唤者（自定义实现，解决每tick更新导致的卡顿）
                new SummonedMaidFlyToSummoner(),
                // 随机看向（与地面模式一致：玩家、女仆、狼、猫、鹦鹉）
                getFlightLookBehaviors(),
                // 随机飞行游走（模仿悦灵的 IDLE 行为）
                createAllayStyleIdleBehaviors(),
                // 下车检测
                new SummonedMaidDismountTask()
            ),
            MaidCastingMemoryModuleTypes.BROOM_ENTITY.get()
        );
    }

    /**
     * 注册 IDLE 活动行为
     */
    private static void registerIdleGoals(Brain<SummonedEntityMaid> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
            // 开始攻击
            StartAttacking.create(
                (mob) -> true,
                SummonedMaidBrain::findNearestHostile
            ),
            // 随机看向和走动
            getLookAndRandomWalk(maid -> true)
        ));
    }

    /**
     * 查找最近的敌对实体
     *
     * @param maid 召唤女仆
     * @return 最近的有效目标
     */
    private static Optional<? extends LivingEntity> findNearestHostile(SummonedEntityMaid maid) {
        return maid.getBrain()
            .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            .flatMap(entities -> entities.findClosest(target -> isValidHostileTarget(maid, target)));
    }

    /**
     * 是否敌对实体
     *
     * @param target 目标实体
     * @return 是否敌对
     */
    private static boolean isEnemyTarget(LivingEntity target) {
        return target instanceof Enemy;
    }

    /**
     * 检查目标是否有效（不含敌对判断）
     *
     * @param maid   召唤女仆
     * @param target 目标实体
     * @return 目标是否有效
     */
    public static boolean isValidTarget(SummonedEntityMaid maid, LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        }
        if (maid.isAlliedTo(target)) {
            return false;
        }
        // 检查是否是召唤者
        if (target.equals(maid.getSummoner())) {
            return false;
        }
        return true;
    }

    /**
     * 检查敌对目标是否有效
     *
     * @param maid   召唤女仆
     * @param target 目标实体
     * @return 目标是否有效
     */
    private static boolean isValidHostileTarget(SummonedEntityMaid maid, LivingEntity target) {
        return isEnemyTarget(target) && isValidTarget(maid, target);
    }

    /**
     * 创建敌对目标验证谓词
     *
     * @param maid 召唤女仆
     * @return 目标验证谓词
     */
    public static Predicate<LivingEntity> createTargetPredicate(SummonedEntityMaid maid) {
        return target -> isValidHostileTarget(maid, target);
    }

    /**
     * 获取随机看向和走动行为
     *
     * @param enableCondition 启用条件
     * @return 行为控制器
     */
    private static SummonedMaidRunOne getLookAndRandomWalk(Predicate<SummonedEntityMaid> enableCondition) {
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToPlayer =
            Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 5), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToMaid =
            Pair.of(SetEntityLookTarget.create(SummonedEntityMaid.TYPE, 5), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToWolf =
            Pair.of(SetEntityLookTarget.create(EntityType.WOLF, 5), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToCat =
            Pair.of(SetEntityLookTarget.create(EntityType.CAT, 5), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToParrot =
            Pair.of(SetEntityLookTarget.create(EntityType.PARROT, 5), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> walkRandomly =
            Pair.of(RandomStroll.stroll(0.3f, 5, 3), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> noLook =
            Pair.of(new DoNothing(30, 60), 2);

        return new SummonedMaidRunOne(
            ImmutableList.of(lookToPlayer, lookToMaid, lookToWolf, lookToCat, lookToParrot, walkRandomly, noLook),
            enableCondition
        );
    }

    /**
     * 获取飞行模式下的随机看向行为（与地面模式一致）
     * <p>
     * 包含看向玩家、女仆、狼、猫、鹦鹉等实体的行为。
     *
     * @return 行为控制器（SummonedMaidRunOne）
     */
    private static SummonedMaidRunOne getFlightLookBehaviors() {
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToPlayer =
            Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8), 2);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToMaid =
            Pair.of(SetEntityLookTarget.create(SummonedEntityMaid.TYPE, 8), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToWolf =
            Pair.of(SetEntityLookTarget.create(EntityType.WOLF, 8), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToCat =
            Pair.of(SetEntityLookTarget.create(EntityType.CAT, 8), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> lookToParrot =
            Pair.of(SetEntityLookTarget.create(EntityType.PARROT, 8), 1);
        Pair<BehaviorControl<? super SummonedEntityMaid>, Integer> noLook =
            Pair.of(new DoNothing(30, 60), 3);

        // 飞行模式不需要 walkRandomly，因为有 SummonedMaidRandomFlight
        return new SummonedMaidRunOne(
            ImmutableList.of(lookToPlayer, lookToMaid, lookToWolf, lookToCat, lookToParrot, noLook),
            maid -> !maid.hasCombatTarget()  // 战斗时不随机看向
        );
    }

    /**
     * 获取随机看向和飞行游走行为（用于 RIDE 模式）
     * <p>
     * 模仿悦灵的 IDLE 行为，但使用自定义的随机飞行避开召唤者头顶和前方。
     *
     * @return 行为控制器（RunOne）
     */
    private static RunOne<SummonedEntityMaid> createAllayStyleIdleBehaviors() {
        return new RunOne<>(ImmutableList.of(
            // 自定义随机飞行（避开召唤者头顶和前方）
            Pair.of((BehaviorControl<? super SummonedEntityMaid>) SummonedMaidRandomFlight.create(1.0f), 2),
            // 飞向看着的目标（原版悦灵使用 SetWalkTargetFromLookTarget）
            Pair.of((BehaviorControl<? super SummonedEntityMaid>) SetWalkTargetFromLookTarget.create(1.0f, 3), 2),
            // 悬停不动
            Pair.of(new DoNothing(30, 60), 1)
        ));
    }
}
