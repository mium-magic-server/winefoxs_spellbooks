package net.magicterra.winefoxsspellbooks.datagen;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.api.spell.SpellFlag;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.AeromancyAdditionsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.AetherSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.AlshanexsFamiliarsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.ApexArcanaTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.ApprenticesCodexTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.AsterismArcanumTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.CataclysmSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.ChronomancyTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.CsCurseSpellsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.CursedSchoolTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.DeeperAndDarkerSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.DiscerningTheEldritchTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.DreamlessSpellsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.EchoingMagicTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.EldritchExpansionsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.EndersSpellsRequiemTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.EsEsotericSpellsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.FarmersSpellTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.FiresEnderExpansionTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.FrierenSpellsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.FrozenDominionSpellsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.GeomancyPlusTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.GtbcsSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.HazenNStuffTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.HyperNHieisShenanigansTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.IronsSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.IssEldenRingTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.LeylinesTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.MagicFromTheEastTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.MonstersSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.OzymandiasSundriesTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.PaladinSpellsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.PeyrosScytheTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.SnowWaifuSpellTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.SomakeSpellsTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.SpellTagContext;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.SpellbooksOfTwilightTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.TouhouLittleMaidSpellTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.UnstableMagicTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.WindsSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.WinefoxsSpellbooksTags;
import net.magicterra.winefoxsspellbooks.datagen.spelltag.WizardsHelpTags;
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
            tag(MaidSpellAction.ATTACK.tag()),
            tag(MaidSpellAction.DEFENSE.tag()),
            tag(MaidSpellAction.MOVEMENT.tag()),
            tag(MaidSpellAction.SUPPORT.tag()),
            tag(MaidSpellAction.POSITIVE.tag()),
            tag(MaidSpellAction.SUPPORT_OTHER.tag()),
            tag(MaidSpellAction.NEGATIVE.tag()),
            tag(SpellFlag.SUMMON.tag()),
            tag(SpellFlag.SHOULD_RECAST.tag())
        );

        AeromancyAdditionsTags.contribute(ctx);
        AetherSpellbooksTags.contribute(ctx);
        AlshanexsFamiliarsTags.contribute(ctx);
        ApexArcanaTags.contribute(ctx);
        ApprenticesCodexTags.contribute(ctx);
        AsterismArcanumTags.contribute(ctx);
        CataclysmSpellbooksTags.contribute(ctx);
        ChronomancyTags.contribute(ctx);
        CsCurseSpellsTags.contribute(ctx);
        CursedSchoolTags.contribute(ctx);
        DeeperAndDarkerSpellbooksTags.contribute(ctx);
        DiscerningTheEldritchTags.contribute(ctx);
        DreamlessSpellsTags.contribute(ctx);
        EchoingMagicTags.contribute(ctx);
        EldritchExpansionsTags.contribute(ctx);
        EndersSpellsRequiemTags.contribute(ctx);
        EsEsotericSpellsTags.contribute(ctx);
        FarmersSpellTags.contribute(ctx);
        FiresEnderExpansionTags.contribute(ctx);
        FrierenSpellsTags.contribute(ctx);
        FrozenDominionSpellsTags.contribute(ctx);
        GeomancyPlusTags.contribute(ctx);
        GtbcsSpellbooksTags.contribute(ctx);
        HazenNStuffTags.contribute(ctx);
        HyperNHieisShenanigansTags.contribute(ctx);
        IronsSpellbooksTags.contribute(ctx);
        IssEldenRingTags.contribute(ctx);
        LeylinesTags.contribute(ctx);
        MagicFromTheEastTags.contribute(ctx);
        MonstersSpellbooksTags.contribute(ctx);
        OzymandiasSundriesTags.contribute(ctx);
        PaladinSpellsTags.contribute(ctx);
        PeyrosScytheTags.contribute(ctx);
        SnowWaifuSpellTags.contribute(ctx);
        SomakeSpellsTags.contribute(ctx);
        SpellbooksOfTwilightTags.contribute(ctx);
        TouhouLittleMaidSpellTags.contribute(ctx);
        UnstableMagicTags.contribute(ctx);
        WindsSpellbooksTags.contribute(ctx);
        WinefoxsSpellbooksTags.contribute(ctx);
        WizardsHelpTags.contribute(ctx);
    }
}
