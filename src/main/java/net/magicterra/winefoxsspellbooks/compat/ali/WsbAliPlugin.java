package net.magicterra.winefoxsspellbooks.compat.ali;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.neoforge.plugin.IForgePlugin;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.EntryTooltipUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.loot.AddItemLootModifier;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Advanced Loot Info 兼容：把本模组的全局战利品修饰器翻译成 ALI 能显示的条目。
 * <p>
 * 注意：除 {@code com.yanny.ali.api} 外，本类引用的 {@code com.yanny.ali.plugin.*} 与
 * {@code com.yanny.ali.neoforge.plugin.*} 都不是 ALI 的公开 API，升级 ALI 时需要复核签名。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-30
 */
@AliEntrypoint
public class WsbAliPlugin implements IForgePlugin {

    @Override
    public String getModId() {
        return WinefoxsSpellbooks.MODID;
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry,
                                           ILootTableIdConditionPredicate predicate) {
        registry.registerGlobalLootModifier(AddItemLootModifier.class,
            (utils, modifier) -> describeAddItem(utils, modifier, predicate));
    }

    /**
     * 把一条「向某张战利品表追加固定物品」的修饰器描述成 ALI 的物品条目。
     * <p>
     * 概率不在这里算：{@code getEnchantedChance} 会把条件里的 {@code random_chance} 折算进展示概率，
     * 故 rawChance 传 1.0。
     */
    private static Optional<ILootModifier<?>> describeAddItem(IServerUtils utils,
                                                              AddItemLootModifier modifier,
                                                              ILootTableIdConditionPredicate predicate) {
        List<LootItemCondition> allConditions = Arrays.asList(modifier.getLootConditions());
        IntProvider provider = modifier.getCountProvider();
        RangeValue countRange = new RangeValue(provider.getMinValue(), provider.getMaxValue());

        return GlobalLootModifierUtils.getLootModifier(allConditions, conditions -> {
            EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, conditions, 1.0f);
            // RangeValue 不可变，tooltip 与条目共用同一份即可
            EnchantedRanges count = new EnchantedRanges(countRange);

            TooltipNode tooltip = EntryTooltipUtils.getTooltip(utils, 0, chance, count,
                Collections.emptyList(), conditions).build();
            ItemNode node = new ItemNode(1.0f, countRange,
                modifier.getItem().getDefaultInstance(), tooltip,
                Collections.emptyList(), conditions);
            return Collections.singletonList(new IOperation.AddOperation(stack -> true, node));
        }, predicate);
    }
}
