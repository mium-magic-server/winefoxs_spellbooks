package net.magicterra.winefoxsspellbooks.entity;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;

/**
 * 魔法女仆提供的内部参数
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-23 00:30
 */
public interface MaidMagicEntity {
    SpellData winefoxsSpellbooks$getCastingSpell();

    AbstractSpell winefoxsSpellbooks$getInstantCastSpellType();

    void winefoxsSpellbooks$setInstantCastSpellType(AbstractSpell instantCastSpellType);

    boolean winefoxsSpellbooks$getCancelCastAnimation();

    void winefoxsSpellbooks$setCancelCastAnimation(boolean cancelCastAnimation);
}
