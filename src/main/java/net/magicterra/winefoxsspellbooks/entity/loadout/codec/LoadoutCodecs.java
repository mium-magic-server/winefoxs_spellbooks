package net.magicterra.winefoxsspellbooks.entity.loadout.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.ArmorConfig;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.ArmorSlotConfig;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.BroomMode;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadout;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadoutTier;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.ModelFilter;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.RollRange;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.SpellPoolConfig;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.ItemEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.ItemTagEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.LoadoutEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.SpellEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.SpellTagEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.ItemPool;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.SpellPool;
import net.minecraft.resources.ResourceLocation;

/**
 * 装备配置系统的 Codec 定义
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public final class LoadoutCodecs {
    private LoadoutCodecs() {
    }

    // ==================== RollRange ====================
    /**
     * RollRange Codec - 支持整数或 {min, max} 对象
     */
    public static final Codec<RollRange> ROLL_RANGE = Codec.either(
        Codec.INT,
        RecordCodecBuilder.<RollRange>create(instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(RollRange::min),
            Codec.INT.fieldOf("max").forGetter(RollRange::max)
        ).apply(instance, RollRange::new))
    ).xmap(
        either -> either.map(RollRange::fixed, Function.identity()),
        range -> range.isFixed() ? Either.left(range.min()) : Either.right(range)
    );

    // ==================== ModelFilter ====================
    /**
     * ModelFilter.Mode Codec
     */
    public static final Codec<ModelFilter.Mode> MODEL_FILTER_MODE = Codec.STRING.xmap(
        ModelFilter.Mode::fromString,
        ModelFilter.Mode::getName
    );

    /**
     * ModelFilter Codec
     */
    public static final Codec<ModelFilter> MODEL_FILTER = RecordCodecBuilder.create(instance -> instance.group(
        MODEL_FILTER_MODE.optionalFieldOf("mode", ModelFilter.Mode.ALL).forGetter(ModelFilter::mode),
        ResourceLocation.CODEC.listOf().optionalFieldOf("models", List.of()).forGetter(ModelFilter::models)
    ).apply(instance, ModelFilter::new));

    // ==================== BroomMode ====================
    /**
     * BroomMode Codec
     */
    public static final Codec<BroomMode> BROOM_MODE = Codec.STRING.xmap(
        BroomMode::fromString,
        BroomMode::getName
    );

    // ==================== Entry Types ====================

    /**
     * 物品相关条目 Codec（item 或 tag）
     */
    public static final Codec<LoadoutEntry> ITEM_LOADOUT_ENTRY = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("type").forGetter(LoadoutEntry::type),
        ResourceLocation.CODEC.fieldOf("name").forGetter(entry -> {
            if (entry instanceof ItemEntry e) return e.name();
            if (entry instanceof ItemTagEntry e) return e.name();
            throw new IllegalStateException("Unknown entry type: " + entry);
        }),
        Codec.INT.optionalFieldOf("weight", 1).forGetter(LoadoutEntry::weight),
        Codec.BOOL.optionalFieldOf("expand", false).forGetter(entry -> {
            if (entry instanceof ItemTagEntry e) return e.expand();
            return false;
        })
    ).apply(instance, (type, name, weight, expand) -> {
        return switch (type) {
            case "item" -> new ItemEntry(name, weight);
            case "tag" -> new ItemTagEntry(name, weight, expand);
            default -> throw new IllegalArgumentException("Unknown item entry type: " + type);
        };
    }));

    /**
     * 法术相关条目 Codec（spell 或 tag）
     */
    public static final Codec<LoadoutEntry> SPELL_LOADOUT_ENTRY = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("type").forGetter(LoadoutEntry::type),
        ResourceLocation.CODEC.fieldOf("name").forGetter(entry -> {
            if (entry instanceof SpellEntry e) return e.name();
            if (entry instanceof SpellTagEntry e) return e.name();
            throw new IllegalStateException("Unknown entry type: " + entry);
        }),
        Codec.INT.optionalFieldOf("weight", 1).forGetter(LoadoutEntry::weight),
        Codec.BOOL.optionalFieldOf("expand", false).forGetter(entry -> {
            if (entry instanceof SpellTagEntry e) return e.expand();
            return false;
        })
    ).apply(instance, (type, name, weight, expand) -> switch (type) {
        case "spell" -> new SpellEntry(name, weight);
        case "tag" -> new SpellTagEntry(name, weight, expand);
        default -> throw new IllegalArgumentException("Unknown spell entry type: " + type);
    }));

    // ==================== Pools ====================
    /**
     * ItemPool Codec
     */
    public static final Codec<ItemPool> ITEM_POOL = RecordCodecBuilder.create(instance -> instance.group(
        ROLL_RANGE.fieldOf("rolls").forGetter(ItemPool::rolls),
        ITEM_LOADOUT_ENTRY.listOf().fieldOf("entries").forGetter(ItemPool::entries)
    ).apply(instance, ItemPool::new));

    /**
     * SpellPool Codec
     */
    public static final Codec<SpellPool> SPELL_POOL = RecordCodecBuilder.create(instance -> instance.group(
        ROLL_RANGE.fieldOf("rolls").forGetter(SpellPool::rolls),
        SPELL_LOADOUT_ENTRY.listOf().fieldOf("entries").forGetter(SpellPool::entries)
    ).apply(instance, SpellPool::new));

    // ==================== Armor Config ====================
    /**
     * ArmorSlotConfig Codec
     */
    public static final Codec<ArmorSlotConfig> ARMOR_SLOT_CONFIG = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.optionalFieldOf("chance", 0.0f).forGetter(ArmorSlotConfig::chance),
        ITEM_POOL.listOf().optionalFieldOf("pools", List.of()).forGetter(ArmorSlotConfig::pools)
    ).apply(instance, ArmorSlotConfig::new));

    /**
     * ArmorConfig Codec
     */
    public static final Codec<ArmorConfig> ARMOR_CONFIG = RecordCodecBuilder.create(instance -> instance.group(
        ARMOR_SLOT_CONFIG.optionalFieldOf("helmet").forGetter(ArmorConfig::helmet),
        ARMOR_SLOT_CONFIG.optionalFieldOf("chestplate").forGetter(ArmorConfig::chestplate),
        ARMOR_SLOT_CONFIG.optionalFieldOf("leggings").forGetter(ArmorConfig::leggings),
        ARMOR_SLOT_CONFIG.optionalFieldOf("boots").forGetter(ArmorConfig::boots)
    ).apply(instance, ArmorConfig::new));

    // ==================== Spell Pool Config ====================
    /**
     * SpellPoolConfig Codec
     */
    public static final Codec<SpellPoolConfig> SPELL_POOL_CONFIG = RecordCodecBuilder.create(instance -> instance.group(
        SPELL_POOL.optionalFieldOf("attack").forGetter(SpellPoolConfig::attack),
        SPELL_POOL.optionalFieldOf("defense").forGetter(SpellPoolConfig::defense),
        SPELL_POOL.optionalFieldOf("movement").forGetter(SpellPoolConfig::movement),
        SPELL_POOL.optionalFieldOf("support").forGetter(SpellPoolConfig::support),
        SPELL_POOL.optionalFieldOf("positive_effect").forGetter(SpellPoolConfig::positiveEffect),
        SPELL_POOL.optionalFieldOf("negative_effect").forGetter(SpellPoolConfig::negativeEffect),
        SPELL_POOL.optionalFieldOf("support_other").forGetter(SpellPoolConfig::supportOther)
    ).apply(instance, SpellPoolConfig::new));

    // ==================== Tier ====================
    /**
     * MaidLoadoutTier Codec
     * 注意：使用 conditions 格式来兼容 JSON schema
     */
    public static final Codec<MaidLoadoutTier> TIER = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("min_level").forGetter(MaidLoadoutTier::minLevel),
        Codec.INT.fieldOf("max_level").forGetter(MaidLoadoutTier::maxLevel),
        ITEM_POOL.optionalFieldOf("weapon").forGetter(MaidLoadoutTier::weapon),
        ARMOR_CONFIG.optionalFieldOf("armor").forGetter(MaidLoadoutTier::armor),
        SPELL_POOL_CONFIG.optionalFieldOf("spells").forGetter(MaidLoadoutTier::spells),
        Codec.INT.optionalFieldOf("spell_level_cap", MaidLoadoutTier.DEFAULT_SPELL_LEVEL_CAP).forGetter(MaidLoadoutTier::spellLevelCap)
    ).apply(instance, MaidLoadoutTier::new));

    /**
     * 支持 conditions 格式的 Tier Codec
     */
    public static final Codec<MaidLoadoutTier> TIER_WITH_CONDITIONS = RecordCodecBuilder.create(instance -> instance.group(
        levelConditionCodec().fieldOf("conditions").forGetter(tier -> new int[]{tier.minLevel(), tier.maxLevel()}),
        ITEM_POOL.optionalFieldOf("weapon").forGetter(MaidLoadoutTier::weapon),
        ARMOR_CONFIG.optionalFieldOf("armor").forGetter(MaidLoadoutTier::armor),
        SPELL_POOL_CONFIG.optionalFieldOf("spells").forGetter(MaidLoadoutTier::spells),
        Codec.INT.optionalFieldOf("spell_level_cap", MaidLoadoutTier.DEFAULT_SPELL_LEVEL_CAP).forGetter(MaidLoadoutTier::spellLevelCap)
    ).apply(instance, (levels, weapon, armor, spells, spellLevelCap) ->
        new MaidLoadoutTier(levels[0], levels[1], weapon, armor, spells, spellLevelCap)));

    /**
     * 条件格式的等级范围 Codec
     */
    private static Codec<int[]> levelConditionCodec() {
        Codec<int[]> conditionEntry = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("condition").forGetter(arr -> "winefoxs_spellbooks:summon_level"),
            Codec.INT.fieldOf("min").forGetter(arr -> arr[0]),
            Codec.INT.fieldOf("max").forGetter(arr -> arr[1])
        ).apply(instance, (condition, min, max) -> new int[]{min, max}));

        return conditionEntry.listOf().xmap(
            list -> list.isEmpty() ? new int[]{1, Integer.MAX_VALUE} : list.getFirst(),
            List::of
        );
    }

    // ==================== MaidLoadout ====================
    /**
     * MaidLoadout Codec（不含 ID，ID 由文件路径决定）
     */
    public static final Codec<MaidLoadoutData> MAID_LOADOUT_DATA = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("type", "winefoxs_spellbooks:maid_loadout").forGetter(d -> "winefoxs_spellbooks:maid_loadout"),
        Codec.INT.optionalFieldOf("weight", MaidLoadout.DEFAULT_WEIGHT).forGetter(MaidLoadoutData::weight),
        MODEL_FILTER.optionalFieldOf("model_filter", ModelFilter.ALL).forGetter(MaidLoadoutData::modelFilter),
        BROOM_MODE.optionalFieldOf("broom_mode", BroomMode.DEFAULT).forGetter(MaidLoadoutData::broomMode),
        TIER_WITH_CONDITIONS.listOf().fieldOf("tiers").forGetter(MaidLoadoutData::tiers)
    ).apply(instance, (type, weight, modelFilter, broomMode, tiers) -> new MaidLoadoutData(weight, modelFilter, broomMode, tiers)));

    /**
     * 中间数据类，用于从 JSON 解析（不含 ID）
     */
    public record MaidLoadoutData(int weight, ModelFilter modelFilter, BroomMode broomMode, List<MaidLoadoutTier> tiers) {
        public MaidLoadout toLoadout(ResourceLocation id) {
            return new MaidLoadout(id, weight, modelFilter, broomMode, tiers);
        }
    }

}
