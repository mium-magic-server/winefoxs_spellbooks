package net.magicterra.winefoxsspellbooks.entity.loadout.data;

import java.util.Optional;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.ItemPool;

/**
 * 护甲配置，包含四个护甲槽位
 *
 * @param helmet     头盔槽位配置
 * @param chestplate 胸甲槽位配置
 * @param leggings   护腿槽位配置
 * @param boots      靴子槽位配置
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record ArmorConfig(
    Optional<ArmorSlotConfig> helmet,
    Optional<ArmorSlotConfig> chestplate,
    Optional<ArmorSlotConfig> leggings,
    Optional<ArmorSlotConfig> boots
) {
    /**
     * 空配置
     */
    public static final ArmorConfig EMPTY = new ArmorConfig(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );

    /**
     * 检查是否有任何护甲槽位配置
     *
     * @return 如果有任何槽位配置则返回 true
     */
    public boolean hasAnySlot() {
        return helmet.isPresent() || chestplate.isPresent() || leggings.isPresent() || boots.isPresent();
    }

    /**
     * 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ArmorSlotConfig helmet;
        private ArmorSlotConfig chestplate;
        private ArmorSlotConfig leggings;
        private ArmorSlotConfig boots;

        public Builder helmet(float chance, ItemPool pool) {
            this.helmet = ArmorSlotConfig.of(chance, pool);
            return this;
        }

        public Builder helmet(ArmorSlotConfig config) {
            this.helmet = config;
            return this;
        }

        public Builder chestplate(float chance, ItemPool pool) {
            this.chestplate = ArmorSlotConfig.of(chance, pool);
            return this;
        }

        public Builder chestplate(ArmorSlotConfig config) {
            this.chestplate = config;
            return this;
        }

        public Builder leggings(float chance, ItemPool pool) {
            this.leggings = ArmorSlotConfig.of(chance, pool);
            return this;
        }

        public Builder leggings(ArmorSlotConfig config) {
            this.leggings = config;
            return this;
        }

        public Builder boots(float chance, ItemPool pool) {
            this.boots = ArmorSlotConfig.of(chance, pool);
            return this;
        }

        public Builder boots(ArmorSlotConfig config) {
            this.boots = config;
            return this;
        }

        public ArmorConfig build() {
            return new ArmorConfig(
                Optional.ofNullable(helmet),
                Optional.ofNullable(chestplate),
                Optional.ofNullable(leggings),
                Optional.ofNullable(boots)
            );
        }
    }
}
