package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.grandsigil.registry.GsSpells;

/**
 * Apex Arcana（grandsigil）：每个学派一个终极法术，几乎都是大范围领域或打击，敌我判定只依赖 isAlliedTo，由修复后的 EntityAllyMixin 保证不波及主人。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class ApexArcanaTags {

    private ApexArcanaTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：COLOSSEUM 每次施放都会扣除施法者 20% 最大生命值（先扣吸收护盾，生命值最低留 1），仅创造模式玩家免除，对女仆属于自伤。
        // audit:ignore COLOSSEUM

        // ========== 终极法术（敌我判定依赖已修复的 EntityAllyMixin） ==========
        // 以下 16 个法术的实体只用 target.isAlliedTo(caster) 单向判定敌我（另排除自己与同主人的驯服动物）。
        // 该判定能否放过主人，取决于 EntityAllyMixin 对「主人 → 女仆」方向的修复：修复前主人会被当作敌人结算，
        // 修复后主人、姐妹女仆及其召唤物均视为盟友，故恢复为攻击法术。
        // 判定半径与伤害均极大（终极法术级），若 EntityAllyMixin 回退，主人会被秒杀，须同步撤回本组分类。
        // 以女仆自身或准星落点为中心的 25 格以上大范围领域或打击
        ctx.attack().addOptional(GsSpells.CRIMSON_THREADS.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.FULL_BLOOM.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.HEAVEN_GATE.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.ICE_AGE.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.PHOENIX.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.SKYBREAK.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.STILLED_WORLD.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.SUNFALL.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.THE_GAZE.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.THOUSAND_BLADES.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.THUNDER_DRAGON.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.VOIDGRIP.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.WORLD_TREE.get().getSpellResource());
        // 落点附近 10 至 18 格的集中打击，范围较小，但落点离战斗中心很近，主人常在其中，伤害同样极高
        ctx.attack().addOptional(GsSpells.BLACK_COFFIN.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.DIMENSION_CUT.get().getSpellResource());
        ctx.attack().addOptional(GsSpells.RECKONING.get().getSpellResource());

        // ========== Evocation ==========
        // GRAND_SIGIL: 必须指向一个生物（施法前检查射线命中），七秒仪式后只对该单体落下光柱，其余生物不受伤；
        // 准星射线用 !found.isAlliedTo(caster) 排除盟友，同样依赖修复后的 EntityAllyMixin，主人不会被选作目标
        ctx.attack().addOptional(GsSpells.GRAND_SIGIL.get().getSpellResource());
    }
}
