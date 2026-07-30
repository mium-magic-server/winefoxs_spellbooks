package net.magicterra.winefoxsspellbooks.event;

import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.ai.goal.AvoidSummonedMaidGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * 世界事件处理
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-23 00:51
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class WorldEventHandler {
    /**
     * 苦力怕退避召唤女仆的距离(格)。
     */
    private static final float CREEPER_AVOID_DISTANCE = 6.0f;

    /**
     * 退避时的行走 / 冲刺速度倍率。
     */
    private static final double CREEPER_AVOID_WALK_SPEED = 1.0;

    private static final double CREEPER_AVOID_SPRINT_SPEED = 1.2;

    /**
     * 给苦力怕挂上「躲开召唤女仆」目标。
     */
    @SubscribeEvent
    public static void onCreeperJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof Creeper creeper)) {
            return;
        }
        if (Config.isServerConfigLoaded() && !Config.isCreeperAvoidSummonedMaid()) {
            return;
        }
        creeper.goalSelector.addGoal(1, new AvoidSummonedMaidGoal(
            creeper, CREEPER_AVOID_DISTANCE, CREEPER_AVOID_WALK_SPEED, CREEPER_AVOID_SPRINT_SPEED));
    }
}
