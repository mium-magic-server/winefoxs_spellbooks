package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.github.yimeng261.maidspell.compat.irons_spellbooks.registry.IronsSpellbooksCompatEffects;
import com.github.yimeng261.maidspell.compat.irons_spellbooks.registry.IronsSpellbooksCompatSpells;

/**
 * Touhou Little Maid: Spell（万法皆通）自带铁魔法法术的数据：施法距离与附带效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class TouhouLittleMaidSpellData {

    private TouhouLittleMaidSpellData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // ========== 近战攻击法术 ==========
        // STAR_SHADOW_STRIKE: 以施法者前方 1.9 格为中心，命中施法者前半球 6.25 格内的目标，留 1 格余量防止目标走位
        ctx.range().add(IronsSpellbooksCompatSpells.STAR_SHADOW_STRIKE.get().getSpellResource(), 5F);
        // MAGIC_SHOTGUN: 锥形散射半角 7.5°，15 格外锥面半径已超 2 格，大部分飞弹落空，限制在 6 格内
        ctx.range().add(IronsSpellbooksCompatSpells.MAGIC_SHOTGUN.get().getSpellResource(), 6F);
        // MODIFIED_TELEPORT: 落点是目标背侧、与目标等距的位置，射程越远传送越远，限制在 8 格内以免女仆一次突进到主人身边之外
        ctx.range().add(IronsSpellbooksCompatSpells.MODIFIED_TELEPORT.get().getSpellResource(), 8F);

        // ========== 附带效果 ==========
        // VOID_PHASE: 施法者获得 void_phase 效果，期间每次伤害追加虚空伤害
        ctx.effect().add(IronsSpellbooksCompatSpells.VOID_PHASE.get().getSpellResource(), IronsSpellbooksCompatEffects.VOID_PHASE.getId());
    }
}
