package net.magicterra.winefoxsspellbooks.entity.loadout.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/**
 * 女仆装备配置，完整的装备配置方案
 *
 * @param id          配置 ID（由文件路径决定）
 * @param weight      选择权重，默认 100，0 表示永不选中
 * @param modelFilter 模型过滤器
 * @param broomMode   扫帚模式（always/never/default）
 * @param tiers       等级层级列表
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record MaidLoadout(
    ResourceLocation id,
    int weight,
    ModelFilter modelFilter,
    BroomMode broomMode,
    List<MaidLoadoutTier> tiers
) {
    /**
     * 默认权重
     */
    public static final int DEFAULT_WEIGHT = 100;

    /**
     * 根据召唤等级匹配层级
     * 规则 (FR-024):
     * 1. 精确匹配：min_level <= summon_level <= max_level
     * 2. 若无精确匹配：选 min_level <= summon_level 的最高层级
     * 3. 若 summon_level < 所有 min_level：选最小层级
     *
     * @param summonLevel 召唤等级
     * @return 匹配的层级
     */
    public Optional<MaidLoadoutTier> matchTier(int summonLevel) {
        if (tiers.isEmpty()) {
            return Optional.empty();
        }

        // 1. 尝试精确匹配
        Optional<MaidLoadoutTier> exactMatch = tiers.stream()
            .filter(tier -> tier.matches(summonLevel))
            .findFirst();
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        // 2. 选择 min_level <= summon_level 的最高层级
        Optional<MaidLoadoutTier> bestMatch = tiers.stream()
            .filter(tier -> tier.meetsMinimum(summonLevel))
            .max(Comparator.comparingInt(MaidLoadoutTier::minLevel));
        if (bestMatch.isPresent()) {
            return bestMatch;
        }

        // 3. 若 summon_level < 所有 min_level，选最小层级
        return tiers.stream()
            .min(Comparator.comparingInt(MaidLoadoutTier::minLevel));
    }

    /**
     * 检查配置是否可被选中（权重大于 0）
     *
     * @return 如果可被选中返回 true
     */
    public boolean isSelectable() {
        return weight > 0;
    }

    /**
     * 构建器
     */
    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static class Builder {
        private final ResourceLocation id;
        private int weight = DEFAULT_WEIGHT;
        private ModelFilter modelFilter = ModelFilter.ALL;
        private BroomMode broomMode = BroomMode.DEFAULT;
        private final List<MaidLoadoutTier> tiers = new ArrayList<>();

        public Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder modelFilter(ModelFilter filter) {
            this.modelFilter = filter;
            return this;
        }

        public Builder broomMode(BroomMode mode) {
            this.broomMode = mode;
            return this;
        }

        public Builder addTier(MaidLoadoutTier tier) {
            this.tiers.add(tier);
            return this;
        }

        public Builder addTiers(List<MaidLoadoutTier> tiers) {
            this.tiers.addAll(tiers);
            return this;
        }

        public MaidLoadout build() {
            return new MaidLoadout(id, weight, modelFilter, broomMode, List.copyOf(tiers));
        }
    }
}
