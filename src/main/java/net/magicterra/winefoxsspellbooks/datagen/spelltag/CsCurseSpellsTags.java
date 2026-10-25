package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.iriri.cursespells.registry.CurseSpellRegistry;

/**
 * CS Curse Spells（诅咒法术）的法术标签分类。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class CsCurseSpellsTags {

    private CsCurseSpellsTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：CURSED_EYE 只给全图生物挂发光（诅咒效果依赖可选模组 l2complements），属于侦测；
        // CURSED_HEART 施放时对自身造成 18 点魔法伤害；CURSED_LUNG 只给水下呼吸、夜视与海豚恩惠，无战斗价值。
        // audit:ignore CURSED_EYE, CURSED_HEART, CURSED_LUNG
        // 原因：WOOD_HAMMER 的锤击对落点半径 6 格内除施法者外的所有生物直接 hurt，不走本模组多数法术共用的 CurseDamage.isValidTarget 敌我判定，
        // 每次挥击（共 5 次）都会伤到主人、宠物与其他女仆，女仆通常就在主人身边作战，误伤几乎不可避免。
        // audit:ignore WOOD_HAMMER

        // ========== 弹道与射线 ==========
        // PRIMAL_SEED: 落地生成草甸，对敌人持续造成伤害，同时给主人与友方回复生命
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_ARROW.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_BREATH.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_SLASH.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.FLAME_OF_STAGNATION.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.HOMING_SOULMASS.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.PRIMAL_SEED.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.RAY_OF_PUNISHMENT.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.SOUL_STREAM.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.WANDERING_GRUDGE.get().getSpellResource());

        // ========== 指定或自动选定目标 ==========
        // CURSED_STRIKE: 自动选取 24 格内最近的敌人并连续弹跳
        // DAGGER_OF_OBSESSION: 施法者先向后跃出 15 格，再用 20 柄飞刀自动锁定敌人；优先锁定非友方玩家
        ctx.attack().addOptional(CurseSpellRegistry.CATASTROPHE.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CHAIN_CURSE.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_STRIKE.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.DAGGER_OF_OBSESSION.get().getSpellResource());

        // ========== 突进 ==========
        // CURSED_CHARGE: 沿水平朝向冲锋 20 刻，途中切割碰到的敌人，伤害为主
        // SHADOW_DIVE: 上升后闪现到目标身后俯冲，落点爆发并击飞周围敌人，伤害为主
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_CHARGE.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.SHADOW_DIVE.get().getSpellResource());

        // ========== 前方区域 ==========
        // FLAMES_OF_FURY: 施法者原地被定身约 2.25 秒，四向地狱火柱分两轮推进
        ctx.attack().addOptional(CurseSpellRegistry.BLADE_OF_JUDGMENT.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_CLAW.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_CRUSH.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_HAND.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_RAGE.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_SCAR.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.DROPLET_OF_STAGNATION.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.FLAMES_OF_FURY.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.FRENZY_PLANT.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.HELL_FIRE.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.LEAF_STORM.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.PILLAR_OF_SIN.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.RAGE_AWAKENED.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.SKYFALL.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.THE_SKY_AND_COSMOS_ARE_ONE.get().getSpellResource());

        // ========== 以自身为中心 ==========
        // A_CALL_BEYOND: 在头顶裂隙向四周抛出多枚落地爆炸的魂火球
        // CURSED_ORB: 以施法者为中心的半径 15 格伤害领域，持续约 10 秒
        // STAGNANT_SPIRAL: 两柄环绕施法者的飞刃，持续 10 秒，只会伤到贴近施法者的敌人
        ctx.attack().addOptional(CurseSpellRegistry.A_CALL_BEYOND.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_ORB.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_SCATTER.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.SILENT_WALTZ.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.STAGNANT_SPIRAL.get().getSpellResource());

        // ========== 持续光环 ==========
        // 三者都给施法者自身挂一个持续约 20 到 30 秒的效果，由效果周期性向周围敌人降下伤害，因此归入攻击
        ctx.attack().addOptional(CurseSpellRegistry.ABHORRENT_STAR.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.ALL_VANITY.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.INFERNO.get().getSpellResource());

        // ========== 召唤 ==========
        // 召唤出的信徒与残骸以施法者为所有者；施法者不是玩家时，其会被标为敌对生物，并主动攻击不属于友方的玩家
        ctx.attack().addOptional(CurseSpellRegistry.CURSED_SABBATH.get().getSpellResource());
        ctx.summon().addOptional(CurseSpellRegistry.CURSED_SABBATH.get().getSpellResource());
        ctx.attack().addOptional(CurseSpellRegistry.THE_COMMAND.get().getSpellResource());
        ctx.summon().addOptional(CurseSpellRegistry.THE_COMMAND.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(CurseSpellRegistry.CURSED_SCAR.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(CurseSpellRegistry.SILENT_WALTZ.get().getSpellResource());
    }
}
