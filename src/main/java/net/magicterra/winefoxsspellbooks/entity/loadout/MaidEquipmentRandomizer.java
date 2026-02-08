package net.magicterra.winefoxsspellbooks.entity.loadout;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.ArmorConfig;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.ArmorSlotConfig;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadout;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadoutTier;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.ModelFilter;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.SpellPoolConfig;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.ItemEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.ItemTagEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.LoadoutEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.SpellEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.entry.SpellTagEntry;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.ItemPool;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.SpellPool;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 女仆装备随机化器，根据配置随机生成装备和法术
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public final class MaidEquipmentRandomizer {
    private MaidEquipmentRandomizer() {
    }

    /**
     * 装备结果记录
     */
    public record EquipmentResult(
        ItemStack weapon,
        Map<EquipmentSlot, ItemStack> armor,
        CategorizedSpells spells,
        ResourceLocation modelId
    ) {
        public static EquipmentResult empty() {
            return new EquipmentResult(ItemStack.EMPTY, Map.of(), CategorizedSpells.EMPTY, null);
        }
    }

    /**
     * 带等级的法术记录
     */
    public record SpellWithLevel(AbstractSpell spell, int level) {
    }

    /**
     * 分类的法术记录，保留从配置文件中读取的类别信息
     */
    public record CategorizedSpells(
        List<SpellWithLevel> attack,
        List<SpellWithLevel> defense,
        List<SpellWithLevel> movement,
        List<SpellWithLevel> support,
        List<SpellWithLevel> positive,
        List<SpellWithLevel> negativeEffect,
        List<SpellWithLevel> supportOther
    ) {
        public static final CategorizedSpells EMPTY = new CategorizedSpells(
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );
    }

    /**
     * 根据配置和召唤等级随机生成装备
     *
     * @param loadout     装备配置
     * @param summonLevel 召唤等级
     * @param random      随机数生成器
     * @param availableModels 可用模型集合
     * @return 装备结果
     */
    public static EquipmentResult randomize(
        MaidLoadout loadout,
        int summonLevel,
        RandomSource random,
        Set<ResourceLocation> availableModels
    ) {
        // 1. 匹配层级
        Optional<MaidLoadoutTier> tierOpt = loadout.matchTier(summonLevel);
        if (tierOpt.isEmpty()) {
            WinefoxsSpellbooks.LOGGER.warn("No tier matched for summon level {} in loadout {}", summonLevel, loadout.id());
            return EquipmentResult.empty();
        }

        MaidLoadoutTier tier = tierOpt.get();

        // 2. 随机选择模型
        ResourceLocation modelId = selectModel(loadout.modelFilter(), availableModels, random);

        // 3. 随机选择武器
        ItemStack weapon = tier.weapon()
            .map(pool -> selectFromItemPool(pool, random))
            .orElse(ItemStack.EMPTY);

        // 4. 随机生成护甲
        Map<EquipmentSlot, ItemStack> armor = generateArmor(tier.armor().orElse(ArmorConfig.EMPTY), random);

        // 5. 随机分配法术（保留类别信息）
        CategorizedSpells spells = generateCategorizedSpells(tier.spells().orElse(SpellPoolConfig.EMPTY), tier.spellLevelCap(), random);

        return new EquipmentResult(weapon, armor, spells, modelId);
    }

    /**
     * 选择模型
     */
    private static ResourceLocation selectModel(
        ModelFilter filter,
        Set<ResourceLocation> availableModels,
        RandomSource random
    ) {
        if (availableModels.isEmpty()) {
            return null;
        }

        Set<ResourceLocation> filtered = filter.filterModels(availableModels);
        if (filtered.isEmpty()) {
            // 回退到默认模型
            WinefoxsSpellbooks.LOGGER.debug("Model filter returned empty set, using first available model");
            return availableModels.iterator().next();
        }

        // 随机选择一个模型
        int index = random.nextInt(filtered.size());
        return filtered.stream().skip(index).findFirst().orElse(null);
    }

    /**
     * 从物品池中随机选择物品
     */
    private static ItemStack selectFromItemPool(ItemPool pool, RandomSource random) {
        int rolls = pool.rolls().roll(random);
        if (rolls <= 0 || pool.entries().isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 展开所有条目并计算总权重
        List<WeightedItem> expandedItems = new ArrayList<>();
        for (LoadoutEntry entry : pool.entries()) {
            expandItemEntry(entry, expandedItems);
        }

        if (expandedItems.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 按权重随机选择
        return selectWeightedItem(expandedItems, random);
    }

    /**
     * 展开物品条目
     */
    private static void expandItemEntry(LoadoutEntry entry, List<WeightedItem> result) {
        if (entry instanceof ItemEntry(ResourceLocation name, int weight)) {
            Item item = BuiltInRegistries.ITEM.get(name);
            if (item != Items.AIR) {
                result.add(new WeightedSingleItem(item, weight));
            }
        } else if (entry instanceof ItemTagEntry(ResourceLocation name, int weight, boolean expand)) {
            TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), name);
            if (expand) {
                // 展开标签，每个物品独立参与权重计算
                BuiltInRegistries.ITEM.getTag(tagKey).ifPresent(tag -> {
                    tag.forEach(holder -> result.add(new WeightedSingleItem(holder.value(), 1)));
                });
            } else {
                // 整个标签作为一个条目
                List<Item> items = new ArrayList<>();
                BuiltInRegistries.ITEM.getTag(tagKey).ifPresent(tag -> {
                    tag.forEach(holder -> items.add(holder.value()));
                });
                if (!items.isEmpty()) {
                    result.add(new WeightedItemTag(items, weight));
                }
            }
        }
    }

    /**
     * 按权重随机选择物品
     */
    private static ItemStack selectWeightedItem(List<WeightedItem> items, RandomSource random) {
        int totalWeight = items.stream().mapToInt(WeightedItem::weight).sum();
        if (totalWeight <= 0) {
            return ItemStack.EMPTY;
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (WeightedItem item : items) {
            cumulative += item.weight();
            if (roll < cumulative) {
                return item.createStack(random);
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * 生成护甲
     */
    private static Map<EquipmentSlot, ItemStack> generateArmor(ArmorConfig config, RandomSource random) {
        Map<EquipmentSlot, ItemStack> armor = new EnumMap<>(EquipmentSlot.class);

        config.helmet().ifPresent(slot ->
            generateArmorSlot(slot, EquipmentSlot.HEAD, armor, random));
        config.chestplate().ifPresent(slot ->
            generateArmorSlot(slot, EquipmentSlot.CHEST, armor, random));
        config.leggings().ifPresent(slot ->
            generateArmorSlot(slot, EquipmentSlot.LEGS, armor, random));
        config.boots().ifPresent(slot ->
            generateArmorSlot(slot, EquipmentSlot.FEET, armor, random));

        return armor;
    }

    /**
     * 生成单个护甲槽位
     */
    private static void generateArmorSlot(
        ArmorSlotConfig config,
        EquipmentSlot slot,
        Map<EquipmentSlot, ItemStack> armor,
        RandomSource random
    ) {
        if (!config.shouldGenerate()) {
            return;
        }

        // 按概率决定是否生成
        if (random.nextFloat() > config.chance()) {
            return;
        }

        // 从所有池中选择
        for (ItemPool pool : config.pools()) {
            ItemStack item = selectFromItemPool(pool, random);
            if (!item.isEmpty()) {
                armor.put(slot, item);
                return;
            }
        }
    }

    /**
     * 生成分类的法术（保留类别信息）
     */
    private static CategorizedSpells generateCategorizedSpells(
        SpellPoolConfig config,
        int spellLevelCap,
        RandomSource random
    ) {
        List<SpellWithLevel> attack = new ArrayList<>();
        List<SpellWithLevel> defense = new ArrayList<>();
        List<SpellWithLevel> movement = new ArrayList<>();
        List<SpellWithLevel> support = new ArrayList<>();
        List<SpellWithLevel> positive = new ArrayList<>();
        List<SpellWithLevel> negativeEffect = new ArrayList<>();
        List<SpellWithLevel> supportOther = new ArrayList<>();

        config.attack().ifPresent(pool ->
            addSpellsFromPool(pool, spellLevelCap, attack, random));
        config.defense().ifPresent(pool ->
            addSpellsFromPool(pool, spellLevelCap, defense, random));
        config.movement().ifPresent(pool ->
            addSpellsFromPool(pool, spellLevelCap, movement, random));
        config.support().ifPresent(pool ->
            addSpellsFromPool(pool, spellLevelCap, support, random));
        config.positiveEffect().ifPresent(pool ->
            addSpellsFromPool(pool, spellLevelCap, positive, random));
        config.negativeEffect().ifPresent(pool ->
            addSpellsFromPool(pool, spellLevelCap, negativeEffect, random));
        config.supportOther().ifPresent(pool ->
            addSpellsFromPool(pool, spellLevelCap, supportOther, random));

        return new CategorizedSpells(attack, defense, movement, support, positive, negativeEffect, supportOther);
    }

    /**
     * 从法术池添加法术
     */
    private static void addSpellsFromPool(
        SpellPool pool,
        int spellLevelCap,
        List<SpellWithLevel> spells,
        RandomSource random
    ) {
        int rolls = pool.rolls().roll(random);
        if (rolls <= 0 || pool.entries().isEmpty()) {
            return;
        }

        // 展开所有法术条目
        List<WeightedSpell> expandedSpells = new ArrayList<>();
        for (LoadoutEntry entry : pool.entries()) {
            expandSpellEntry(entry, expandedSpells);
        }

        if (expandedSpells.isEmpty()) {
            return;
        }

        // 随机选择指定次数
        for (int i = 0; i < rolls; i++) {
            AbstractSpell spell = selectWeightedSpell(expandedSpells, random);
            if (spell != null && spell != SpellRegistry.none()) {
                // 计算实际法术等级：min(配置上限, 法术最大等级)
                int actualLevel = Math.min(spellLevelCap, spell.getMaxLevel());
                spells.add(new SpellWithLevel(spell, actualLevel));
            }
        }
    }

    /**
     * 展开法术条目
     */
    private static void expandSpellEntry(LoadoutEntry entry, List<WeightedSpell> result) {
        if (entry instanceof SpellEntry(ResourceLocation name, int weight)) {
            SpellRegistry.REGISTRY.getOptional(name)
                .ifPresent(spell -> result.add(new WeightedSingleSpell(spell, weight)));
        } else if (entry instanceof SpellTagEntry(ResourceLocation name, int weight, boolean expand)) {
            TagKey<AbstractSpell> tagKey = TagKey.create(SpellRegistry.SPELL_REGISTRY_KEY, name);
            if (expand) {
                // 展开标签
                SpellRegistry.REGISTRY.getTag(tagKey).ifPresent(tag -> {
                    tag.forEach(holder -> result.add(new WeightedSingleSpell(holder.value(), 1)));
                });
            } else {
                // 整个标签作为一个条目
                List<AbstractSpell> spells = new ArrayList<>();
                SpellRegistry.REGISTRY.getTag(tagKey).ifPresent(tag -> {
                    tag.forEach(holder -> spells.add(holder.value()));
                });
                if (!spells.isEmpty()) {
                    result.add(new WeightedSpellTag(spells, weight));
                }
            }
        }
    }

    /**
     * 按权重随机选择法术
     */
    private static AbstractSpell selectWeightedSpell(List<WeightedSpell> spells, RandomSource random) {
        int totalWeight = spells.stream().mapToInt(WeightedSpell::weight).sum();
        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (WeightedSpell spell : spells) {
            cumulative += spell.weight();
            if (roll < cumulative) {
                return spell.select(random);
            }
        }

        return null;
    }

    // ==================== 内部辅助类 ====================

    private sealed interface WeightedItem permits WeightedSingleItem, WeightedItemTag {
        int weight();
        ItemStack createStack(RandomSource random);
    }

    private record WeightedSingleItem(Item item, int weight) implements WeightedItem {
        @Override
        public ItemStack createStack(RandomSource random) {
            return new ItemStack(item);
        }
    }

    private record WeightedItemTag(List<Item> items, int weight) implements WeightedItem {
        @Override
        public ItemStack createStack(RandomSource random) {
            if (items.isEmpty()) return ItemStack.EMPTY;
            return new ItemStack(items.get(random.nextInt(items.size())));
        }
    }

    private sealed interface WeightedSpell permits WeightedSingleSpell, WeightedSpellTag {
        int weight();
        AbstractSpell select(RandomSource random);
    }

    private record WeightedSingleSpell(AbstractSpell spell, int weight) implements WeightedSpell {
        @Override
        public AbstractSpell select(RandomSource random) {
            return spell;
        }
    }

    private record WeightedSpellTag(List<AbstractSpell> spells, int weight) implements WeightedSpell {
        @Override
        public AbstractSpell select(RandomSource random) {
            if (spells.isEmpty()) return null;
            return spells.get(random.nextInt(spells.size()));
        }
    }
}
