package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import jp.aquafactory.apprenticecodex.registry.SpellRegistry;

public final class ApprenticesCodexTags {

    private ApprenticesCodexTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 纯玩家向工具：搬运、建造、挖矿、照明、探测、宠物收纳，均与战斗无关。
        // audit:ignore AUTO_MAGNET, COMPANION_TRUNK, DEEP_SENSOR, DEMICREATOR_WINGS
        // audit:ignore EARTH_FORGE, EDGE_DANCER, HARVEST_MOON, LINEAR_BUILD
        // audit:ignore MAGE_LIGHT, MANA_CHARGE, MANA_MENDING, MANIFESTATION_GRIMOIRE
        // audit:ignore OTHERWORLD_LENS, PERSONAL_SHELF, REMOTE_EYE, RIFT_HOLE
        // audit:ignore SEARCH_BEACON, SENSE_EVIL, TAMERS_POCKET, TERRA_RESONANCE
        // audit:ignore TINY_LUMBERJACK, TREASURE_DIVINATION, WIZARDLAMP, WORLD_FLATTER

        // 召唤扫帚坐骑：仅限 ServerPlayer 且需要佩戴扫帚饰品，女仆无法施放。
        // audit:ignore CALL_BROOM
        // 偶像重构：施放前置条件要求 Player 且佩戴不死图腾徽章，女仆恒定失败。
        // audit:ignore IDOL_RECONSTRUCTION
        // 魔力转录：仅限 ServerPlayer，消耗经验为物品附魔，纯玩家工具。
        // audit:ignore MANA_TRANSCRIPTION
        // 瞬步：施放前置条件要求 ServerPlayer，位移依赖玩家输入与披风运行时状态。
        // audit:ignore QUICK_BLINK
        // 灵魂转换：仅限 Player 且需要 Malum 魔力转换器，用于恢复法杖充能。
        // audit:ignore SOUL_CONVERSION
        // 摇曳之星：施放前置条件要求 Player，仅切换玩家披风的流星状态。
        // audit:ignore WAVERING_STAR
        // 锚点闪现：checkPreCastConditions 对非 Player 恒返回 false，且要求副手持有镜子物品，女仆无法施放。
        // audit:ignore ANCHOR_BLINK
        // 仆从凝视 / 多重射手：生成法杖或弓实体及其 recast 的代码全部包在 entity instanceof ServerPlayer 分支内，
        // 女仆施放只会白耗冷却，不会产生任何实体或伤害。
        // audit:ignore ARCHER_MULTIPLE, SERVANT_GAZE
        // 具现弓 / 具现太刀：checkPreCastConditions 对非 Player 恒返回 false，女仆无法施放。
        // audit:ignore BOUND_BOW, BOUND_SWORD
        // 幻影回避：checkPreCastConditions 对非 Player 恒返回 false，且依赖玩家输入锁定状态。
        // audit:ignore MIRAGE_AVOIDANCE
        // 灵翼：checkPreCastConditions 对非 Player 恒返回 false，效果为玩家飞行，女仆无法施放。
        // audit:ignore SPECTRAL_WING

        // ========== 攻击 ==========
        ctx.attack().addOptional(SpellRegistry.ARCANE_BEAM.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ARCANE_BLAST.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ARTISAN_SMASH.get().getSpellResource());
        // BLOOD_BRAND: 掷出飞刀并在目标身上刻印血痕，被刻印者死亡时爆炸
        ctx.attack().addOptional(SpellRegistry.BLOOD_BRAND.get().getSpellResource());
        // BLOODY_ARROW: 射出血箭并生成回复友军的血球
        ctx.attack().addOptional(SpellRegistry.BLOODY_ARROW.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.BREACHING_ENEMY.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.BULLET_STREAM.get().getSpellResource());
        // CATCH_FLAME: 3 格内的短射程火焰，点燃目标
        ctx.attack().addOptional(SpellRegistry.CATCH_FLAME.get().getSpellResource());
        // COMBUSTION_JET: 前方 10 格的火焰冲击波
        ctx.attack().addOptional(SpellRegistry.COMBUSTION_JET.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.COMMENCE_FIRE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.COMPOUND_PHIAL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.DUAL_ACROBAT.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ECHO_ARROW.get().getSpellResource());
        // EXTRACT: 把炼金瓶里存的药水甩出去，伤害取决于所存药水
        ctx.attack().addOptional(SpellRegistry.EXTRACT.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FEATHER_RUSH.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FLY_SWATTER.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FROST_RUNE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FUJIN.get().getSpellResource());
        // GRIND_RUNNER: 滚轮带着施法者冲撞，接触造成伤害
        ctx.attack().addOptional(SpellRegistry.GRIND_RUNNER.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.HEAVENLY_FIST.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.HIGANBANA.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ILLUMINATE_STELLAR.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.INSCRIBE_ICE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.LETHAL_ASSAULT.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.LIGHTNING_ARROW.get().getSpellResource());
        // LOCK_ON_RAY: 锁定视线内目标后持续引导激光，没有目标则取消施法
        ctx.attack().addOptional(SpellRegistry.LOCK_ON_RAY.get().getSpellResource());
        // LUNAR_AIM: 四枚月光箭，锁定准星目标追踪，爆炸造成范围伤害
        ctx.attack().addOptional(SpellRegistry.LUNAR_AIM.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MAGIC_SPEAR.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MANA_SLASH.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MANTIS_LEAP.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MOON_LIGHT.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.PHALANX_CHARGE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.PRECISION_JACK.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.QUICK_ARMS.get().getSpellResource());
        // SACRED_ARROW: 圣箭命中后给目标附加圣印，下一发会追踪带圣印的敌人
        ctx.attack().addOptional(SpellRegistry.SACRED_ARROW.get().getSpellResource());
        // SHIDEN: 具现太刀居合斩，前方 4.5 格判定
        ctx.attack().addOptional(SpellRegistry.SHIDEN.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SHOCK.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SILENT_ASSASSIN.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SKY_EDGE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SLASH_BLADE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.THERMAL_PROCESS.get().getSpellResource());
        // THERMAL_SLICE: 具现太刀斩击并削弱火焰抗性；冲刺位移仅玩家有效，女仆原地斩击
        ctx.attack().addOptional(SpellRegistry.THERMAL_SLICE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.TIRO_VOLLEY.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.TOTEM_OF_PERMAFROST.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.UNITE_LUNA.get().getSpellResource());

        // ========== 自我强化 ==========
        ctx.defense().addOptional(SpellRegistry.DIVINE_POSSESSION.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.ECHO_CAST.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.FORCE_FIELD.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.MYSTIC_SHIELD.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.PALETTE_SHIFT.get().getSpellResource());

        // ========== 位移 ==========
        ctx.movement().addOptional(SpellRegistry.ASSIST_WINGS.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistry.LONG_STRIDE.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistry.MIST_FORM.get().getSpellResource());

        // ========== 治疗队友 ==========
        // GRACED_RAIN / HEALING_BLOOM 都是放置一个持续治疗范围内友军的实体
        ctx.supportEffect().addOptional(SpellRegistry.GRACED_RAIN.get().getSpellResource());
        ctx.supportEffect().addOptional(SpellRegistry.HEALING_BLOOM.get().getSpellResource());

        // ========== 召唤 ==========
        // 只给真正召唤独立单位的两个打标记（ARCHER_MULTIPLE 因玩家专属已排除）；其余「召唤武器」是持续时间内的具现兵器，不算召唤物
        ctx.attack().addOptional(SpellRegistry.AUTO_TURRET.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistry.AUTO_TURRET.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FIELD_OVERSEER.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistry.FIELD_OVERSEER.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(SpellRegistry.ARCANE_BEAM.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.BULLET_STREAM.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.COMMENCE_FIRE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.DUAL_ACROBAT.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.FEATHER_RUSH.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.FIELD_OVERSEER.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.FLY_SWATTER.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.FORCE_FIELD.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.FUJIN.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.GRACED_RAIN.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.GRIND_RUNNER.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.LONG_STRIDE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.MYSTIC_SHIELD.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.PHALANX_CHARGE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.THERMAL_PROCESS.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.TIRO_VOLLEY.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.TOTEM_OF_PERMAFROST.get().getSpellResource());
    }
}
