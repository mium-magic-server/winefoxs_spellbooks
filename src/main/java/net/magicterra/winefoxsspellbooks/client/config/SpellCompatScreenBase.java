package net.magicterra.winefoxsspellbooks.client.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.api.spell.SpellCategory;
import net.magicterra.winefoxsspellbooks.api.spell.SpellFlag;
import net.magicterra.winefoxsspellbooks.client.config.SpellCompatModel.CategoryState;
import net.magicterra.winefoxsspellbooks.client.config.SpellCompatModel.Change;
import net.magicterra.winefoxsspellbooks.client.config.SpellCompatModel.SpellInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.UndoManager;

/**
 * 法术分类编辑器两个界面的公共部分：把模型给出的修改交给 NeoForge 的撤销管理器，以及共用的文案。
 * <p>
 * 继承 {@link ConfigurationSectionScreen} 是为了沿用它的保存与「需要重新加载世界」提示：
 * 每次修改都经 {@link #onChanged} 记下重启类型，关闭时由父界面统一保存。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
abstract class SpellCompatScreenBase extends ConfigurationSectionScreen {
    private static final String LANG_PREFIX = "config." + WinefoxsSpellbooks.MODID + ".spell_editor.";

    protected SpellCompatScreenBase(Context parentContext, Screen parent, Map<String, Object> valueSpecs, String key,
                                    Set<? extends UnmodifiableConfig.Entry> entrySet, Component title) {
        super(parentContext, parent, valueSpecs, key, entrySet, title);
    }

    /**
     * 作为一个撤销步骤执行一组修改；空表直接忽略。
     */
    void commit(List<Change<?>> changes) {
        if (changes.isEmpty()) {
            return;
        }
        List<UndoManager.Step<?>> steps = new ArrayList<>(changes.size());
        for (Change<?> change : changes) {
            steps.add(step(change));
        }
        undoManager.add(steps);
        onCommitted();
    }

    private <T> UndoManager.Step<T> step(Change<T> change) {
        Consumer<T> setter = v -> {
            change.value().set(v);
            onChanged(change.key());
        };
        return undoManager.step(setter, change.newValue(), setter, change.value().getRaw());
    }

    /**
     * 修改执行之后刷新界面。
     */
    protected abstract void onCommitted();

    static MutableComponent tr(String key, Object... args) {
        return Component.translatable(LANG_PREFIX + key, args);
    }

    static MutableComponent categoryName(SpellCategory category) {
        return tr("category." + category.catalogName());
    }

    static MutableComponent categoryShort(SpellCategory category) {
        return tr("category." + category.catalogName() + ".short");
    }

    static MutableComponent categoryDesc(SpellCategory category) {
        return tr("category." + category.catalogName() + ".desc");
    }

    /**
     * 分类状态的文字与颜色：关为灰、内置为白、追加为绿、移除内置为红色删除线。
     */
    static MutableComponent styled(MutableComponent text, CategoryState state, ChatFormatting offColor) {
        return switch (state) {
            case OFF -> text.withStyle(offColor);
            case BUILTIN -> text.withStyle(ChatFormatting.WHITE);
            case ADDED -> text.withStyle(ChatFormatting.GREEN);
            case REMOVED -> text.withStyle(ChatFormatting.RED, ChatFormatting.STRIKETHROUGH);
        };
    }

    static MutableComponent stateName(CategoryState state) {
        return styled(tr("state." + state.name().toLowerCase(Locale.ROOT)), state, ChatFormatting.WHITE);
    }

    /**
     * 分类按钮的悬浮提示：分类名与状态、用途说明、点击后果、配置键。
     */
    static Component categoryTooltip(SpellCompatModel model, SpellInfo info, SpellCategory category) {
        CategoryState state = model.state(info, category);
        MutableComponent tip = Component.empty()
            .append(categoryName(category).withStyle(ChatFormatting.BOLD)).append(tr("colon")).append(stateName(state))
            .append("\n").append(categoryDesc(category).withStyle(ChatFormatting.GRAY));
        Component note = recastNote(model, info, category);
        if (note != null) {
            tip.append("\n").append(note);
        }
        if (!model.isLocked(info, category)) {
            tip.append("\n").append(tr("action." + state.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_GRAY));
        }
        return tip.append("\n").append(Component.literal(configKey(category, state)).withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * 「多次咏唱」按钮的补充说明：没有二段咏唱、召唤类不会多次咏唱、有二段咏唱但未标记。
     */
    @Nullable
    private static Component recastNote(SpellCompatModel model, SpellInfo info, SpellCategory category) {
        if (category != SpellFlag.SHOULD_RECAST) {
            return null;
        }
        if (!info.recastable()) {
            return tr("recast.none").withStyle(ChatFormatting.RED);
        }
        if (model.state(info, SpellFlag.SUMMON).active()) {
            return tr("recast.summon").withStyle(ChatFormatting.GOLD);
        }
        if (model.isRecastUnmarked(info)) {
            return tr("recast.unmarked").withStyle(ChatFormatting.YELLOW);
        }
        return null;
    }

    private static String configKey(SpellCategory category, CategoryState state) {
        return state == CategoryState.BUILTIN || state == CategoryState.REMOVED
            ? category.removedConfigName() : category.configName();
    }

    /**
     * 正数格式化：整数不带小数点。
     */
    static String formatNumber(float value) {
        return value == Math.floor(value) && !Float.isInfinite(value) ? Integer.toString((int) value) : Float.toString(value);
    }

    /**
     * 解析输入框里的正数；空串为 {@code null}，非法时抛 {@link NumberFormatException}。
     */
    static Float parsePositive(String text) {
        if (text.isEmpty()) {
            return null;
        }
        float value = Float.parseFloat(text);
        if (!(value > 0) || Float.isInfinite(value)) {
            throw new NumberFormatException(text);
        }
        return value;
    }
}
