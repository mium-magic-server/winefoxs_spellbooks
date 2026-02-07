package net.magicterra.winefoxsspellbooks.datagen;

import com.gametechbc.gtbcs_geomancy_plus.init.GGEffects;
import com.gametechbc.gtbcs_geomancy_plus.init.GGSpells;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.snackpirate.aeromancy.spells.AASpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.Iceforkkk.DreamlessAditions.effect.DSSEffects;
import net.Iceforkkk.DreamlessAditions.registries.SpellRegistries;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSPotionEffectRegistry;
import net.acetheeldritchking.discerning_the_eldritch.registries.DTEPotionEffectRegistry;
import net.ender.ess_requiem.registries.GGEffectRegistry;
import net.ender.ess_requiem.registries.GGSpellRegistry;
import net.fireofpower.firesenderexpansion.registries.EffectRegistry;
import net.hazen.hazennstuff.Registries.HnSEffects;
import net.hazen.hazennstuff.Spells.HnSSpellRegistries;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.data.SpellCastingRangeData;
import net.magicterra.winefoxsspellbooks.magic.data.SpellCausedEffectData;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffects;
import net.warphan.iss_magicfromtheeast.registries.MFTEEffectRegistries;
import net.warphan.iss_magicfromtheeast.registries.MFTESpellRegistries;

/**
 * 法术数据生成器
 * 生成默认的法术施法范围和法术导致效果的 JSON 文件
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
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // 生成施法范围数据
        futures.add(saveCastingRangeData(cache, createDefaultCastingRanges()));

        // 生成法术导致效果数据
        futures.add(saveCausedEffectData(cache, createDefaultCausedEffects()));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveCastingRangeData(CachedOutput cache, SpellCastingRangeData data) {
        Path path = output.getOutputFolder()
            .resolve("data")
            .resolve(WinefoxsSpellbooks.MODID)
            .resolve("magic_maid_spell_data")
            .resolve("default_casting_range.json");

        var result = SpellCastingRangeData.CODEC.encodeStart(JsonOps.INSTANCE, data);

        if (result.error().isPresent()) {
            WinefoxsSpellbooks.LOGGER.error("Failed to encode spell casting range data: {}",
                result.error().get().message());
            return CompletableFuture.completedFuture(null);
        }

        JsonElement json = result.result().orElseThrow();
        return DataProvider.saveStable(cache, json, path);
    }

    private CompletableFuture<?> saveCausedEffectData(CachedOutput cache, SpellCausedEffectData data) {
        Path path = output.getOutputFolder()
            .resolve("data")
            .resolve(WinefoxsSpellbooks.MODID)
            .resolve("magic_maid_spell_data")
            .resolve("default_caused_effect.json");

        var result = SpellCausedEffectData.CODEC.encodeStart(JsonOps.INSTANCE, data);

        if (result.error().isPresent()) {
            WinefoxsSpellbooks.LOGGER.error("Failed to encode spell caused effect data: {}",
                result.error().get().message());
            return CompletableFuture.completedFuture(null);
        }

        JsonElement json = result.result().orElseThrow();
        return DataProvider.saveStable(cache, json, path);
    }

    @Override
    public String getName() {
        return "Spell Data";
    }

    /**
     * 创建默认的施法范围数据
     * 分析攻击法术的距离限制，需要近战的设置为2，没有特殊限制的忽略
     */
    private SpellCastingRangeData createDefaultCastingRanges() {
        return SpellCastingRangeData.builder()
            // ========== Iron's Spellbooks - 近战攻击法术 ==========
            // DIVINE_SMITE: 在施法者前方 1.7 格位置触发 AOE，范围 2.2*2 = 4.4 格
            .add(SpellRegistry.DIVINE_SMITE_SPELL.get().getSpellResource(), 2F)
            // FLAMING_STRIKE: 在施法者前方 1.9 格位置触发，检测距离 3.25 格
            .add(SpellRegistry.FLAMING_STRIKE_SPELL.get().getSpellResource(), 2F)

            // ========== Iron's Spellbooks - 支援法术范围限制 ==========
            .add(SpellRegistry.CLEANSE_SPELL.get().getSpellResource(), 3F)
            .add(SpellRegistry.HEALING_CIRCLE_SPELL.get().getSpellResource(), 5F)

            // ========== Discerning the Eldritch - 近战攻击法术 ==========
            // ESOTERIC_STRIKE: 在施法者前方 1.2 格位置触发，检测距离 2.15 格
            .add(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.ESOTERIC_STRIKE.get().getSpellResource(), 2F)
            // SOUL_SLICE: 在施法者前方 1.9 格位置触发，检测距离 3.25 格
            .add(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.SOUL_SLICE.get().getSpellResource(), 2F)

            // ========== Cataclysm Spellbooks - 近战攻击法术 ==========
            // ABYSSAL_SLASH: 在施法者前方 2 格位置触发，检测距离 4.1 格
            .add(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSSAL_SLASH.get().getSpellResource(), 2F)

            // ========== Enders Spells Requiem - 近战攻击法术 ==========
            // CLAW (Rip and Tear): 在施法者前方 1.9 格位置触发，检测距离 3.25 格
            .add(GGSpellRegistry.CLAW.get().getSpellResource(), 2F)
            // CATAPHRACT_SLAM: 在施法者前方触发 AOE，需要近战
            .add(GGSpellRegistry.CATAPHRACT_SLAM.get().getSpellResource(), 2F)
            // CATAPHRACT_TACKLE: 在施法者前方 1.9 格位置触发，检测距离 3.25 格
            .add(GGSpellRegistry.CATAPHRACT_TACKLE.get().getSpellResource(), 2F)

            // ========== Hazen N Stuff - 近战攻击法术 ==========
            // SCORCHING_SLASH: 在施法者前方 1.9 格位置触发，检测距离 3.25 格（同时发射投射物）
            .add(HnSSpellRegistries.SCORCHING_SLASH.get().getSpellResource(), 2F)
            // NIGHTS_EDGE_STRIKE: 在施法者前方 1.9 格位置触发，检测距离 3.25 格（同时发射投射物）
            .add(HnSSpellRegistries.NIGHTS_EDGE_STRIKE.get().getSpellResource(), 2F)

            // ========== 默认远程距离 ==========
            .add(SpellRegistry.none().getSpellResource(), 15F)
            .build();
    }

    /**
     * 创建默认的法术导致效果数据
     * 使用类型安全的注册表引用，确保编译时检查
     */
    private SpellCausedEffectData createDefaultCausedEffects() {
        return SpellCausedEffectData.builder()
            // ========== Iron's Spellbooks - Defense ==========
            .add(SpellRegistry.HEARTSTOP_SPELL.get().getSpellResource(), MobEffectRegistry.HEARTSTOP.getId())
            .add(SpellRegistry.ECHOING_STRIKES_SPELL.get().getSpellResource(), MobEffectRegistry.ECHOING_STRIKES.getId())
            .add(SpellRegistry.EVASION_SPELL.get().getSpellResource(), MobEffectRegistry.EVASION.getId())
            .add(SpellRegistry.INVISIBILITY_SPELL.get().getSpellResource(), MobEffectRegistry.TRUE_INVISIBILITY.getId())
            .add(SpellRegistry.CHARGE_SPELL.get().getSpellResource(), MobEffectRegistry.CHARGED.getId())
            .add(SpellRegistry.SPIDER_ASPECT_SPELL.get().getSpellResource(), MobEffectRegistry.SPIDER_ASPECT.getId())
            .add(SpellRegistry.OAKSKIN_SPELL.get().getSpellResource(), MobEffectRegistry.OAKSKIN.getId())
            .add(SpellRegistry.ABYSSAL_SHROUD_SPELL.get().getSpellResource(), MobEffectRegistry.ABYSSAL_SHROUD.getId())

            // ========== Iron's Spellbooks - Support ==========
            .add(SpellRegistry.GLUTTONY_SPELL.get().getSpellResource(), MobEffectRegistry.GLUTTONY.getId())

            // ========== Iron's Spellbooks - Positive Effect ==========
            .add(SpellRegistry.FORTIFY_SPELL.get().getSpellResource(), MobEffectRegistry.FORTIFY.getId())
            .add(SpellRegistry.HASTE_SPELL.get().getSpellResource(), MobEffectRegistry.HASTENED.getId())

            // ========== Iron's Spellbooks - Negative Effect ==========
            .add(SpellRegistry.SLOW_SPELL.get().getSpellResource(), MobEffectRegistry.SLOWED.getId())
            .add(SpellRegistry.HEAT_SURGE_SPELL.get().getSpellResource(), MobEffectRegistry.REND.getId())

            // ========== Iron's Spellbooks - Attack (有药水效果的攻击法术) ==========
            .add(SpellRegistry.FROSTWAVE_SPELL.get().getSpellResource(), MobEffectRegistry.CHILLED.getId())
            .add(SpellRegistry.BLIGHT_SPELL.get().getSpellResource(), MobEffectRegistry.BLIGHT.getId())
            .add(SpellRegistry.FROSTBITE_SPELL.get().getSpellResource(), MobEffectRegistry.FROSTBITTEN_STRIKES.getId())

            // ========== Aeromancy Additions - Defense ==========
            .add(AASpells.AIRSTEP.get().getSpellResource(), AASpells.MobEffects.AIRSTEPPING.getId())
            .add(AASpells.WIND_SHIELD.get().getSpellResource(), AASpells.MobEffects.WIND_SHIELD.getId())

            // ========== Aeromancy Additions - Positive Effect ==========
            .add(AASpells.FEATHER_FALL.get().getSpellResource(), AASpells.MobEffects.FLIGHT.getId())

            // ========== Aeromancy Additions - Attack ==========
            .add(AASpells.ASPHYXIATE.get().getSpellResource(), AASpells.MobEffects.BREATHLESS.getId())

            // ========== GTBCS Geomancy Plus - Defense ==========
            // GEO_CONDUCTOR 通过召唤 ResonatorEntity 间接给范围内友军施加 AEGIS_EFFECT
            .add(GGSpells.GEO_CONDUCTOR_SPELL.get().getSpellResource(), GGEffects.AEGIS_EFFECT.getId())

            // ========== GTBCS Geomancy Plus - Movement ==========
            .add(GGSpells.TREMOR_STEP_SPELL.get().getSpellResource(), GGEffects.TREMOR_STEP_EFFECT.getId())

            // ========== Dreamless Spells - Defense ==========
            .add(SpellRegistries.JADESKIN.get().getSpellResource(), DSSEffects.JADESKIN_EFFECT.getKey().location())

            // ========== Cataclysm Spellbooks - Defense ==========
            .add(net.acetheeldritchking.cataclysm_spellbooks.registries.SpellRegistries.ABYSSAL_PREDATOR.get().getSpellResource(),
                CSPotionEffectRegistry.ABYSSAL_PREDATOR_EFFECT.getId())

            // ========== Discerning the Eldritch - Support ==========
            .add(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.MEND_FLESH.get().getSpellResource(),
                DTEPotionEffectRegistry.MEND_FLESH_EFFECT.getId())

            // ========== Discerning the Eldritch - Defense ==========
            .add(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.ABRACADABRA.get().getSpellResource(),
                DTEPotionEffectRegistry.ABRACADABRA_EFFECT.getId())
            .add(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.RAVENOUS_REVENANT.get().getSpellResource(),
                DTEPotionEffectRegistry.PREDATOR_POTION_EFFECT.getId())

            // ========== Discerning the Eldritch - Negative Effect ==========
            .add(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.SILENCE.get().getSpellResource(),
                DTEPotionEffectRegistry.SILENCE_POTION_EFFECT.getId())

            // ========== Discerning the Eldritch - Movement (有药水效果的移动法术) ==========
            .add(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.BOOGIE_WOOGIE.get().getSpellResource(),
                MobEffects.CONFUSION.getKey().location())

            // ========== Discerning the Eldritch - Attack (有药水效果的攻击法术) ==========
            .add(net.acetheeldritchking.discerning_the_eldritch.registries.SpellRegistries.GUARDIANS_GAZE.get().getSpellResource(),
                MobEffects.DIG_SLOWDOWN.getKey().location())

            // ========== ESS Requiem - Defense ==========
            .add(GGSpellRegistry.UNDEAD_PACT.get().getSpellResource(),
                GGEffectRegistry.UNDEAD_PACT.getId())
            .add(GGSpellRegistry.STRAIN.get().getSpellResource(),
                GGEffectRegistry.STRAINED.getId())
            .add(GGSpellRegistry.REAPER.get().getSpellResource(),
                GGEffectRegistry.REAPER.getId())
            .add(GGSpellRegistry.EBONY_ARMOR.get().getSpellResource(),
                GGEffectRegistry.EBONY_ARMOR.getId())
            .add(GGSpellRegistry.PROTECTION_OF_THE_FALLEN.get().getSpellResource(),
                GGEffectRegistry.PROTECTION_OF_ASHES.getId())
            .add(GGSpellRegistry.CURSED_IMMORTALITY.get().getSpellResource(),
                GGEffectRegistry.CURSED_IMMORTALITY.getId())
            .add(GGSpellRegistry.EBONY_CATAPHRACT.get().getSpellResource(),
                GGEffectRegistry.EBONY_CATAPHRACT.getId())
            .add(GGSpellRegistry.BASTION_OF_LIGHT.get().getSpellResource(),
                GGEffectRegistry.BASTION_OF_LIGHT.getId())

            // ========== ESS Requiem - Support ==========
            .add(GGSpellRegistry.CATAPHRACT_HEAL.get().getSpellResource(),
                MobEffects.REGENERATION.getKey().location())

            // ========== ESS Requiem - Negative Effect ==========
            .add(GGSpellRegistry.FINALITY_OF_DECAY.get().getSpellResource(),
                GGEffectRegistry.FINALITY_OF_DECAY.getId())
            // ETERNAL_BATTLEFIELD 通过 EternalBattlefield 实体给范围内目标施加 CURSED_IMMORTALITY 效果
            .add(GGSpellRegistry.ETERNAL_BATTLEFIELD.get().getSpellResource(),
                GGEffectRegistry.CURSED_IMMORTALITY.getId())

            // ========== Fires Ender Expansion - Defense ==========
            .add(net.fireofpower.firesenderexpansion.registries.SpellRegistries.ASPECT_OF_THE_SHULKER.get().getSpellResource(),
                EffectRegistry.ASPECT_OF_THE_SHULKER_EFFECT.getId())
            // DIMENSIONAL_ADAPTATION 根据维度施加不同效果：主世界夜视、下界抗火、末地缓降、口袋维度饱和
            // 以下界抗火为主要效果（最常用且影响最大）
            .add(net.fireofpower.firesenderexpansion.registries.SpellRegistries.DIMENSIONAL_ADAPTATION.get().getSpellResource(),
                MobEffects.FIRE_RESISTANCE.getKey().location())

            // ========== Fires Ender Expansion - Negative Effect ==========
            .add(net.fireofpower.firesenderexpansion.registries.SpellRegistries.INFINITE_VOID.get().getSpellResource(),
                EffectRegistry.INFINITE_VOID_EFFECT.getId())

            // ========== Magic from the East - Defense ==========
            // BAGUA_ARRAY_CIRCLE 创建八卦阵区域，给施法者施加 REVERSAL_HEALING 效果（伤害转化为治疗）
            .add(MFTESpellRegistries.BAGUA_ARRAY_CIRCLE_SPELL.get().getSpellResource(),
                MFTEEffectRegistries.REVERSAL_HEALING.getId())

            // ========== Hazen N Stuff - Negative Effect ==========
            // GOLDEN_SHOWER 发射 IchorStream 弹射物，命中目标施加 ICHOR 效果（降低护甲、护甲韧性、法术抗性）
            .add(HnSSpellRegistries.GOLDEN_SHOWER.get().getSpellResource(),
                HnSEffects.ICHOR.getId())

            .build();
    }
}
