package net.magicterra.winefoxsspellbooks.registry;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import java.util.function.Supplier;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.damage.WsbDamageTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 注册"酒狐巫法 Winefox Hex"学派到 Iron's Spellbooks 的 SchoolRegistry。
 * <p>
 * 学派颜色 = 紫粉 #C846FF（设计文档 07-focus §二）。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public class WsbSchools {
    public static final ResourceLocation WINEFOX_HEX_RESOURCE =
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "winefox_hex");

    private static final DeferredRegister<SchoolType> SCHOOLS =
        DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, WinefoxsSpellbooks.MODID);

    public static final Supplier<SchoolType> WINEFOX_HEX = SCHOOLS.register(
        WINEFOX_HEX_RESOURCE.getPath(),
        () -> new SchoolType(
            WINEFOX_HEX_RESOURCE,
            WsbItemTags.WINEFOX_HEX_FOCUS,
            Component.translatable("school." + WinefoxsSpellbooks.MODID + ".winefox_hex")
                .withStyle(Style.EMPTY.withColor(0xC846FF)),
            WsbAttributes.WINEFOX_HEX_SPELL_POWER,
            WsbAttributes.WINEFOX_HEX_MAGIC_RESIST,
            SoundRegistry.ENDER_CAST,           // 先复用 Ender 咏唱音效，待自有音效再换
            WsbDamageTypes.WINEFOX_HEX_MAGIC));

    public static void register(IEventBus modBus) {
        SCHOOLS.register(modBus);
    }
}
