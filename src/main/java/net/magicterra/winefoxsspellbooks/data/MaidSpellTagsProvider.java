package net.magicterra.winefoxsspellbooks.data;

import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

/**
 * 根据法术的描述，生成标签
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-26 00:33
 */
public class MaidSpellTagsProvider extends IntrinsicHolderTagsProvider<AbstractSpell> {
    private final static Logger logger = LogUtils.getLogger();

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
            if (descriptions.contains("damage") || descriptions.contains("base_damage") || descriptions.contains("aoe_damage") || descriptions.contains("impact_damage")) {
                // 描述：伤害
                attackTag.add(abstractSpell);
            } else if (descriptions.contains("summon_count")) {
                // 召唤术，也算作攻击
                attackTag.add(abstractSpell);
            } else if (descriptions.contains("effect_length") ||  descriptions.contains("absorption")) {
                // 描述：持续时间，伤害吸收
                defenseTag.add(abstractSpell);
            } else if (descriptions.contains("distance")) {
                // 描述：距离
                movementTag.add(abstractSpell);
            } else if (descriptions.contains("healing") || descriptions.contains("greater_healing") || descriptions.contains("aoe_healing")) {
                // 描述：治疗
                supportTag.add(abstractSpell);
            } else if ("irons_spellbooks:evasion".equals(spellId)) {
                // 闪避术
                movementTag.add(abstractSpell);
            } else if ("irons_spellbooks:shield".equals(spellId)) {
                // 护盾
                defenseTag.add(abstractSpell);
            } else if ("irons_spellbooks:wololo".equals(spellId)) {
                // Wololo!
                attackTag.add(abstractSpell);
            } else if ("irons_spellbooks:cleanse".equals(spellId)) {
                // 净化
                defenseTag.add(abstractSpell);
            } else if ("irons_spellbooks:snowball".equals(spellId)) {
                // 雪球
                attackTag.add(abstractSpell);
            } else {
                logger.warn("Unknown spell: {}, [{}]", spellId, String.join(",", descriptions));
            }
        }
    }
}
