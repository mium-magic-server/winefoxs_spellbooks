package net.magicterra.winefoxsspellbooks.datagen;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.loot.AddItemLootModifier;
import net.magicterra.winefoxsspellbooks.loot.AddSchoolScrollLootModifier;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

/**
 * 灵狐精魂掉落注入 + Winefox Hex 卷轴地牢注入 + 残月血酿宝箱注入
 * <p>
 * 路 1：女仆击杀妖精时按概率掉落（驯化 25%、召唤 35%，Looting 每级 +5%）。<br> 路 2：在原版 / Iron's Spells 的若干宝箱表里按概率追加灵狐精魂。<br> 路 3：在原版 / Iron's Spells 的若干宝箱表里按概率追加 Winefox Hex 学派卷轴。<br> 路 4：在原版村庄民居 / 掠夺者前哨站 / 林地府邸与 Iron's 酒桶、补给类宝箱里按概率追加残月血酿。
 * <p>
 * 全部通过 vanilla {@code entity_properties} / {@code random_chance(_with_enchanted_bonus)} + NeoForge {@code loot_table_id} 条件组合实现，{@link AddItemLootModifier} / {@link AddSchoolScrollLootModifier} 只负责在条件全通过后塞 ItemStack。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public class WsbGlobalLootModifierProvider extends GlobalLootModifierProvider {
    /**
     * Looting 附魔每级追加的概率加成
     */
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

        // ---- 路 2：灵狐精魂 宝箱掉落 ----
        addAnimaChest("chest_woodland_mansion",
            ResourceLocation.withDefaultNamespace("chests/woodland_mansion"), 0.25f, UniformInt.of(1, 2));
        addAnimaChest("chest_stronghold_library",
            ResourceLocation.withDefaultNamespace("chests/stronghold_library"), 0.15f, ConstantInt.of(1));
        addAnimaChest("chest_nether_bridge",
            ResourceLocation.withDefaultNamespace("chests/nether_bridge"), 0.20f, ConstantInt.of(1));
        addAnimaChest("chest_pyromancer_old_cask",
            ironsId("chests/pyromancer_tower/old_cask"), 0.40f, UniformInt.of(1, 2));
        addAnimaChest("chest_pyromancer_supplies",
            ironsId("chests/pyromancer_tower/pyromancer_supplies"), 0.30f, ConstantInt.of(1));
        addAnimaChest("chest_catacombs_coffin",
            ironsId("chests/catacombs/coffin_loot"), 0.25f, ConstantInt.of(1));
        addAnimaChest("chest_citadel_tomes",
            ironsId("chests/citadel/citadel_tomes"), 0.35f, UniformInt.of(1, 2));

        // ---- 路 3：Winefox Hex 卷轴 ----
        addScroll("scroll_pyromancer_old_cask",
            ironsId("chests/pyromancer_tower/old_cask"), 0.30f, 0.0f, 0.4f);
        addScroll("scroll_citadel_tomes",
            ironsId("chests/citadel/citadel_tomes"), 0.30f, 0.2f, 0.7f);
        addScroll("scroll_catacombs_coffin",
            ironsId("chests/catacombs/coffin_loot"), 0.25f, 0.1f, 0.6f);
        addScroll("scroll_stronghold_library",
            ResourceLocation.withDefaultNamespace("chests/stronghold_library"), 0.20f, 0.1f, 0.5f);
        addScroll("scroll_nether_bridge",
            ResourceLocation.withDefaultNamespace("chests/nether_bridge"), 0.15f, 0.0f, 0.4f);

        // ---- 路 4：残月血酿 宝箱掉落 ----
        // 村庄民居：五个生物群系变体是同一张「村民自家箱子」的表，全部挂上才不挑村庄生成的群系
        addVintage("vintage_village_plains_house",
            ResourceLocation.withDefaultNamespace("chests/village/village_plains_house"), 0.20f, UniformInt.of(1, 2));
        addVintage("vintage_village_desert_house",
            ResourceLocation.withDefaultNamespace("chests/village/village_desert_house"), 0.20f, UniformInt.of(1, 2));
        addVintage("vintage_village_savanna_house",
            ResourceLocation.withDefaultNamespace("chests/village/village_savanna_house"), 0.20f, UniformInt.of(1, 2));
        addVintage("vintage_village_snowy_house",
            ResourceLocation.withDefaultNamespace("chests/village/village_snowy_house"), 0.20f, UniformInt.of(1, 2));
        addVintage("vintage_village_taiga_house",
            ResourceLocation.withDefaultNamespace("chests/village/village_taiga_house"), 0.20f, UniformInt.of(1, 2));
        addVintage("vintage_village_temple",
            ResourceLocation.withDefaultNamespace("chests/village/village_temple"), 0.25f, UniformInt.of(1, 2));
        addVintage("vintage_pillager_outpost",
            ResourceLocation.withDefaultNamespace("chests/pillager_outpost"), 0.30f, UniformInt.of(1, 3));
        addVintage("vintage_woodland_mansion",
            ResourceLocation.withDefaultNamespace("chests/woodland_mansion"), 0.40f, UniformInt.of(2, 3));
        // Iron's：优先挂酒桶 / 食物 / 补给类，与「酒」这个物品形象对得上
        addVintage("vintage_pyromancer_fire_ale_cask",
            ironsId("chests/pyromancer_tower/fire_ale_cask"), 0.50f, UniformInt.of(2, 3));
        addVintage("vintage_pyromancer_old_cask",
            ironsId("chests/pyromancer_tower/old_cask"), 0.45f, UniformInt.of(1, 3));
        addVintage("vintage_pyromancer_fire_ale_trove",
            ironsId("chests/pyromancer_tower/fire_ale_trove"), 0.35f, UniformInt.of(1, 2));
        addVintage("vintage_ice_spider_den_cask",
            ironsId("chests/ice_spider_den/cask"), 0.40f, UniformInt.of(1, 2));
        addVintage("vintage_icebreaker_food_barrel",
            ironsId("chests/impaled_icebreaker/food_barrel"), 0.30f, UniformInt.of(1, 2));
        addVintage("vintage_citadel_rampart_supplies",
            ironsId("chests/citadel/rampart_supplies"), 0.30f, UniformInt.of(1, 2));
        addVintage("vintage_mangrove_hut_potion_storage",
            ironsId("chests/mangrove_hut/hidden_potion_storage"), 0.25f, ConstantInt.of(1));
        addVintage("vintage_catacombs_coffin",
            ironsId("chests/catacombs/coffin_loot"), 0.15f, ConstantInt.of(1));
    }

    private static ResourceLocation ironsId(String path) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, path);
    }

    /**
     * 灵狐精魂：在指定宝箱表里按概率追加。
     */
    private void addAnimaChest(String name, ResourceLocation tableId, float chance, IntProvider count) {
        addChest(name, WsbItems.VULPINE_ANIMA.get(), tableId, chance, count);
    }

    /**
     * 残月血酿：在指定宝箱表里按概率追加。
     */
    private void addVintage(String name, ResourceLocation tableId, float chance, IntProvider count) {
        addChest(name, WsbItems.CRESCENT_BLOOD_VINTAGE.get(), tableId, chance, count);
    }

    private void addChest(String name, Item item, ResourceLocation tableId, float chance, IntProvider count) {
        this.add(name, new AddItemLootModifier(new LootItemCondition[] {
            LootTableIdCondition.builder(tableId).build(),
            LootItemRandomChanceCondition.randomChance(chance).build()
        }, item, count));
    }

    private void addDrop(String name, IntProvider count, LootItemCondition... conditions) {
        this.add(name, new AddItemLootModifier(conditions, WsbItems.VULPINE_ANIMA.get(), count));
    }

    private void addScroll(String name, ResourceLocation tableId, float chance, float minQuality, float maxQuality) {
        LootItemCondition[] conditions = {
            LootTableIdCondition.builder(tableId).build(),
            LootItemRandomChanceCondition.randomChance(chance).build()
        };
        this.add(name, new AddSchoolScrollLootModifier(conditions,
            WinefoxHexHelper.WINEFOX_HEX_ID, minQuality, maxQuality));
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
