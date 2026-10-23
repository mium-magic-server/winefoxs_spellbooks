package net.magicterra.winefoxsspellbooks.client.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.api.spell.SpellCategory;
import net.magicterra.winefoxsspellbooks.api.spell.SpellFlag;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 法术分类编辑器的数据模型：把「法术 × 分类」的开关状态映射到 {@code spell_compat} 小节的配置值上。
 * <p>
 * 内置分类读法术标签，用户改动只落在「额外法术」「移除法术」两组列表与三张映射表里。
 * 所有修改都以 {@link Change} 列表返回，由界面统一交给 NeoForge 的撤销管理器执行，模型自身不写配置。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
public final class SpellCompatModel {
    static final String RANGE_KEY = "extra_spell_casting_range";

    static final String EFFECT_KEY = "extra_spell_caused_effects";

    static final String VALUE_KEY = "extra_spell_value";

    /**
     * 界面上的分类列顺序：七个行为分类在前，两个标记在后。
     */
    public static final List<SpellCategory> CATEGORIES;

    static {
        List<SpellCategory> list = new ArrayList<>(List.of(MaidSpellAction.ALL));
        list.addAll(List.of(SpellFlag.ALL));
        CATEGORIES = List.copyOf(list);
    }

    /**
     * 一个法术在某个分类上的状态。
     */
    public enum CategoryState {
        /** 未标记 */
        OFF,
        /** 数据包内置 */
        BUILTIN,
        /** 用户追加 */
        ADDED,
        /** 用户移除了内置标记 */
        REMOVED;

        public boolean active() {
            return this == BUILTIN || this == ADDED;
        }
    }

    /**
     * 一次待执行的配置修改。
     */
    public record Change<T>(ModConfigSpec.ConfigValue<T> value, String key, T newValue) {
    }

    /**
     * 列表里显示的一个法术。
     *
     * @param recastable 是否有二段咏唱，见 {@link #probeRecast}
     */
    public record SpellInfo(AbstractSpell spell, String id, String namespace, Component modName, Component name,
                            boolean recastable) {
    }

    private final Map<String, ModConfigSpec.ConfigValue<?>> values = new HashMap<>();

    private final Map<SpellCategory, Set<AbstractSpell>> builtin = new HashMap<>();

    private final List<SpellInfo> spells;

    /**
     * @param entries {@code spell_compat} 小节的配置条目
     */
    public SpellCompatModel(Collection<? extends UnmodifiableConfig.Entry> entries) {
        for (UnmodifiableConfig.Entry entry : entries) {
            if (entry.getRawValue() instanceof ModConfigSpec.ConfigValue<?> cv) {
                values.put(entry.getKey(), cv);
            }
        }
        for (SpellCategory category : CATEGORIES) {
            Set<AbstractSpell> set = new HashSet<>();
            SpellRegistry.REGISTRY.getTag(category.tag()).ifPresent(tag -> tag.forEach(holder -> set.add(holder.value())));
            builtin.put(category, set);
        }
        spells = collectSpells();
    }

    private static List<SpellInfo> collectSpells() {
        Map<String, Component> modNames = new HashMap<>();
        List<SpellInfo> result = new ArrayList<>();
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            if (spell == SpellRegistry.none()) {
                continue;
            }
            ResourceLocation id = spell.getSpellResource();
            Component modName = modNames.computeIfAbsent(id.getNamespace(), ns -> Component.literal(
                ModList.get().getModContainerById(ns).map(c -> c.getModInfo().getDisplayName()).orElse(ns)));
            result.add(new SpellInfo(spell, id.toString(), id.getNamespace(), modName,
                Component.translatable(spell.getComponentId()), probeRecast(spell, Minecraft.getInstance().player)));
        }
        // 本体排最前，其余按模组显示名、再按法术 id 排
        result.sort(Comparator.<SpellInfo>comparingInt(s -> s.namespace().equals("irons_spellbooks") ? 0 : 1)
            .thenComparing(s -> s.modName().getString())
            .thenComparing(SpellInfo::id));
        return List.copyOf(result);
    }

    /**
     * 法术是否有二段咏唱：任一可用等级的 {@code getRecastCount} 大于 0。
     * <p>
     * 只说明「能不能二段咏唱」，说明不了「该不该」——召唤类的二段咏唱会收回召唤物，雷霆步会传送回起点，
     * 所以「多次咏唱」标记仍需逐个法术设置，这里只用来提示与拦住无效的标记。
     * 个别附属的实现会读施法者属性，查询抛异常时按「有」处理，不拦用户。
     */
    private static boolean probeRecast(AbstractSpell spell, @Nullable LivingEntity probe) {
        try {
            for (int level = Math.max(1, spell.getMinLevel()); level <= spell.getMaxLevel(); level++) {
                if (spell.getRecastCount(level, probe) > 0) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }

    public List<SpellInfo> spells() {
        return spells;
    }

    // ---------- 分类 ----------

    public boolean isBuiltin(SpellInfo info, SpellCategory category) {
        return builtin.get(category).contains(info.spell());
    }

    public CategoryState state(SpellInfo info, SpellCategory category) {
        if (isBuiltin(info, category)) {
            return list(category.removedConfigName()).contains(info.id()) ? CategoryState.REMOVED : CategoryState.BUILTIN;
        }
        return list(category.configName()).contains(info.id()) ? CategoryState.ADDED : CategoryState.OFF;
    }

    /**
     * 未分类：没有任何生效的行为分类，女仆不会使用。标记不算分类。
     */
    public boolean isUnclassified(SpellInfo info) {
        for (MaidSpellAction action : MaidSpellAction.ALL) {
            if (state(info, action).active()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 该按钮是否锁定：没有二段咏唱的法术加「多次咏唱」标记不会有任何效果。
     * 已有的标记（内置或旧配置留下的）不锁，好让用户能撤掉。
     */
    public boolean isLocked(SpellInfo info, SpellCategory category) {
        return category == SpellFlag.SHOULD_RECAST && !info.recastable() && state(info, category) == CategoryState.OFF;
    }

    /**
     * 有二段咏唱却没标「多次咏唱」、也不是召唤类：可能是漏标，供用户核对。
     */
    public boolean isRecastUnmarked(SpellInfo info) {
        return info.recastable() && !state(info, SpellFlag.SHOULD_RECAST).active() && !state(info, SpellFlag.SUMMON).active();
    }

    /**
     * 切换一个分类：内置的在「移除」列表里增删，非内置的在「额外」列表里增删。
     */
    public List<Change<?>> toggle(SpellInfo info, SpellCategory category) {
        String key = isBuiltin(info, category) ? category.removedConfigName() : category.configName();
        List<String> current = list(key);
        List<String> next = new ArrayList<>(current);
        if (!next.remove(info.id())) {
            next.add(info.id());
        }
        return List.of(listChange(key, next));
    }

    // ---------- 映射表 ----------

    @Nullable
    public Float range(SpellInfo info) {
        return map(RANGE_KEY).get(info.id()) instanceof Number n ? n.floatValue() : null;
    }

    @Nullable
    public String effect(SpellInfo info) {
        return map(EFFECT_KEY).get(info.id()) instanceof String s ? s : null;
    }

    @Nullable
    public Float value(SpellInfo info) {
        return map(VALUE_KEY).get(info.id()) instanceof Number n ? n.floatValue() : null;
    }

    /**
     * 设置或清除（{@code null}）施法距离覆盖；与现值相同时返回空表。
     */
    public List<Change<?>> setRange(SpellInfo info, @Nullable Float range) {
        return putMap(RANGE_KEY, info.id(), range);
    }

    public List<Change<?>> setEffect(SpellInfo info, @Nullable String effect) {
        return putMap(EFFECT_KEY, info.id(), effect);
    }

    public List<Change<?>> setValue(SpellInfo info, @Nullable Float value) {
        return putMap(VALUE_KEY, info.id(), value);
    }

    // ---------- 修改状态与重置 ----------

    public boolean isModified(SpellInfo info) {
        for (SpellCategory category : CATEGORIES) {
            if (list(category.configName()).contains(info.id()) || list(category.removedConfigName()).contains(info.id())) {
                return true;
            }
        }
        return map(RANGE_KEY).containsKey(info.id()) || map(EFFECT_KEY).containsKey(info.id())
            || map(VALUE_KEY).containsKey(info.id());
    }

    public boolean isAnyModified() {
        for (String key : listKeys()) {
            if (!list(key).isEmpty()) {
                return true;
            }
        }
        return !map(RANGE_KEY).isEmpty() || !map(EFFECT_KEY).isEmpty() || !map(VALUE_KEY).isEmpty();
    }

    /**
     * 清除一个法术的全部用户修改。
     */
    public List<Change<?>> resetSpell(SpellInfo info) {
        List<Change<?>> changes = new ArrayList<>();
        for (String key : listKeys()) {
            List<String> current = list(key);
            if (current.contains(info.id())) {
                List<String> next = new ArrayList<>(current);
                next.removeIf(info.id()::equals);
                changes.add(listChange(key, next));
            }
        }
        changes.addAll(putMap(RANGE_KEY, info.id(), null));
        changes.addAll(putMap(EFFECT_KEY, info.id(), null));
        changes.addAll(putMap(VALUE_KEY, info.id(), null));
        return changes;
    }

    /**
     * 清空整个小节的用户修改，包括未安装模组的法术 id。
     */
    public List<Change<?>> resetAll() {
        List<Change<?>> changes = new ArrayList<>();
        for (String key : listKeys()) {
            if (!list(key).isEmpty()) {
                changes.add(listChange(key, List.of()));
            }
        }
        for (String key : List.of(RANGE_KEY, EFFECT_KEY, VALUE_KEY)) {
            if (!map(key).isEmpty()) {
                changes.add(mapChange(key, newConfig()));
            }
        }
        return changes;
    }

    // ---------- 配置值读写 ----------

    private static List<String> listKeys() {
        List<String> keys = new ArrayList<>();
        for (SpellCategory category : CATEGORIES) {
            keys.add(category.configName());
            keys.add(category.removedConfigName());
        }
        return keys;
    }

    /**
     * 读取列表配置的当前值（含未保存的修改）。读 {@code getRaw} 而非 {@code get}，后者有缓存。
     */
    private List<String> list(String key) {
        if (values.get(key) instanceof ModConfigSpec.ConfigValue<?> cv && cv.getRaw() instanceof List<?> raw) {
            List<String> result = new ArrayList<>(raw.size());
            for (Object o : raw) {
                if (o instanceof String s) {
                    result.add(s);
                }
            }
            return result;
        }
        return List.of();
    }

    private Map<String, Object> map(String key) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (values.get(key) instanceof ModConfigSpec.ConfigValue<?> cv && cv.getRaw() instanceof UnmodifiableConfig config) {
            for (UnmodifiableConfig.Entry entry : config.entrySet()) {
                result.put(entry.getKey(), entry.getRawValue());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Change<?> listChange(String key, List<String> next) {
        return new Change<>((ModConfigSpec.ConfigValue<List<String>>) values.get(key), key, List.copyOf(next));
    }

    @SuppressWarnings("unchecked")
    private Change<?> mapChange(String key, Config next) {
        return new Change<>((ModConfigSpec.ConfigValue<Config>) values.get(key), key, next);
    }

    private List<Change<?>> putMap(String key, String spellId, @Nullable Object value) {
        if (!values.containsKey(key)) {
            return List.of();
        }
        Map<String, Object> current = map(key);
        Object old = current.get(spellId);
        if (value == null ? old == null : value.equals(old)) {
            return List.of();
        }
        Config next = newConfig();
        for (Map.Entry<String, Object> entry : current.entrySet()) {
            // 法术 id 里没有点号，但仍按单段路径写，免得 NightConfig 把键拆成多级
            next.set(List.of(entry.getKey()), entry.getValue());
        }
        if (value == null) {
            next.remove(List.of(spellId));
        } else {
            next.set(List.of(spellId), value);
        }
        return List.of(mapChange(key, next));
    }

    private static Config newConfig() {
        return Config.of(LinkedHashMap::new, InMemoryFormat.withUniversalSupport());
    }
}
