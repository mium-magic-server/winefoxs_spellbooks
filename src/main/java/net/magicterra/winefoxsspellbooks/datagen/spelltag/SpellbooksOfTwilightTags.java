package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.redreaper.twilight_spellbooks.init.ModSpells;

/**
 * Spellbooks of Twilight（暮色法术书，暮色森林联动）的法术分类贡献者。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class SpellbooksOfTwilightTags {

    private SpellbooksOfTwilightTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // ========== 攻击：远程弹射 / 射线 ==========
        ctx.attack().addOptional(ModSpells.AURORA_MISSILE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.DRUID_BOLT.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.EXANIMATED_FIREBALL.get().getSpellResource());
        // EXANIMATED_RAY: 即时射线，命中后附加 OMINOUS_BURN
        ctx.attack().addOptional(ModSpells.EXANIMATED_RAY.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.EXANIMATED_TRIDENTS.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FIRE_BETTLE_SPIT.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.HYDRA_MORTAR_SHOT.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.ICE_BOMB.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.TWILIGHT_BOLT.get().getSpellResource());

        // ========== 攻击：范围 / 持续 ==========
        ctx.attack().addOptional(ModSpells.AVALANCHE.get().getSpellResource());
        // CARMINITE_PULL: 持续锥形喷射，造成小额伤害并把敌人向施法者拉近
        ctx.attack().addOptional(ModSpells.CARMINITE_PULL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.JET_ERUPTION.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.MOSQUITO_SWARM.get().getSpellResource());

        // ========== 攻击：吸血 / 冲刺 ==========
        // LIFE_SACRIFICE: 对射线命中的目标造成伤害并带吸血；目标是自己的召唤物时改为献祭回血，不消耗施法者生命
        ctx.attack().addOptional(ModSpells.LIFE_SACRIFICE.get().getSpellResource());
        // MINOTAUR_LUNGE: 向前冲刺 8 格并伤害路径上最近的敌人，与 BURNING_DASH 同按攻击处理
        ctx.attack().addOptional(ModSpells.MINOTAUR_LUNGE.get().getSpellResource());

        // ========== 召唤 ==========
        // 召唤物均实现 IMagicSummon，设置了召唤者且目标选择只跟随召唤者的仇恨，不会攻击女仆一方
        ctx.attack().addOptional(ModSpells.SUMMON_CARMINITE_GOLEMS.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SUMMON_DEATH_TOME.get().getSpellResource());
        // SUMMON_LOYAL_ZOMBIE: 首次施放的僵尸仅存活 5 秒（summonTimer=100），再次施放才召唤长期版本
        ctx.attack().addOptional(ModSpells.SUMMON_LOYAL_ZOMBIE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SUMMON_MINOTAURS.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SUMMON_WINTER_WOLVES.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.SUMMON_CARMINITE_GOLEMS.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.SUMMON_DEATH_TOME.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.SUMMON_LOYAL_ZOMBIE.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.SUMMON_MINOTAURS.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.SUMMON_WINTER_WOLVES.get().getSpellResource());

        // ========== 自我强化 ==========
        // FORTIFYING_SHIELDS: 给施法者附加 6 层暮色森林的护盾层（FortificationShieldAttachment）
        ctx.defense().addOptional(ModSpells.FORTIFYING_SHIELDS.get().getSpellResource());

        // ========== 位移 ==========
        // EXANIMATED_STEP: 沿视线瞬移，落点处对周围敌人造成范围伤害
        ctx.movement().addOptional(ModSpells.EXANIMATED_STEP.get().getSpellResource());

        // 需要多次施放（recast）的法术
        // EXANIMATED_TRIDENTS: 首发后可在 100 刻内连续追加 2+等级 发三叉戟
        ctx.maidShouldRecast().addOptional(ModSpells.EXANIMATED_TRIDENTS.get().getSpellResource());
    }
}
