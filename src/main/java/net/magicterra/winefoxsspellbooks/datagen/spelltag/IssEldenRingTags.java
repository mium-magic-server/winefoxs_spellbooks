package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.eldenring.spells.registry.ModSpells;

/**
 * Elden Ring Spells（iss_elden_ring）的法术分类：Glintstone 系弹体、Carian 剑技、Haima 系与引力法术。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class IssEldenRingTags {

    private IssEldenRingTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // STARLIGHT 只在施法者头顶挂一颗发光的星并放置光源方块，没有任何战斗效果。
        // audit:ignore STARLIGHT

        // ========== Glintstone 弹体 ==========
        // COMET_AZUR: 持续施法的彗星光束
        ctx.attack().addOptional(ModSpells.COMET.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.COMET_AZUR.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GLINTSTONE_ARC.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GLINTSTONE_COMET.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GLINTSTONE_ICECRAG.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GLINTSTONE_PEBBLE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GREAT_GLINTSTONE_SHARD.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SPIRAL_SHARD.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SWIFT_GLINTSTONE_SHARD.get().getSpellResource());

        // ========== Glintstone 连射与落星 ==========
        // CRYSTAL_BARRAGE / METEORITE / METEORITE_OF_ASTEL: 持续施法，施法期间不断落下晶片或陨石
        ctx.attack().addOptional(ModSpells.CRYSTAL_BARRAGE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.CRYSTAL_BURST.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FOUNDING_RAIN_OF_STARS.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GLINTSTONE_STARS.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.METEORITE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.METEORITE_OF_ASTEL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.STARS_OF_RUIN.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.STAR_SHOWER.get().getSpellResource());

        // ========== Carian 剑技 ==========
        // ADULAS_MOONBLADE / CARIAN_GREATSWORD / CARIAN_PIERCER / CARIAN_SLICER: 持续施法期间的剑形实体挥砍
        // CARIAN_PHALANX / GLINTBLADE_PHALANX / GREATBLADE_PHALANX / MAGIC_GLINTBLADE: 悬浮飞剑，自动射向目标
        ctx.attack().addOptional(ModSpells.ADULAS_MOONBLADE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.CARIAN_GREATSWORD.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.CARIAN_PHALANX.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.CARIAN_PIERCER.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.CARIAN_SLICER.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GLINTBLADE_PHALANX.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GREATBLADE_PHALANX.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.MAGIC_GLINTBLADE.get().getSpellResource());

        // ========== Haima 与其他 ==========
        // GAVEL_OF_HAIMA: 槌击落在施法者前方约 1.6 格
        // ROCK_SLING: 施法期间在身边悬浮岩石，施法完成后射出
        ctx.attack().addOptional(ModSpells.CANNON_OF_HAIMA.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.GAVEL_OF_HAIMA.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LORETTA_GREATBOW.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.ROCK_SLING.get().getSpellResource());

        // ========== 引力 ==========
        // COLLAPSING_STARS / GRAVITY_BALL: 不造成伤害，仅把范围内敌人向施法者拉近
        // 重复施放无法由数据文件抑制：二者既不造成伤害也不施加任何 MobEffect（落点判定后只用 teleport 把目标拉到施法者身边），
        // 而防重复机制只认「目标身上是否已有 caused_effect 记录的效果」，且在 NEGATIVE 类别中也只调整类别权重，不会排除单个法术。
        // 目前只有 Iron's 自带冷却（GRAVITY_BALL 1.5 秒、COLLAPSING_STARS 2 秒）与「上一轮为 NEGATIVE 则类别权重 -100」兜底。
        // 另：落点判定用 isFriendlyFireBetween(候选目标, 施法者)，方向为「主人 → 女仆」，依赖修复后的 EntityAllyMixin，否则主人也会被拉近。
        ctx.negativeEffect().addOptional(ModSpells.COLLAPSING_STARS.get().getSpellResource());
        ctx.negativeEffect().addOptional(ModSpells.GRAVITY_BALL.get().getSpellResource());

        // ========== 自我强化 ==========
        // TERRA_MAGICA: 以施法者为中心生成增加法术强度的区域，施法者自身在区域内
        ctx.defense().addOptional(ModSpells.TERRA_MAGICA.get().getSpellResource());
    }
}
