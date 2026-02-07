package net.magicterra.winefoxsspellbooks.datagen;

import com.gametechbc.gtbcs_geomancy_plus.init.GGSpells;
import com.rinko1231.SnowWaifuSpell.init.ModSpellRegistry;
import com.snackpirate.aeromancy.spells.AASpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.concurrent.CompletableFuture;
import net.Iceforkkk.DreamlessAditions.registries.SpellRegistries;
import net.ender.ess_requiem.registries.GGSpellRegistry;
import net.magicterra.winefoxsspellbooks.registry.InitSpells;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.warphan.iss_magicfromtheeast.registries.MFTESpellRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * 根据法术的特征，生成标签
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-26 00:33
 */
public class MaidSpellTagsProvider extends IntrinsicHolderTagsProvider<AbstractSpell> {

    public MaidSpellTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, SpellRegistry.SPELL_REGISTRY_KEY, lookupProvider,
            (spell) -> ResourceKey.create(SpellRegistry.SPELL_REGISTRY_KEY, spell.getSpellResource()), modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var attackTag = tag(MaidSpellRegistry.ATTACK_SPELLS_TAG);
        var defenseTag = tag(MaidSpellRegistry.DEFENSE_SPELLS_TAG);
        var movementTag = tag(MaidSpellRegistry.MOVEMENT_SPELLS_TAG);
        var supportTag = tag(MaidSpellRegistry.SUPPORT_SPELLS_TAG);
        var positiveEffectTag = tag(MaidSpellRegistry.POSITIVE_EFFECT_SPELLS_TAG);
        var supportEffectTag = tag(MaidSpellRegistry.SUPPORT_EFFECT_SPELLS_TAG);
        var negativeEffectTag = tag(MaidSpellRegistry.NEGATIVE_EFFECT_SPELLS_TAG);
        var summonTag = tag(MaidSpellRegistry.SUMMON_SPELLS_TAG);
        var maidShouldRecastTag = tag(MaidSpellRegistry.MAID_SHOULD_RECAST_SPELLS_TAG);

        // ========== Iron's Spellbooks - Blood ==========
        attackTag.addOptional(SpellRegistry.ACUPUNCTURE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.BLOOD_NEEDLES_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.BLOOD_SLASH_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.DEVOUR_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.RAY_OF_SIPHONING_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.WITHER_SKULL_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SACRIFICE_SPELL.get().getSpellResource());
        movementTag.addOptional(SpellRegistry.BLOOD_STEP_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.HEARTSTOP_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.RAISE_DEAD_SPELL.get().getSpellResource());
        summonTag.addOptional(SpellRegistry.RAISE_DEAD_SPELL.get().getSpellResource());

        // ========== Iron's Spellbooks - Eldritch ==========
        attackTag.addOptional(SpellRegistry.ELDRITCH_BLAST_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SCULK_TENTACLES_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SONIC_BOOM_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.ABYSSAL_SHROUD_SPELL.get().getSpellResource());

        // ========== Iron's Spellbooks - Ender ==========
        attackTag.addOptional(SpellRegistry.BLACK_HOLE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.DRAGON_BREATH_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.MAGIC_ARROW_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.MAGIC_MISSILE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SHADOW_SLASH.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.STARFALL_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.ECHOING_STRIKES_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.EVASION_SPELL.get().getSpellResource());
        movementTag.addOptional(SpellRegistry.TELEPORT_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SUMMON_SWORDS.get().getSpellResource());
        summonTag.addOptional(SpellRegistry.SUMMON_SWORDS.get().getSpellResource());

        // ========== Iron's Spellbooks - Evocation ==========
        attackTag.addOptional(SpellRegistry.ARROW_VOLLEY_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.CHAIN_CREEPER_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FANG_STRIKE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FANG_WARD_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FIRECRACKER_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.GUST_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.LOB_CREEPER_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.THROW_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.INVISIBILITY_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.SHIELD_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistry.SLOW_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistry.WOLOLO_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SUMMON_VEX_SPELL.get().getSpellResource());
        summonTag.addOptional(SpellRegistry.SUMMON_VEX_SPELL.get().getSpellResource());

        // ========== Iron's Spellbooks - Fire ==========
        attackTag.addOptional(SpellRegistry.BLAZE_STORM_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.BURNING_DASH_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FIRE_ARROW_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FIRE_BREATH_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FIREBALL_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FIREBOLT_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FLAMING_BARRAGE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FLAMING_STRIKE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.MAGMA_BOMB_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.RAISE_HELL_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SCORCH_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.WALL_OF_FIRE_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistry.HEAT_SURGE_SPELL.get().getSpellResource());

        // ========== Iron's Spellbooks - Holy ==========
        attackTag.addOptional(SpellRegistry.DIVINE_SMITE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.GUIDING_BOLT_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SUNBEAM_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.WISP_SPELL.get().getSpellResource());
        supportTag.addOptional(SpellRegistry.GREATER_HEAL_SPELL.get().getSpellResource());
        supportTag.addOptional(SpellRegistry.HEAL_SPELL.get().getSpellResource());
        positiveEffectTag.addOptional(SpellRegistry.FORTIFY_SPELL.get().getSpellResource());
        positiveEffectTag.addOptional(SpellRegistry.HASTE_SPELL.get().getSpellResource());
        positiveEffectTag.addOptional(SpellRegistry.CLEANSE_SPELL.get().getSpellResource());
        supportEffectTag.addOptional(SpellRegistry.BLESSING_OF_LIFE_SPELL.get().getSpellResource());
        supportEffectTag.addOptional(SpellRegistry.CLOUD_OF_REGENERATION_SPELL.get().getSpellResource());
        supportEffectTag.addOptional(SpellRegistry.HEALING_CIRCLE_SPELL.get().getSpellResource());

        // ========== Iron's Spellbooks - Ice ==========
        attackTag.addOptional(SpellRegistry.CONE_OF_COLD_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FROSTBITE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FROSTWAVE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.ICE_BLOCK_SPELL.get().getSpellResource()); // 在目标上方生成冰块砸落
        attackTag.addOptional(SpellRegistry.ICE_SPIKES_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.ICICLE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.RAY_OF_FROST_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SNOWBALL_SPELL.get().getSpellResource());
        movementTag.addOptional(SpellRegistry.FROST_STEP_SPELL.get().getSpellResource());
        supportTag.addOptional(SpellRegistry.ICE_TOMB_SPELL.get().getSpellResource()); // 控制法术，对敌人施放
        attackTag.addOptional(SpellRegistry.SUMMON_POLAR_BEAR_SPELL.get().getSpellResource());
        summonTag.addOptional(SpellRegistry.SUMMON_POLAR_BEAR_SPELL.get().getSpellResource());

        // ========== Iron's Spellbooks - Lightning ==========
        attackTag.addOptional(SpellRegistry.BALL_LIGHTNING_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.CHAIN_LIGHTNING_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.ELECTROCUTE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.LIGHTNING_BOLT_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.LIGHTNING_LANCE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.SHOCKWAVE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.THUNDERSTORM_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.VOLT_STRIKE_SPELL.get().getSpellResource());
        movementTag.addOptional(SpellRegistry.ASCENSION_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.CHARGE_SPELL.get().getSpellResource());

        // ========== Iron's Spellbooks - Nature ==========
        attackTag.addOptional(SpellRegistry.ACID_ORB_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.BLIGHT_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.EARTHQUAKE_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.FIREFLY_SWARM_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.POISON_ARROW_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.POISON_BREATH_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.POISON_SPLASH_SPELL.get().getSpellResource());
        attackTag.addOptional(SpellRegistry.STOMP_SPELL.get().getSpellResource());
        supportTag.addOptional(SpellRegistry.GLUTTONY_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.OAKSKIN_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.SPIDER_ASPECT_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistry.ROOT_SPELL.get().getSpellResource());

        // ========== Aeromancy Additions ==========
        attackTag.addOptional(AASpells.WIND_CHARGE.get().getSpellResource());
        attackTag.addOptional(AASpells.ASPHYXIATE.get().getSpellResource());
        attackTag.addOptional(AASpells.WIND_BLADE.get().getSpellResource());
        defenseTag.addOptional(AASpells.AIRSTEP.get().getSpellResource());
        defenseTag.addOptional(AASpells.WIND_SHIELD.get().getSpellResource());
        defenseTag.addOptional(AASpells.AIRBLAST.get().getSpellResource());
        defenseTag.addOptional(AASpells.FLUSH.get().getSpellResource());
        movementTag.addOptional(AASpells.DASH.get().getSpellResource());
        positiveEffectTag.addOptional(AASpells.FEATHER_FALL.get().getSpellResource());

        // ========== Cataclysm Spellbooks ==========
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.VOID_BEAM.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSSAL_BLAST.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.DIMENSIONAL_RIFT.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.DEPTH_CHARGE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSSAL_SLASH.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.TIDAL_GRAB.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.VOID_RUNE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.VOID_BULWARK.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.INCINERATION.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.INFERNAL_STRIKE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.HELLISH_BLADE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.BONE_STORM.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.BONE_PIERCE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ASHEN_BREATH.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSS_FIREBALL.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.TECTONIC_TREMBLE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.MALEVOLENT_BATTLEFIELD.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.SANDSTORM.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.DESERT_WINDS.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.MONOLITH_CRASH.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.AMETHYST_PUNCTURE.get().getSpellResource());
        defenseTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSSAL_PREDATOR.get().getSpellResource());
        movementTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CURSED_RUSH.get().getSpellResource());
        negativeEffectTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.GRAVITY_STORM.get().getSpellResource());
        negativeEffectTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.PILFER.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_KOBOLDIATOR.get().getSpellResource());
        summonTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_KOBOLDIATOR.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_KOBOLETON.get().getSpellResource());
        summonTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_KOBOLETON.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_IGNITED_REINFORCEMENT.get().getSpellResource());
        summonTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_IGNITED_REINFORCEMENT.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_THRALL.get().getSpellResource());
        summonTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_THRALL.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_AMETHYST_CRAB.get().getSpellResource());
        summonTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_AMETHYST_CRAB.get().getSpellResource());

        // ========== Discerning the Eldritch ==========
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.VEIN_RIPPER.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.ESOTERIC_EDGE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.ESOTERIC_STRIKE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.GUARDIANS_GAZE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.SOUL_SLICE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.SOUL_SET_ABLAZE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.GLACIAL_EDGE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.BLADES_OF_RANCOR.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.ZEALOUS_HARBINGER.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.LIBRAS_JUDGEMENT.get().getSpellResource());
        defenseTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.ABRACADABRA.get().getSpellResource());
        defenseTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.RAVENOUS_REVENANT.get().getSpellResource());
        movementTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.OTHERWORLDLY_PRESENCE.get().getSpellResource());
        movementTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.RIFT_WALKER.get().getSpellResource());
        movementTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.BOOGIE_WOOGIE.get().getSpellResource());
        supportTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.MEND_FLESH.get().getSpellResource());
        negativeEffectTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.SILENCE.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.CONJURE_FORSAKE_AID.get().getSpellResource());
        summonTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.CONJURE_FORSAKE_AID.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.CONJURE_GAOLER.get().getSpellResource());
        summonTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.CONJURE_GAOLER.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.CALL_ASCENDED_ONE.get().getSpellResource());
        summonTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.CALL_ASCENDED_ONE.get().getSpellResource());

        // ========== Dreamless Spells ==========
        defenseTag.addOptional(SpellRegistries.JADESKIN.get().getSpellResource());

        // ========== Enders Spells Requiem ==========
        attackTag.addOptional(GGSpellRegistry.CLAW.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.WRETCH.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.BOILING_BLOOD.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.NECROTIC_BURST.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.DECAYING_WILL.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.SPIKES_OF_AGONY.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.TENTACLE_WHIP.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.PALE_FLAME.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.TWILIGHT_ASSAULT.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.DAMNATION.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.LORD_OF_FROST.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.CATAPHRACT_TACKLE.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.CATAPHRACT_SLAM.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.UNDEAD_PACT.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.STRAIN.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.REAPER.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.EBONY_ARMOR.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.PROTECTION_OF_THE_FALLEN.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.CURSED_IMMORTALITY.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.EBONY_CATAPHRACT.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.BASTION_OF_LIGHT.get().getSpellResource());
        supportTag.addOptional(GGSpellRegistry.CATAPHRACT_HEAL.get().getSpellResource());
        negativeEffectTag.addOptional(GGSpellRegistry.FINALITY_OF_DECAY.get().getSpellResource());
        negativeEffectTag.addOptional(GGSpellRegistry.ETERNAL_BATTLEFIELD.get().getSpellResource());
        attackTag.addOptional(GGSpellRegistry.SKULLS.get().getSpellResource());
        summonTag.addOptional(GGSpellRegistry.SKULLS.get().getSpellResource());

        // ========== Fires Ender Expansion ==========
        attackTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.PARTIAL_TELEPORT.get().getSpellResource());
        attackTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.HOLLOW_CRYSTAL.get().getSpellResource());
        attackTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.OBSIDIAN_ROD.get().getSpellResource());
        attackTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.DRAGONS_FURY.get().getSpellResource());
        attackTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.GATE_OF_ENDER.get().getSpellResource());
        attackTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.BINARY_STARS.get().getSpellResource());
        defenseTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.ASPECT_OF_THE_SHULKER.get().getSpellResource());
        defenseTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.DIMENSIONAL_ADAPTATION.get().getSpellResource());
        movementTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.SCINTILLATING_STRIDE.get().getSpellResource());
        negativeEffectTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.INFINITE_VOID.get().getSpellResource());
        negativeEffectTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.DISPLACEMENT_CAGE.get().getSpellResource());

        // ========== Geomancy Plus ==========
        attackTag.addOptional(GGSpells.FISSURE_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.TREMOR_SPIKE_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.ERODING_BOULDER_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.CHUNKER_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.DRIPSTONE_BOLT.get().getSpellResource());
        attackTag.addOptional(GGSpells.PILLAR_OF_THE_RESOUNDING_EARTH.get().getSpellResource());
        attackTag.addOptional(GGSpells.PETRIVISE_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.SOLAR_STORM_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.SOLAR_BEAM_SPELL.get().getSpellResource());
        defenseTag.addOptional(GGSpells.GEO_CONDUCTOR_SPELL.get().getSpellResource());
        movementTag.addOptional(GGSpells.TREMOR_STEP_SPELL.get().getSpellResource());
        movementTag.addOptional(GGSpells.SEISMIC_SURF.get().getSpellResource());

        // ========== Magic from the East ==========
        attackTag.addOptional(MFTESpellRegistries.SWORD_DANCE_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.DRAGON_GLIDE_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.JADE_JUDGEMENT_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.PUNISHING_HEAVEN_SPELL.get().getSpellResource()); // 召唤玉刑天
        summonTag.addOptional(MFTESpellRegistries.PUNISHING_HEAVEN_SPELL.get().getSpellResource()); // 召唤玉刑天
        attackTag.addOptional(MFTESpellRegistries.NEPHRITE_SLASH_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.JADE_BULLET_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.SOUL_BURST_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.SPIRIT_CHALLENGING.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.BONE_HANDS_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.CALAMITY_CUT_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.PHANTOM_CHARGE_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.ANCHORING_KUNAI.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.SPLITTING_SHURIKEN.get().getSpellResource());
        defenseTag.addOptional(MFTESpellRegistries.BAGUA_ARRAY_CIRCLE_SPELL.get().getSpellResource());
        defenseTag.addOptional(MFTESpellRegistries.DRAPES_OF_REFLECTION_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.SOUL_CATALYST_SPELL.get().getSpellResource()); // 发射灵魂骷髅投射物
        movementTag.addOptional(MFTESpellRegistries.CLOUD_RIDE_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.JIANGSHI_INVOKE_SPELL.get().getSpellResource());
        summonTag.addOptional(MFTESpellRegistries.JIANGSHI_INVOKE_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.UNDERWORLD_AID_SPELL.get().getSpellResource()); // AOE 伤害区域，非召唤物
        attackTag.addOptional(MFTESpellRegistries.KITSUNE_PACK_SPELL.get().getSpellResource());
        summonTag.addOptional(MFTESpellRegistries.KITSUNE_PACK_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.REVENANT_OF_HONOR_SPELL.get().getSpellResource());
        summonTag.addOptional(MFTESpellRegistries.REVENANT_OF_HONOR_SPELL.get().getSpellResource());
        attackTag.addOptional(MFTESpellRegistries.ASHIGARU_SQUAD_SPELL.get().getSpellResource());
        summonTag.addOptional(MFTESpellRegistries.ASHIGARU_SQUAD_SPELL.get().getSpellResource());

        // ========== Snow Waifu Spell ==========
        attackTag.addOptional(ModSpellRegistry.SUMMON_SNOW_QUEEN_SPELL.get().getSpellResource());
        summonTag.addOptional(ModSpellRegistry.SUMMON_SNOW_QUEEN_SPELL.get().getSpellResource());

        // ========== Hazen N Stuff ==========
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.BRIMSTONE_HELLBLAST.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.SCORCHING_SLASH.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.FIERY_DAGGER.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.ICE_ARROW.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.CRYSTAL_VOLLEY.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.ENERGY_BURST.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.THORN_CHAKRAM.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.COUNTERSPELL_SPIDER_LILY.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.SHARD_SWORD.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.DEATH_SENTENCE.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.SPECTRAL_AXE.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.SYRINGE_BARRAGE.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.SHOOTING_STAR.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.STELLAR_COLLAPSE.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.TERRAPRISMIC_BARRAGE.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.NIGHTS_EDGE_STRIKE.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.SOUL_SEEKERS.get().getSpellResource());
        movementTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.CINDEROUS_STEP.get().getSpellResource());
        movementTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.CHAOTIC_TELEPORT.get().getSpellResource());
        negativeEffectTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.GOLDEN_SHOWER.get().getSpellResource());
        attackTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.CALL_FORTH_TERRAPRISMA.get().getSpellResource());
        summonTag.addOptional(net.hazen.hazennstuff.Spells.HnSSpellRegistries.CALL_FORTH_TERRAPRISMA.get().getSpellResource());

        // ========== Winefox's Spellbooks ==========
        attackTag.addOptional(InitSpells.SUMMON_MAID_SPELL.get().getSpellResource());
        summonTag.addOptional(InitSpells.SUMMON_MAID_SPELL.get().getSpellResource());

        // ========== Maid Should Recast Spells (需要多次施放的法术) ==========
        // Iron's Spellbooks
        maidShouldRecastTag.addOptional(SpellRegistry.ELDRITCH_BLAST_SPELL.get().getSpellResource());
        maidShouldRecastTag.addOptional(SpellRegistry.FLAMING_BARRAGE_SPELL.get().getSpellResource());
        maidShouldRecastTag.addOptional(SpellRegistry.WALL_OF_FIRE_SPELL.get().getSpellResource());
        maidShouldRecastTag.addOptional(SpellRegistry.RAISE_HELL_SPELL.get().getSpellResource());
        // Geomancy Plus
        maidShouldRecastTag.addOptional(GGSpells.GEO_CONDUCTOR_SPELL.get().getSpellResource());
        maidShouldRecastTag.addOptional(GGSpells.TREMOR_SPIKE_SPELL.get().getSpellResource());
        // Enders Spells Requiem
        maidShouldRecastTag.addOptional(GGSpellRegistry.CLAW.get().getSpellResource());
        maidShouldRecastTag.addOptional(GGSpellRegistry.BOILING_BLOOD.get().getSpellResource());
        maidShouldRecastTag.addOptional(GGSpellRegistry.TWILIGHT_ASSAULT.get().getSpellResource());
        // Fires Ender Expansion
        maidShouldRecastTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.HOLLOW_CRYSTAL.get().getSpellResource());
        maidShouldRecastTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.BINARY_STARS.get().getSpellResource());
        maidShouldRecastTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.SCINTILLATING_STRIDE.get().getSpellResource());
        // Discerning the Eldritch
        maidShouldRecastTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.RIFT_WALKER.get().getSpellResource());
        maidShouldRecastTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.BOOGIE_WOOGIE.get().getSpellResource());
        maidShouldRecastTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.BLADES_OF_RANCOR.get().getSpellResource());
        // Magic from the East
        maidShouldRecastTag.addOptional(MFTESpellRegistries.ANCHORING_KUNAI.get().getSpellResource());
        maidShouldRecastTag.addOptional(MFTESpellRegistries.BONE_HANDS_SPELL.get().getSpellResource());
        // Cataclysm Spellbooks
        maidShouldRecastTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.VOID_BEAM.get().getSpellResource());
        maidShouldRecastTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.BONE_PIERCE.get().getSpellResource());
        maidShouldRecastTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.AMETHYST_PUNCTURE.get().getSpellResource());
        maidShouldRecastTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSS_FIREBALL.get().getSpellResource());
    }
}
