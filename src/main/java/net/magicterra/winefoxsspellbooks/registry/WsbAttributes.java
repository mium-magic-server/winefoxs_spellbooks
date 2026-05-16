package net.magicterra.winefoxsspellbooks.registry;

import io.redspace.ironsspellbooks.api.attribute.MagicPercentAttribute;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 酒狐巫法学派的属性
 * <p>
 * 仿 {@link io.redspace.ironsspellbooks.api.registry.AttributeRegistry} 的 power / resist 双属性范式，
 * 让 vanilla Iron's 的 SchoolType 系统能正确读出本学派的强度与抗性。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
@SuppressWarnings("removal")
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class WsbAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(Registries.ATTRIBUTE, WinefoxsSpellbooks.MODID);

    public static final DeferredHolder<Attribute, Attribute> WINEFOX_HEX_SPELL_POWER = ATTRIBUTES.register(
        "winefox_hex_spell_power",
        () -> new MagicPercentAttribute("attribute.winefoxs_spellbooks.winefox_hex_spell_power", 1.0, -100.0, 100.0)
            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> WINEFOX_HEX_MAGIC_RESIST = ATTRIBUTES.register(
        "winefox_hex_magic_resist",
        () -> new MagicPercentAttribute("attribute.winefoxs_spellbooks.winefox_hex_magic_resist", 1.0, -100.0, 100.0)
            .setSyncable(true));

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
    }

    /**
     * 把本学派的属性附加到所有生物身上，这样 vanilla {@code SchoolType.getPowerFor / getResistanceFor} 才能取值。
     * 沿用 Iron's 的做法：遍历所有 EntityType 全量挂载。
     */
    @SubscribeEvent
    public static void onModifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(holder ->
            event.add(entity, (Holder<Attribute>) holder)));
    }
}
