package net.magicterra.winefoxsspellbooks.entity.ai.memory;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 女仆晨赠待办：目标玩家 + 待掉落物品
 * <p>
 * 由 PlayerWakeUpEvent 写入到女仆 Brain 的 GIFT_DELIVERY 记忆，
 * 由 {@code MaidDeliverGiftBehavior} 消费。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-15
 */
public record MaidGiftDelivery(Player target, List<ItemStack> drops) {
}
