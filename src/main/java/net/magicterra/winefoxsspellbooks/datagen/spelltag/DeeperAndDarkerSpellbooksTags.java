package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.rev.darkermagic.registeries.DDISSSpellRegistery;

/**
 * Deeper and Darker Spellbooks 的法术标签分类。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class DeeperAndDarkerSpellbooksTags {

    private DeeperAndDarkerSpellbooksTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // ========== 召唤 ==========
        // 四个召唤物均通过 SummonManager.initSummon 绑定施法者，目标选择沿用跟随主人的通用目标逻辑
        ctx.attack().addOptional(DDISSSpellRegistery.SUMMON_SCULK_CENTIPEDE_SPELL.get().getSpellResource());
        ctx.summon().addOptional(DDISSSpellRegistery.SUMMON_SCULK_CENTIPEDE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(DDISSSpellRegistery.SUMMON_SCULK_SNAPPER_SPELL.get().getSpellResource());
        ctx.summon().addOptional(DDISSSpellRegistery.SUMMON_SCULK_SNAPPER_SPELL.get().getSpellResource());
        ctx.attack().addOptional(DDISSSpellRegistery.SUMMON_SHATTERED_SPELL.get().getSpellResource());
        ctx.summon().addOptional(DDISSSpellRegistery.SUMMON_SHATTERED_SPELL.get().getSpellResource());
        ctx.attack().addOptional(DDISSSpellRegistery.SUMMON_WARDEN_SPELL.get().getSpellResource());
        ctx.summon().addOptional(DDISSSpellRegistery.SUMMON_WARDEN_SPELL.get().getSpellResource());
    }
}
