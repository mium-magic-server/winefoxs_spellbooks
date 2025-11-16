package net.magicterra.winefoxsspellbooks.datagen;

import com.gametechbc.gtbcs_geomancy_plus.init.GGSpells;
import com.google.common.collect.Sets;
import com.rinko1231.SnowWaifuSpell.init.ModSpellRegistry;
import com.snackpirate.aeromancy.spells.AASpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.Iceforkkk.DreamlessAditions.registries.SpellRegistries;
import net.acetheeldritchking.cataclysm_spellbooks.spells.nature.AmethystPunctureSpell;
import net.ender.ess_requiem.registries.GGSpellRegistry;
import net.ender.ess_requiem.spells.blood.uncraftable.DecayingWillSpell;
import net.ender.ess_requiem.spells.ice.uncraftable.LordOfTheFinalFrostSpell;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.warphan.iss_magicfromtheeast.registries.MFTESpellRegistries;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * 根据法术的特征，生成标签
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-26 00:33
 */
public class MaidSpellTagsProvider extends IntrinsicHolderTagsProvider<AbstractSpell> {
    // 女仆不能使用的法术
    private final static Set<String> UNSUPPORTED_SPELLS = Sets.newHashSet(
        "irons_spellbooks:angel_wing",
        "irons_spellbooks:counterspell",
        "irons_spellbooks:summon_ender_chest",
        "irons_spellbooks:recall",
        "irons_spellbooks:portal",
        "irons_spellbooks:summon_horse",
        "irons_spellbooks:pocket_dimension",
        "irons_spellbooks:planar_sight",
        "irons_spellbooks:spectral_hammer",
        "irons_spellbooks:touch_dig",
        "irons_spellbooks:telekinesis"
    );

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

        List<String> unknownSpells = new ArrayList<>();
        LivingEntity fakeEntity = new LivingEntity(EntityType.ZOMBIE, null) {
            @Override
            public Iterable<ItemStack> getArmorSlots() {
                return Collections.emptyList();
            }

            @Override
            public ItemStack getItemBySlot(EquipmentSlot slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public void setItemSlot(EquipmentSlot slot, ItemStack stack) {

            }

            @Override
            public HumanoidArm getMainArm() {
                return HumanoidArm.RIGHT;
            }
        };
        // 物理攻击法术 （词条包含伤害）
        for (AbstractSpell abstractSpell : SpellRegistry.REGISTRY) {
            LivingEntity caster = null;
            if (abstractSpell instanceof DecayingWillSpell || abstractSpell instanceof LordOfTheFinalFrostSpell || abstractSpell instanceof AmethystPunctureSpell) {
                caster = fakeEntity;
            }
            Set<String> descriptions = abstractSpell.getUniqueInfo(1, caster)
                .stream()
                .map(MutableComponent::getContents)
                .filter(c -> c instanceof TranslatableContents)
                .map(TranslatableContents.class::cast)
                .map(TranslatableContents::getKey)
                .map(s -> StringUtils.removeStart(s, "ui.irons_spellbooks."))
                .collect(Collectors.toSet());
            String spellId = abstractSpell.getSpellId();
            if (UNSUPPORTED_SPELLS.contains(spellId)) {
                continue;
            }

            if (descriptions.contains("damage")
                || descriptions.contains("base_damage")
                || descriptions.contains("aoe_damage")
                || descriptions.contains("impact_damage")
                || descriptions.contains("summon_count")
                || "irons_spellbooks:wololo".equals(spellId)
                || "irons_spellbooks:snowball".equals(spellId)) {
                attackTag.addOptional(abstractSpell.getSpellResource());
            } else {
                unknownSpells.add(spellId);
            }

            int minLevelRecast = abstractSpell.getRecastCount(abstractSpell.getMinLevel(), caster);
            if (minLevelRecast > 1) {
                if (minLevelRecast == 2 && minLevelRecast == abstractSpell.getRecastCount(abstractSpell.getMaxLevel(), caster) && abstractSpell.getCastType() == CastType.LONG) {
                    summonTag.addOptional(abstractSpell.getSpellResource());
                } else {
                    maidShouldRecastTag.addOptional(abstractSpell.getSpellResource());
                }
            }
        }

        WinefoxsSpellbooks.LOGGER.info("Unknown spells: [\"{}\"]", String.join("\", \"", unknownSpells));

        // 攻击法术
        attackTag.addOptional(AASpells.WIND_CHARGE.get().getSpellResource());
        attackTag.addOptional(GGSpells.FISSURE_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.TREMOR_SPIKE_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.ERODING_BOULDER_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.CHUNKER_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.DRIPSTONE_BOLT.get().getSpellResource());
        attackTag.addOptional(GGSpells.PILLAR_OF_THE_RESOUNDING_EARTH.get().getSpellResource());
        attackTag.addOptional(GGSpells.GEO_CONDUCTOR_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.PETRIVISE_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.SOLAR_STORM_SPELL.get().getSpellResource());
        attackTag.addOptional(GGSpells.SOLAR_BEAM_SPELL.get().getSpellResource());
        attackTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.HOLLOW_CRYSTAL.get().getSpellResource());
        attackTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.OBSIDIAN_ROD.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.VOID_BEAM.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSSAL_BLAST.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.DIMENSIONAL_RIFT.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.TIDAL_GRAB.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.GRAVITY_STORM.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.PILFER.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_KOBOLDIATOR.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_KOBOLETON.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.INCINERATION.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_IGNITED_REINFORCEMENT.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSS_FIREBALL.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_THRALL.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.CONJURE_AMETHYST_CRAB.get().getSpellResource());
        attackTag.addOptional(ModSpellRegistry.SUMMON_SNOW_QUEEN_SPELL.get().getSpellResource());
        attackTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.CONJURE_FORSAKE_AID.get().getSpellResource());

        // 自我增益法术
        defenseTag.addOptional(SpellRegistry.HEARTSTOP_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.ECHOING_STRIKES_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.INVISIBILITY_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.SHIELD_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.CHARGE_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.SPIDER_ASPECT_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.OAKSKIN_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.GLUTTONY_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistry.ABYSSAL_SHROUD_SPELL.get().getSpellResource());
        defenseTag.addOptional(AASpells.WIND_SHIELD.get().getSpellResource());
        defenseTag.addOptional(AASpells.AIRBLAST.get().getSpellResource());
        defenseTag.addOptional(GGSpells.TREMOR_STEP_SPELL.get().getSpellResource());
        defenseTag.addOptional(SpellRegistries.JADESKIN.get().getSpellResource());
        defenseTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.DIMENSIONAL_ADAPTATION.get().getSpellResource());
        defenseTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.DEPTH_CHARGE.get().getSpellResource());
        defenseTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSSAL_PREDATOR.get().getSpellResource());
        defenseTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.SANDSTORM.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.UNDEAD_PACT.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.STRAIN.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.REAPER.get().getSpellResource());
        defenseTag.addOptional(GGSpellRegistry.FINALITY_OF_DECAY.get().getSpellResource());
        defenseTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.MEND_FLESH.get().getSpellResource());
        defenseTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.ABRACADABRA.get().getSpellResource());
        defenseTag.addOptional(MFTESpellRegistries.DRAPES_OF_REFLECTION.get().getSpellResource());

        // 移动法术
        movementTag.addOptional(SpellRegistry.BLOOD_STEP_SPELL.get().getSpellResource());
        movementTag.addOptional(SpellRegistry.EVASION_SPELL.get().getSpellResource());
        movementTag.addOptional(SpellRegistry.TELEPORT_SPELL.get().getSpellResource());
        movementTag.addOptional(SpellRegistry.FROST_STEP_SPELL.get().getSpellResource());
        movementTag.addOptional(AASpells.DASH.get().getSpellResource());
        movementTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.OTHERWORLDLY_PRESENCE.get().getSpellResource());

        // 自我恢复法术
        supportTag.addOptional(SpellRegistry.GREATER_HEAL_SPELL.get().getSpellResource());
        supportTag.addOptional(SpellRegistry.HEAL_SPELL.get().getSpellResource());
        supportTag.addOptional(SpellRegistry.ICE_TOMB_SPELL.get().getSpellResource());

        // 正面效果法术
        positiveEffectTag.addOptional(SpellRegistry.FORTIFY_SPELL.get().getSpellResource());
        positiveEffectTag.addOptional(SpellRegistry.HASTE_SPELL.get().getSpellResource());
        positiveEffectTag.addOptional(SpellRegistry.CLEANSE_SPELL.get().getSpellResource());
        positiveEffectTag.addOptional(SpellRegistry.FROSTBITE_SPELL.get().getSpellResource());
        positiveEffectTag.addOptional(AASpells.FEATHER_FALL.get().getSpellResource());
        positiveEffectTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.ASPECT_OF_THE_SHULKER.get().getSpellResource());
        positiveEffectTag.addOptional(GGSpellRegistry.FINALITY_OF_DECAY.get().getSpellResource());

        // 恢复效果法术
        supportEffectTag.addOptional(SpellRegistry.HEAL_SPELL.get().getSpellResource());
        supportEffectTag.addOptional(SpellRegistry.BLESSING_OF_LIFE_SPELL.get().getSpellResource());
        supportEffectTag.addOptional(SpellRegistry.CLOUD_OF_REGENERATION_SPELL.get().getSpellResource());
        supportEffectTag.addOptional(SpellRegistry.HEALING_CIRCLE_SPELL.get().getSpellResource());

        // 负面效果法术
        negativeEffectTag.addOptional(SpellRegistry.SLOW_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistry.HEAT_SURGE_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistry.FROSTWAVE_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistry.ACID_ORB_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistry.BLIGHT_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistry.ROOT_SPELL.get().getSpellResource());
        negativeEffectTag.addOptional(AASpells.UPDRAFT.get().getSpellResource());
        negativeEffectTag.addOptional(AASpells.ASPHYXIATE.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistries.DRAINED.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistries.DULLARD.get().getSpellResource());
        negativeEffectTag.addOptional(SpellRegistries.DOORWAY_EFFECT.get().getSpellResource());
        negativeEffectTag.addOptional(net.fireofpower.firesenderexpansion.registries.SpellRegistries.DISPLACEMENT_CAGE.get().getSpellResource());
        negativeEffectTag.addOptional(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.PILFER.get().getSpellResource());
        negativeEffectTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.SILENCE.get().getSpellResource());
        negativeEffectTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.BOOGIE_WOOGIE.get().getSpellResource());
        negativeEffectTag.addOptional(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.GUARDIANS_GAZE.get().getSpellResource());
        negativeEffectTag.addOptional(MFTESpellRegistries.LAUNCH_SPELL.get().getSpellResource());
    }
}
