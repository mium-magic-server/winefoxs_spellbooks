package net.magicterra.winefoxsspellbooks.entity;

import com.github.tartaricacid.touhoulittlemaid.api.animation.IMagicCastingState;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import net.magicterra.winefoxsspellbooks.entity.adapter.MagicMaidAdapter;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * 把 {@link IMagicEntity} 与 {@link MaidMagicEntity} 的全部方法转发到 {@link MagicMaidAdapter}。
 * <p>
 * 两种会施法的女仆（{@code MaidEntityMixin} 与 {@code SummonedEntityMaid}）各持一个适配器，
 * 转发样板收在这里。实现方只需提供 {@link #winefoxsSpellbooks$getMagicMaidAdapter()}；
 * 需要不同行为时在实现类里覆写同名方法即可。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public interface MagicMaidDelegate extends IMagicEntity, MaidMagicEntity {

    /**
     * 本实体的施法适配器。<b>这是实现方唯一必须提供的方法。</b>
     */
    @Override
    MagicMaidAdapter winefoxsSpellbooks$getMagicMaidAdapter();

    // ==================== IMagicEntity ====================

    @Override
    default MagicData getMagicData() {
        return winefoxsSpellbooks$getMagicMaidAdapter().getMagicData();
    }

    @Override
    default void setSyncedSpellData(SyncedSpellData syncedSpellData) {
        winefoxsSpellbooks$getMagicMaidAdapter().setSyncedSpellData(syncedSpellData);
    }

    @Override
    default boolean isCasting() {
        return winefoxsSpellbooks$getMagicMaidAdapter().isCasting();
    }

    @Override
    default void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        winefoxsSpellbooks$getMagicMaidAdapter().initiateCastSpell(spell, spellLevel);
    }

    @Override
    default void cancelCast() {
        winefoxsSpellbooks$getMagicMaidAdapter().cancelCast();
    }

    @Override
    default void castComplete() {
        winefoxsSpellbooks$getMagicMaidAdapter().castComplete();
    }

    @Override
    default void notifyDangerousProjectile(Projectile projectile) {
        winefoxsSpellbooks$getMagicMaidAdapter().notifyDangerousProjectile(projectile);
    }

    @Override
    default boolean setTeleportLocationBehindTarget(int distance) {
        return winefoxsSpellbooks$getMagicMaidAdapter().setTeleportLocationBehindTarget(distance);
    }

    @Override
    default void setBurningDashDirectionData() {
        winefoxsSpellbooks$getMagicMaidAdapter().setBurningDashDirectionData();
    }

    @Override
    default boolean isDrinkingPotion() {
        return winefoxsSpellbooks$getMagicMaidAdapter().isDrinkingPotion();
    }

    @Override
    default void startDrinkingPotion() {
        winefoxsSpellbooks$getMagicMaidAdapter().startDrinkingPotion();
    }

    @Override
    default boolean getHasUsedSingleAttack() {
        return winefoxsSpellbooks$getMagicMaidAdapter().getHasUsedSingleAttack();
    }

    @Override
    default void setHasUsedSingleAttack(boolean hasUsedSingleAttack) {
        winefoxsSpellbooks$getMagicMaidAdapter().setHasUsedSingleAttack(hasUsedSingleAttack);
    }

    // ==================== MaidMagicEntity ====================

    @Override
    default IMagicCastingState winefoxSpellbooks$getMagicCastingState() {
        return winefoxsSpellbooks$getMagicMaidAdapter().winefoxSpellbooks$getMagicCastingState();
    }

    @Override
    default boolean winefoxsSpellbooks$getCancelCastAnimation() {
        return winefoxsSpellbooks$getMagicMaidAdapter().winefoxsSpellbooks$getCancelCastAnimation();
    }

    @Override
    default void winefoxsSpellbooks$setCancelCastAnimation(boolean cancelCastAnimation) {
        winefoxsSpellbooks$getMagicMaidAdapter().winefoxsSpellbooks$setCancelCastAnimation(cancelCastAnimation);
    }

    @Override
    default float winefoxsSpellbooks$getMana() {
        return winefoxsSpellbooks$getMagicMaidAdapter().winefoxsSpellbooks$getMana();
    }

    @Override
    default void winefoxsSpellbooks$setMana(float mana) {
        winefoxsSpellbooks$getMagicMaidAdapter().winefoxsSpellbooks$setMana(mana);
    }

    @Override
    default int winefoxsSpellbooks$getManaCost(AbstractSpell spell, int level) {
        return winefoxsSpellbooks$getMagicMaidAdapter().winefoxsSpellbooks$getManaCost(spell, level);
    }

    @Override
    default MaidSpellDataHolder winefoxsSpellbooks$getSpellDataHolder() {
        return winefoxsSpellbooks$getMagicMaidAdapter().winefoxsSpellbooks$getSpellDataHolder();
    }
}
