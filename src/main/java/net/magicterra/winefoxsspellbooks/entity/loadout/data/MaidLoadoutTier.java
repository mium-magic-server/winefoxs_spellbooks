package net.magicterra.winefoxsspellbooks.entity.loadout.data;

import java.util.Optional;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.ItemPool;

/**
 * 女仆装备层级配置，对应特定等级范围的装备配置
 *
 * @param minLevel      最小召唤等级
 * @param maxLevel      最大召唤等级
 * @param weapon        武器物品池（可选）
 * @param armor         护甲配置（可选）
 * @param spells        法术配置（可选）
 * @param spellLevelCap 法术等级上限，默认 1
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record MaidLoadoutTier(
    int minLevel,
    int maxLevel,
    Optional<ItemPool> weapon,
    Optional<ArmorConfig> armor,
    Optional<SpellPoolConfig> spells,
    int spellLevelCap
) {
    /**
     * 默认法术等级上限
     */
    public static final int DEFAULT_SPELL_LEVEL_CAP = 1;

    /**
     * 检查召唤等级是否在此层级范围内
     *
     * @param summonLevel 召唤等级
     * @return 如果在范围内返回 true
     */
    public boolean matches(int summonLevel) {
        return summonLevel >= minLevel && summonLevel <= maxLevel;
    }

    /**
     * 检查召唤等级是否满足最低要求
     *
     * @param summonLevel 召唤等级
     * @return 如果满足最低要求返回 true
     */
    public boolean meetsMinimum(int summonLevel) {
        return summonLevel >= minLevel;
    }

    /**
     * 构建器
     */
    public static Builder builder(int minLevel, int maxLevel) {
        return new Builder(minLevel, maxLevel);
    }

    public static class Builder {
        private final int minLevel;
        private final int maxLevel;
        private ItemPool weapon;
        private ArmorConfig armor;
        private SpellPoolConfig spells;
        private int spellLevelCap = DEFAULT_SPELL_LEVEL_CAP;

        public Builder(int minLevel, int maxLevel) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        public Builder weapon(ItemPool pool) {
            this.weapon = pool;
            return this;
        }

        public Builder armor(ArmorConfig config) {
            this.armor = config;
            return this;
        }

        public Builder spells(SpellPoolConfig config) {
            this.spells = config;
            return this;
        }

        public Builder spellLevelCap(int cap) {
            this.spellLevelCap = cap;
            return this;
        }

        public MaidLoadoutTier build() {
            return new MaidLoadoutTier(
                minLevel,
                maxLevel,
                Optional.ofNullable(weapon),
                Optional.ofNullable(armor),
                Optional.ofNullable(spells),
                spellLevelCap
            );
        }
    }
}
