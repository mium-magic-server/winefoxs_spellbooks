package net.magicterra.winefoxsspellbooks.client.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.api.spell.SpellCategory;
import net.magicterra.winefoxsspellbooks.client.config.SpellCompatModel.CategoryState;
import net.magicterra.winefoxsspellbooks.client.config.SpellCompatModel.Change;
import net.magicterra.winefoxsspellbooks.client.config.SpellCompatModel.SpellInfo;
import net.magicterra.winefoxsspellbooks.magic.data.SpellDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

/**
 * 单个法术的详细设置：分类、起手施法距离、施加的药水效果、伤害先验。
 * <p>
 * 完全沿用 NeoForge 配置界面的两列布局，条目由 {@link #createSyntheticValues()} 生成。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
public class SpellCompatDetailScreen extends SpellCompatScreenBase {
    private final SpellCompatModel model;

    private final SpellInfo info;

    public SpellCompatDetailScreen(SpellCompatScreen parent, SpellInfo info) {
        super(parent.context(), parent, parent.context().valueSpecs(), info.id(), Set.of(), info.name());
        this.model = parent.model;
        this.info = info;
    }

    @Override
    protected Collection<? extends Element> createSyntheticValues() {
        List<Element> elements = new ArrayList<>();
        elements.add(new Element(tr("detail.id"), tr("detail.id"),
            label(Component.literal(info.id()).withStyle(ChatFormatting.GRAY)), false));
        elements.add(new Element(tr("detail.mod"), tr("detail.mod"), label(info.modName()), false));

        for (SpellCategory category : SpellCompatModel.CATEGORIES) {
            CategoryState state = model.state(info, category);
            Button button = Button.builder(stateName(state), b -> {
                    commit(model.toggle(info, category));
                    rebuild();
                })
                .width(Button.DEFAULT_WIDTH)
                .tooltip(Tooltip.create(categoryTooltip(model, info, category)))
                .build();
            button.active = !model.isLocked(info, category);
            elements.add(new Element(categoryName(category), categoryTooltip(model, info, category), button));
        }

        Float packRange = SpellDataManager.getInstance().getPackSpellRange(info.spell());
        Float range = model.range(info);
        elements.add(new Element(tr("range"), tooltip(tr("range"), tr("range.clear")),
            numberBox(range, packRange != null ? tr("detail.range.hint", formatNumber(packRange))
                : tr("detail.range.hint", formatNumber(SpellDataManager.DEFAULT_SPELL_RANGE)), v -> model.setRange(info, v))));

        Holder<MobEffect> packEffect = SpellDataManager.getInstance().getPackSpellCausedEffect(info.spell());
        ResourceLocation packEffectId = packEffect != null ? BuiltInRegistries.MOB_EFFECT.getKey(packEffect.value()) : null;
        elements.add(new Element(tr("detail.effect"), tooltip(tr("detail.effect"), tr("detail.effect.tooltip")),
            effectBox(packEffectId)));

        elements.add(new Element(tr("detail.value"), tooltip(tr("detail.value"), tr("detail.value.tooltip")),
            numberBox(model.value(info), tr("detail.value.hint"), v -> model.setValue(info, v))));
        return elements;
    }

    /**
     * 原版输入框不裁剪提示文字，过长会画出边框，这里按框宽截断。
     */
    private Component fitHint(Component hint) {
        int maxWidth = Button.DEFAULT_WIDTH - 8;
        String text = hint.getString();
        if (font.width(text) > maxWidth) {
            text = font.plainSubstrByWidth(text, maxWidth - font.width("...")) + "...";
        }
        return Component.literal(text).withStyle(ChatFormatting.DARK_GRAY);
    }

    private StringWidget label(Component text) {
        return new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, text, font).alignLeft();
    }

    private static Component tooltip(MutableComponent title, MutableComponent body) {
        return Component.empty().append(title.withStyle(ChatFormatting.BOLD)).append("\n").append(body.withStyle(ChatFormatting.GRAY));
    }

    private interface NumberSetter {
        List<Change<?>> apply(@Nullable Float value);
    }

    /**
     * 正数输入框：留空即清除覆盖，非法输入标红且不写入。
     */
    private EditBox numberBox(@Nullable Float current, Component hint, NumberSetter setter) {
        EditBox box = new EditBox(font, 0, 0, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, hint);
        box.setMaxLength(12);
        box.setFilter(s -> s.matches("[0-9]*\\.?[0-9]*"));
        box.setHint(fitHint(hint));
        box.setValue(current != null ? formatNumber(current) : "");
        box.setResponder(text -> {
            Float value;
            try {
                value = parsePositive(text);
            } catch (NumberFormatException e) {
                box.setTextColor(0xFF5555);
                return;
            }
            box.setTextColor(0xE0E0E0);
            commit(setter.apply(value));
        });
        return box;
    }

    /**
     * 药水效果 id 输入框：只接受已注册的效果。
     */
    private EditBox effectBox(@Nullable ResourceLocation packEffectId) {
        EditBox box = new EditBox(font, 0, 0, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, tr("detail.effect"));
        box.setMaxLength(128);
        Component hint = packEffectId != null ? Component.literal(packEffectId.toString()) : tr("detail.effect.none");
        box.setHint(fitHint(hint));
        String current = model.effect(info);
        box.setValue(current != null ? current : "");
        box.setResponder(text -> {
            String trimmed = text.trim();
            if (trimmed.isEmpty()) {
                box.setTextColor(0xE0E0E0);
                commit(model.setEffect(info, null));
                return;
            }
            ResourceLocation id = ResourceLocation.tryParse(trimmed);
            if (id == null || !BuiltInRegistries.MOB_EFFECT.containsKey(id)) {
                box.setTextColor(0xFF5555);
                return;
            }
            box.setTextColor(0xE0E0E0);
            commit(model.setEffect(info, id.toString()));
        });
        return box;
    }

    @Override
    protected void onCommitted() {
        // 输入框的修改不重建列表，免得丢焦点；分类按钮在点击回调里自行 rebuild
    }

    @Override
    protected void createResetButton() {
        resetButton = Button.builder(Component.translatable("neoforge.configuration.uitext.reset"), b -> {
                commit(model.resetSpell(info));
                rebuild();
            })
            .tooltip(Tooltip.create(Component.translatable("neoforge.configuration.uitext.reset.tooltip")))
            .width(Button.SMALL_WIDTH).build();
    }

    @Override
    protected boolean isAnyNondefault() {
        return model.isModified(info);
    }
}
