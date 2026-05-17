package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;

/**
 * Iron's Spellbooks 本体九大学派的标签贡献。
 */
public final class IronsSpellbooksTags {

    private IronsSpellbooksTags() {}

    public static void contribute(SpellTagContext ctx) {
        // ========== Blood ==========
        ctx.attack().addOptional(SpellRegistry.ACUPUNCTURE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.BLOOD_NEEDLES_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.BLOOD_SLASH_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.DEVOUR_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.RAY_OF_SIPHONING_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.WITHER_SKULL_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SACRIFICE_SPELL.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistry.BLOOD_STEP_SPELL.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.HEARTSTOP_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.RAISE_DEAD_SPELL.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistry.RAISE_DEAD_SPELL.get().getSpellResource());

        // ========== Eldritch ==========
        ctx.attack().addOptional(SpellRegistry.ELDRITCH_BLAST_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SCULK_TENTACLES_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SONIC_BOOM_SPELL.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.ABYSSAL_SHROUD_SPELL.get().getSpellResource());

        // ========== Ender ==========
        ctx.attack().addOptional(SpellRegistry.BLACK_HOLE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.DRAGON_BREATH_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MAGIC_ARROW_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MAGIC_MISSILE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SHADOW_SLASH.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.STARFALL_SPELL.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.ECHOING_STRIKES_SPELL.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.EVASION_SPELL.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistry.TELEPORT_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SUMMON_SWORDS.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistry.SUMMON_SWORDS.get().getSpellResource());

        // ========== Evocation ==========
        ctx.attack().addOptional(SpellRegistry.ARROW_VOLLEY_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.CHAIN_CREEPER_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FANG_STRIKE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FANG_WARD_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FIRECRACKER_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.GUST_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.LOB_CREEPER_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.THROW_SPELL.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.INVISIBILITY_SPELL.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.SHIELD_SPELL.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistry.SLOW_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.WOLOLO_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SUMMON_VEX_SPELL.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistry.SUMMON_VEX_SPELL.get().getSpellResource());

        // ========== Fire ==========
        ctx.attack().addOptional(SpellRegistry.BLAZE_STORM_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.BURNING_DASH_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FIRE_ARROW_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FIRE_BREATH_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FIREBALL_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FIREBOLT_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FLAMING_BARRAGE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FLAMING_STRIKE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.MAGMA_BOMB_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.RAISE_HELL_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SCORCH_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.WALL_OF_FIRE_SPELL.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistry.HEAT_SURGE_SPELL.get().getSpellResource());

        // ========== Holy ==========
        ctx.attack().addOptional(SpellRegistry.DIVINE_SMITE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.GUIDING_BOLT_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SUNBEAM_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.WISP_SPELL.get().getSpellResource());
        ctx.support().addOptional(SpellRegistry.GREATER_HEAL_SPELL.get().getSpellResource());
        ctx.support().addOptional(SpellRegistry.HEAL_SPELL.get().getSpellResource());
        ctx.supportEffect().addOptional(SpellRegistry.HEAL_SPELL.get().getSpellResource());
        ctx.positiveEffect().addOptional(SpellRegistry.FORTIFY_SPELL.get().getSpellResource());
        ctx.positiveEffect().addOptional(SpellRegistry.HASTE_SPELL.get().getSpellResource());
        ctx.positiveEffect().addOptional(SpellRegistry.CLEANSE_SPELL.get().getSpellResource());
        ctx.supportEffect().addOptional(SpellRegistry.BLESSING_OF_LIFE_SPELL.get().getSpellResource());
        ctx.supportEffect().addOptional(SpellRegistry.CLOUD_OF_REGENERATION_SPELL.get().getSpellResource());
        ctx.supportEffect().addOptional(SpellRegistry.HEALING_CIRCLE_SPELL.get().getSpellResource());

        // ========== Ice ==========
        ctx.attack().addOptional(SpellRegistry.CONE_OF_COLD_SPELL.get().getSpellResource());
        ctx.positiveEffect().addOptional(SpellRegistry.FROSTBITE_SPELL.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistry.FROSTWAVE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ICE_BLOCK_SPELL.get().getSpellResource()); // 在目标上方生成冰块砸落
        ctx.attack().addOptional(SpellRegistry.ICE_SPIKES_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ICICLE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.RAY_OF_FROST_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SNOWBALL_SPELL.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistry.FROST_STEP_SPELL.get().getSpellResource());
        ctx.support().addOptional(SpellRegistry.ICE_TOMB_SPELL.get().getSpellResource()); // 控制法术，对敌人施放
        ctx.attack().addOptional(SpellRegistry.SUMMON_POLAR_BEAR_SPELL.get().getSpellResource());
        ctx.summon().addOptional(SpellRegistry.SUMMON_POLAR_BEAR_SPELL.get().getSpellResource());

        // ========== Lightning ==========
        ctx.attack().addOptional(SpellRegistry.BALL_LIGHTNING_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.CHAIN_LIGHTNING_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.ELECTROCUTE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.LIGHTNING_BOLT_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.LIGHTNING_LANCE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.SHOCKWAVE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.THUNDERSTORM_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.VOLT_STRIKE_SPELL.get().getSpellResource());
        ctx.movement().addOptional(SpellRegistry.ASCENSION_SPELL.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.CHARGE_SPELL.get().getSpellResource());

        // ========== Nature ==========
        ctx.negativeEffect().addOptional(SpellRegistry.ACID_ORB_SPELL.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistry.BLIGHT_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.EARTHQUAKE_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.FIREFLY_SWARM_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.POISON_ARROW_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.POISON_BREATH_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.POISON_SPLASH_SPELL.get().getSpellResource());
        ctx.attack().addOptional(SpellRegistry.STOMP_SPELL.get().getSpellResource());
        ctx.support().addOptional(SpellRegistry.GLUTTONY_SPELL.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.OAKSKIN_SPELL.get().getSpellResource());
        ctx.defense().addOptional(SpellRegistry.SPIDER_ASPECT_SPELL.get().getSpellResource());
        ctx.negativeEffect().addOptional(SpellRegistry.ROOT_SPELL.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(SpellRegistry.ELDRITCH_BLAST_SPELL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.FLAMING_BARRAGE_SPELL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.WALL_OF_FIRE_SPELL.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(SpellRegistry.RAISE_HELL_SPELL.get().getSpellResource());
    }
}
