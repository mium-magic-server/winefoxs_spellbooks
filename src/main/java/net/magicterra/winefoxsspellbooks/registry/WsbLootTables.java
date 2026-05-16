package net.magicterra.winefoxsspellbooks.registry;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * 模组自有战利品表常量
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public final class WsbLootTables {
    private WsbLootTables() {
    }

    /**
     * 满好感度女仆的晨间礼物战利品表。
     * <p>
     * 调用方在 {@code PlayerWakeUpEvent} 中完成"挑选女仆 + 写冷却"，把"具体掉什么"交给本表。
     * 仿 vanilla {@code minecraft:gameplay/cat_morning_gift} 的范式。
     */
    public static final ResourceKey<LootTable> MAID_MORNING_GIFT = ResourceKey.create(
        Registries.LOOT_TABLE,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "gameplay/maid_morning_gift")
    );
}
