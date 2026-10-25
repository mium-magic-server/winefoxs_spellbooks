package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.cursedschool.registry.ModSpells;

/**
 * Cursed School 附属模组的法术标签贡献。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class CursedSchoolTags {

    private CursedSchoolTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 空咬：checkPreCastConditions 与 onCast 都要求施法者是 Player 并消耗其快捷栏食物，女仆恒定无法施放。
        // audit:ignore HOLLOW_BITE
        // 苍白灯笼：只给周围敌人加发光、揭穿隐身，仅对亡灵有每半秒约 1 点伤害，属于探测类，战斗价值很低。
        // audit:ignore PALE_LANTERN
        // 贪婪页边：只挂一个 GREEDY_MARGIN 状态，其法力折扣逻辑（trySpendCharge）在模组内无任何调用，实际无效果。
        // audit:ignore GREEDY_MARGIN
        // 无签审判：施放前置条件读取玩家专属的「腐蚀值」，非玩家恒为 0 而低于门槛，女仆恒定无法施放。
        // audit:ignore UNSIGNED_JUDGMENT

        // ========== 攻击 ==========
        ctx.attack().addOptional(ModSpells.ASH_NEEDLE.get().getSpellResource());
        // BONE_TOLL: 以自身为中心半径 4 的近身爆发，同时给自己叠过路费标记
        ctx.attack().addOptional(ModSpells.BONE_TOLL.get().getSpellResource());
        // CHOIR_OF_FLIES: 持续引导，蝇群跟随施法者视线点并周期性伤害
        ctx.attack().addOptional(ModSpells.CHOIR_OF_FLIES.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.CROW_FEAST.get().getSpellResource());
        // DEBT_SPIKE: 直接伤害，并挂「欠款」持续伤害，到期再追加一次重击
        ctx.attack().addOptional(ModSpells.DEBT_SPIKE.get().getSpellResource());
        // DECAY_FIELD: 持续引导，对脚下半径约 1.6 的敌人周期性伤害并施加凋零
        ctx.attack().addOptional(ModSpells.DECAY_FIELD.get().getSpellResource());
        // ECLIPSE_MAW: 把前方的敌人拉到一点，延迟约 8 刻后重击
        ctx.attack().addOptional(ModSpells.ECLIPSE_MAW.get().getSpellResource());
        // FINISHED_MARGIN: 半径 12 的处决领域，被烙印且血量低于 25% 的敌人会被直接斩杀
        ctx.attack().addOptional(ModSpells.FINISHED_MARGIN.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GRAVE_DUST.get().getSpellResource());
        // HEX_BARRAGE: 给前方最多 5 个敌人种下咒印，咒印到期或施法者再次施放咒术系法术时引爆造成伤害
        ctx.attack().addOptional(ModSpells.HEX_BARRAGE.get().getSpellResource());
        // LEECH_THREAD: 每半秒一次吸取伤害，并按伤害的 40% 治疗施法者
        ctx.attack().addOptional(ModSpells.LEECH_THREAD.get().getSpellResource());
        // MARROW_DRAIN: 高额单体伤害，命中后给施法者吸收护盾
        ctx.attack().addOptional(ModSpells.MARROW_DRAIN.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.ROTTING_RAIN.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.ROT_BOLT.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.RUIN_SLASH.get().getSpellResource());
        // SEVERED_TITHE: 射线长度仅 6 格的高额单体伤害，击杀时回血
        ctx.attack().addOptional(ModSpells.SEVERED_TITHE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.TESTAMENT_OF_ASH.get().getSpellResource());
        // THRICE_SIGNED_NAME: 回响「上一次咒术」依赖玩家专属记录，女仆恒走兜底分支，连发三枚腐朽弹
        ctx.attack().addOptional(ModSpells.THRICE_SIGNED_NAME.get().getSpellResource());
        // WAILING_DIRGE: 前方锥形范围伤害，并把敌方生物击退、施加恐惧
        ctx.attack().addOptional(ModSpells.WAILING_DIRGE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.WITHERING_TOUCH.get().getSpellResource());

        // ========== 召唤 ==========
        // 四个召唤物都以施法者为主人，沿用 Iron's 的 GenericOwner* 目标选择，不会攻击主人一方。
        ctx.attack().addOptional(ModSpells.ASH_PACK.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.CALL_CLERK.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.HATCH_CYSTS.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.ISSUE_INVOICE.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.ASH_PACK.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.CALL_CLERK.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.HATCH_CYSTS.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.ISSUE_INVOICE.get().getSpellResource());

        // ========== 控制 / 削弱 ==========
        // BLACK_ROOT: 小额伤害 + 强制定身 + 凋零 II，主要用途是控场
        ctx.negativeEffect().addOptional(ModSpells.BLACK_ROOT.get().getSpellResource());
        // BLACK_VERSE: 以自身为中心半径 8 的反魔领域，每次脉冲伤害极低，主要给敌人施加虚弱、挖掘疲劳与缓慢
        ctx.negativeEffect().addOptional(ModSpells.BLACK_VERSE.get().getSpellResource());
        // CHAIN_OF_REMORSE: 给最多 3 个敌人挂悔恨链，其造成伤害时按比例反噬自身
        ctx.negativeEffect().addOptional(ModSpells.CHAIN_OF_REMORSE.get().getSpellResource());
        // INVOICE: 给目标挂发票，目标施法或被治疗时引爆，到期则小额结算
        ctx.negativeEffect().addOptional(ModSpells.INVOICE.get().getSpellResource());
        // STAIN_BRAND: 给目标烙印，使其受到的咒术伤害放大，自身几乎无伤害
        ctx.negativeEffect().addOptional(ModSpells.STAIN_BRAND.get().getSpellResource());
        // UMBRAL_COFFIN: 使目标无法移动的牢笼，附带周期性伤害；目标血量低于 20% 时会自动解除
        ctx.negativeEffect().addOptional(ModSpells.UMBRAL_COFFIN.get().getSpellResource());

        // ========== 自我强化 ==========
        // APOSTATE_CROWN: 咒术强度 +30% 与护甲韧性，到期时对周围敌人引爆一次并自带短暂缓慢，期间无法被移除
        ctx.defense().addOptional(ModSpells.APOSTATE_CROWN.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.ASH_GUARD.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.MARGIN_PACT.get().getSpellResource());
        // NIGHT_LEDGER: 咒术强度加成，黑暗中附带夜视与加速
        ctx.defense().addOptional(ModSpells.NIGHT_LEDGER.get().getSpellResource());
        // SOOT_SHROUD: 隐身 + 加速，下一次攻击破隐并增伤
        ctx.defense().addOptional(ModSpells.SOOT_SHROUD.get().getSpellResource());

        // ========== 位移 ==========
        // BLIGHT_STEP: 沿视线闪现，起点与终点各留下一个腐蚀水洼
        ctx.movement().addOptional(ModSpells.BLIGHT_STEP.get().getSpellResource());
    }
}
