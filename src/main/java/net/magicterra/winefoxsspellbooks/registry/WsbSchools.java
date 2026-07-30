package net.magicterra.winefoxsspellbooks.registry;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import java.util.function.Supplier;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.damage.WsbDamageTypes;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 注册"酒狐巫法 Winefox Hex"学派到 Iron's Spellbooks 的 SchoolRegistry。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public class WsbSchools {
    /**
     * 学派注册 id；别名自 {@link WinefoxHexHelper#WINEFOX_HEX_ID}，保持单一事实源。
     */
    public static final ResourceLocation WINEFOX_HEX_RESOURCE = WinefoxHexHelper.WINEFOX_HEX_ID;

    private static final DeferredRegister<SchoolType> SCHOOLS =
        DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, WinefoxsSpellbooks.MODID);

    public static final Supplier<SchoolType> WINEFOX_HEX = SCHOOLS.register(
        WINEFOX_HEX_RESOURCE.getPath(),
        () -> new SchoolType(
            WINEFOX_HEX_RESOURCE,
            WsbItemTags.WINEFOX_HEX_FOCUS,
            Component.translatable("school." + WinefoxsSpellbooks.MODID + ".winefox_hex")
                .withStyle(Style.EMPTY.withColor(WinefoxHexHelper.SCHOOL_COLOR)),
            WsbAttributes.WINEFOX_HEX_SPELL_POWER,
            WsbAttributes.WINEFOX_HEX_MAGIC_RESIST,
            WsbSounds.WINEFOX_HEX_CAST,         // 咏唱音效：瓶身叮当 + 液体晃动 + 狐鸣收尾
            WsbDamageTypes.WINEFOX_HEX_MAGIC));

    public static void register(IEventBus modBus) {
        SCHOOLS.register(modBus);
    }
}
