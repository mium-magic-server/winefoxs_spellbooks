package net.magicterra.winefoxsspellbooks.client.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;

/**
 * 本模组的配置界面入口：其余配置项沿用 NeoForge 自动生成的界面，只把「法术兼容」小节换成法术分类编辑器。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
public final class WsbConfigScreens {
    /**
     * 被替换的小节名，与 {@code Config} 里 {@code push("spell_compat")} 一致。
     */
    static final String SPELL_COMPAT_SECTION = "spell_compat";

    private WsbConfigScreens() {
    }

    /**
     * 供 {@code IConfigScreenFactory} 使用。
     */
    public static Screen create(ModContainer container, Screen parent) {
        return new ConfigurationScreen(container, parent, RootSectionScreen::new);
    }

    /**
     * 配置文件的顶层界面，仅改写「法术兼容」小节的入口。
     */
    static class RootSectionScreen extends ConfigurationSectionScreen {
        RootSectionScreen(ConfigurationScreen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
            super(parent, type, modConfig, title);
        }

        @Override
        protected Element createSection(String key, UnmodifiableConfig subconfig, UnmodifiableConfig subsection) {
            if (!SPELL_COMPAT_SECTION.equals(key)) {
                return super.createSection(key, subconfig, subsection);
            }
            Component name = Component.translatable("neoforge.configuration.uitext.section", getTranslationComponent(key));
            Component tooltip = getTooltipComponent(key, null);
            Button button = Button.builder(
                    Component.translatable("neoforge.configuration.uitext.section",
                        Component.translatable("neoforge.configuration.uitext.sectiontext")),
                    b -> minecraft.setScreen(sectionCache.computeIfAbsent(key, k -> new SpellCompatScreen(context, this,
                        subconfig.valueMap(), key, subsection.entrySet(), getTranslationComponent(key)))))
                .tooltip(Tooltip.create(tooltip))
                .width(Button.DEFAULT_WIDTH)
                .build();
            return new Element(name, tooltip, button, false);
        }
    }
}
