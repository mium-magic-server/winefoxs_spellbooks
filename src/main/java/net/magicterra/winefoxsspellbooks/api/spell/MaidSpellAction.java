package net.magicterra.winefoxsspellbooks.api.spell;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * 女仆施法的行为分类。法术标签、附加数据字段、服务端配置项、配装法术池、AI 权重表都由这里派生。
 * <p>
 * 三个名字分别对应三套已落盘格式的字段名：
 * <ul>
 *   <li>{@link #poolName()} —— 配装数据包 {@code spells} 块里的字段（{@code support_other}）</li>
 *   <li>{@link #catalogName()} —— 法术标签 path 与实体附加数据字段（{@code support_effect_spells}）</li>
 *   <li>{@link #configName()} —— 服务端配置键（{@code extraSupportEffectSpells}）</li>
 * </ul>
 * <p>
 * 构造器最后一项是写进 toml 的英文说明，与 {@code en_us.json} 里
 * {@code config.winefoxs_spellbooks.spell_compat.<translationSuffix>} 的值是同一句话的两份拷贝，
 * 没有任何机制校验二者一致。改分类名时两处（含 {@code zh_cn.json}）必须一起改。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-03 01:39
 */
public enum MaidSpellAction implements SpellCategory {
    /**
     * 攻击法术：法术攻击模式下对敌人施放。
     */
    ATTACK("attack", "attack_spells", "extraAttackSpells",
        "extra_attack_spells", "Extra attack spell ids"),
    /**
     * 自我强化法术：所有模式下对自身施放。
     */
    DEFENSE("defense", "defense_spells", "extraDefenseSpells",
        "extra_defense_spells", "Extra defense spell ids"),
    /**
     * 移动法术：法术攻击模式下用于位移到目标位置。
     */
    MOVEMENT("movement", "movement_spells", "extraMovementSpells",
        "extra_movement_spells", "Extra movement spell ids"),
    /**
     * 自我治疗法术：所有模式下对自身施放。
     */
    SUPPORT("support", "support_spells", "extraSupportSpells",
        "extra_support_spells", "Extra self-heal spell ids"),
    /**
     * 增益队友法术：法术支援模式下对队友施放。
     */
    POSITIVE("positive_effect", "positive_effect_spells", "extraPositiveEffectSpells",
        "extra_positive_effect_spells", "Extra ally buff spell ids"),
    /**
     * 负面效果法术：所有模式下对敌人施放。
     */
    NEGATIVE("negative_effect", "negative_effect_spells", "extraNegativeEffectSpells",
        "extra_negative_effect_spells", "Extra negative effect spell ids"),
    /**
     * 治疗队友法术：法术支援模式下对队友施放。
     */
    SUPPORT_OTHER("support_other", "support_effect_spells", "extraSupportEffectSpells",
        "extra_support_effect_spells", "Extra ally heal spell ids");

    /**
     * 固定顺序的只读视图。热路径上遍历用它，避免 {@code values()} 每次拷贝数组。
     */
    public static final MaidSpellAction[] ALL = values();

    private final String poolName;

    private final String catalogName;

    private final String configName;

    private final String translationSuffix;

    private final String comment;

    private final TagKey<AbstractSpell> tag;

    MaidSpellAction(String poolName, String catalogName, String configName,
                    String translationSuffix, String comment) {
        this.poolName = poolName;
        this.catalogName = catalogName;
        this.configName = configName;
        this.translationSuffix = translationSuffix;
        this.comment = comment;
        this.tag = TagKey.create(SpellRegistry.SPELL_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, catalogName));
    }

    /**
     * 配装数据包 {@code spells} 块里的字段名。
     */
    public String poolName() {
        return poolName;
    }

    @Override
    public String catalogName() {
        return catalogName;
    }

    @Override
    public String configName() {
        return configName;
    }

    @Override
    public String translationSuffix() {
        return translationSuffix;
    }

    @Override
    public String comment() {
        return comment;
    }

    @Override
    public TagKey<AbstractSpell> tag() {
        return tag;
    }
}
