package net.magicterra.winefoxsspellbooks.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidPickupEvent;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 拦截女仆拾取自己投出的晨赠掉落物
 * <p>
 * 由 {@code MaidDeliverGiftBehavior} 在生成礼物 ItemEntity 时给实体打上
 * {@link #MAID_GIFT_TAG} 标记，此处监听 TLM 的 {@link MaidPickupEvent.ItemResultPre}
 * 并将带标记的物品拦下来——避免女仆把刚送出去的礼物又捡回去。
 * <p>
 * 用 {@code Entity#addTag} 的字符串标签而不是 {@code ItemEntity#getOwner()}，
 * 是为了把拦截范围严格限定在本模组的晨赠礼物——避免误伤其他模组里
 * "由女仆投出但本就允许其他女仆拾取"的物品。
 * <p>
 * 标记打在 ItemEntity 上（{@code Entity#addTag}），随实体存档；玩家拾取后实体消失，
 * 标记自然不会污染玩家背包里的 ItemStack。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-15
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public final class MaidGiftPickupBlocker {
    /** ItemEntity 上的标记串，仅匹配 ASCII 字母数字下划线 */
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
