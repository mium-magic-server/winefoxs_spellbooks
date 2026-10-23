package net.magicterra.winefoxsspellbooks.api.spell;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.tags.TagKey;

/**
 * 「一类女仆法术」的共同契约：有标签、有配置项、有名字。
 * <p>
 * 实现分两种，语义不同，别混用：
 * <ul>
 *   <li>{@link MaidSpellAction} —— <b>行为分类</b>。女仆每次选法术就是在这些类别里挑一个，
 *       彼此互斥，一个法术只会落进其中一类。</li>
 *   <li>{@link SpellFlag} —— <b>标记</b>。与行为分类正交，用来给法术打附加属性
 *       （是不是召唤术、要不要二段咏唱），一个法术可以既是攻击法术又带召唤标记。</li>
 * </ul>
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public sealed interface SpellCategory permits MaidSpellAction, SpellFlag {

    /**
     * 目录名：法术标签的 path、附加数据 Codec 的字段名、配置翻译键的后缀，三者共用这一个串。
     * <p>
     * <b>这是已落盘的序列化名，改动会破坏现存数据包与存档。</b>
     * 例如 {@link MaidSpellAction#SUPPORT_OTHER} 是 {@code "support_effect_spells"} 而不是望文生义的 {@code "support_other_spells"}。
     */
    String catalogName();

    /**
     * 服务端配置里「额外法术」列表的键名（camelCase，如 {@code extraAttackSpells}）。
     */
    String configName();

    /**
     * 配置翻译键在 {@code config.winefoxs_spellbooks.spell_compat.} 之下的后缀，不能由
     * {@link #catalogName()} 推导（见 {@link SpellFlag#SHOULD_RECAST}）。
     */
    String translationSuffix();

    /**
     * 写进 toml 的英文注释。
     */
    String comment();

    /**
     * 该类别的法术标签。
     */
    TagKey<AbstractSpell> tag();

    /**
     * 服务端配置里「移除法术」列表的键名，由 {@link #catalogName()} 转驼峰拼出，如 {@code removedAttackSpells}。
     * <p>
     * 已落盘的配置键，改 {@code catalogName} 时这里跟着变，存量 toml 里的对应项会失效。
     */
    default String removedConfigName() {
        StringBuilder sb = new StringBuilder("removed");
        for (String part : catalogName().split("_")) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part, 1, part.length());
        }
        return sb.toString();
    }

    /**
     * 「移除法术」列表的翻译键后缀，挂在 {@code config.winefoxs_spellbooks.spell_compat.} 之下。
     */
    default String removedTranslationSuffix() {
        return "removed_" + catalogName();
    }

    /**
     * 「移除法术」列表写进 toml 的英文注释。
     */
    default String removedComment() {
        return "Spell ids removed from '" + catalogName() + "', including ones tagged by datapacks. Applied after the extra list.";
    }
}
