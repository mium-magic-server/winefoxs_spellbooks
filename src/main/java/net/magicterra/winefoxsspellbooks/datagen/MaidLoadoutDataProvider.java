package net.magicterra.winefoxsspellbooks.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
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
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * 女仆装备配置数据生成器 生成默认的 maid_loadouts JSON 文件
 * <p>
 * 装备与法术均以 tag 引用（见 {@link MaidLoadoutTagsProvider}）。
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

    private static ItemPool weaponPool(int rolls, TagKey<Item> tag) {
        return ItemPool.of(rolls, List.of(ItemTagEntry.of(tag.location(), 1, true)));
    }

    private static ItemPool armorPool(TagKey<Item> tag) {
        return ItemPool.of(1, List.of(ItemTagEntry.of(tag.location(), 1, true)));
    }

    private static SpellPool spellPool(int minRolls, int maxRolls, MaidSpellAction action) {
        return SpellPool.of(minRolls, maxRolls, List.of(SpellTagEntry.of(action.tag().location(), 1, true)));
    }

    /**
     * 各层通用的轻甲配置（法师 / 辅助共用）。
     */
    private static ArmorConfig lightArmor(int tier) {
        return switch (tier) {
            case 1 -> ArmorConfig.builder()
                .helmet(0.2f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER1))
                .build();
            case 2 -> ArmorConfig.builder()
                .helmet(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER2))
                .chestplate(0.3f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER2))
                .build();
            default -> ArmorConfig.builder()
                .helmet(0.6f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER3))
                .chestplate(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER3))
                .leggings(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_LEGGINGS_TIER3))
                .boots(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_BOOTS_TIER3))
                .build();
        };
    }

    // ==================== 战士职业配置 ====================

    /**
     * 创建战士职业配置 侧重近战武器和护甲，攻击法术为主
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
        return MaidLoadoutTier.builder(1, 2)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER1))
            .armor(ArmorConfig.builder()
                .helmet(0.3f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER1))
                .boots(0.3f, armorPool(MaidLoadoutTagsProvider.MAID_BOOTS_TIER1))
                .build())
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(1, 2, MaidSpellAction.ATTACK))
                .build())
            .spellLevelCap(3)
            .build();
    }

    private MaidLoadoutTier createWarriorTier2() {
        return MaidLoadoutTier.builder(3, 4)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER2))
            .armor(ArmorConfig.builder()
                .helmet(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER2))
                .chestplate(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER2))
                .leggings(0.4f, armorPool(MaidLoadoutTagsProvider.MAID_LEGGINGS_TIER2))
                .boots(0.5f, armorPool(MaidLoadoutTagsProvider.MAID_BOOTS_TIER2))
                .build())
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(2, 3, MaidSpellAction.ATTACK))
                .defense(spellPool(1, 1, MaidSpellAction.DEFENSE))
                .build())
            .spellLevelCap(6)
            .build();
    }

    private MaidLoadoutTier createWarriorTier3() {
        return MaidLoadoutTier.builder(5, 6)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER3))
            .armor(ArmorConfig.builder()
                .helmet(0.8f, armorPool(MaidLoadoutTagsProvider.MAID_HELMETS_TIER3))
                .chestplate(0.7f, armorPool(MaidLoadoutTagsProvider.MAID_CHESTPLATES_TIER3))
                .leggings(0.7f, armorPool(MaidLoadoutTagsProvider.MAID_LEGGINGS_TIER3))
                .boots(0.8f, armorPool(MaidLoadoutTagsProvider.MAID_BOOTS_TIER3))
                .build())
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(3, 4, MaidSpellAction.ATTACK))
                .defense(spellPool(1, 2, MaidSpellAction.DEFENSE))
                .build())
            .spellLevelCap(10)
            .build();
    }

    // ==================== 法师职业配置 ====================

    /**
     * 创建法师职业配置 侧重法杖和轻甲，多种法术
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
        return MaidLoadoutTier.builder(1, 2)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER1))
            .armor(lightArmor(1))
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(2, 3, MaidSpellAction.ATTACK))
                .build())
            .spellLevelCap(3)
            .build();
    }

    private MaidLoadoutTier createMageTier2() {
        return MaidLoadoutTier.builder(3, 4)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER2))
            .armor(lightArmor(2))
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(1, 3, MaidSpellAction.ATTACK))
                .defense(spellPool(0, 2, MaidSpellAction.DEFENSE))
                .negativeEffect(spellPool(0, 1, MaidSpellAction.NEGATIVE))
                .support(spellPool(0, 1, MaidSpellAction.SUPPORT))
                .build())
            .spellLevelCap(6)
            .build();
    }

    private MaidLoadoutTier createMageTier3() {
        return MaidLoadoutTier.builder(5, 6)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER3))
            .armor(lightArmor(3))
            .spells(SpellPoolConfig.builder()
                .attack(spellPool(1, 5, MaidSpellAction.ATTACK))
                .defense(spellPool(1, 2, MaidSpellAction.DEFENSE))
                .negativeEffect(spellPool(1, 2, MaidSpellAction.NEGATIVE))
                .support(spellPool(1, 2, MaidSpellAction.SUPPORT))
                .movement(spellPool(1, 1, MaidSpellAction.MOVEMENT))
                .build())
            .spellLevelCap(10)
            .build();
    }

    // ==================== 辅助职业配置 ====================

    /**
     * 创建辅助职业配置 侧重治疗和支援法术，轻装
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
        return MaidLoadoutTier.builder(1, 2)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER1))
            .armor(lightArmor(1))
            .spells(SpellPoolConfig.builder()
                .support(spellPool(0, 1, MaidSpellAction.SUPPORT))
                .supportOther(spellPool(1, 2, MaidSpellAction.SUPPORT_OTHER))
                .attack(spellPool(0, 1, MaidSpellAction.ATTACK))
                .build())
            .spellLevelCap(3)
            .build();
    }

    private MaidLoadoutTier createSupportTier2() {
        return MaidLoadoutTier.builder(3, 4)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER2))
            .armor(lightArmor(2))
            .spells(SpellPoolConfig.builder()
                .support(spellPool(1, 1, MaidSpellAction.SUPPORT))
                .supportOther(spellPool(0, 2, MaidSpellAction.SUPPORT_OTHER))
                .positiveEffect(spellPool(0, 2, MaidSpellAction.POSITIVE))
                .defense(spellPool(1, 1, MaidSpellAction.DEFENSE))
                .attack(spellPool(1, 2, MaidSpellAction.ATTACK))
                .build())
            .spellLevelCap(6)
            .build();
    }

    private MaidLoadoutTier createSupportTier3() {
        return MaidLoadoutTier.builder(5, 6)
            .weapon(weaponPool(1, MaidLoadoutTagsProvider.MAID_WEAPONS_TIER3))
            .armor(lightArmor(3))
            .spells(SpellPoolConfig.builder()
                .support(spellPool(1, 2, MaidSpellAction.SUPPORT))
                .supportOther(spellPool(1, 2, MaidSpellAction.SUPPORT_OTHER))
                .positiveEffect(spellPool(1, 2, MaidSpellAction.POSITIVE))
                .defense(spellPool(1, 2, MaidSpellAction.DEFENSE))
                .attack(spellPool(1, 2, MaidSpellAction.ATTACK))
                .build())
            .spellLevelCap(10)
            .build();
    }
}
