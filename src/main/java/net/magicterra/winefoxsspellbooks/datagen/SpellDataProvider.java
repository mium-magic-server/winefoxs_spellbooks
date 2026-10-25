package net.magicterra.winefoxsspellbooks.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.AeromancyAdditionsData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.AetherSpellbooksData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.AlshanexsFamiliarsData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.ApexArcanaData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.ApprenticesCodexData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.AsterismArcanumData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.CataclysmSpellbooksData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.ChronomancyData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.CsCurseSpellsData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.CursedSchoolData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.DiscerningTheEldritchData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.DreamlessSpellsData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.EchoingMagicData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.EldritchExpansionsData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.EndersSpellsRequiemData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.EsEsotericSpellsData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.FarmersSpellData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.FiresEnderExpansionData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.FrozenDominionSpellsData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.GeomancyPlusData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.GtbcsSpellbooksData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.HazenNStuffData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.HyperNHieisShenanigansData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.IronsSpellbooksData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.IssEldenRingData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.LeylinesData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.MagicFromTheEastData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.MonstersSpellbooksData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.OzymandiasSundriesData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.PaladinSpellsData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.PeyrosScytheData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.SomakeSpellsData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.SpellDataContext;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.SpellbooksOfTwilightData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.TouhouLittleMaidSpellData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.UnstableMagicData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.WindsSpellbooksData;
import net.magicterra.winefoxsspellbooks.datagen.spelldata.WinefoxsSpellbooksData;
import net.magicterra.winefoxsspellbooks.magic.data.SpellCastingRangeData;
import net.magicterra.winefoxsspellbooks.magic.data.SpellCausedEffectData;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/**
 * 法术数据生成器：生成默认的施法范围与法术导致效果 JSON。
 *
 * <p>每个 addon 的数据放在 {@code datagen.spelldata} 子包里独立成文件，
 * 这样多 agent 可以并行更新不同 addon 而不互相阻塞。新增 addon 时新建文件即可，不要追加到现有文件。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-31
 */
public class SpellDataProvider implements DataProvider {
    private final PackOutput output;

    public SpellDataProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var rangeBuilder = SpellCastingRangeData.builder();
        var effectBuilder = SpellCausedEffectData.builder();
        var ctx = new SpellDataContext(rangeBuilder, effectBuilder);

        AeromancyAdditionsData.contribute(ctx);
        AetherSpellbooksData.contribute(ctx);
        AlshanexsFamiliarsData.contribute(ctx);
        ApexArcanaData.contribute(ctx);
        ApprenticesCodexData.contribute(ctx);
        AsterismArcanumData.contribute(ctx);
        CataclysmSpellbooksData.contribute(ctx);
        ChronomancyData.contribute(ctx);
        CsCurseSpellsData.contribute(ctx);
        CursedSchoolData.contribute(ctx);
        DiscerningTheEldritchData.contribute(ctx);
        DreamlessSpellsData.contribute(ctx);
        EchoingMagicData.contribute(ctx);
        EldritchExpansionsData.contribute(ctx);
        EndersSpellsRequiemData.contribute(ctx);
        EsEsotericSpellsData.contribute(ctx);
        FarmersSpellData.contribute(ctx);
        FiresEnderExpansionData.contribute(ctx);
        FrozenDominionSpellsData.contribute(ctx);
        GeomancyPlusData.contribute(ctx);
        GtbcsSpellbooksData.contribute(ctx);
        HazenNStuffData.contribute(ctx);
        HyperNHieisShenanigansData.contribute(ctx);
        IronsSpellbooksData.contribute(ctx);
        IssEldenRingData.contribute(ctx);
        LeylinesData.contribute(ctx);
        MagicFromTheEastData.contribute(ctx);
        MonstersSpellbooksData.contribute(ctx);
        OzymandiasSundriesData.contribute(ctx);
        PaladinSpellsData.contribute(ctx);
        PeyrosScytheData.contribute(ctx);
        SomakeSpellsData.contribute(ctx);
        SpellbooksOfTwilightData.contribute(ctx);
        TouhouLittleMaidSpellData.contribute(ctx);
        UnstableMagicData.contribute(ctx);
        WindsSpellbooksData.contribute(ctx);
        WinefoxsSpellbooksData.contribute(ctx);

        return CompletableFuture.allOf(
            saveJson(cache, SpellCastingRangeData.CODEC, rangeBuilder.build(), "default_casting_range.json", "spell casting range data"),
            saveJson(cache, SpellCausedEffectData.CODEC, effectBuilder.build(), "default_caused_effect.json", "spell caused effect data")
        );
    }

    private <T> CompletableFuture<?> saveJson(CachedOutput cache, Codec<T> codec, T data, String filename, String desc) {
        Path path = output.getOutputFolder()
            .resolve("data")
            .resolve(WinefoxsSpellbooks.MODID)
            .resolve("magic_maid_spell_data")
            .resolve(filename);

        var result = codec.encodeStart(JsonOps.INSTANCE, data);
        if (result.error().isPresent()) {
            WinefoxsSpellbooks.LOGGER.error("Failed to encode {}: {}", desc, result.error().get().message());
            return CompletableFuture.completedFuture(null);
        }
        return DataProvider.saveStable(cache, result.result().orElseThrow(), path);
    }

    @Override
    public String getName() {
        return "Spell Data";
    }
}
