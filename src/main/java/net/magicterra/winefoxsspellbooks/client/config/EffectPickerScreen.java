package net.magicterra.winefoxsspellbooks.client.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.fml.ModList;

import static net.magicterra.winefoxsspellbooks.client.config.SpellCompatScreenBase.tr;

/**
 * 药水效果选择页：列出全部已注册的效果，点选即返回。第一行固定为「使用数据包默认」，选它即清除覆盖。
 * <p>
 * 本页不直接写配置，选择结果交给回调，由详情页记入撤销记录。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
public class EffectPickerScreen extends Screen {
    private static final int ROW_WIDTH = 300;

    private static final int ITEM_HEIGHT = 24;

    private final Screen parent;

    private final Consumer<String> onPick;

    @Nullable
    private final String current;

    @Nullable
    private final Holder<MobEffect> packDefault;

    private final List<EffectInfo> effects;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 54, 33);

    private EffectList effectList;

    private String query = "";

    private String modFilter = "";

    @Nullable
    private MobEffectCategory categoryFilter;

    private record EffectInfo(Holder<MobEffect> holder, String id, String namespace, Component modName) {
    }

    /**
     * @param current     配置里当前的覆盖值，未覆盖为 {@code null}；可能是未安装模组的 id
     * @param packDefault 数据包给出的默认效果，可能为 {@code null}
     * @param onPick      选中后的回调，{@code null} 表示改回数据包默认
     */
    public EffectPickerScreen(Screen parent, @Nullable String current,
                              @Nullable Holder<MobEffect> packDefault, Consumer<String> onPick) {
        super(Component.translatable("neoforge.configuration.uitext.breadcrumb.order", parent.getTitle(),
            Component.translatable("neoforge.configuration.uitext.breadcrumb.separator").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            tr("detail.effect")));
        this.parent = parent;
        this.current = current;
        this.packDefault = packDefault;
        this.onPick = onPick;
        this.effects = collectEffects();
    }

    private static List<EffectInfo> collectEffects() {
        Map<String, Component> modNames = new HashMap<>();
        List<EffectInfo> result = new ArrayList<>();
        BuiltInRegistries.MOB_EFFECT.holders().forEach(holder -> {
            ResourceLocation id = holder.key().location();
            Component modName = modNames.computeIfAbsent(id.getNamespace(), ns -> Component.literal(
                ModList.get().getModContainerById(ns).map(c -> c.getModInfo().getDisplayName()).orElse(ns)));
            result.add(new EffectInfo(holder, id.toString(), id.getNamespace(), modName));
        });
        // 原版排最前，其余按模组显示名、再按效果 id 排
        result.sort(Comparator.<EffectInfo>comparingInt(e -> e.namespace().equals("minecraft") ? 0 : 1)
            .thenComparing(e -> e.modName().getString())
            .thenComparing(EffectInfo::id));
        return List.copyOf(result);
    }

    /**
     * 效果性质的标签：增益蓝、减益红、中性灰，与原版提示框配色一致（中性改用灰色以便区分）。
     */
    static MutableComponent categoryLabel(MobEffectCategory category) {
        MutableComponent label = tr("effect_picker.category." + category.name().toLowerCase(Locale.ROOT));
        return switch (category) {
            case BENEFICIAL -> label.withStyle(ChatFormatting.BLUE);
            case HARMFUL -> label.withStyle(ChatFormatting.RED);
            case NEUTRAL -> label.withStyle(ChatFormatting.GRAY);
        };
    }

    @Override
    protected void init() {
        LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(4),
            s -> s.alignHorizontallyCenter().alignVerticallyTop().paddingTop(5));
        header.addChild(new StringWidget(title, font), s -> s.alignHorizontallyCenter());
        LinearLayout toolbar = header.addChild(LinearLayout.horizontal().spacing(4));

        EditBox search = new EditBox(font, 0, 0, 120, 20, tr("effect_picker.search"));
        search.setHint(tr("effect_picker.search").withStyle(ChatFormatting.DARK_GRAY));
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
        for (EffectInfo info : effects) {
            if (modNames.putIfAbsent(info.namespace(), info.modName()) == null) {
                mods.add(info.namespace());
            }
        }
        toolbar.addChild(CycleButton.<String>builder(ns -> ns.isEmpty() ? tr("filter.all") : modNames.get(ns))
            .withValues(mods).withInitialValue(modFilter).withTooltip(v -> cycleHint)
            .create(0, 0, 100, 20, tr("filter.mod"), (b, v) -> {
                modFilter = v;
                refilter();
            }));
        // CycleButton 的取值表是 ImmutableList，不能放 null，「全部」用 Optional.empty() 表示
        List<Optional<MobEffectCategory>> categories = new ArrayList<>();
        categories.add(Optional.empty());
        for (MobEffectCategory category : MobEffectCategory.values()) {
            categories.add(Optional.of(category));
        }
        toolbar.addChild(CycleButton.<Optional<MobEffectCategory>>builder(c -> c.<Component>map(EffectPickerScreen::categoryLabel).orElse(tr("filter.all")))
            .withValues(categories).withInitialValue(Optional.ofNullable(categoryFilter)).withTooltip(v -> cycleHint)
            .create(0, 0, 90, 20, tr("effect_picker.filter.category"), (b, v) -> {
                categoryFilter = v.orElse(null);
                refilter();
            }));

        effectList = layout.addToContents(new EffectList(minecraft));
        layout.addToFooter(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose()).width(Button.DEFAULT_WIDTH).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
        refilter();
        effectList.scrollToSelected();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        if (effectList != null) {
            effectList.updateSize(width, layout);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void pick(@Nullable String effectId) {
        onPick.accept(effectId);
        onClose();
    }

    private void refilter() {
        if (effectList != null) {
            effectList.refilter();
        }
    }

    private boolean matches(EffectInfo info) {
        if (!modFilter.isEmpty() && !modFilter.equals(info.namespace())) {
            return false;
        }
        if (categoryFilter != null && info.holder().value().getCategory() != categoryFilter) {
            return false;
        }
        return query.isEmpty() || info.id().contains(query)
            || info.holder().value().getDisplayName().getString().toLowerCase(Locale.ROOT).contains(query);
    }

    /**
     * 效果列表。行对象一次建好，筛选只换可见子集。
     */
    class EffectList extends ObjectSelectionList<EffectList.Entry> {
        private final DefaultEntry defaultEntry = new DefaultEntry();

        private final List<EffectEntry> rows = new ArrayList<>();

        EffectList(Minecraft minecraft) {
            super(minecraft, EffectPickerScreen.this.width, layout.getContentHeight(), layout.getHeaderHeight(), ITEM_HEIGHT);
            for (EffectInfo info : effects) {
                rows.add(new EffectEntry(info));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(ROW_WIDTH, width - 20);
        }

        void refilter() {
            List<Entry> entries = new ArrayList<>();
            entries.add(defaultEntry);
            for (EffectEntry row : rows) {
                if (matches(row.info)) {
                    entries.add(row);
                }
            }
            replaceEntries(entries);
            setClampedScrollAmount(getScrollAmount());
            setSelected(entries.stream().filter(Entry::isCurrent).findFirst().orElse(null));
        }

        void scrollToSelected() {
            if (getSelected() != null) {
                centerScrollOn(getSelected());
            }
        }

        abstract class Entry extends ObjectSelectionList.Entry<Entry> {
            abstract boolean isCurrent();

            abstract void onPicked();

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                onPicked();
                return true;
            }
        }

        /**
         * 第一行：使用数据包默认，即清除覆盖。
         */
        class DefaultEntry extends Entry {
            private final Component label;

            DefaultEntry() {
                Component packName = packDefault != null ? packDefault.value().getDisplayName() : tr("detail.effect.none");
                this.label = tr("effect_picker.use_default", packName);
            }

            @Override
            boolean isCurrent() {
                return current == null;
            }

            @Override
            void onPicked() {
                pick(null);
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                graphics.drawString(font, label, left + 22, top + (height - 9) / 2, 0xFFFFFF);
            }

            @Override
            public Component getNarration() {
                return label;
            }
        }

        /**
         * 一个效果：图标、名称与 id、增益 / 减益 / 中性标签。
         */
        class EffectEntry extends Entry {
            final EffectInfo info;

            EffectEntry(EffectInfo info) {
                this.info = info;
            }

            @Override
            boolean isCurrent() {
                return info.id().equals(current);
            }

            @Override
            void onPicked() {
                pick(info.id());
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(info.holder());
                graphics.blit(left, top + 1, 0, 18, 18, sprite);
                MobEffect effect = info.holder().value();
                Component category = categoryLabel(effect.getCategory());
                int categoryX = left + width - 4 - font.width(category);
                int textWidth = categoryX - 6 - (left + 22);
                graphics.drawString(font, font.plainSubstrByWidth(effect.getDisplayName().getString(), textWidth),
                    left + 22, top + 1, 0xFFFFFF);
                graphics.drawString(font, SpellCompatScreenBase.truncateStart(font, info.id(), textWidth), left + 22, top + 11, 0x808080);
                graphics.drawString(font, category, categoryX, top + (height - 9) / 2, 0xFFFFFF);
            }

            @Override
            public Component getNarration() {
                return info.holder().value().getDisplayName();
            }
        }
    }
}
