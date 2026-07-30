package net.magicterra.winefoxsspellbooks.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidPickupEvent;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 拦截女仆拾取自己投出的晨间礼物掉落物
 * <p>
 * 由 {@code MaidDeliverGiftBehavior} 在生成礼物 ItemEntity 时给实体打上 {@link #MAID_GIFT_TAG} 标记，此处监听 TLM 的 {@link MaidPickupEvent.ItemResultPre} 并将带标记的物品拦下来——避免女仆把刚送出去的礼物又捡回去。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-15
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public final class MaidGiftPickupBlocker {
    /**
     * ItemEntity 上的标记串，仅匹配 ASCII 字母数字下划线
     */
    public static final String MAID_GIFT_TAG = "winefoxs_spellbooks_maid_gift";

    private MaidGiftPickupBlocker() {
    }

    @SubscribeEvent
    public static void onMaidTryPickupItem(MaidPickupEvent.ItemResultPre event) {
        if (event.getEntityItem().getTags().contains(MAID_GIFT_TAG)) {
            event.setCanPickup(false);
            event.setCanceled(true);
        }
    }
}
