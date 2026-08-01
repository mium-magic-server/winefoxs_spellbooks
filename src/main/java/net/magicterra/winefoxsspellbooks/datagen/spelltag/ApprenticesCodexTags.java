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

        // ========== 攻击 ==========
        // ANCHOR_BLINK: 掷出锚点匕首造成伤害，随后可闪回锚点
        ctx.attack().addOptional(SpellRegistry.ANCHOR_BLINK.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ARCANE_BEAM.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ARCANE_BLAST.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ARTISAN_SMASH.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.BOUND_BOW.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.BOUND_SWORD.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.BREACHING_ENEMY.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.BULLET_STREAM.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.COMMENCE_FIRE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.COMPOUND_PHIAL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.DUAL_ACROBAT.get().getSpellResource());
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
        ctx.attack().addOptional(SpellRegistry.MAGIC_SPEAR.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MANA_SLASH.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MANTIS_LEAP.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MOON_LIGHT.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.PHALANX_CHARGE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.PRECISION_JACK.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.QUICK_ARMS.get().getSpellResource());
        // SERVANT_GAZE: 开启后自动消耗法力攻击半径 20 内的敌人
        ctx.attack().addOptional(SpellRegistry.SERVANT_GAZE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SHOCK.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SILENT_ASSASSIN.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SKY_EDGE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SLASH_BLADE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.THERMAL_PROCESS.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.TIRO_VOLLEY.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.TOTEM_OF_PERMAFROST.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.UNITE_LUNA.get().getSpellResource());

        // ========== 自我强化 ==========
        ctx.defense().addOptional(SpellRegistry.DIVINE_POSSESSION.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.ECHO_CAST.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.FORCE_FIELD.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.MIRAGE_AVOIDANCE.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.MYSTIC_SHIELD.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.PALETTE_SHIFT.get().getSpellResource());

        // ========== 位移 ==========
        ctx.movement().addOptional(SpellRegistry.ASSIST_WINGS.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistry.LONG_STRIDE.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistry.MIST_FORM.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistry.SPECTRAL_WING.get().getSpellResource());

        // ========== 治疗队友 ==========
        // GRACED_RAIN / HEALING_BLOOM 都是放置一个持续治疗范围内友军的实体
        ctx.supportEffect().addOptional(SpellRegistry.GRACED_RAIN.get().getSpellResource());
        ctx.supportEffect().addOptional(SpellRegistry.HEALING_BLOOM.get().getSpellResource());

        // ========== 召唤 ==========
        // 只给真正召唤独立单位的三个打标记；其余「召唤武器」是持续时间内的具现兵器，不算召唤物
        ctx.attack().addOptional(SpellRegistry.ARCHER_MULTIPLE.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistry.ARCHER_MULTIPLE.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.AUTO_TURRET.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistry.AUTO_TURRET.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FIELD_OVERSEER.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistry.FIELD_OVERSEER.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(SpellRegistry.ARCANE_BEAM.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.ARCHER_MULTIPLE.get().getSpellResource());
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
