package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider.IntrinsicTagAppender;

/**
 * 各附属模组向法术标签贡献条目时的收集器。
 * <p>
 * 前七项是<b>互斥</b>的行为类别：一个法术只该进其中一个。
 * {@code MaidSpellCatalog.actionOf} 按 {@code MaidSpellAction.ALL} 的顺序取首个命中，
 * 多挂的那些既不生效也不报错，只会让标签文件与实际行为对不上。法术兼具多种效果时，
 * 挑女仆该拿它当什么用的那一类；「顺带施加的效果」记在 spelldata 的 caused effect 里。
 * <p>
 * {@code summon} 与 {@code maidShouldRecast} 是标记而非类别，可与任一行为类别并存。
 */
public record SpellTagContext(
    IntrinsicTagAppender<AbstractSpell> attack,
    IntrinsicTagAppender<AbstractSpell> defense,
    IntrinsicTagAppender<AbstractSpell> movement,
    IntrinsicTagAppender<AbstractSpell> support,
    IntrinsicTagAppender<AbstractSpell> positiveEffect,
    IntrinsicTagAppender<AbstractSpell> supportEffect,
    IntrinsicTagAppender<AbstractSpell> negativeEffect,
    IntrinsicTagAppender<AbstractSpell> summon,
    IntrinsicTagAppender<AbstractSpell> maidShouldRecast
) {}
