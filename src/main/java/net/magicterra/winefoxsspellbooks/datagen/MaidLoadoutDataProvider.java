package net.magicterra.winefoxsspellbooks.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.loadout.codec.LoadoutCodecs;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.ArmorConfig;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.BroomMode;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadout;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadoutTier;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.ModelFilter;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.SpellPoolConfig;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.ItemTagEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.SpellTagEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.ItemPool;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.SpellPool;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

/**
 * 女仆装备配置数据生成器
 * 生成默认的 maid_loadouts JSON 文件
 * <p>
 * 使用 {@link MaidLoadoutTagsProvider} 中定义的物品 tag 来引用装备，
 * 使用 {@link MaidSpellRegistry} 中定义的法术 tag 来引用法术。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public class MaidLoadoutDataProvider implements DataProvider {
    private final PackOutput output;

    public MaidLoadoutDataProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // 生成战士职业配置
        futures.add(saveLoadout(cache, createWarriorLoadout()));

        // 生成法师职业配置
        futures.add(saveLoadout(cache, createMageLoadout()));

        // 生成辅助职业配置
        futures.add(saveLoadout(cache, createSupportLoadout()));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveLoadout(CachedOutput cache, MaidLoadout loadout) {
        ResourceLocation id = loadout.id();
        Path path = output.getOutputFolder()
            .resolve("data")
            .resolve(id.getNamespace())
            .resolve("summoned_maid_loadouts")
            .resolve(id.getPath() + ".json");

        // 使用 Codec 序列化为 JSON
        LoadoutCodecs.MaidLoadoutData data = new LoadoutCodecs.MaidLoadoutData(
            loadout.weight(),
            loadout.modelFilter(),
            loadout.broomMode(),
            loadout.tiers()
        );

        var result = LoadoutCodecs.MAID_LOADOUT_DATA.encodeStart(JsonOps.INSTANCE, data);

        if (result.error().isPresent()) {
            WinefoxsSpellbooks.LOGGER.error("Failed to encode maid loadout {}: {}",
                id, result.error().get().message());
            return CompletableFuture.completedFuture(null);
        }

        JsonElement json = result.result().orElseThrow();
        return DataProvider.saveStable(cache, json, path);
    }

    @Override
    public String getName() {
        return "Maid Loadout Data";
    }

    // ==================== 辅助方法 ====================

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, path);
    }

    /** 创建武器池，使用 tag 引用 */
    private static ItemPool weaponPool(int rolls, ResourceLocation tagId) {
        return ItemPool.of(rolls, List.of(ItemTagEntry.of(tagId, 1, true)));
    }

    /** 创建护甲池，使用 tag 引用 */
    private static ItemPool armorPool(ResourceLocation tagId) {
        return ItemPool.of(1, List.of(ItemTagEntry.of(tagId, 1, true)));
    }

    /** 创建法术池，使用 tag 引用 */
    private static SpellPool spellPool(int minRolls, int maxRolls, ResourceLocation tagId) {
        return SpellPool.of(minRolls, maxRolls, List.of(SpellTagEntry.of(tagId, 1, true)));
    }

    // ==================== 战士职业配置 ====================

    /**
     * 创建战士职业配置
     * 侧重近战武器和护甲，攻击法术为主
     */
    private MaidLoadout createWarriorLoadout() {
        return MaidLoadout.builder(id("warrior"))
            .weight(40)
            .modelFilter(ModelFilter.ALL)
            .broomMode(BroomMode.NEVER) // 战士不使用扫帚
            .addTier(createWarriorTier1())
            .addTier(createWarriorTier2())
            .addTier(createWarriorTier3())
            .build();
    }

    private MaidLoadoutTier createWarriorTier1() {
        return MaidLoadoutTier.builder(1, 3)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER1.location()))
            .armor(ArmorConfig.builder()
                .helmet(0.3f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER1.location()))
                .boots(0.3f, armorPool(MaidLoadoutTagsProvider.MAID_BOOTS_TIER1.location()))
                .build())
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(1, 2, MaidSpellRegistry.ATTACK_SPELLS_TAG.location()))
                .build())
            .spellLevelCap(1)
            .build();
    }

    private MaidLoadoutTier createWarriorTier2() {
        return MaidLoadoutTier.builder(4, 6)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER2.location()))
            .armor(ArmorConfig.builder()
                .helmet(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER2.location()))
                .chestplate(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER2.location()))
                .leggings(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_LEGGINGS_TIER2.location()))
                .boots(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_BOOTS_TIER2.location()))
                .build())
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(2, 3, MaidSpellRegistry.ATTACK_SPELLS_TAG.location()))
                .defense(spellPool(1, 1, MaidSpellRegistry.DEFENSE_SPELLS_TAG.location()))
                .build())
            .spellLevelCap(3)
            .build();
    }

    private MaidLoadoutTier createWarriorTier3() {
        return MaidLoadoutTier.builder(7, 10)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER3.location()))
            .armor(ArmorConfig.builder()
                .helmet(0.8f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER3.location()))
                .chestplate(0.7f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER3.location()))
                .leggings(0.7f, armorPool(MaidLoadoutTagsProvider.MAID_LEGGINGS_TIER3.location()))
                .boots(0.8f, armorPool(MaidLoadoutTagsProvider.MAID_BOOTS_TIER3.location()))
                .build())
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(3, 4, MaidSpellRegistry.ATTACK_SPELLS_TAG.location()))
                .defense(spellPool(1, 2, MaidSpellRegistry.DEFENSE_SPELLS_TAG.location()))
                .build())
            .spellLevelCap(5)
            .build();
    }

    // ==================== 法师职业配置 ====================

    /**
     * 创建法师职业配置
     * 侧重法杖和轻甲，多种法术
     */
    private MaidLoadout createMageLoadout() {
        return MaidLoadout.builder(id("mage"))
            .weight(35)
            .modelFilter(ModelFilter.ALL)
            .broomMode(BroomMode.DEFAULT) // 法师使用默认概率
            .addTier(createMageTier1())
            .addTier(createMageTier2())
            .addTier(createMageTier3())
            .build();
    }

    private MaidLoadoutTier createMageTier1() {
        return MaidLoadoutTier.builder(1, 3)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER1.location()))
            .armor(ArmorConfig.builder()
                .helmet(0.2f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER1.location()))
                .build())
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(2, 3, MaidSpellRegistry.ATTACK_SPELLS_TAG.location()))
                .build())
            .spellLevelCap(2)
            .build();
    }

    private MaidLoadoutTier createMageTier2() {
        return MaidLoadoutTier.builder(4, 6)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER2.location()))
            .armor(ArmorConfig.builder()
                .helmet(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER2.location()))
                .chestplate(0.3f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER2.location()))
                .build())
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(3, 4, MaidSpellRegistry.ATTACK_SPELLS_TAG.location()))
                .defense(spellPool(1, 1, MaidSpellRegistry.DEFENSE_SPELLS_TAG.location()))
                .negativeEffect(spellPool(0, 1, MaidSpellRegistry.NEGATIVE_EFFECT_SPELLS_TAG.location()))
                .build())
            .spellLevelCap(4)
            .build();
    }

    private MaidLoadoutTier createMageTier3() {
        return MaidLoadoutTier.builder(7, 10)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER3.location()))
            .armor(ArmorConfig.builder()
                .helmet(0.6f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER3.location()))
                .chestplate(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER3.location()))
                .leggings(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_LEGGINGS_TIER3.location()))
                .boots(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_BOOTS_TIER3.location()))
                .build())
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(4, 5, MaidSpellRegistry.ATTACK_SPELLS_TAG.location()))
                .defense(spellPool(1, 2, MaidSpellRegistry.DEFENSE_SPELLS_TAG.location()))
                .negativeEffect(spellPool(1, 2, MaidSpellRegistry.NEGATIVE_EFFECT_SPELLS_TAG.location()))
                .build())
            .spellLevelCap(6)
            .build();
    }

    // ==================== 辅助职业配置 ====================

    /**
     * 创建辅助职业配置
     * 侧重治疗和支援法术，轻装
     */
    private MaidLoadout createSupportLoadout() {
        return MaidLoadout.builder(id("support"))
            .weight(25)
            .modelFilter(ModelFilter.ALL)
            .broomMode(BroomMode.DEFAULT) // 辅助使用默认概率
            .addTier(createSupportTier1())
            .addTier(createSupportTier2())
            .addTier(createSupportTier3())
            .build();
    }

    private MaidLoadoutTier createSupportTier1() {
        return MaidLoadoutTier.builder(1, 3)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER1.location()))
            .armor(ArmorConfig.builder()
                .helmet(0.2f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER1.location()))
                .build())
            .spells(SpellPoolConfig.builder()
                .support(spellPool(1, 2, MaidSpellRegistry.SUPPORT_SPELLS_TAG.location()))
                .attack(spellPool(1, 1, MaidSpellRegistry.ATTACK_SPELLS_TAG.location()))
                .build())
            .spellLevelCap(2)
            .build();
    }

    private MaidLoadoutTier createSupportTier2() {
        return MaidLoadoutTier.builder(4, 6)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER2.location()))
            .armor(ArmorConfig.builder()
                .helmet(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER2.location()))
                .chestplate(0.3f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER2.location()))
                .build())
            .spells(SpellPoolConfig.builder()
                .support(spellPool(2, 3, MaidSpellRegistry.SUPPORT_SPELLS_TAG.location()))
                .defense(spellPool(1, 1, MaidSpellRegistry.DEFENSE_SPELLS_TAG.location()))
                .attack(spellPool(1, 2, MaidSpellRegistry.ATTACK_SPELLS_TAG.location()))
                .build())
            .spellLevelCap(4)
            .build();
    }

    private MaidLoadoutTier createSupportTier3() {
        return MaidLoadoutTier.builder(7, 10)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER3.location()))
            .armor(ArmorConfig.builder()
                .helmet(0.6f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER3.location()))
                .chestplate(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER3.location()))
                .leggings(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_LEGGINGS_TIER3.location()))
                .boots(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_BOOTS_TIER3.location()))
                .build())
            .spells(SpellPoolConfig.builder()
                .support(spellPool(3, 4, MaidSpellRegistry.SUPPORT_SPELLS_TAG.location()))
                .defense(spellPool(1, 2, MaidSpellRegistry.DEFENSE_SPELLS_TAG.location()))
                .attack(spellPool(2, 3, MaidSpellRegistry.ATTACK_SPELLS_TAG.location()))
                .build())
            .spellLevelCap(6)
            .build();
    }
}
