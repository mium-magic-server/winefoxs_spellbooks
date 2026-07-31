package net.magicterra.winefoxsspellbooks.entity.loadout;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
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
 * <p>
 * 法术一路额外受三个运行时配置约束（学派准入、稀有度上限、等级上限），不合格的法术在展开池子时
 * 就剔除，剩下的再按等级上限定级，判据见 {@link #isEligible}。三者只会往下压，压不出比数据包更强的配装。
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
     *
     * @param byAction 行为类别 → 该类随机到的法术；没随机到法术的类别不出现在 map 里
     */
    public record CategorizedSpells(Map<MaidSpellAction, List<SpellWithLevel>> byAction) {
        public static final CategorizedSpells EMPTY = new CategorizedSpells(Map.of());

        /**
         * 取某一类别随机到的法术，没有时返回空表。
         */
        public List<SpellWithLevel> get(MaidSpellAction action) {
            return byAction.getOrDefault(action, List.of());
        }
    }

    /**
     * 根据配置和召唤等级随机生成装备
     *
     * @param loadout         装备配置
     * @param summonLevel     召唤等级
     * @param random          随机数生成器
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
        Map<MaidSpellAction, List<SpellWithLevel>> byAction = new EnumMap<>(MaidSpellAction.class);
        for (MaidSpellAction action : MaidSpellAction.ALL) {
            config.pool(action).ifPresent(pool -> {
                List<SpellWithLevel> rolled = new ArrayList<>();
                addSpellsFromPool(pool, spellLevelCap, rolled, random);
                if (!rolled.isEmpty()) {
                    byAction.put(action, rolled);
                }
            });
        }
        return new CategorizedSpells(byAction);
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

        // 配装分层上限与全局配置上限在这里合成一个，入池筛选与定级都按它来，两处不会各算各的
        int levelCap = Math.min(spellLevelCap, Config.getSummonedMaidMaxSpellLevel());

        // 展开所有法术条目
        List<WeightedSpell> expandedSpells = new ArrayList<>();
        for (LoadoutEntry entry : pool.entries()) {
            expandSpellEntry(entry, expandedSpells, levelCap);
        }

        if (expandedSpells.isEmpty()) {
            return;
        }

        // 随机选择指定次数
        for (int i = 0; i < rolls; i++) {
            AbstractSpell spell = selectWeightedSpell(expandedSpells, random);
            if (spell != null && spell != SpellRegistry.none()) {
                spells.add(new SpellWithLevel(spell, resolveSpellLevel(spell, levelCap)));
            }
        }
    }

    /**
     * 计算法术实际等级：分层上限、法术自身最大等级、全局等级上限三者取小，再逐级下探到满足稀有度上限。
     * <p>
     * Iron's 的 {@code getRarity} 在 {@code level >= maxLevel} 时硬返回 LEGENDARY，所以光压等级压不住
     * 稀有度，需逐级下探；稀有度随等级单调不降，故第一个合格等级即最高合格等级。
     *
     * @param spell    法术
     * @param levelCap 配装分层上限与全局配置上限取小后的等级上限
     * @return 实际使用的法术等级（不低于法术自身的 {@code getMinLevel}）
     */
    private static int resolveSpellLevel(AbstractSpell spell, int levelCap) {
        int floor = Math.max(1, spell.getMinLevel());
        // 各路上限压到下限以下时以下限为准：低于 getMinLevel 的等级法术自己没定义，
        // 按等级差算时长/强度的法术会算出负数
        int level = Math.max(floor, Math.min(levelCap, spell.getMaxLevel()));
        while (level > floor && !withinRarityCap(spell, level)) {
            level--;
        }
        return level;
    }

    /**
     * 该法术在指定等级下是否不超过配置的稀有度上限。
     */
    private static boolean withinRarityCap(AbstractSpell spell, int level) {
        return spell.getRarity(level).getValue() <= Config.getSummonedMaidMaxSpellRarity().getValue();
    }

    /**
     * 法术能否进入召唤女仆的法术池：学派准入 + 稀有度上限 + 等级上限，三关都过才收。
     * <p>
     * 剔除放在展开池子时而不是抽中之后，否则会连带削掉女仆实际拿到的法术数量。
     */
    private static boolean isEligible(AbstractSpell spell, int levelCap) {
        return isSchoolAllowed(spell) && isRarityEligible(spell) && isLevelEligible(spell, levelCap);
    }

    /**
     * 学派准入：默认不给召唤女仆发习得制学派（Iron's 邪术）的法术。
     * <p>
     * 两条判据缺一不可：{@link AbstractSpell#requiresLearning()} 可被单个法术覆写（现装附属里有邪术
     * 法术把它关掉了），而学派级的 {@code SchoolType.requiresLearning} 包私有读不到，故补一条学派 id 比对。
     */
    private static boolean isSchoolAllowed(AbstractSpell spell) {
        if (Config.isSummonedMaidAllowEldritch()) {
            return true;
        }
        return !spell.requiresLearning()
            && !SchoolRegistry.ELDRITCH_RESOURCE.equals(spell.getSchoolType().getId());
    }

    /**
     * 法术是否有任何等级满足稀有度上限；稀有度随等级单调不降，故只看最低等级。
     */
    private static boolean isRarityEligible(AbstractSpell spell) {
        return withinRarityCap(spell, Math.max(1, spell.getMinLevel()));
    }

    /**
     * 法术的最低等级是否还在等级上限之内。
     * <p>
     * {@link #resolveSpellLevel} 会把等级兜回 {@code getMinLevel}——低于下限的等级法术自身没定义——
     * 所以下限本就高于上限的法术只能在入池时剔除，否则等级上限对它形同虚设。
     */
    private static boolean isLevelEligible(AbstractSpell spell, int levelCap) {
        return Math.max(1, spell.getMinLevel()) <= levelCap;
    }

    private static void expandSpellEntry(LoadoutEntry entry, List<WeightedSpell> result, int levelCap) {
        if (entry instanceof SpellEntry(ResourceLocation name, int weight)) {
            SpellRegistry.REGISTRY.getOptional(name)
                .filter(spell -> isEligible(spell, levelCap))
                .ifPresent(spell -> result.add(new WeightedSingleSpell(spell, weight)));
        } else if (entry instanceof SpellTagEntry(ResourceLocation name, int weight, boolean expand)) {
            TagKey<AbstractSpell> tagKey = TagKey.create(SpellRegistry.SPELL_REGISTRY_KEY, name);
            if (expand) {
                // 展开标签
                SpellRegistry.REGISTRY.getTag(tagKey).ifPresent(tag -> {
                    tag.forEach(holder -> {
                        if (isEligible(holder.value(), levelCap)) {
                            result.add(new WeightedSingleSpell(holder.value(), 1));
                        }
                    });
                });
            } else {
                // 整个标签作为一个条目
                List<AbstractSpell> spells = new ArrayList<>();
                SpellRegistry.REGISTRY.getTag(tagKey).ifPresent(tag -> {
                    tag.forEach(holder -> {
                        if (isEligible(holder.value(), levelCap)) {
                            spells.add(holder.value());
                        }
                    });
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
