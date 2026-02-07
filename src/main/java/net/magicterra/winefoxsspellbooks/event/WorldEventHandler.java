package net.magicterra.winefoxsspellbooks.event;

import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
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
@EventBusSubscriber
public class WorldEventHandler {
    @SubscribeEvent
    public static void onCreeperJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Creeper creeper) {
            creeper.goalSelector.addGoal(1, new AvoidEntityGoal<>(creeper, SummonedEntityMaid.class, 6, 1, 1.2));
        }
    }
}
