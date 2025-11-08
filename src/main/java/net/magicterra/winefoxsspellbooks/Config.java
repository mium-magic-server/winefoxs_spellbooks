package net.magicterra.winefoxsspellbooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
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
            .defineInRange("maxComboDelayTick", 4, 1, 600);

        DRINK_POTION_IN_BATTLE = builder.comment("Drink potion in battle (Default: true)")
            .translation(translateKey("drink_potion_in_battle"))
            .define("drinkPotionInSpellBattle", true);

        SHOW_CHAT_BUBBLES = builder.comment("Show chat bubbles in battle (Default: true)")
            .translation(translateKey("show_chat_bubbles"))
            .define("showChatBubblesInSpellBattle", true);

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

    private static String translateKey(String key) {
        return TRANSLATE_KEY + "." + key;
    }

    private static final MixinConfigSpec MIXIN_CONFIG_SPEC;

    static {
        // 混入配置只能在 neoforge 初始化前读取，不能动态修改
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        var configPath = FMLLoader.getGamePath().resolve(FMLPaths.CONFIGDIR.relative()).resolve(WinefoxsSpellbooks.MODID + "_mixin.json");
        MixinConfigSpec mixinConfigSpec0 = new MixinConfigSpec();
        if (Files.exists(configPath)) {
            try {
                mixinConfigSpec0 = gson.fromJson(Files.readString(configPath), MixinConfigSpec.class);
            } catch (IOException e) {
                WinefoxsSpellbooks.LOGGER.error("Failed to load config file", e);
            }
        } else {
            String json = gson.toJson(mixinConfigSpec0);
            try {
                Files.writeString(configPath, json);
            } catch (IOException e) {
                WinefoxsSpellbooks.LOGGER.error("Could not write config", e);
            }
        }
        MIXIN_CONFIG_SPEC = mixinConfigSpec0;
    }

    public static boolean ysmSupport() {
        return MIXIN_CONFIG_SPEC.ysmSupport;
    }

    static class MixinConfigSpec {
        public boolean ysmSupport = false;
    }
}
