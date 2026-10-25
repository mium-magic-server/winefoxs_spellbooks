package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import dev.morsecodeguy.eldritchexpansion.spell.EldritchExpansionSpells;

/**
 * Eldritch Expansions 附属：为其 Eldritch 学派法术分类，玩家专用或对女仆无效的法术一律排除。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class EldritchExpansionsTags {

    private EldritchExpansionsTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：以下法术的 onCast 或实际生效部分只对 ServerPlayer 执行，女仆施放后只会白耗法力：
        // ANCHOR_STEP（时间锚点）、ASTRAL_LENS（怪物发光标记）、CHASM_DRAW（粒子解构）、DREAD_MIRROR（反射仅对 ServerPlayer 生效）、
        // RELIC_SIFTER（战利品劫持）、SPECTRAL_LADDER（飞行）、TEMPORAL_STASIS（时间停滞）、VEIN_SIPHON（需手持镐子的采矿）、
        // VOID_CACHE（虚空储物界面）。
        // 原因：以下法术属于纯玩家功能或会危害己方：ABYSSAL_MAW（持续挖掘视线方块）、CATACOMB_STEP（按俯仰角向地下 / 地面传送）、
        // HOSTILE_CALL（需手持特定物品，召唤无主敌对怪物，会攻击主人）、MUTABLE_VISAGE（需抬头 / 低头 25 度以上，
        // 其挖掘、恐吓、砸地效果仅对 ServerPlayer 生效）、SKYFALL（需正对太阳 / 月亮，并由玩家视线逐个标记目标）。
        // audit:ignore ABYSSAL_MAW, ANCHOR_STEP, ASTRAL_LENS, CATACOMB_STEP, CHASM_DRAW, DREAD_MIRROR, HOSTILE_CALL,
        // audit:ignore MUTABLE_VISAGE, RELIC_SIFTER, SKYFALL, SPECTRAL_LADDER, TEMPORAL_STASIS, VEIN_SIPHON, VOID_CACHE

        // ========== 攻击 ==========
        // GRAVEHOOK: 把目标拽向施法者并造成伤害与缓慢
        ctx.attack().addOptional(EldritchExpansionSpells.GRAVEHOOK.get().getSpellResource());
        ctx.attack().addOptional(EldritchExpansionSpells.NULL_REND.get().getSpellResource());
        // PROFANE_BOND: 把目标怪物暂时策反为施法者的仆从，替其作战（玩家目标除外）
        ctx.attack().addOptional(EldritchExpansionSpells.PROFANE_BOND.get().getSpellResource());
        // STARLESS_WELL: 以施法者为中心的引力井，吸住范围内敌人并持续结算塌缩伤害
        ctx.attack().addOptional(EldritchExpansionSpells.STARLESS_WELL.get().getSpellResource());
        // THE_UNBINDING: 将周围敌人悬浮后抛出，落地造成伤害与虚弱缓慢
        ctx.attack().addOptional(EldritchExpansionSpells.THE_UNBINDING.get().getSpellResource());
        // VOID_LEECH: 对范围内敌对生物汲取生命并治疗自己，主要用途仍是伤害
        ctx.attack().addOptional(EldritchExpansionSpells.VOID_LEECH.get().getSpellResource());

        // ========== 自我强化 ==========
        ctx.defense().addOptional(EldritchExpansionSpells.FERAL_DISTORTION.get().getSpellResource());
        // PHANTOM_STRIDE: 隐身 + 加速 + 缓降
        ctx.defense().addOptional(EldritchExpansionSpells.PHANTOM_STRIDE.get().getSpellResource());

        // ========== 治疗 ==========
        // PALE_COVENANT: 付出少量生命换取吸收护盾、再生并清除负面效果，净收益为正
        ctx.support().addOptional(EldritchExpansionSpells.PALE_COVENANT.get().getSpellResource());
    }
}
