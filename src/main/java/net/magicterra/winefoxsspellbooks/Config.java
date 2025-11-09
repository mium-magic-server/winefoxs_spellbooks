package net.magicterra.winefoxsspellbooks;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 配置文件
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-31 02:08
 */
public class Config {
    private static final String TRANSLATE_KEY = "config." + WinefoxsSpellbooks.MODID;

    private static ModConfigSpec.IntValue MAX_SPELL_RANGE;

    private static ModConfigSpec.IntValue START_SPELL_RANGE;

    private static ModConfigSpec.DoubleValue MAX_MANA_MULTIPLIER;

    private static ModConfigSpec.DoubleValue BATTLE_WALK_SPEED;

    private static ModConfigSpec.IntValue MAX_COMBO_DELAY_TICK;

    private static ModConfigSpec.BooleanValue DRINK_POTION_IN_BATTLE;

    private static ModConfigSpec.BooleanValue SHOW_CHAT_BUBBLES;

    private static ModConfigSpec.BooleanValue MELEE_ATTACK_IN_MAGIC_TASK;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_ATTACK_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_DEFENSE_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_MOVEMENT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_SUPPORT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_POSITIVE_EFFECT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_NEGATIVE_EFFECT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> EXTRA_SUPPORT_EFFECT_SPELLS;

    private static ModConfigSpec.ConfigValue<List<String>> MAID_SHOULD_RECAST_SPELLS;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init(ModConfigSpec.Builder builder) {
        builder.translation(TRANSLATE_KEY).push(WinefoxsSpellbooks.MODID);

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

        builder.translation(translateKey("spell_compat")).push("spell_compat");

        Supplier<String> placeholderSupplier = () -> SPELL_ID_PLACEHOLDER;
        Predicate<Object> spellElementChecker = (s) -> LittleMaidSpellbooksCompat.isSpellId(String.valueOf(s));
        ModConfigSpec.Range<Integer> spellIdListRange = ModConfigSpec.Range.of(0, 65535);

        EXTRA_ATTACK_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra attack spell ids")
                .translation(translateKey("spell_compat.extra_attack_spells"))
                .defineList(Collections.singletonList("extraAttackSpells"), Collections::emptyList, placeholderSupplier, spellElementChecker, spellIdListRange);

        EXTRA_DEFENSE_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra defense spell ids")
                .translation(translateKey("spell_compat.extra_defense_spells"))
                .defineList(Collections.singletonList("extraDefenseSpells"), Collections::emptyList, placeholderSupplier, spellElementChecker, spellIdListRange);

        EXTRA_MOVEMENT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra movement spell ids")
                .translation(translateKey("spell_compat.extra_movement_spells"))
                .defineList(Collections.singletonList("extraMovementSpells"), Collections::emptyList, placeholderSupplier, spellElementChecker, spellIdListRange);

        EXTRA_SUPPORT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra support spell ids")
                .translation(translateKey("spell_compat.extra_support_spells"))
                .defineList(Collections.singletonList("extraSupportSpells"), Collections::emptyList, placeholderSupplier, spellElementChecker, spellIdListRange);

        EXTRA_POSITIVE_EFFECT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra positive effect spell ids")
                .translation(translateKey("spell_compat.extra_positive_effect_spells"))
                .defineList(Collections.singletonList("extraPositiveEffectSpells"), Collections::emptyList, placeholderSupplier, spellElementChecker, spellIdListRange);

        EXTRA_NEGATIVE_EFFECT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra negative effect spell ids")
                .translation(translateKey("spell_compat.extra_negative_effect_spells"))
                .defineList(Collections.singletonList("extraNegativeEffectSpells"), Collections::emptyList, placeholderSupplier, spellElementChecker, spellIdListRange);

        EXTRA_SUPPORT_EFFECT_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Extra support other spell ids")
                .translation(translateKey("spell_compat.extra_support_effect_spells"))
                .defineList(Collections.singletonList("extraSupportEffectSpells"), Collections::emptyList, placeholderSupplier, spellElementChecker, spellIdListRange);

        MAID_SHOULD_RECAST_SPELLS = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment("Maid should recast spell ids")
                .translation(translateKey("spell_compat.maid_should_recast_spells"))
                .defineList(Collections.singletonList("maidShouldRecastSpells"), Collections::emptyList, placeholderSupplier, spellElementChecker, spellIdListRange);

        builder.pop();

        builder.pop();
    }

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

    public static List<String> getMaidShouldRecastSpells() {
        return MAID_SHOULD_RECAST_SPELLS.get();
    }

    private static String translateKey(String key) {
        return TRANSLATE_KEY + "." + key;
    }

    private final static String SPELL_ID_PLACEHOLDER = "irons_spellbooks:fireball";
}
