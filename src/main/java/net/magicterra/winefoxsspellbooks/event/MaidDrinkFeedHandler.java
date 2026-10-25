package net.magicterra.winefoxsspellbooks.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.item.consumables.DrinkableItem;
import java.lang.reflect.Field;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.item.VixensFlightItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * 潜行右键把品酒排与铁魔法的药剂喂给女仆，同车万女仆自带的喂药水（{@code ApplyPotionEffectEvent}，只认原版药水）
 * <ul>
 *   <li>品酒排：女仆喝掉最右一支，空酒签留在托板上，品酒排仍在玩家手里</li>
 *   <li>铁魔法的 {@link DrinkableItem}（药剂、茶、麦酒等）：女仆获得其效果，玩家手中扣掉一个并拿回其容器</li>
 * </ul>
 * 车万女仆只对主人、主手发出交互事件；两端都会触发，改动只在服务端做，客户端只取消事件以免打开女仆界面。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public final class MaidDrinkFeedHandler {
    /**
     * {@link DrinkableItem} 喝完返还的容器：它只在饮用者是玩家时扣物品、给容器，喂给女仆时得自己来
     */
    @Nullable
    private static final Field RETURN_ITEM = findReturnItemField();

    private MaidDrinkFeedHandler() {
    }

    @SubscribeEvent
    public static void onInteractMaid(InteractMaidEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getStack();
        if (!player.isDiscrete()) {
            return;
        }
        EntityMaid maid = event.getMaid();
        Level level = event.getWorld();
        if (stack.getItem() instanceof VixensFlightItem) {
            if (VixensFlightItem.vials(stack).lastFilled() < 0) {
                return;
            }
            if (!level.isClientSide) {
                // 饮用者不是玩家，finishUsingItem 总会清空那一支；创造模式下也照样喝掉，同喂原版药水时的行为
                stack.getItem().finishUsingItem(stack, level, maid);
            }
        } else if (stack.getItem() instanceof DrinkableItem drinkable) {
            if (!level.isClientSide) {
                drinkable.finishUsingItem(stack.copy(), level, maid);
                if (!player.isCreative()) {
                    Item container = returnItem(drinkable);
                    stack.shrink(1);
                    if (container != null) {
                        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(container));
                    }
                }
            }
        } else {
            return;
        }
        maid.playSound(SoundEvents.GENERIC_DRINK, 0.6f, 0.8f + level.random.nextFloat() * 0.4f);
        event.setCanceled(true);
    }

    @Nullable
    private static Item returnItem(DrinkableItem item) {
        if (RETURN_ITEM == null) {
            return null;
        }
        try {
            return (Item) RETURN_ITEM.get(item);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    private static Field findReturnItemField() {
        try {
            Field field = DrinkableItem.class.getDeclaredField("returnItem");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            WinefoxsSpellbooks.LOGGER.warn("无法读取铁魔法饮品的返还容器，喂给女仆后将不返还容器：{}", e.toString());
            return null;
        }
    }
}
