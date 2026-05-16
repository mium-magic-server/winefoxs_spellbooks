package net.magicterra.winefoxsspellbooks.datagen;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.loot.AddItemLootModifier;
import net.magicterra.winefoxsspellbooks.registry.WsbEntities;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

/**
 * 灵狐精魂掉落注入
 * <p>
 * 路 1：女仆击杀妖精时按概率掉落（驯化 25%、召唤 35%，Looting 每级 +5%）。<br>
 * 路 2：在原版 / Iron's Spells 的若干宝箱表里按概率追加。
 * <p>
 * 全部通过 vanilla {@code entity_properties} / {@code random_chance(_with_enchanted_bonus)} + NeoForge
 * {@code loot_table_id} 条件组合实现，{@link AddItemLootModifier} 只负责在条件全通过后塞 ItemStack。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public class WsbGlobalLootModifierProvider extends GlobalLootModifierProvider {
    /** Looting 附魔每级追加的概率加成 */
    private static final float LOOTING_PER_LEVEL = 0.05f;

    private static final ResourceLocation FAIRY_TABLE =
        ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "entities/fairy");

    public WsbGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, WinefoxsSpellbooks.MODID);
    }

    @Override
    protected void start() {
        // ---- 路 1：女仆击杀妖精 ----
        addDrop("fairy_drop_tamed_maid",
            ConstantInt.of(1),
            LootTableIdCondition.builder(FAIRY_TABLE).build(),
            killerOfType(InitEntities.MAID.get()),
            randomChanceWithLooting(0.25f));

        addDrop("fairy_drop_summoned_maid",
            ConstantInt.of(1),
            LootTableIdCondition.builder(FAIRY_TABLE).build(),
            killerOfType(WsbEntities.SUMMONED_MAID.get()),
            randomChanceWithLooting(0.35f));

        // ---- 路 2：宝箱掉落 ----
        addChest("chest_woodland_mansion",
            ResourceLocation.withDefaultNamespace("chests/woodland_mansion"), 0.25f, UniformInt.of(1, 2));
        addChest("chest_stronghold_library",
            ResourceLocation.withDefaultNamespace("chests/stronghold_library"), 0.15f, ConstantInt.of(1));
        addChest("chest_nether_bridge",
            ResourceLocation.withDefaultNamespace("chests/nether_bridge"), 0.20f, ConstantInt.of(1));
        addChest("chest_pyromancer_old_cask",
            ironsId("chests/pyromancer_tower/old_cask"), 0.40f, UniformInt.of(1, 2));
        addChest("chest_pyromancer_supplies",
            ironsId("chests/pyromancer_tower/pyromancer_supplies"), 0.30f, ConstantInt.of(1));
        addChest("chest_catacombs_coffin",
            ironsId("chests/catacombs/coffin_loot"), 0.25f, ConstantInt.of(1));
        addChest("chest_citadel_tomes",
            ironsId("chests/citadel/citadel_tomes"), 0.35f, UniformInt.of(1, 2));
    }

    private static ResourceLocation ironsId(String path) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, path);
    }

    private void addChest(String name, ResourceLocation tableId, float chance, IntProvider count) {
        addDrop(name,
            count,
            LootTableIdCondition.builder(tableId).build(),
            LootItemRandomChanceCondition.randomChance(chance).build());
    }

    private void addDrop(String name, IntProvider count, LootItemCondition... conditions) {
        this.add(name, new AddItemLootModifier(conditions, WsbItems.VULPINE_ANIMA.get(), count));
    }

    private static LootItemCondition killerOfType(EntityType<?> type) {
        return LootItemEntityPropertyCondition.hasProperties(
            LootContext.EntityTarget.ATTACKER,
            EntityPredicate.Builder.entity().of(type)).build();
    }

    private LootItemCondition randomChanceWithLooting(float unenchanted) {
        return LootItemRandomChanceWithEnchantedBonusCondition
            .randomChanceAndLootingBoost(this.registries, unenchanted, LOOTING_PER_LEVEL).build();
    }
}
