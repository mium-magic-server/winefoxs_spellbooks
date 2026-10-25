package net.magicterra.winefoxsspellbooks.compat.ali;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.loot.AddItemLootModifier;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Advanced Loot Info 兼容：把本模组的全局战利品修饰器翻译成 ALI 能显示的条目。
 * <p>
 * 除 {@code com.yanny.ali.api} 外，本类引用的 {@code com.yanny.ali.plugin.*} 与 {@code com.yanny.aci.*}
 * 都不是公开 API，升级 ALI / ACI 时需要复核签名；选 {@link IGlobalLootModifierPlugin} 而非
 * {@code IForgePlugin} 的理由见 DEVELOPMENT.md「第三方插件 API 的软依赖」。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-30
 */
@AliEntrypoint
public class WsbAliPlugin implements IGlobalLootModifierPlugin {

    @Override
    public String getModId() {
        return WinefoxsSpellbooks.MODID;
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        registry.registerGlobalLootModifier(AddItemLootModifier.class, WsbAliPlugin::describeAddItem);
    }

    /**
     * 把一条「向某张战利品表追加固定物品」的修饰器描述成 ALI 的物品条目。
     * <p>
     * 概率不在这里算：{@code getChance} 会把条件里的 {@code random_chance} 折算进展示概率，
     * 故 rawChance 传 1.0。
     */
    private static Optional<IPageLootModifier> describeAddItem(IServerUtils utils, AddItemLootModifier modifier) {
        List<LootItemCondition> allConditions = Arrays.asList(modifier.getLootConditions());
        IntProvider provider = modifier.getCountProvider();
        int min = provider.getMinValue();
        int max = provider.getMaxValue();
        NumberExpr countExpr = min == max ? NumberExpr.constant(min) : NumberExpr.uniformInt(min, max);
        ItemStack stack = modifier.getItem().getDefaultInstance();

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, modifier, allConditions, (page, conditions) -> {
            LootCount chance = NodeUtils.getChance(utils, conditions, 1.0f);
            // NumberExpr 不可变，tooltip 与条目共用同一份即可
            LootCount count = LootCount.of(countExpr);
            NumberInterval countLimit = NodeUtils.getCountLimit(stack);

            TooltipNode tooltip = TooltipUtils.getTooltip(utils, 0, chance, count, countLimit,
                List.of(), conditions).build();
            ItemNode node = new ItemNode(1.0f, countExpr, stack, tooltip,
                List.of(), conditions);
            return List.of(new IOperation.AddOperation(any -> true, node));
        }));
    }
}
