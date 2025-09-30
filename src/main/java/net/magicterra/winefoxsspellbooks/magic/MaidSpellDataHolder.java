package net.magicterra.winefoxsspellbooks.magic;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 可用法术和等级包装
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-09-11 00:41
 */
public class MaidSpellDataHolder {
    private final ArrayList<SpellData> attackSpells = new ArrayList<>();
    private final ArrayList<SpellData> defenseSpells = new ArrayList<>();
    private final ArrayList<SpellData> movementSpells = new ArrayList<>();
    private final ArrayList<SpellData> supportSpells = new ArrayList<>();
    private final ArrayList<SpellData> positiveEffectSpells = new ArrayList<>();
    private final ArrayList<SpellData> negativeEffectSpells = new ArrayList<>();
    private final ArrayList<SpellData> supportEffectSpells = new ArrayList<>();

    public ArrayList<SpellData> getAttackSpells() {
        return attackSpells;
    }

    public ArrayList<SpellData> getDefenseSpells() {
        return defenseSpells;
    }

    public ArrayList<SpellData> getMovementSpells() {
        return movementSpells;
    }

    public ArrayList<SpellData> getSupportSpells() {
        return supportSpells;
    }

    public ArrayList<SpellData> getPositiveEffectSpells() {
        return positiveEffectSpells;
    }

    public ArrayList<SpellData> getNegativeEffectSpells() {
        return negativeEffectSpells;
    }

    public ArrayList<SpellData> getSupportEffectSpells() {
        return supportEffectSpells;
    }

    public boolean hasAnySpells() {
        return attackSpells.size() +
            defenseSpells.size() +
            movementSpells.size() +
            supportSpells.size() +
            positiveEffectSpells.size() +
            negativeEffectSpells.size() +
            supportEffectSpells.size() > 0;
    }

    public void updateAttackSpells(Collection<SpellData> spells) {
        attackSpells.clear();
        attackSpells.addAll(spells);
    }

    public void updateDefenseSpells(Collection<SpellData> spells) {
        defenseSpells.clear();
        defenseSpells.addAll(spells);
    }

    public void updateMovementSpells(Collection<SpellData> spells) {
        movementSpells.clear();
        movementSpells.addAll(spells);
    }

    public void updateSupportSpells(Collection<SpellData> spells) {
        supportSpells.clear();
        supportSpells.addAll(spells);
    }

    public void updatePositiveEffectSpells(Collection<SpellData> spells) {
        positiveEffectSpells.clear();
        positiveEffectSpells.addAll(spells);
    }

    public void updateNegativeEffectSpells(Collection<SpellData> spells) {
        negativeEffectSpells.clear();
        negativeEffectSpells.addAll(spells);
    }

    public void updateSupportEffectSpells(Collection<SpellData> spells) {
        supportEffectSpells.clear();
        supportEffectSpells.addAll(spells);
    }

    public void clear() {
        attackSpells.clear();
        defenseSpells.clear();
        movementSpells.clear();
        supportSpells.clear();
        positiveEffectSpells.clear();
        negativeEffectSpells.clear();
        supportEffectSpells.clear();
    }
}
