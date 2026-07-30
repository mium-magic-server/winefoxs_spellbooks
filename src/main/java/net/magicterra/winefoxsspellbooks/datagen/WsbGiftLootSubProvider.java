package net.magicterra.winefoxsspellbooks.datagen;

import java.util.function.BiConsumer;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.magicterra.winefoxsspellbooks.registry.WsbLootTables;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * 模组自有"赠礼"类战利品表生成器
 * <p>
 * JAVA 端只负责"是否赠送 + 哪只女仆"，物品内容交给本表。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public class WsbGiftLootSubProvider implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        // 95 : 5 加权，留一点"今天什么都没收到"的运气感
        consumer.accept(WsbLootTables.MAID_MORNING_GIFT,
            LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1f))
                .add(LootItem.lootTableItem(WsbItems.VULPINE_ANIMA.get()).setWeight(95))
                .add(EmptyLootItem.emptyItem().setWeight(5))));
    }
}
