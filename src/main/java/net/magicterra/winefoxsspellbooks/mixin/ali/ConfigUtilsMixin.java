package net.magicterra.winefoxsspellbooks.mixin.ali;

import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.configuration.GameplayLootCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.magicterra.winefoxsspellbooks.registry.WsbLootTables;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把 {@code winefoxs_spellbooks:gameplay/maid_morning_gift} 战利品表注入
 * Advanced Loot Info 的 gameplay category 列表，让它在 JEI/EMI/REI 里
 * 以独立类别页（图标=灵狐精魂）展示，而不是落入兜底的 {@code ali:gameplay_loot}。
 * <p>
 * 注入点：{@link com.yanny.ali.configuration.ConfigUtils#readConfiguration()} 的 RETURN —
 * 无论玩家本地 {@code config/ali/ali_common.json} 是首次生成还是手改过，
 * 都能在 config 进入 {@code AbstractServer.processLootTables} 之前补上我们的类别。
 * <p>
 * {@link Pseudo} + {@code targets="..."}：让 mixin 在 ALI 类不存在时静默失败；
 * 真正的"是否安装"判断由
 * {@link net.magicterra.winefoxsspellbooks.mixin.WsbMixinPlugin#shouldApplyMixin}
 * 按子包名 {@code ali} 解析后通过 {@code LoadingModList} 完成。
 * {@code require=0, expect=1}：ALI 升版本若改了 {@code readConfiguration} 签名只警告不崩。
 *
 * @author Gardel
 * @since 2026-05-16
 */
@Pseudo
@Mixin(targets = "com.yanny.ali.configuration.ConfigUtils", remap = false)
public abstract class ConfigUtilsMixin {
    private static final ResourceLocation CATEGORY_KEY = ResourceLocation.fromNamespaceAndPath(
        WinefoxsSpellbooks.MODID, "maid_morning_gift");
    // GameplayLootCategory.validate 用的是 Pattern#find，所以 ^…$ 锚是必要的
    private static final Pattern LOOT_TABLE_PATTERN = Pattern.compile(
        "^" + Pattern.quote(WsbLootTables.MAID_MORNING_GIFT.location().toString()) + "$");

    @Inject(
        method = "readConfiguration",
        at = @At("RETURN"),
        require = 0,
        expect = 1,
        remap = false
    )
    private static void wsb$injectMaidMorningGiftCategory(CallbackInfoReturnable<AliConfig> cir) {
        AliConfig config = cir.getReturnValue();
        if (config == null || config.gameplayCategories == null) {
            return;
        }

        // 幂等：玩家或别的 mod 可能已经把同 key 类别加进 config
        boolean already = config.gameplayCategories.stream()
            .anyMatch(c -> CATEGORY_KEY.equals(c.getKey()));
        if (already) {
            return;
        }

        GameplayLootCategory category = new GameplayLootCategory(
            CATEGORY_KEY,
            WsbItems.VULPINE_ANIMA.get(),
            false,
            List.of(),
            List.of(LOOT_TABLE_PATTERN)
        );

        // AliConfig.CODEC 反序列化出来的 list 是不可变的（仅 disabledEntities 被 ALI 自己 wrap 进 ArrayList），
        // 直接 .add 会抛 UnsupportedOperationException。先 copy 成可变副本再写回字段。
        List<GameplayLootCategory> mutable = new ArrayList<>(config.gameplayCategories);
        // 必须插在末尾的 `.*` 兜底之前 —— ALI 用 findFirst 取第一个 validate 通过的类别。
        // 放最前最稳：我们的正则极窄，不会误伤别人。
        mutable.add(0, category);
        config.gameplayCategories = mutable;
    }
}
