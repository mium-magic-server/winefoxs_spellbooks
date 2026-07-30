package net.magicterra.winefoxsspellbooks;

import com.electronwill.nightconfig.core.InMemoryFormat;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.api.spell.SpellCategory;
import net.magicterra.winefoxsspellbooks.api.spell.SpellFlag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 服务端配置：全部配置值只被逻辑服务端读取，由服务端权威持有并同步给客户端。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-31 02:08
 */
public class Config {
    private static final String TRANSLATE_KEY = "config." + WinefoxsSpellbooks.MODID;

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    private static ModConfigSpec.IntValue MAX_SPELL_RANGE;

    private static ModConfigSpec.IntValue START_SPELL_RANGE;

    private static ModConfigSpec.DoubleValue MAX_MANA_MULTIPLIER;

    private static ModConfigSpec.BooleanValue LIGHTNING_MANA_REGEN;

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

    /**
     * 各法术类别的「额外法术 id」列表配置项，由 {@link MaidSpellAction} / {@link SpellFlag} 枚举驱动生成，键名与翻译键都从枚举取，这里不再逐项手写。
     */
    private static final Map<SpellCategory, ModConfigSpec.ConfigValue<List<String>>> EXTRA_SPELLS =
        new LinkedHashMap<>();

    private static ModConfigSpec.ConfigValue<com.electronwill.nightconfig.core.Config> EXTRA_SPELL_CASTING_RANGE;

    private static ModConfigSpec.ConfigValue<com.electronwill.nightconfig.core.Config> EXTRA_SPELL_CAUSED_EFFECTS;

    private static ModConfigSpec.BooleanValue VULPINE_GIFT_ENABLED;

    private static ModConfigSpec.DoubleValue VULPINE_GIFT_RADIUS;

    private static ModConfigSpec.BooleanValue VULPINE_GIFT_REQUIRE_FULL_SLEEP;

    private static ModConfigSpec.IntValue SUMMONED_MAID_MAX_SPELL_LEVEL;

    private static ModConfigSpec.EnumValue<SpellRarity> SUMMONED_MAID_MAX_SPELL_RARITY;

    private static ModConfigSpec.BooleanValue SUMMONED_MAID_ALLOW_ELDRITCH;

    private static ModConfigSpec.BooleanValue CREEPER_AVOID_SUMMONED_MAID;

    private final static String SPELL_ID_PLACEHOLDER = "irons_spellbooks:fireball";

    /**
     * 空的配置对象，用于配置默认值
     */
    private final static com.electronwill.nightconfig.core.Config EMPTY_CONFIG = com.electronwill.nightconfig.core.Config.of(
        LinkedHashMap::new, InMemoryFormat.withUniversalSupport());

    static {
        initServer(SERVER_BUILDER);
    }

    /**
     * 服务端配置：玩法数值。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void initServer(ModConfigSpec.Builder builder) {
        MAX_SPELL_RANGE = builder.comment("Maximum spell attack search range")
            .translation(translateKey("max_spell_range"))
            .defineInRange("maxSpellRange", 64, 8, 192);

        START_SPELL_RANGE = builder.comment("Start spell casting range")
            .translation(translateKey("start_spell_range"))
            .defineInRange("startSpellRange", 15, 2, 64);

        MAX_MANA_MULTIPLIER = builder.comment("Maximum mana multiplier, relative to the player's (Default: 1)")
            .translation(translateKey("max_mana_multiplier"))
            .defineInRange("maxManaMultiplier", 1D, 0.01, 100);

        LIGHTNING_MANA_REGEN = builder.comment("Lightning-struck maids regenerate mana 100x faster (Default: false)")
            .translation(translateKey("lightning_mana_regen"))
            .define("lightningManaRegen", false);

        BATTLE_WALK_SPEED = builder.comment("Walk speed in spell battle")
            .translation(translateKey("battle_walk_speed"))
            .defineInRange("spellBattleWalkSpeed", 1, 0.1, 10);

        MAX_COMBO_DELAY_TICK = builder.comment("Max combo delay tick")
            .translation(translateKey("max_combo_delay_tick"))
            .defineInRange("maxComboDelayTick", 10, 1, 600);

        DRINK_POTION_IN_BATTLE = builder.comment("Maids below 60% health drink a healing potion without consuming an item (Default: false)")
            .translation(translateKey("drink_potion_in_battle"))
            .define("drinkPotionInSpellBattle", false);

        SHOW_CHAT_BUBBLES = builder.comment("Show spell names in spell battle (Default: true)")
            .translation(translateKey("show_chat_bubbles"))
            .define("showChatBubblesInSpellBattle", true);

        MELEE_ATTACK_IN_MAGIC_TASK = builder.comment("Allow maids to use melee weapons during spell attacks (Default: true)")
            .translation(translateKey("melee_attack_in_magic_task"))
            .define("meleeAttackInMagicTask", true);

        AIR_FORCE_BASE_CHANCE = builder.comment("Base chance for air force maid (Default: 0.1)")
            .translation(translateKey("air_force_base_chance"))
            .defineInRange("airForceBaseChance", 0.1D, 0.0D, 1.0D);

        AIR_FORCE_CHANCE_PER_LEVEL = builder.comment("Air force chance per level (Default: 0.1)")
            .translation(translateKey("air_force_chance_per_level"))
            .defineInRange("airForceChancePerLevel", 0.1D, 0.0D, 1.0D);

        FOLLOW_HEIGHT = builder.comment("Air force follow height (Default: 4.0 blocks)")
            .translation(translateKey("air_force_follow_height"))
            .defineInRange("airForceFollowHeight", 4.0D, 0.0D, 64.0D);

        FOLLOW_RADIUS = builder.comment("Air force follow radius (Default: 6.0 blocks)")
            .translation(translateKey("air_force_follow_radius"))
            .defineInRange("airForceFollowRadius", 6.0D, 0.0D, 32.0D);

        FLY_SPEED = builder.comment("Air force fly speed (Default: 0.4)")
            .translation(translateKey("air_force_fly_speed"))
            .defineInRange("airForceFlySpeed", 0.4D, 0.0D, 3.0D);

        MIN_AIR_ATTACK_RANGE = builder.comment("Minimum air casting distance (Default: 5.0 blocks)")
            .translation(translateKey("air_force_min_attack_range"))
            .defineInRange("airForceMinAttackRange", 5.0D, 0.0D, 64.0D);

        builder.translation(translateKey("spell_compat")).push("spell_compat");

        Supplier<String> placeholderSupplier = () -> SPELL_ID_PLACEHOLDER;
        Predicate<Object> spellElementChecker = Config::checkIsSpellId;

        // 九个「额外法术」列表由枚举驱动生成，键名 / 翻译键 / 注释全部从类别自身取
        EXTRA_SPELLS.clear();
        for (MaidSpellAction action : MaidSpellAction.ALL) {
            EXTRA_SPELLS.put(action, defineExtraSpells(builder, action, placeholderSupplier, spellElementChecker));
        }
        for (SpellFlag flag : SpellFlag.ALL) {
            EXTRA_SPELLS.put(flag, defineExtraSpells(builder, flag, placeholderSupplier, spellElementChecker));
        }

        EXTRA_SPELL_CASTING_RANGE = builder.comment("Extra spell casting range (spell id => range). Default values are loaded from datapack.")
            .translation(translateKey("spell_compat.extra_spell_casting_range")).worldRestart()
            .define("extra_spell_casting_range", EMPTY_CONFIG, Config::checkSpellCastingRangeMap);

        EXTRA_SPELL_CAUSED_EFFECTS = builder.comment("Extra spell caused effects (spell id => effect id). Default values are loaded from datapack.")
            .translation(translateKey("spell_compat.extra_spell_caused_effects")).worldRestart()
            .define("extra_spell_caused_effects", EMPTY_CONFIG, Config::checkSpellCausedEffectsMap);

        builder.pop();

        builder.translation(translateKey("vulpine_gift")).push("vulpine_gift");

        VULPINE_GIFT_ENABLED = builder.comment("Enable max-favorability maid morning gift on player wakeup (Default: true)")
            .translation(translateKey("vulpine_gift.enabled"))
            .define("vulpineGiftEnabled", true);

        VULPINE_GIFT_RADIUS = builder.comment("Radius (in blocks) around the bed to search for an eligible maid gifter (Default: 8.0)")
            .translation(translateKey("vulpine_gift.radius"))
            .defineInRange("vulpineGiftRadius", 8.0D, 1.0D, 32.0D);

        VULPINE_GIFT_REQUIRE_FULL_SLEEP = builder.comment("Require a normal full sleep cycle. If false, gifts can also be given when waking immediately due to monsters/thunder (Default: true)")
            .translation(translateKey("vulpine_gift.require_full_sleep"))
            .define("vulpineGiftRequireFullSleep", true);

        builder.pop();

        builder.translation(translateKey("summoned_maid")).push("summoned_maid");

        SUMMONED_MAID_MAX_SPELL_LEVEL = builder.comment(
                "Highest spell level a summoned maid may carry. Set to 10 to disable. (Default: 3)")
            .translation(translateKey("summoned_maid.max_spell_level"))
            .defineInRange("summonedMaidMaxSpellLevel", 3, 1, 20);

        SUMMONED_MAID_MAX_SPELL_RARITY = builder.comment(
                "Highest spell rarity a summoned maid may carry. Set to LEGENDARY to disable. (Default: EPIC)")
            .translation(translateKey("summoned_maid.max_spell_rarity"))
            .defineEnum("summonedMaidMaxSpellRarity", SpellRarity.EPIC);

        SUMMONED_MAID_ALLOW_ELDRITCH = builder.comment(
                "Allow summoned maids to carry learned-only school spells, i.e. Eldritch (Default: false)")
            .translation(translateKey("summoned_maid.allow_eldritch"))
            .define("summonedMaidAllowEldritch", false);

        CREEPER_AVOID_SUMMONED_MAID = builder.comment(
                "Creepers flee from summoned maids. Costs one extra AI goal on every creeper in the world;",
                "the goal short-circuits in dimensions with no summoned maid, but set to false to remove it",
                "entirely. (Default: true)")
            .translation(translateKey("summoned_maid.creeper_avoid"))
            .define("creeperAvoidSummonedMaid", true);

        builder.pop();
    }

    /**
     * 为一个法术类别定义「额外法术 id」列表配置项。
     * <p>
     * {@code defineListAllowEmpty} 的泛型签名对 {@code List<String>} 不友好（它按 {@code List<?>} 收），故需 raw 转型。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ModConfigSpec.ConfigValue<List<String>> defineExtraSpells(
        ModConfigSpec.Builder builder, SpellCategory category,
        Supplier<String> placeholderSupplier, Predicate<Object> elementChecker) {
        return (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue)
            builder.comment(category.comment()).worldRestart()
                .translation(translateKey("spell_compat." + category.translationSuffix()))
                .defineListAllowEmpty(category.configName(), Collections::emptyList,
                    placeholderSupplier, elementChecker);
    }

    static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    /**
     * SERVER 配置是否已加载。数据包/标签加载期早于配置加载，读配置前必须先问这一句。
     */
    public static boolean isServerConfigLoaded() {
        return SERVER_SPEC.isLoaded();
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

    /**
     * 被雷劈过的女仆是否享受魔力恢复加速。
     */
    public static boolean isLightningManaRegenEnabled() {
        return LIGHTNING_MANA_REGEN.getAsBoolean();
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

    /**
     * 取某个法术类别配置的「额外法术 id」列表；类别未定义时返回空表。
     *
     * @param category 行为类别或标记
     */
    public static List<String> getExtraSpells(SpellCategory category) {
        ModConfigSpec.ConfigValue<List<String>> value = EXTRA_SPELLS.get(category);
        return value == null ? List.of() : value.get();
    }

    public static Map<String, Float> getExtraSpellCastingRange() {
        Map<String, Float> map = new LinkedHashMap<>();
        for (com.electronwill.nightconfig.core.Config.Entry entry : EXTRA_SPELL_CASTING_RANGE.get().entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static boolean isVulpineGiftEnabled() {
        return VULPINE_GIFT_ENABLED.getAsBoolean();
    }

    public static double getVulpineGiftRadius() {
        return VULPINE_GIFT_RADIUS.getAsDouble();
    }

    public static boolean isVulpineGiftRequireFullSleep() {
        return VULPINE_GIFT_REQUIRE_FULL_SLEEP.getAsBoolean();
    }

    /**
     * 召唤女仆可携带的最高法术等级；与配装数据包里的分层上限取小。
     */
    public static int getSummonedMaidMaxSpellLevel() {
        return SUMMONED_MAID_MAX_SPELL_LEVEL.getAsInt();
    }

    /**
     * 召唤女仆可携带的最高法术稀有度。
     */
    public static SpellRarity getSummonedMaidMaxSpellRarity() {
        return SUMMONED_MAID_MAX_SPELL_RARITY.get();
    }

    /**
     * 召唤女仆是否可以携带「习得制」学派（当前即 Iron's 邪术 Eldritch）的法术。
     */
    public static boolean isSummonedMaidAllowEldritch() {
        return SUMMONED_MAID_ALLOW_ELDRITCH.getAsBoolean();
    }

    /**
     * 苦力怕是否要躲开召唤女仆。关掉即彻底不给苦力怕挂这个 AI 目标。
     * <p>
     * 读取点在 {@code EntityJoinLevelEvent} 里，早于服务端配置加载的苦力怕（世界加载首帧） 会读到默认值 {@code true}，故调用方须先问 {@link #isServerConfigLoaded()}。
     */
    public static boolean isCreeperAvoidSummonedMaid() {
        return CREEPER_AVOID_SUMMONED_MAID.getAsBoolean();
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
