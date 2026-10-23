package net.magicterra.winefoxsspellbooks.client.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.api.spell.SpellCategory;
import net.magicterra.winefoxsspellbooks.client.config.SpellCompatModel.CategoryState;
import net.magicterra.winefoxsspellbooks.client.config.SpellCompatModel.SpellInfo;
import net.magicterra.winefoxsspellbooks.magic.data.SpellDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;

/**
 * 法术分类编辑器：在一个列表里按模组列出全部法术，逐个设置女仆用途分类与起手施法距离。
 * <p>
 * 取代原先「法术兼容」小节里的十八个法术 id 列表与三张映射表；底层仍写这些配置项，存档格式不变。
 * 只用原版控件，布局参照按键绑定界面。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
public class SpellCompatScreen extends SpellCompatScreenBase {
    private static final int HEADER_HEIGHT = 60;

    private static final int ROW_WIDTH = 404;

    private static final int ITEM_HEIGHT = 24;

    private static final int TOGGLE_SIZE = 20;

    private static final int RANGE_WIDTH = 30;

    /**
     * 行右侧控件块的宽度：七个行为分类、组间距、两个标记、施法距离、更多按钮。
     */
    private static final int RIGHT_BLOCK = 7 * 21 - 1 + 4 + 2 * 21 - 1 + 4 + RANGE_WIDTH + 2 + TOGGLE_SIZE;

    final SpellCompatModel model;

    private SpellList spellList;

    private String query = "";

    private String modFilter = "";

    private StatusFilter statusFilter = StatusFilter.ALL;

    private int categoryFilter = -1;

    /**
     * 显示状态的筛选。
     */
    enum StatusFilter {
        ALL, UNCLASSIFIED, MODIFIED, CUSTOM_RANGE, UNMARKED_RECAST;

        Component label() {
            return tr("filter.status." + name().toLowerCase(Locale.ROOT));
        }
    }

    public SpellCompatScreen(Context parentContext, Screen parent, Map<String, Object> valueSpecs, String key,
                             Set<? extends UnmodifiableConfig.Entry> entrySet, Component title) {
        super(parentContext, parent, valueSpecs, key, entrySet, title);
        this.model = new SpellCompatModel(entrySet);
    }

    /**
     * 详情页沿用本界面的上下文，以便修改记到同一组配置项上。
     */
    Context context() {
        return context;
    }

    @Override
    protected void addTitle() {
        layout.setHeaderHeight(HEADER_HEIGHT);
        LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(4),
            s -> s.alignHorizontallyCenter().alignVerticallyTop().paddingTop(5));
        header.addChild(new StringWidget(title, font), s -> s.alignHorizontallyCenter());

        LinearLayout toolbar = header.addChild(LinearLayout.horizontal().spacing(4));
        EditBox search = new EditBox(font, 0, 0, 112, 20, tr("search"));
        search.setHint(tr("search").withStyle(ChatFormatting.DARK_GRAY));
        search.setValue(query);
        search.setResponder(text -> {
            query = text.trim().toLowerCase(Locale.ROOT);
            refilter();
        });
        toolbar.addChild(search);

        Tooltip cycleHint = Tooltip.create(tr("filter.hint"));
        List<String> mods = new ArrayList<>();
        mods.add("");
        Map<String, Component> modNames = new HashMap<>();
        for (SpellInfo info : model.spells()) {
            if (modNames.putIfAbsent(info.namespace(), info.modName()) == null) {
                mods.add(info.namespace());
            }
        }
        toolbar.addChild(CycleButton.<String>builder(ns -> ns.isEmpty() ? tr("filter.all") : modNames.get(ns))
            .withValues(mods).withInitialValue(modFilter).withTooltip(v -> cycleHint)
            .create(0, 0, 116, 20, tr("filter.mod"), (b, v) -> {
                modFilter = v;
                refilter();
            }));
        toolbar.addChild(CycleButton.builder(StatusFilter::label)
            .withValues(StatusFilter.values()).withInitialValue(statusFilter).withTooltip(v -> cycleHint)
            .create(0, 0, 90, 20, tr("filter.status"), (b, v) -> {
                statusFilter = v;
                refilter();
            }));
        List<Integer> categoryValues = new ArrayList<>();
        for (int i = -1; i < SpellCompatModel.CATEGORIES.size(); i++) {
            categoryValues.add(i);
        }
        toolbar.addChild(CycleButton.<Integer>builder(i -> i < 0 ? tr("filter.any") : categoryName(SpellCompatModel.CATEGORIES.get(i)))
            .withValues(categoryValues).withInitialValue(categoryFilter).withTooltip(v -> cycleHint)
            .create(0, 0, 70, 20, tr("filter.category"), (b, v) -> {
                categoryFilter = v;
                refilter();
            }));
    }

    @Override
    protected void addContents() {
        spellList = layout.addToContents(new SpellList(minecraft));
        createUndoButton();
        createResetButton();
        refilter();
    }

    @Override
    protected void createResetButton() {
        resetButton = Button.builder(Component.translatable("neoforge.configuration.uitext.reset"), b -> commit(model.resetAll()))
            .tooltip(Tooltip.create(Component.translatable("neoforge.configuration.uitext.reset.tooltip")))
            .width(Button.SMALL_WIDTH).build();
    }

    @Override
    protected boolean isAnyNondefault() {
        return model.isAnyModified();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (spellList != null) {
            spellList.updateSize(width, layout);
        }
    }

    /**
     * 撤销后由基类调用。基类实现只认 OptionsList，这里改为刷新自己的列表。
     */
    @Override
    protected ConfigurationSectionScreen rebuild() {
        if (spellList != null) {
            spellList.refresh();
        }
        return this;
    }

    @Override
    protected void onCommitted() {
        rebuild();
    }

    /**
     * 从详情页返回时界面不会重新 init，在这里刷新一次。
     */
    @Override
    public void added() {
        super.added();
        rebuild();
    }

    private void refilter() {
        if (spellList != null) {
            spellList.refilter();
        }
    }

    private boolean matches(SpellInfo info) {
        if (!modFilter.isEmpty() && !modFilter.equals(info.namespace())) {
            return false;
        }
        if (!query.isEmpty() && !info.id().contains(query)
            && !info.name().getString().toLowerCase(Locale.ROOT).contains(query)) {
            return false;
        }
        boolean statusOk = switch (statusFilter) {
            case ALL -> true;
            case UNCLASSIFIED -> model.isUnclassified(info);
            case MODIFIED -> model.isModified(info);
            case CUSTOM_RANGE -> model.range(info) != null;
            case UNMARKED_RECAST -> model.isRecastUnmarked(info);
        };
        if (!statusOk) {
            return false;
        }
        return categoryFilter < 0 || model.state(info, SpellCompatModel.CATEGORIES.get(categoryFilter)).active();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (spellList == null) {
            return;
        }
        // 列头：画在标题栏底部，与行内按钮对齐
        int y = spellList.getY() - 11;
        for (int i = 0; i < SpellCompatModel.CATEGORIES.size(); i++) {
            SpellCategory category = SpellCompatModel.CATEGORIES.get(i);
            int x = spellList.toggleX(i);
            Component label = categoryShort(category);
            graphics.drawString(font, label, x + (TOGGLE_SIZE - font.width(label)) / 2, y, 0xA0A0A0, false);
            if (mouseX >= x && mouseX < x + TOGGLE_SIZE && mouseY >= y - 1 && mouseY < y + 9) {
                setTooltipForNextRenderPass(Component.empty().append(categoryName(category).withStyle(ChatFormatting.BOLD))
                    .append("\n").append(categoryDesc(category).withStyle(ChatFormatting.GRAY)));
            }
        }
        if (!spellList.compact()) {
            graphics.drawString(font, tr("column.range"), spellList.rangeX(), y, 0xA0A0A0, false);
        }
    }

    /**
     * 法术列表：模组分组标题 + 法术行。行对象按法术缓存，筛选只换可见的子集，输入框焦点不会丢。
     */
    class SpellList extends ContainerObjectSelectionList<SpellList.Entry> {
        private final Map<SpellInfo, SpellRow> rows = new HashMap<>();

        SpellList(Minecraft minecraft) {
            super(minecraft, SpellCompatScreen.this.width, layout.getContentHeight(), layout.getHeaderHeight(), ITEM_HEIGHT);
            for (SpellInfo info : model.spells()) {
                rows.put(info, new SpellRow(info));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(ROW_WIDTH, width - 20);
        }

        /**
         * 屏幕太窄时隐藏行内的施法距离输入框，只在详情页里设置。
         */
        boolean compact() {
            return getRowWidth() < ROW_WIDTH;
        }

        private int rightEdge() {
            return getRowLeft() + getRowWidth() - 6;
        }

        int toggleX(int index) {
            int right = rightEdge() - TOGGLE_SIZE - 2 - (compact() ? 0 : RANGE_WIDTH + 4);
            int flagsStart = right - (2 * 21 - 1);
            if (index >= 7) {
                return flagsStart + (index - 7) * 21;
            }
            return flagsStart - 4 - (7 * 21 - 1) + index * 21;
        }

        int rangeX() {
            return rightEdge() - TOGGLE_SIZE - 2 - RANGE_WIDTH;
        }

        void refilter() {
            List<Entry> entries = new ArrayList<>();
            String currentMod = null;
            List<SpellRow> group = new ArrayList<>();
            for (SpellInfo info : model.spells()) {
                if (!info.namespace().equals(currentMod)) {
                    flushGroup(entries, group);
                    currentMod = info.namespace();
                }
                if (matches(info)) {
                    group.add(rows.get(info));
                }
            }
            flushGroup(entries, group);
            if (entries.isEmpty()) {
                entries.add(new TextEntry(tr("empty").withStyle(ChatFormatting.GRAY)));
            }
            replaceEntries(entries);
            setClampedScrollAmount(getScrollAmount());
        }

        private void flushGroup(List<Entry> entries, List<SpellRow> group) {
            if (!group.isEmpty()) {
                SpellInfo first = group.getFirst().info;
                entries.add(new TextEntry(Component.empty().append(first.modName())
                    .append(Component.literal(" (" + group.size() + ")").withStyle(ChatFormatting.GRAY))));
                entries.addAll(group);
                group.clear();
            }
        }

        void refresh() {
            rows.values().forEach(SpellRow::refresh);
            refilter();
        }

        abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        }

        /**
         * 居中的一行文字：模组分组标题或空列表提示，参照按键绑定界面的分类行。
         */
        class TextEntry extends Entry {
            private final Component text;

            TextEntry(Component text) {
                this.text = text;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                graphics.drawString(font, text, SpellList.this.getX() + SpellList.this.width / 2 - font.width(text) / 2,
                    top + height - 9 - 1, -1, false);
            }

            @Nullable
            @Override
            public ComponentPath nextFocusPath(FocusNavigationEvent event) {
                return null;
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return Collections.emptyList();
            }
        }

        /**
         * 一个法术：图标、名称与 id、九个分类按钮、施法距离输入框、详情按钮。
         */
        class SpellRow extends Entry {
            final SpellInfo info;

            private final List<Button> toggles = new ArrayList<>();

            private final EditBox rangeBox;

            private final Button moreButton;

            private final List<GuiEventListener> children;

            SpellRow(SpellInfo info) {
                this.info = info;
                for (SpellCategory category : SpellCompatModel.CATEGORIES) {
                    toggles.add(Button.builder(Component.empty(), b -> commit(model.toggle(info, category)))
                        .size(TOGGLE_SIZE, TOGGLE_SIZE).build());
                }
                rangeBox = new EditBox(font, 0, 0, RANGE_WIDTH, 20, tr("column.range"));
                rangeBox.setMaxLength(6);
                rangeBox.setFilter(s -> s.matches("[0-9]*\\.?[0-9]*"));
                Float packRange = SpellDataManager.getInstance().getPackSpellRange(info.spell());
                rangeBox.setHint(Component.literal(formatNumber(packRange != null ? packRange : SpellDataManager.DEFAULT_SPELL_RANGE))
                    .withStyle(ChatFormatting.DARK_GRAY));
                rangeBox.setTooltip(Tooltip.create(rangeTooltip(packRange)));
                rangeBox.setResponder(this::onRangeEdited);
                moreButton = Button.builder(Component.literal("…"), b -> minecraft.setScreen(new SpellCompatDetailScreen(SpellCompatScreen.this, info)))
                    .size(TOGGLE_SIZE, TOGGLE_SIZE)
                    .tooltip(Tooltip.create(Component.empty().append(tr("more").withStyle(ChatFormatting.BOLD))
                        .append("\n").append(tr("more.tooltip").withStyle(ChatFormatting.GRAY))))
                    .build();
                List<GuiEventListener> all = new ArrayList<>(toggles);
                all.add(rangeBox);
                all.add(moreButton);
                children = ImmutableList.copyOf(all);
                refresh();
            }

            private Component rangeTooltip(@Nullable Float packRange) {
                return Component.empty().append(tr("range").withStyle(ChatFormatting.BOLD))
                    .append("\n").append((packRange != null
                        ? tr("range.pack_default", formatNumber(packRange))
                        : tr("range.no_pack_default", formatNumber(SpellDataManager.DEFAULT_SPELL_RANGE))).withStyle(ChatFormatting.GRAY))
                    .append("\n").append(tr("range.clear").withStyle(ChatFormatting.DARK_GRAY));
            }

            private void onRangeEdited(String text) {
                Float range;
                try {
                    range = parsePositive(text);
                } catch (NumberFormatException e) {
                    rangeBox.setTextColor(0xFF5555);
                    return;
                }
                rangeBox.setTextColor(range != null ? 0xFFFF55 : 0xE0E0E0);
                commit(model.setRange(info, range));
            }

            void refresh() {
                for (int i = 0; i < toggles.size(); i++) {
                    SpellCategory category = SpellCompatModel.CATEGORIES.get(i);
                    CategoryState state = model.state(info, category);
                    Button toggle = toggles.get(i);
                    toggle.setMessage(styled(categoryShort(category), state, ChatFormatting.DARK_GRAY));
                    toggle.setTooltip(Tooltip.create(categoryTooltip(model, info, category)));
                    toggle.active = !model.isLocked(info, category);
                }
                // 正在输入时不回写，免得把「12.」这类中间态改掉
                if (!rangeBox.isFocused()) {
                    Float range = model.range(info);
                    String want = range != null ? formatNumber(range) : "";
                    if (!rangeBox.getValue().equals(want)) {
                        rangeBox.setValue(want);
                    }
                }
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                graphics.blit(info.spell().getSpellIconResource(), left, top + 2, 0, 0, 16, 16, 16, 16);

                boolean compact = compact();
                for (int i = 0; i < toggles.size(); i++) {
                    Button toggle = toggles.get(i);
                    toggle.setPosition(toggleX(i), top);
                    toggle.render(graphics, mouseX, mouseY, partialTick);
                }
                rangeBox.visible = !compact;
                if (!compact) {
                    rangeBox.setPosition(rangeX(), top);
                    rangeBox.render(graphics, mouseX, mouseY, partialTick);
                }
                moreButton.setPosition(rightEdge() - TOGGLE_SIZE, top);
                moreButton.render(graphics, mouseX, mouseY, partialTick);

                // 名称与 id，超宽截断
                int textX = left + 20;
                int textWidth = toggleX(0) - 4 - textX;
                boolean modified = model.isModified(info);
                boolean unclassified = model.isUnclassified(info);
                String prefix = modified ? "* " : "";
                int prefixWidth = font.width(prefix);
                if (modified) {
                    graphics.drawString(font, prefix, textX, top + 1, 0xFFFF55);
                }
                graphics.drawString(font, truncate(info.name().getString(), textWidth - prefixWidth), textX + prefixWidth, top + 1,
                    unclassified ? 0xA0A0A0 : 0xFFFFFF);
                graphics.drawString(font, truncate(info.id(), textWidth), textX, top + 11, 0x808080);

                if (mouseX >= left && mouseX < left + 20 + textWidth && mouseY >= top && mouseY < top + height) {
                    MutableComponent tip = Component.empty().append(info.name().copy().withStyle(ChatFormatting.BOLD))
                        .append("\n").append(Component.literal(info.id()).withStyle(ChatFormatting.DARK_GRAY))
                        .append("\n").append(info.modName().copy().withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                    if (unclassified) {
                        tip.append("\n").append(tr("unclassified").withStyle(ChatFormatting.RED));
                    }
                    if (modified) {
                        tip.append("\n").append(tr("modified").withStyle(ChatFormatting.YELLOW));
                    }
                    setTooltipForNextRenderPass(tip);
                }
            }

            private String truncate(String text, int maxWidth) {
                if (font.width(text) <= maxWidth) {
                    return text;
                }
                return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...";
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                List<NarratableEntry> list = new ArrayList<>(toggles);
                list.add(rangeBox);
                list.add(moreButton);
                return list;
            }
        }
    }
}
