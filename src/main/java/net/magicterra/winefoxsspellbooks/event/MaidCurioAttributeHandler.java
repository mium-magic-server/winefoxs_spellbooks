package net.magicterra.winefoxsspellbooks.event;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.mixin.CurioBaseItemAccessor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/**
 * 女仆的 Iron's 饰品在任意 Curios 栏位都生效属性加成
 * <p>
 * Iron's 的 {@link CurioBaseItem} 只在栏位名等于其原生栏位（戒指为 {@code ring}）时才给属性加成，放进通用栏 {@code curio} 等其他栏位时加成为空，玩家亦然。
 * 女仆的饰品栏布局与玩家不同，这里只对女仆放宽：按原生栏位算出加成补进当前栏位。玩家保持 Iron's 原设计。
 * <p>
 * 法术书不放宽，仍只认法术书栏，配合「只能装一本」的限制。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public final class MaidCurioAttributeHandler {
    private MaidCurioAttributeHandler() {
    }

    @SubscribeEvent
    public static void onCurioAttributeModifiers(CurioAttributeModifierEvent event) {
        SlotContext slotContext = event.getSlotContext();
        if (!(slotContext.entity() instanceof IMaid) || slotContext.cosmetic()
            || !(event.getItemStack().getItem() instanceof CurioBaseItem curioItem)) {
            return;
        }
        String nativeSlot = ((CurioBaseItemAccessor) curioItem).winefoxsSpellbooks$getAttributeSlot();
        if (nativeSlot.isEmpty() || nativeSlot.equals(slotContext.identifier()) || Curios.SPELLBOOK_SLOT.equals(nativeSlot)) {
            return;
        }
        SlotContext nativeContext = new SlotContext(nativeSlot, slotContext.entity(), slotContext.index(), false, slotContext.visible());
        Multimap<Holder<Attribute>, AttributeModifier> nativeModifiers = curioItem.getAttributeModifiers(nativeContext, event.getId(), event.getItemStack());
        // 原生修饰符 id 按「原生栏位_序号」生成，通用栏 0 号位的戒指会和戒指栏 0 号位撞 id、互相覆盖，故按实际栏位重新编号
        String prefix = "maid_curio/" + slotContext.identifier() + "_" + slotContext.index() + "/";
        nativeModifiers.forEach((attribute, modifier) -> {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID,
                prefix + modifier.id().getNamespace() + "/" + modifier.id().getPath());
            event.addModifier(attribute, new AttributeModifier(id, modifier.amount(), modifier.operation()));
        });
    }
}
