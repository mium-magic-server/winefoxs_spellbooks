package net.magicterra.winefoxsspellbooks.entity;

import com.github.tartaricacid.touhoulittlemaid.api.animation.IMagicCastingState;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
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
    IMagicCastingState winefoxSpellbooks$getMagicCastingState();

    boolean winefoxsSpellbooks$getCancelCastAnimation();

    void winefoxsSpellbooks$setCancelCastAnimation(boolean cancelCastAnimation);

    float winefoxsSpellbooks$getMana();

    void winefoxsSpellbooks$setMana(float mana);

    int winefoxsSpellbooks$getManaCost(AbstractSpell spell, int level);

    MaidSpellDataHolder winefoxsSpellbooks$getSpellDataHolder();
}
