package net.magicterra.winefoxsspellbooks.datagen;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.AeromancyAdditionsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.CataclysmSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.DiscerningTheEldritchTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.DreamlessSpellsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.EndersSpellsRequiemTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.FiresEnderExpansionTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.GeomancyPlusTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.HazenNStuffTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.IronsSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.MagicFromTheEastTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.SnowWaifuSpellTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.SpellTagContext;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.WinefoxsSpellbooksTags;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/**
 * 根据法术的特征，生成标签。
 *
 * <p>每个 addon 的标签放在 {@code datagen.spelltag} 子包里独立成文件，
 * 这样多 agent 可以并行更新不同 addon 而不互相阻塞。新增 addon 时新建文件即可，不要追加到现有文件。
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
        var ctx = new SpellTagContext(
            tag(MaidSpellRegistry.ATTACK_SPELLS_TAG),
            tag(MaidSpellRegistry.DEFENSE_SPELLS_TAG),
            tag(MaidSpellRegistry.MOVEMENT_SPELLS_TAG),
            tag(MaidSpellRegistry.SUPPORT_SPELLS_TAG),
            tag(MaidSpellRegistry.POSITIVE_EFFECT_SPELLS_TAG),
            tag(MaidSpellRegistry.SUPPORT_EFFECT_SPELLS_TAG),
            tag(MaidSpellRegistry.NEGATIVE_EFFECT_SPELLS_TAG),
            tag(MaidSpellRegistry.SUMMON_SPELLS_TAG),
            tag(MaidSpellRegistry.MAID_SHOULD_RECAST_SPELLS_TAG)
        );

        AeromancyAdditionsTags.contribute(ctx);
        CataclysmSpellbooksTags.contribute(ctx);
        DiscerningTheEldritchTags.contribute(ctx);
        DreamlessSpellsTags.contribute(ctx);
        EndersSpellsRequiemTags.contribute(ctx);
        FiresEnderExpansionTags.contribute(ctx);
        GeomancyPlusTags.contribute(ctx);
        HazenNStuffTags.contribute(ctx);
        IronsSpellbooksTags.contribute(ctx);
        MagicFromTheEastTags.contribute(ctx);
        SnowWaifuSpellTags.contribute(ctx);
        WinefoxsSpellbooksTags.contribute(ctx);
    }
}
