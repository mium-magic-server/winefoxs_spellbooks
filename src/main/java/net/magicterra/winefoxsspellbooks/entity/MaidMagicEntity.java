package net.magicterra.winefoxsspellbooks.entity;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;

/**
 * 魔法女仆提供的内部参数
 * <br>
 * 这个接口是内部使用不保证稳定
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

    float winefoxsSpellbooks$getMana();

    int winefoxsSpellbooks$getManaCost(AbstractSpell spell, int level);

    MaidSpellDataHolder winefoxsSpellbooks$getSpellDataHolder();
}
