package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.magicterra.winefoxsspellbooks.magic.spell.CrescentBrandSpell;
import net.magicterra.winefoxsspellbooks.magic.spell.CrescentMantleSpell;
import net.magicterra.winefoxsspellbooks.magic.spell.DecantTheBindSpell;
import net.magicterra.winefoxsspellbooks.magic.spell.FoxshadeAmbushSpell;
import net.magicterra.winefoxsspellbooks.magic.spell.FoxshadeWispsSpell;
import net.magicterra.winefoxsspellbooks.magic.spell.TipsyMistSpell;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbSpells;

/**
 * 本 mod 自带法术的法术数据贡献。
 * <p>
 * 包括施法范围（range）与法术导致效果（effect）条目， 供 {@link net.magicterra.winefoxsspellbooks.magic.data.SpellDataManager} 在运行时加载使用。
 */
public final class WinefoxsSpellbooksData {

    private WinefoxsSpellbooksData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // ========== 残月烙印 ==========
        // 单目标射线施法，射程取法术自身的射线常量
        ctx.range().add(WsbSpells.CRESCENT_BRAND_SPELL.get().getSpellResource(), CrescentBrandSpell.CAST_RANGE);
        // 残月烙印法术会在目标身上施加 CRESCENT_BRAND 效果
        ctx.effect().add(
            WsbSpells.CRESCENT_BRAND_SPELL.get().getSpellResource(),
            WsbEffects.CRESCENT_BRAND.getId()
        );

        // ========== 酒杯狐影 ==========
        // 女仆 AI 施法距离：比弹体目标搜索半径小的专用常量，保证进入施法点后必能锁到目标
        ctx.range().add(WsbSpells.FOXSHADE_WISPS_SPELL.get().getSpellResource(), FoxshadeWispsSpell.MAID_CAST_RANGE);

        // ========== 狐影伏击 ==========
        // 自增益法术，女仆 AI 据此判断预挂伏击的交战距离
        ctx.range().add(WsbSpells.FOXSHADE_AMBUSH_SPELL.get().getSpellResource(), FoxshadeAmbushSpell.MAID_CAST_RANGE);
        // 狐影伏击法术会给施法者自身挂上 FOXSHADE_AMBUSH 标记
        ctx.effect().add(
            WsbSpells.FOXSHADE_AMBUSH_SPELL.get().getSpellResource(),
            WsbEffects.FOXSHADE_AMBUSH.getId()
        );

        // ========== 残月披风 ==========
        // 自身范围型法术，女仆 AI 据此判断近身施法距离（取披风的女仆搜索半径）
        ctx.range().add(WsbSpells.CRESCENT_MANTLE_SPELL.get().getSpellResource(),
            (float) CrescentMantleSpell.MAID_RADIUS);

        // ========== 醒酒之礼 ==========
        // 视线选取自家女仆的施法距离
        ctx.range().add(WsbSpells.DECANT_THE_BIND_SPELL.get().getSpellResource(), DecantTheBindSpell.TARGET_RANGE);

        // ========== 醉狐之雾 ==========
        // 落点选取射程
        ctx.range().add(WsbSpells.TIPSY_MIST_SPELL.get().getSpellResource(), TipsyMistSpell.CAST_RANGE);
        // 雾团命中会持续施加 TIPSY 效果
        ctx.effect().add(
            WsbSpells.TIPSY_MIST_SPELL.get().getSpellResource(),
            WsbEffects.TIPSY.getId()
        );

        // ========== 狐酿炸弹 ==========
        // 投掷弹出手射程 20 格
        ctx.range().add(WsbSpells.VINTNERS_BOMB_SPELL.get().getSpellResource(), 20F);
        // 落点酒池会持续施加 TIPSY 效果
        ctx.effect().add(
            WsbSpells.VINTNERS_BOMB_SPELL.get().getSpellResource(),
            WsbEffects.TIPSY.getId()
        );
    }
}
