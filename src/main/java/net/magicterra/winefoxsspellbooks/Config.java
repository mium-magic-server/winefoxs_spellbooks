package net.magicterra.winefoxsspellbooks;

import com.electronwill.nightconfig.core.InMemoryFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 配置文件
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-31 02:08
 */
public class Config {
    private static final String TRANSLATE_KEY = "config." + WinefoxsSpellbooks.MODID;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static ModConfigSpec.IntValue MAX_SPELL_RANGE;

    private static ModConfigSpec.IntValue START_SPELL_RANGE;

    private static ModConfigSpec.DoubleValue MAX_MANA_MULTIPLIER;

    private static ModConfigSpec.DoubleValue BATTLE_WALK_SPEED;

    private static ModConfigSpec.IntValue MAX_COMBO_DELAY_TICK;

    private static ModConfigSpec.BooleanValue DRINK_POTION_IN_BATTLE;

    private static ModConfigSpec.BooleanValue SHOW_CHAT_BUBBLES;

    private static ModConfigSpec.BooleanValue MELEE_ATTACK_IN_MAGIC_TASK;

    private static ModConfigSpec.DoubleValue AIR_FORCE_BASE_CHANCE;

    private static ModConfigSpec.DoubleValue AIR_FORCE_CHANCE_PER_LEVEL;

    private static ModConfigSpec.DoubleValue FOLLOW_HEIGHT;

    private static ModConfigSpec.DoubleValue FOLLOW_RADIUS;

    private static ModConfigSpec.DoubleValue FLY_SPEED;

    private static ModConfigSpec.DoubleValue MIN_AIR_ATTACK_RANGE;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_ATTACK_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_DEFENSE_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_MOVEMENT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_SUPPORT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_POSITIVE_EFFECT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_NEGATIVE_EFFECT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_SUPPORT_EFFECT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_SUMMON_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> MAID_SHOULD_RECAST_SPELLS;

    private static ModConfigSpec.ConfigValue<com.electronwill.nightconfig.core.Config> EXTRA_SPELL_CASTING_RANGE;

    private static ModConfigSpec.ConfigValue<com.electronwill.nightconfig.core.Config> EXTRA_SPELL_CAUSED_EFFECTS;

    private final static String SPELL_ID_PLACEHOLDER = "irons_spellbooks:fireball";

    /** 空的配置对象，用于配置默认值 */
    private final static com.electronwill.nightconfig.core.Config EMPTY_CONFIG = com.electronwill.nightconfig.core.Config.of(
        LinkedHashMap::new, InMemoryFormat.withUniversalSupport());

    static {
        init(BUILDER);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init(ModConfigSpec.Builder builder) {
        // builder.translation(TRANSLATE_KEY).push("basic_settings");

        MAX_SPELL_RANGE = builder.comment("Maximum spell attack search range")
            .translation(translateKey("max_spell_range"))
            .defineInRange("maxSpellRange", 64, 8, 192);

        START_SPELL_RANGE = builder.comment("Start spell casting range")
            .translation(translateKey("start_spell_range"))
            .defineInRange("startSpellRange", 15, 2, 64);

        MAX_MANA_MULTIPLIER = builder.comment("Maximum mana multiplier")
            .translation(translateKey("max_mana_multiplier"))
            .defineInRange("maxManaMultiplier", 1D, 0.01, 100);

        BATTLE_WALK_SPEED = builder.comment("Walk speed in battle")
            .translation(translateKey("battle_walk_speed"))
            .defineInRange("spellBattleWalkSpeed", 1, 0.1, 10);

        MAX_COMBO_DELAY_TICK = builder.comment("Max combo delay tick")
            .translation(translateKey("max_combo_delay_tick"))
            .defineInRange("maxComboDelayTick", 10, 1, 600);

        DRINK_POTION_IN_BATTLE = builder.comment("Drink potion in battle (Default: true)")
            .translation(translateKey("drink_potion_in_battle"))
            .define("drinkPotionInSpellBattle", true);

        SHOW_CHAT_BUBBLES = builder.comment("Show chat bubbles in battle (Default: true)")
            .translation(translateKey("show_chat_bubbles"))
            .define("showChatBubblesInSpellBattle", true);

        MELEE_ATTACK_IN_MAGIC_TASK = builder.comment("Allow maid use melee attack in magic attack task (Default: true)")
            .translation(translateKey("melee_attack_in_magic_task"))
            .define("meleeAttackInMagicTask", true);

        AIR_FORCE_BASE_CHANCE = builder.comment("Base chance for air force maid (Default: 0.1)")
            .translation(translateKey("air_force_base_chance"))
            .defineInRange("airForceBaseChance", 0.1D, 0.0D, 1.0D);

        AIR_FORCE_CHANCE_PER_LEVEL = builder.comment("Air force chance per level (Default: 0.1)")
            .translation(translateKey("air_force_chance_per_level"))
            .defineInRange("airForceChancePerLevel", 0.1D, 0.0D, 1.0D);

        FOLLOW_HEIGHT = builder.comment("Air force follow height (Default: 4.0)")
            .translation(translateKey("air_force_follow_height"))
            .defineInRange("airForceFollowHeight", 4.0D, 0.0D, 64.0D);

        FOLLOW_RADIUS = builder.comment("Air force follow radius (Default: 6.0)")
            .translation(translateKey("air_force_follow_radius"))
            .defineInRange("airForceFollowRadius", 6.0D, 0.0D, 32.0D);

        FLY_SPEED = builder.comment("Air force fly speed (Default: 0.4)")
            .translation(translateKey("air_force_fly_speed"))
            .defineInRange("airForceFlySpeed", 0.4D, 0.0D, 3.0D);

        MIN_AIR_ATTACK_RANGE = builder.comment("Min air attack spell range (Default: 5.0)")
            .translation(translateKey("air_force_min_attack_range"))
            .defineInRange("airForceMinAttackRange", 5.0D, 0.0D, 64.0D);

        builder.translation(translateKey("spell_compat")).push("spell_compat");

        Supplier<String> placeholderSupplier = () -> SPELL_ID_PLACEHOLDER;
        Predicate<Object> spellElementChecker = Config::checkIsSpellId;

        EXTRA_ATTACK_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra attack spell ids").worldRestart()
                .translation(translateKey("spell_compat.extra_attack_spells"))
                .defineListAllowEmpty("extraAttackSpells", Collections::emptyList, placeholderSupplier, spellElementChecker);

        EXTRA_DEFENSE_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra defense spell ids").worldRestart()
                .translation(translateKey("spell_compat.extra_defense_spells"))
                .defineListAllowEmpty("extraDefenseSpells", Collections::emptyList, placeholderSupplier, spellElementChecker);

        EXTRA_MOVEMENT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra movement spell ids").worldRestart()
                .translation(translateKey("spell_compat.extra_movement_spells"))
                .defineListAllowEmpty("extraMovementSpells", Collections::emptyList, placeholderSupplier, spellElementChecker);

        EXTRA_SUPPORT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra support spell ids").worldRestart()
                .translation(translateKey("spell_compat.extra_support_spells"))
                .defineListAllowEmpty("extraSupportSpells", Collections::emptyList, placeholderSupplier, spellElementChecker);

        EXTRA_POSITIVE_EFFECT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra positive effect spell ids").worldRestart()
                .translation(translateKey("spell_compat.extra_positive_effect_spells"))
                .defineListAllowEmpty("extraPositiveEffectSpells", Collections::emptyList, placeholderSupplier, spellElementChecker);

        EXTRA_NEGATIVE_EFFECT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra negative effect spell ids").worldRestart()
                .translation(translateKey("spell_compat.extra_negative_effect_spells"))
                .defineListAllowEmpty("extraNegativeEffectSpells", Collections::emptyList, placeholderSupplier, spellElementChecker);

        EXTRA_SUPPORT_EFFECT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra support other spell ids").worldRestart()
                .translation(translateKey("spell_compat.extra_support_effect_spells"))
                .defineListAllowEmpty("extraSupportEffectSpells", Collections::emptyList, placeholderSupplier, spellElementChecker);

        EXTRA_SUMMON_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra summon spell ids").worldRestart()
                .translation(translateKey("spell_compat.extra_summon_spells"))
                .defineListAllowEmpty("extraSummonSpells", Collections::emptyList, placeholderSupplier, spellElementChecker);

        MAID_SHOULD_RECAST_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Maid should recast spell ids").worldRestart()
                .translation(translateKey("spell_compat.maid_should_recast_spells"))
                .defineListAllowEmpty("maidShouldRecastSpells", Collections::emptyList, placeholderSupplier, spellElementChecker);

        EXTRA_SPELL_CASTING_RANGE = builder.comment("Extra spell casting range (spell id => range). Default values are loaded from datapack.")
                .translation(translateKey("spell_compat.extra_spell_casting_range")).worldRestart()
                .define("extra_spell_casting_range", EMPTY_CONFIG, Config::checkSpellCastingRangeMap);

        EXTRA_SPELL_CAUSED_EFFECTS = builder.comment("Extra spell caused effects (spell id => effect id). Default values are loaded from datapack.")
            .translation(translateKey("spell_compat.extra_spell_caused_effects")).worldRestart()
            .define("extra_spell_caused_effects", EMPTY_CONFIG, Config::checkSpellCausedEffectsMap);

        builder.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int getMaxSpellRange() {
        return MAX_SPELL_RANGE.getAsInt();
    }

    public static int getStartSpellRange() {
        return START_SPELL_RANGE.getAsInt();
    }

    public static double getMaxManaMultiplier() {
        return MAX_MANA_MULTIPLIER.getAsDouble();
    }

    public static double getBattleWalkSpeed() {
        return BATTLE_WALK_SPEED.getAsDouble();
    }

    public static int getMaxComboDelayTick() {
        return MAX_COMBO_DELAY_TICK.getAsInt();
    }

    public static boolean getDrinkPotionInBattle() {
        return DRINK_POTION_IN_BATTLE.getAsBoolean();
    }

    public static boolean getShowChatBubbles() {
        return SHOW_CHAT_BUBBLES.getAsBoolean();
    }

    public static boolean getMeleeAttackInMagicTask() {
        return MELEE_ATTACK_IN_MAGIC_TASK.getAsBoolean();
    }

    public static float getAirForceBaseChance() {
        return AIR_FORCE_BASE_CHANCE.get().floatValue();
    }

    public static float getAirForceChancePerLevel() {
        return AIR_FORCE_CHANCE_PER_LEVEL.get().floatValue();
    }

    public static float getFollowHeight() {
        return FOLLOW_HEIGHT.get().floatValue();
    }

    public static float getFollowRadius() {
        return FOLLOW_RADIUS.get().floatValue();
    }

    public static float getFlySpeed() {
        return FLY_SPEED.get().floatValue();
    }

    public static float getMinAirAttackRange() {
        return MIN_AIR_ATTACK_RANGE.get().floatValue();
    }

    public static List<String> getExtraAttackSpells() {
        return EXTRA_ATTACK_SPELLS.get();
    }

    public static List<String> getExtraDefenseSpells() {
        return EXTRA_DEFENSE_SPELLS.get();
    }

    public static List<String> getExtraMovementSpells() {
        return EXTRA_MOVEMENT_SPELLS.get();
    }

    public static List<String> getExtraSupportSpells() {
        return EXTRA_SUPPORT_SPELLS.get();
    }

    public static List<String> getExtraPositiveEffectSpells() {
        return EXTRA_POSITIVE_EFFECT_SPELLS.get();
    }

    public static List<String> getExtraNegativeEffectSpells() {
        return EXTRA_NEGATIVE_EFFECT_SPELLS.get();
    }

    public static List<String> getExtraSupportEffectSpells() {
        return EXTRA_SUPPORT_EFFECT_SPELLS.get();
    }

    public static List<String> getExtraSummonSpells() {
        return EXTRA_SUMMON_SPELLS.get();
    }

    public static List<String> getMaidShouldRecastSpells() {
        return MAID_SHOULD_RECAST_SPELLS.get();
    }

    public static Map<String, Float> getExtraSpellCastingRange() {
        Map<String, Float> map = new LinkedHashMap<>();
        for (com.electronwill.nightconfig.core.Config.Entry entry : EXTRA_SPELL_CASTING_RANGE.get().entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static Map<String, String> getExtraSpellCausedEffects() {
        Map<String, String> map = new LinkedHashMap<>();
        for (com.electronwill.nightconfig.core.Config.Entry entry : EXTRA_SPELL_CAUSED_EFFECTS.get().entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private static String translateKey(String key) {
        return TRANSLATE_KEY + "." + key;
    }

    private static boolean checkIsSpellId(Object value) {
        return value instanceof String s && LittleMaidSpellbooksCompat.isSpellId(s);
    }

    private static boolean checkSpellCastingRangeMap(Object value) {
        if (!(value instanceof com.electronwill.nightconfig.core.Config config)) {
            return false;
        }
        for (var entry : config.entrySet()) {
            String key = entry.getKey();
            Object range = entry.getValue();
            if (ResourceLocation.tryParse(key) == null) {
                return false;
            }
            if (!checkIsSpellId(key)) {
                WinefoxsSpellbooks.LOGGER.error("spell id '{}' does not exist", key);
            }
            if (!(range instanceof Number num)) {
                return false;
            }
            if (!(range instanceof Float)) {
                entry.setValue(num.floatValue());
            }
        }
        return true;
    }

    private static boolean checkSpellCausedEffectsMap(Object value) {
        if (!(value instanceof com.electronwill.nightconfig.core.Config config)) {
            return false;
        }
        for (var entry : config.entrySet()) {
            String key = entry.getKey();
            Object effectKey = entry.getValue();
            if (ResourceLocation.tryParse(key) == null) {
                return false;
            }
            if (!checkIsSpellId(key)) {
                WinefoxsSpellbooks.LOGGER.error("spell id '{}' does not exist", key);
            }
            if (!(effectKey instanceof String)) {
                return false;
            }
        }
        return true;
    }
}
