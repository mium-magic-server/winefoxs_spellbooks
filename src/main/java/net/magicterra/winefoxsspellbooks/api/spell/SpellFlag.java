package net.magicterra.winefoxsspellbooks.api.spell;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * 法术标记：与 {@link MaidSpellAction} 行为分类<b>正交</b>的附加属性。
 * <p>
 * 一个法术只属于一个 {@code MaidSpellAction}，但可以同时带任意多个 {@code SpellFlag}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public enum SpellFlag implements SpellCategory {
    /**
     * 召唤法术：需要标记以避免女仆把二段咏唱用成「收回召唤物」。
     */
    SUMMON("summon_spells", "extraSummonSpells",
        "extra_summon_spells", "Extra summon spell ids"),
    /**
     * 需要二重咏唱才能生效的法术，例如炽焰追踪弹幕、火墙术。
     * <p>
     * 注意它的翻译键是 {@code maid_should_recast_spells} 而非 {@code extra_maid_should_recast_spells}—— 与其余八项的命名规律不一致，是历史遗留，已进语言文件，不能顺手「修正」。
     */
    SHOULD_RECAST("maid_should_recast_spells", "maidShouldRecastSpells",
        "maid_should_recast_spells", "Maid should recast spell ids");

    /**
     * 固定顺序的只读视图，避免 {@code values()} 每次拷贝数组。
     */
    public static final SpellFlag[] ALL = values();

    private final String catalogName;

    private final String configName;

    private final String translationSuffix;

    private final String comment;

    private final TagKey<AbstractSpell> tag;

    SpellFlag(String catalogName, String configName, String translationSuffix, String comment) {
        this.catalogName = catalogName;
        this.configName = configName;
        this.translationSuffix = translationSuffix;
        this.comment = comment;
        this.tag = TagKey.create(SpellRegistry.SPELL_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, catalogName));
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
