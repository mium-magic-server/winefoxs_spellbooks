package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider.IntrinsicTagAppender;

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
