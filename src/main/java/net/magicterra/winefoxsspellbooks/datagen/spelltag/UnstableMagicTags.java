package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.github.astralight37.ironsspellsandunstablespellbooks.SpellsRegister;

/**
 * Iron's Spells 'n Unstable Spellbooks（不稳定魔法）的法术分类贡献者。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class UnstableMagicTags {

    private UnstableMagicTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：三个轨道打击都以施法者为所有者生成数十到数百个 PrimedTnt，在射线落点（最远 24 格）集中引爆，
        // 爆炸会同样伤到女仆自己与其主人，STABSHOT 的 TNT 未打 orbital_strike_tnt 标签，还会破坏地形。
        // audit:ignore ORBITAL_STRIKE_BOMB, ORBITAL_STRIKE_NUKE, ORBITAL_STRIKE_STABSHOT

        // 原因：生成狼的分支被 entity instanceof Player 限制，女仆施放只会登记 recast 而不召唤任何狼。
        // audit:ignore ORBITAL_WOLFS

        // 原因：地雷是无所有者的物品实体，存活 10 到 60 秒，任何活体（含女仆、主人与友方生物）进入 2.5 格即爆炸，
        // 爆炸不区分敌我，且落点就在目标脚下，近身时会炸到施法者。
        // audit:ignore SPYGLASS_LANDMINE

        // ========== 攻击 ==========
        // RAILGUN_ARROW: 以高初速射出原版箭矢，穿透力靠速度换伤害，所有者为施法者
        ctx.attack().addOptional(SpellsRegister.RAILGUN_ARROW.get().getSpellResource());

        // ========== 召唤 ==========
        // PHANTOM_HELPERS: 召唤隐身的下界合金装备僵尸（SummonedZombie，设置了召唤者），存活 10 分钟
        ctx.attack().addOptional(SpellsRegister.PHANTOM_HELPERS.get().getSpellResource());
        ctx.summon().addOptional(SpellsRegister.PHANTOM_HELPERS.get().getSpellResource());

        // ========== 自我强化 ==========
        // PONDER_HORN: 为施法者附加 90 秒速度与力量（3 级再加防火）
        ctx.defense().addOptional(SpellsRegister.PONDER_HORN.get().getSpellResource());
    }
}
