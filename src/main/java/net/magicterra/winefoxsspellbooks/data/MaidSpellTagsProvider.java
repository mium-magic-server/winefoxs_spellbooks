package net.magicterra.winefoxsspellbooks.data;

import com.google.common.collect.Sets;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * 根据法术的描述，生成标签
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

        // 物理攻击法术 （词条包含伤害）
        for (AbstractSpell abstractSpell : SpellRegistry.REGISTRY) {
            Set<String> descriptions = abstractSpell.getUniqueInfo(1, null)
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
                attackTag.add(abstractSpell);
            }
        }

        // 自我增益法术
        defenseTag.add(SpellRegistry.HEARTSTOP_SPELL.get());
        defenseTag.add(SpellRegistry.ECHOING_STRIKES_SPELL.get());
        defenseTag.add(SpellRegistry.INVISIBILITY_SPELL.get());
        defenseTag.add(SpellRegistry.SHIELD_SPELL.get());
        defenseTag.add(SpellRegistry.CHARGE_SPELL.get());
        defenseTag.add(SpellRegistry.SPIDER_ASPECT_SPELL.get());
        defenseTag.add(SpellRegistry.OAKSKIN_SPELL.get());
        defenseTag.add(SpellRegistry.GLUTTONY_SPELL.get());
        defenseTag.add(SpellRegistry.ABYSSAL_SHROUD_SPELL.get());

        // 移动法术
        movementTag.add(SpellRegistry.BLOOD_STEP_SPELL.get());
        movementTag.add(SpellRegistry.EVASION_SPELL.get());
        movementTag.add(SpellRegistry.TELEPORT_SPELL.get());
        movementTag.add(SpellRegistry.FROST_STEP_SPELL.get());

        // 自我恢复法术
        supportTag.add(SpellRegistry.GREATER_HEAL_SPELL.get());
        supportTag.add(SpellRegistry.HEAL_SPELL.get());
        supportTag.add(SpellRegistry.ICE_TOMB_SPELL.get());

        // 正面效果法术
        positiveEffectTag.add(SpellRegistry.FORTIFY_SPELL.get());
        positiveEffectTag.add(SpellRegistry.HASTE_SPELL.get());
        positiveEffectTag.add(SpellRegistry.CLEANSE_SPELL.get());
        positiveEffectTag.add(SpellRegistry.FROSTBITE_SPELL.get());

        // 恢复效果法术
        supportEffectTag.add(SpellRegistry.BLESSING_OF_LIFE_SPELL.get());
        supportEffectTag.add(SpellRegistry.CLOUD_OF_REGENERATION_SPELL.get());
        supportEffectTag.add(SpellRegistry.HEALING_CIRCLE_SPELL.get());

        // 负面效果法术
        negativeEffectTag.add(SpellRegistry.SLOW_SPELL.get());
        negativeEffectTag.add(SpellRegistry.HEAT_SURGE_SPELL.get());
        negativeEffectTag.add(SpellRegistry.FROSTWAVE_SPELL.get());
        negativeEffectTag.add(SpellRegistry.ACID_ORB_SPELL.get());
        negativeEffectTag.add(SpellRegistry.BLIGHT_SPELL.get());
        negativeEffectTag.add(SpellRegistry.ROOT_SPELL.get());
    }
}
