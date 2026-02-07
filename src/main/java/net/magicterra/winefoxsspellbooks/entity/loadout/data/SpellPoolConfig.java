package net.magicterra.winefoxsspellbooks.entity.loadout.data;

import java.util.Optional;
import net.magicterra.winefoxsspellbooks.entity.loadout.pool.SpellPool;

/**
 * 法术池配置，包含七类法术池（对应 MaidSpellAction）
 *
 * @param attack         攻击法术池
 * @param defense        防御/自我强化法术池
 * @param movement       移动法术池
 * @param support        自我治疗法术池
 * @param positive       正面效果法术池（对队友施放）
 * @param negativeEffect 负面效果法术池（对敌人施放）
 * @param supportOther   治疗效果法术池（对队友施放）
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record SpellPoolConfig(
    Optional<SpellPool> attack,
    Optional<SpellPool> defense,
    Optional<SpellPool> movement,
    Optional<SpellPool> support,
    Optional<SpellPool> positive,
    Optional<SpellPool> negativeEffect,
    Optional<SpellPool> supportOther
) {
    /**
     * 空配置
     */
    public static final SpellPoolConfig EMPTY = new SpellPoolConfig(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );

    /**
     * 创建只有攻击法术池的配置
     *
     * @param attack 攻击法术池
     * @return SpellPoolConfig
     */
    public static SpellPoolConfig attackOnly(SpellPool attack) {
        return new SpellPoolConfig(
            Optional.of(attack),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
    }

    /**
     * 检查是否有任何法术池配置
     *
     * @return 如果有任何法术池则返回 true
     */
    public boolean hasAnyPool() {
        return attack.isPresent() || defense.isPresent() || movement.isPresent()
            || support.isPresent() || positive.isPresent()
            || negativeEffect.isPresent() || supportOther.isPresent();
    }

    /**
     * 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SpellPool attack;
        private SpellPool defense;
        private SpellPool movement;
        private SpellPool support;
        private SpellPool positive;
        private SpellPool negativeEffect;
        private SpellPool supportOther;

        public Builder attack(SpellPool pool) {
            this.attack = pool;
            return this;
        }

        public Builder defense(SpellPool pool) {
            this.defense = pool;
            return this;
        }

        public Builder movement(SpellPool pool) {
            this.movement = pool;
            return this;
        }

        public Builder support(SpellPool pool) {
            this.support = pool;
            return this;
        }

        public Builder positive(SpellPool pool) {
            this.positive = pool;
            return this;
        }

        public Builder negativeEffect(SpellPool pool) {
            this.negativeEffect = pool;
            return this;
        }

        public Builder supportOther(SpellPool pool) {
            this.supportOther = pool;
            return this;
        }

        public SpellPoolConfig build() {
            return new SpellPoolConfig(
                Optional.ofNullable(attack),
                Optional.ofNullable(defense),
                Optional.ofNullable(movement),
                Optional.ofNullable(support),
                Optional.ofNullable(positive),
                Optional.ofNullable(negativeEffect),
                Optional.ofNullable(supportOther)
            );
        }
    }
}
