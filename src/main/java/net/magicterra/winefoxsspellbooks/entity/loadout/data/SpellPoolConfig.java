package net.magicterra.winefoxsspellbooks.entity.loadout.data;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.SpellPool;

/**
 * 法术池配置：每个 {@link MaidSpellAction} 行为类别一个池，都可缺省。
 * <p>
 * 数据包里的字段名取自 {@link MaidSpellAction#poolName()}（{@code attack}、{@code positive_effect}、
 * {@code support_other} …）。
 *
 * @param pools 类别 → 法术池；缺省的类别不出现在 map 里
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record SpellPoolConfig(Map<MaidSpellAction, SpellPool> pools) {

    /**
     * 空配置
     */
    public static final SpellPoolConfig EMPTY = new SpellPoolConfig(Map.of());

    public SpellPoolConfig {
        // 防御性拷贝进 EnumMap：调用方可能传可变 map，而本记录会被数据包管理器长期持有
        pools = pools.isEmpty() ? Map.of() : new EnumMap<>(pools);
    }

    /**
     * 创建只有攻击法术池的配置
     *
     * @param attack 攻击法术池
     * @return SpellPoolConfig
     */
    public static SpellPoolConfig attackOnly(SpellPool attack) {
        return new SpellPoolConfig(Map.of(MaidSpellAction.ATTACK, attack));
    }

    /**
     * 取某一类别的法术池。
     *
     * @param action 行为类别
     * @return 该类别的池，未配置时为 empty
     */
    public Optional<SpellPool> pool(MaidSpellAction action) {
        return Optional.ofNullable(pools.get(action));
    }

    /**
     * 检查是否有任何法术池配置
     *
     * @return 如果有任何法术池则返回 true
     */
    public boolean hasAnyPool() {
        return !pools.isEmpty();
    }

    /**
     * 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<MaidSpellAction, SpellPool> pools = new EnumMap<>(MaidSpellAction.class);

        /**
         * 设置某一类别的法术池。传 {@code null} 视为不设置。
         *
         * @param action 行为类别
         * @param pool   法术池
         */
        public Builder pool(MaidSpellAction action, SpellPool pool) {
            if (pool != null) {
                pools.put(action, pool);
            }
            return this;
        }

        public Builder attack(SpellPool pool) {
            return pool(MaidSpellAction.ATTACK, pool);
        }

        public Builder defense(SpellPool pool) {
            return pool(MaidSpellAction.DEFENSE, pool);
        }

        public Builder movement(SpellPool pool) {
            return pool(MaidSpellAction.MOVEMENT, pool);
        }

        public Builder support(SpellPool pool) {
            return pool(MaidSpellAction.SUPPORT, pool);
        }

        public Builder positiveEffect(SpellPool pool) {
            return pool(MaidSpellAction.POSITIVE, pool);
        }

        public Builder negativeEffect(SpellPool pool) {
            return pool(MaidSpellAction.NEGATIVE, pool);
        }

        public Builder supportOther(SpellPool pool) {
            return pool(MaidSpellAction.SUPPORT_OTHER, pool);
        }

        public SpellPoolConfig build() {
            return new SpellPoolConfig(pools);
        }
    }
}
