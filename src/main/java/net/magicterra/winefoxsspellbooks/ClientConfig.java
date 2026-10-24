package net.magicterra.winefoxsspellbooks;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 客户端配置：只影响本机显示，不随服务端同步
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
public final class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SHOW_MAID_MANA_BAR = BUILDER
        .comment("Show a floating mana bar beside your own spellcasting maids; it fades out once their mana is full (Default: true)")
        .translation("config.winefoxs_spellbooks.show_maid_mana_bar")
        .define("showMaidManaBar", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {
    }

    public static boolean isMaidManaBarShown() {
        return SPEC.isLoaded() && SHOW_MAID_MANA_BAR.getAsBoolean();
    }
}
