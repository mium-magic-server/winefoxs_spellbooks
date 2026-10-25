package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.github.yimeng261.maidspell.compat.irons_spellbooks.registry.IronsSpellbooksCompatSpells;

/**
 * Touhou Little Maid: Spell（万法皆通，{@code touhou_little_maid_spell}）自带的铁魔法法术标签分类。
 * <p>
 * 这批法术本是为其星辰魔女 Boss（{@code MagicalWinefoxBossEntity}）设计的，Boss 经自身的施法逻辑直接施放，不经过本模组的标签；
 * 但其中多数能由星辰魔女 / 星界商人出售的卷轴转入法术书（{@code void_phase}、{@code three_enclosures}、{@code palm_black_hole} 无商店来源，
 * 仍可经指令或创造模式获得），女仆装备后走本模组的选术，因此仍需分类。
 * 伤害与控制类法术都带 {@code MaidSpellAllyResolver} 友军判定，不会误伤主人；{@code SPELLBREAKING_ECHO} 原本例外，已由本模组 mixin 修补。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class TouhouLittleMaidSpellTags {

    private TouhouLittleMaidSpellTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // PALM_BLACK_HOLE: checkPreCastConditions 要求施法者是 MagicalWinefoxBossEntity 且处于二阶段战斗中，女仆恒定无法施放
        // audit:ignore PALM_BLACK_HOLE

        // ========== 末影学派：攻击 ==========
        // MODIFIED_STARFALL: 星坠的改版，施放后在施法者身边 8 秒内每 4 tick 落 2 颗彗星并追踪 20 格内敌人，有友军判定
        ctx.attack().addOptional(IronsSpellbooksCompatSpells.MODIFIED_STARFALL.get().getSpellResource());
        // MAGIC_SHOTGUN: 向前方 ±7.5° 锥形散射 4~8 枚魔法飞弹，每发无敌帧为 0；散布随距离变大，需近距离使用
        ctx.attack().addOptional(IronsSpellbooksCompatSpells.MAGIC_SHOTGUN.get().getSpellResource());
        // SWORD_PRISON: 对射线选中的敌人落下剑阵，剑阵接触伤害带友军判定
        ctx.attack().addOptional(IronsSpellbooksCompatSpells.SWORD_PRISON.get().getSpellResource());
        // MODIFIED_TELEPORT: 传送到目标背侧（与目标等距的对面）并发射环绕追踪飞弹；伤害来自飞弹，故按攻击归类（同 SHADOW_SLASH）
        ctx.attack().addOptional(IronsSpellbooksCompatSpells.MODIFIED_TELEPORT.get().getSpellResource());
        // STAR_SHADOW_STRIKE: 前方半径 6.25 格的近战斩击，伤害为法术强度加武器伤害
        ctx.attack().addOptional(IronsSpellbooksCompatSpells.STAR_SHADOW_STRIKE.get().getSpellResource());
        // TRIPLE_STAR_ARROW: 间隔 4 tick 连射 3 支魔法箭
        ctx.attack().addOptional(IronsSpellbooksCompatSpells.TRIPLE_STAR_ARROW.get().getSpellResource());
        // THREE_ENCLOSURES: 以目标（女仆则取其攻击目标）为中心生成收缩的三角结界，贴壁敌人受伤并被减速
        ctx.attack().addOptional(IronsSpellbooksCompatSpells.THREE_ENCLOSURES.get().getSpellResource());
        // SEVEN_STAR_GUIDANCE: 在目标处布七星阵，逐星打击后终结一击，并为施法者回少量生命；无目标数据时自行就近索敌
        ctx.attack().addOptional(IronsSpellbooksCompatSpells.SEVEN_STAR_GUIDANCE.get().getSpellResource());

        // ========== 末影学派：自我强化 ==========
        // VOID_PHASE: 给施法者挂 void_phase 效果，持续期间每次造成伤害都追加一次虚空伤害
        ctx.defense().addOptional(IronsSpellbooksCompatSpells.VOID_PHASE.get().getSpellResource());
        // COMPANION_BLACK_HOLE: 悬在施法者头顶并跟随的黑洞，吞噬非友方弹射物与法术实体，同时伤害附近敌人；以自身为中心，不需要瞄准
        ctx.defense().addOptional(IronsSpellbooksCompatSpells.COMPANION_BLACK_HOLE.get().getSpellResource());
        // SPELLBREAKING_ECHO: 以施法者为中心的破魔力场，驱散半径内非施法者所有的弹射物、召唤物与法术实体；以自身为中心，不需要瞄准。
        // 原判定只看「所有者不是施法者」，会连主人的箭矢与召唤物一并清除；SpellbreakingEchoEntityMixin 已改为放过同一主人一方的实体（已列入向万法皆通提交的长期 PR）
        ctx.defense().addOptional(IronsSpellbooksCompatSpells.SPELLBREAKING_ECHO.get().getSpellResource());
    }
}
