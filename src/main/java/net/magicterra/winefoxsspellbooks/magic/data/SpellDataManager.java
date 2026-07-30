package net.magicterra.winefoxsspellbooks.magic.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.HashMap;
import java.util.Map;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;

/**
 * 法术数据管理器 负责从数据包加载法术施法范围和法术导致效果的配置
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-31
 */
public class SpellDataManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String DIRECTORY = "magic_maid_spell_data";

    private static SpellDataManager instance;

    /**
     * 法术施法范围映射（数据包 + 配置覆盖，查询用）
     */
    private Map<AbstractSpell, Float> spellRangeMap = new HashMap<>();

    /**
     * 法术导致效果映射（数据包 + 配置覆盖，查询用）
     */
    private Map<AbstractSpell, Holder<MobEffect>> spellEffectMap = new HashMap<>();

    /**
     * 仅数据包解析结果，保留下来供 {@link #applyConfigOverrides()} 重新叠加配置，免得重解析 JSON
     */
    private Map<AbstractSpell, Float> packRangeMap = new HashMap<>();

    /**
     * 仅数据包解析结果，同上
     */
    private Map<AbstractSpell, Holder<MobEffect>> packEffectMap = new HashMap<>();

    public SpellDataManager() {
        super(GSON, DIRECTORY);
        instance = this;
    }

    public static SpellDataManager getInstance() {
        if (instance == null) {
            instance = new SpellDataManager();
        }
        return instance;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<AbstractSpell, Float> newRangeMap = new HashMap<>();
        Map<AbstractSpell, Holder<MobEffect>> newEffectMap = new HashMap<>();

        WinefoxsSpellbooks.LOGGER.info("Loading spell data configurations...");
        long startTime = System.currentTimeMillis();

        newRangeMap.put(SpellRegistry.none(), 15.0F);

        var ops = this.makeConditionalOps();

        for (var entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();

            if (id.getPath().contains("casting_range")) {
                SpellCastingRangeData.CODEC.parse(ops, json)
                    .resultOrPartial(err -> WinefoxsSpellbooks.LOGGER.warn("Failed to parse casting range {}: {}", id, err))
                    .ifPresent(data -> processCastingRangeData(data, newRangeMap));
            } else if (id.getPath().contains("caused_effect")) {
                SpellCausedEffectData.CODEC.parse(ops, json)
                    .resultOrPartial(err -> WinefoxsSpellbooks.LOGGER.warn("Failed to parse caused effect {}: {}", id, err))
                    .ifPresent(data -> processCausedEffectData(data, newEffectMap));
            }
        }

        this.packRangeMap = newRangeMap;
        this.packEffectMap = newEffectMap;
        applyConfigOverrides();

        long elapsed = System.currentTimeMillis() - startTime;
        WinefoxsSpellbooks.LOGGER.info("Loaded {} spell range entries and {} spell effect entries in {}ms",
            spellRangeMap.size(), spellEffectMap.size(), elapsed);
    }

    /**
     * 把配置覆盖叠回数据包解析结果之上。须在 {@code ServerAboutToStartEvent} 上再调一次：
     * 初次进世界的数据包重载早于 SERVER 配置加载，那一趟 {@link #apply} 读不到配置。
     */
    public void applyConfigOverrides() {
        Map<AbstractSpell, Float> rangeMap = new HashMap<>(this.packRangeMap);
        Map<AbstractSpell, Holder<MobEffect>> effectMap = new HashMap<>(this.packEffectMap);
        if (Config.isServerConfigLoaded()) {
            loadExtraFromConfig(rangeMap, effectMap);
        }
        this.spellRangeMap = rangeMap;
        this.spellEffectMap = effectMap;
    }

    private void processCastingRangeData(SpellCastingRangeData data, Map<AbstractSpell, Float> rangeMap) {
        for (var entry : data.entries()) {
            SpellRegistry.REGISTRY.getOptional(entry.spellId()).ifPresentOrElse(
                spell -> rangeMap.put(spell, entry.range()),
                () -> WinefoxsSpellbooks.LOGGER.warn("Unknown spell id in casting range data: {}", entry.spellId())
            );
        }
    }

    private void processCausedEffectData(SpellCausedEffectData data, Map<AbstractSpell, Holder<MobEffect>> effectMap) {
        for (var entry : data.entries()) {
            var spellOpt = SpellRegistry.REGISTRY.getOptional(entry.spellId());
            var effectOpt = BuiltInRegistries.MOB_EFFECT.getOptional(entry.effectId());

            if (spellOpt.isPresent() && effectOpt.isPresent()) {
                effectMap.put(spellOpt.get(), Holder.direct(effectOpt.get()));
            } else {
                if (spellOpt.isEmpty()) {
                    WinefoxsSpellbooks.LOGGER.warn("Unknown spell id in caused effect data: {}", entry.spellId());
                }
                if (effectOpt.isEmpty()) {
                    WinefoxsSpellbooks.LOGGER.warn("Unknown effect id in caused effect data: {}", entry.effectId());
                }
            }
        }
    }

    private void loadExtraFromConfig(Map<AbstractSpell, Float> rangeMap, Map<AbstractSpell, Holder<MobEffect>> effectMap) {
        for (var entry : Config.getExtraSpellCastingRange().entrySet()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(entry.getKey()))
                .ifPresent(spell -> rangeMap.put(spell, entry.getValue()));
        }

        for (var entry : Config.getExtraSpellCausedEffects().entrySet()) {
            var spellOpt = SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(entry.getKey()));
            var effectOpt = BuiltInRegistries.MOB_EFFECT.getOptional(ResourceLocation.parse(entry.getValue()));

            if (spellOpt.isPresent() && effectOpt.isPresent()) {
                effectMap.put(spellOpt.get(), Holder.direct(effectOpt.get()));
            }
        }
    }

    /**
     * 获取法术施法范围
     *
     * @param spell 法术
     * @return 施法范围，默认返回 10.0F
     */
    public float getSpellRange(AbstractSpell spell) {
        return spellRangeMap.getOrDefault(spell, 10.0F);
    }

    /**
     * 获取法术导致的效果
     *
     * @param spell 法术
     * @return 效果 Holder，可能为 null
     */
    public Holder<MobEffect> getSpellCausedEffect(AbstractSpell spell) {
        return spellEffectMap.get(spell);
    }

    /**
     * 获取法术范围映射（只读）
     */
    public Map<AbstractSpell, Float> getSpellRangeMap() {
        return Map.copyOf(spellRangeMap);
    }

    /**
     * 获取法术效果映射（只读）
     */
    public Map<AbstractSpell, Holder<MobEffect>> getSpellEffectMap() {
        return Map.copyOf(spellEffectMap);
    }
}
