package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.redreaper.monsterspellbooks.init.ModSpellRegistry;

public final class MonstersSpellbooksTags {

    private MonstersSpellbooksTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // WITHER_BOMB 的 onCast 整个函数体包在 entity instanceof Player 里且没有 else 分支，
        // 女仆施放它不发弹、不挂 recast，纯粹白耗蓝和吟唱时间，故不给任何行为标签。
        // audit:ignore WITHER_BOMB

        // ========== Aero ==========
        // Monsters & Spellbooks 0.0.16.3 起不再注册下列 Aero 法术（类仍在 jar 内，但 ModSpellRegistry 中无对应字段），
        // 以及 OBSIDIAN_ARSENAL、CLOUD_DASH，故暂时注释掉，待上游恢复注册后再启用。
        // ctx.positiveEffect().addOptional(ModSpellRegistry.AIR_BUBBLE.get().getSpellResource());
        // ctx.movement().addOptional(ModSpellRegistry.AIR_PROPULSION.get().getSpellResource());
        // ctx.attack().addOptional(ModSpellRegistry.FORCEFUL_WIND.get().getSpellResource());
        // ctx.attack().addOptional(ModSpellRegistry.STEAM_STREAM.get().getSpellResource());
        // ctx.attack().addOptional(ModSpellRegistry.SUFFOCATE.get().getSpellResource());
        // ctx.attack().addOptional(ModSpellRegistry.WIND_CHARGE.get().getSpellResource());
        // ctx.attack().addOptional(ModSpellRegistry.WIND_WAVE.get().getSpellResource());

        // ========== Blood ==========
        ctx.negativeEffect().addOptional(ModSpellRegistry.ANTICOAGULATION.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.BLOOD_PIERCE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.BLOOD_THORN.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.CAUTERIZING_TOUCH.get().getSpellResource());
        // HYSTERIA: 自身狂暴，代价是 CURSE/HEARTSTOP，属于自我强化
        ctx.defense().addOptional(ModSpellRegistry.HYSTERIA.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SANGUINITE_EVISCERATION.get().getSpellResource());

        // ========== Ender ==========
        ctx.defense().addOptional(ModSpellRegistry.ENDERSENT_FORM.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.ENDERSENT_SMASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.GRAVITY_WELL.get().getSpellResource());
        // ctx.attack().addOptional(ModSpellRegistry.OBSIDIAN_ARSENAL.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SPACE_BREAKER.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SPACE_RUPTURE.get().getSpellResource());
        // UNSTABLE_TELEPORT: 先在原地引爆再传送，位移只是副产物
        ctx.attack().addOptional(ModSpellRegistry.UNSTABLE_TELEPORT.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.VOID_ARROWS.get().getSpellResource());

        // ========== Evocation ==========
        ctx.positiveEffect().addOptional(ModSpellRegistry.ENCHANTERS_BOOST.get().getSpellResource());
        ctx.positiveEffect().addOptional(ModSpellRegistry.ENCHANTERS_PROTECTION.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.CRUSH.get().getSpellResource());
        // CRUSHING_PRESENCE: 自身光环，持续麻痹周围敌人，交战时施放
        ctx.attack().addOptional(ModSpellRegistry.CRUSHING_PRESENCE.get().getSpellResource());
        ctx.defense().addOptional(ModSpellRegistry.SPELL_OF_UNDYING.get().getSpellResource());

        // ========== Fire ==========
        ctx.attack().addOptional(ModSpellRegistry.BLAST_FUNGUS.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.BRIMSTONE_BUZZSAW.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.BRIMSTONE_RAIN.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.BRIMSTONE_WRATH.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.NAPALM_ORB.get().getSpellResource());
        ctx.defense().addOptional(ModSpellRegistry.OVERHEAT.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.WRATH.get().getSpellResource());

        // ========== Holy ==========
        ctx.attack().addOptional(ModSpellRegistry.DIVINE_INTERVENTION.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.HALLOW_SLASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.PALADIN_THROW.get().getSpellResource());

        // ========== Hydro ==========
        ctx.attack().addOptional(ModSpellRegistry.BUBBLE_SPRAY.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.EFFERVESCENCE_BUBBLE.get().getSpellResource());
        ctx.negativeEffect().addOptional(ModSpellRegistry.GLOW_INK_RELEASE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.INK_BOMB.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.RAZORBLADE_TYPHOON.get().getSpellResource());
        // RIPTIDE_DASH: 激流冲刺，冲撞造成伤害，按攻击处理（与本体燃烧冲刺一致）
        ctx.attack().addOptional(ModSpellRegistry.RIPTIDE_DASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.WATER_TRIDENT.get().getSpellResource());

        // ========== Ice ==========
        // BLIZZARD_ASPECT: 自身增益，但增益本体是半径 20 的持续伤害光环
        ctx.attack().addOptional(ModSpellRegistry.BLIZZARD_ASPECT.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.FROST_BREATH.get().getSpellResource());
        ctx.positiveEffect().addOptional(ModSpellRegistry.FROST_COATING.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.FROSTED_SNOWBOLT.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.ICE_ARSENAL.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.ORBITAL_SNOWBALL.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.ORBITAL_SNOWBALL.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SUMMON_ICE_HYDRA.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.SUMMON_ICE_HYDRA.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.TUNDRA_TERRAIN.get().getSpellResource());

        // ========== Lightning ==========
        ctx.attack().addOptional(ModSpellRegistry.DISCHARGE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.GUARDIANS_NEUTRALIZER.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.PRIMORDIAL_FLASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.RAIGO.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.RAIJIN_JUDGEMENT.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.REDSTONE_BOLT.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.REDSTONE_CHAIN.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.REDSTONE_LASERS.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.REDSTONE_MINES.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.STATIC_CLEAVE.get().getSpellResource());
        // THUNDER_STEP: 闪电链 + 传送，主用途是打伤害
        ctx.attack().addOptional(ModSpellRegistry.THUNDER_STEP.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.THUNDERSTORM_WAVE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.VOLTAIC_MULTISHOT.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.ZAP.get().getSpellResource());

        // ========== Nature ==========
        ctx.attack().addOptional(ModSpellRegistry.INFECTION_SLASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.LEAF_CRYSTAL.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.LEAF_CRYSTAL.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.POISON_QUILL.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SPIDER_FANGS.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SUMMON_POISON_VINE.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.SUMMON_POISON_VINE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.VITAL_BLAST.get().getSpellResource());

        // ========== Necromancy ==========
        ctx.negativeEffect().addOptional(ModSpellRegistry.FALL_CURSE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.GRAVEYARD_FISSURE.get().getSpellResource());
        ctx.defense().addOptional(ModSpellRegistry.LICHDOM.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.LIFE_DRAIN.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.PUTRESCENCE_MASS.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.RANCORCALL.get().getSpellResource());
        ctx.defense().addOptional(ModSpellRegistry.REAPER_ASPECT.get().getSpellResource());
        // SOUL_CASTING_FIELD: 灵魂力场治疗范围内友军
        ctx.supportEffect().addOptional(ModSpellRegistry.SOUL_CASTING_FIELD.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SOUL_CHAIN.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SOUL_FIREBOLT.get().getSpellResource());
        ctx.movement().addOptional(ModSpellRegistry.SOUL_FIRE_TELEPORT.get().getSpellResource());
        ctx.defense().addOptional(ModSpellRegistry.SOUL_FORM.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SOUL_SCORCH.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SPECTRAL_BLAST.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SPIRIT_STRIKE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.STRAY_GRASP.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.TORMENT_ARROW.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.VILE_SLASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.WITHER_NOVA.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SUMMON_DEATH_KNIGHT.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.SUMMON_DEATH_KNIGHT.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SUMMON_SOUL_WIZARD.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.SUMMON_SOUL_WIZARD.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SUMMON_WITHER_ARMY.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.SUMMON_WITHER_ARMY.get().getSpellResource());

        // ========== 其余召唤 ==========
        ctx.attack().addOptional(ModSpellRegistry.SUMMON_AEGIS_PATROL.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.SUMMON_AEGIS_PATROL.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SUMMON_DWARVEN_DRONES.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.SUMMON_DWARVEN_DRONES.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SUMMON_PRISMARINE_SQUAD.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.SUMMON_PRISMARINE_SQUAD.get().getSpellResource());

        // ========== Technomancy / 其他 ==========
        ctx.attack().addOptional(ModSpellRegistry.CORRUPTED_BEACON_RAY.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.DRAGON_CHARGE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.FIRMAMENT_SPLITTER.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.FRENZIED_BURST.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.FRENZIED_STORM.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.FRENZY_SURGE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.SPINBLADE.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.BANSHEE_SCREAM.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.BONE_DAGGER.get().getSpellResource());
        // ctx.movement().addOptional(ModSpellRegistry.CLOUD_DASH.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.BRIMSTONE_RAIN.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.BRIMSTONE_WRATH.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.BUBBLE_SPRAY.get().getSpellResource());
        // ctx.maidShouldRecast().addOptional(ModSpellRegistry.CLOUD_DASH.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.CORRUPTED_BEACON_RAY.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.DISCHARGE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.FRENZIED_STORM.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.FRENZY_SURGE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.GUARDIANS_NEUTRALIZER.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.LIFE_DRAIN.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.PRIMORDIAL_FLASH.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.RANCORCALL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.REDSTONE_LASERS.get().getSpellResource());
        // ctx.maidShouldRecast().addOptional(ModSpellRegistry.STEAM_STREAM.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.THUNDER_STEP.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.VOID_ARROWS.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.WITHER_NOVA.get().getSpellResource());
    }
}
