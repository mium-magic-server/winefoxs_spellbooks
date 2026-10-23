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
     * 法术伤害先验（只来自配置，没有数据包默认值）。规划器对攻击法术的伤害估计以女仆实战学习为主， 见 {@link net.magicterra.winefoxsspellbooks.magic.MaidSpellMemory}；这里只是给学不准的法术一个手动修正入口。
     */
    private Map<AbstractSpell, Float> spellValueMap = new HashMap<>();

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
        // 条目覆盖全部受支持的附属，未安装的附属自然解析不到，只统计不逐条打日志
        int[] skipped = {0};

        for (var entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();

            if (id.getPath().contains("casting_range")) {
                SpellCastingRangeData.CODEC.parse(ops, json)
                    .resultOrPartial(err -> WinefoxsSpellbooks.LOGGER.warn("Failed to parse casting range {}: {}", id, err))
                    .ifPresent(data -> skipped[0] += processCastingRangeData(data, newRangeMap));
            } else if (id.getPath().contains("caused_effect")) {
                SpellCausedEffectData.CODEC.parse(ops, json)
                    .resultOrPartial(err -> WinefoxsSpellbooks.LOGGER.warn("Failed to parse caused effect {}: {}", id, err))
                    .ifPresent(data -> skipped[0] += processCausedEffectData(data, newEffectMap));
            }
        }

        this.packRangeMap = newRangeMap;
        this.packEffectMap = newEffectMap;
        applyConfigOverrides();

        long elapsed = System.currentTimeMillis() - startTime;
        WinefoxsSpellbooks.LOGGER.info(
            "Loaded {} spell range entries and {} spell effect entries in {}ms ({} skipped: spell or effect not present)",
            spellRangeMap.size(), spellEffectMap.size(), elapsed, skipped[0]);
    }

    /**
     * 把配置覆盖叠回数据包解析结果之上。须在 {@code ServerAboutToStartEvent} 上再调一次： 初次进世界的数据包重载早于 SERVER 配置加载，那一趟 {@link #apply} 读不到配置。
     */
    public void applyConfigOverrides() {
        Map<AbstractSpell, Float> rangeMap = new HashMap<>(this.packRangeMap);
        Map<AbstractSpell, Holder<MobEffect>> effectMap = new HashMap<>(this.packEffectMap);
        Map<AbstractSpell, Float> valueMap = new HashMap<>();
        if (Config.isServerConfigLoaded()) {
            loadExtraFromConfig(rangeMap, effectMap, valueMap);
        }
        this.spellRangeMap = rangeMap;
        this.spellEffectMap = effectMap;
        this.spellValueMap = valueMap;
    }

    /**
     * @return 因法术未注册而跳过的条目数
     */
    private int processCastingRangeData(SpellCastingRangeData data, Map<AbstractSpell, Float> rangeMap) {
        int skipped = 0;
        for (var entry : data.entries()) {
            var spellOpt = SpellRegistry.REGISTRY.getOptional(entry.spellId());
            if (spellOpt.isPresent()) {
                rangeMap.put(spellOpt.get(), entry.range());
            } else {
                skipped++;
                WinefoxsSpellbooks.LOGGER.debug("Unknown spell id in casting range data: {}", entry.spellId());
            }
        }
        return skipped;
    }

    /**
     * @return 因法术或效果未注册而跳过的条目数
     */
    private int processCausedEffectData(SpellCausedEffectData data, Map<AbstractSpell, Holder<MobEffect>> effectMap) {
        int skipped = 0;
        for (var entry : data.entries()) {
            var spellOpt = SpellRegistry.REGISTRY.getOptional(entry.spellId());
            var effectOpt = BuiltInRegistries.MOB_EFFECT.getOptional(entry.effectId());

            if (spellOpt.isPresent() && effectOpt.isPresent()) {
                effectMap.put(spellOpt.get(), Holder.direct(effectOpt.get()));
            } else {
                skipped++;
                if (spellOpt.isEmpty()) {
                    WinefoxsSpellbooks.LOGGER.debug("Unknown spell id in caused effect data: {}", entry.spellId());
                }
                if (effectOpt.isEmpty()) {
                    WinefoxsSpellbooks.LOGGER.debug("Unknown effect id in caused effect data: {}", entry.effectId());
                }
            }
        }
        return skipped;
    }

    private void loadExtraFromConfig(Map<AbstractSpell, Float> rangeMap, Map<AbstractSpell, Holder<MobEffect>> effectMap,
                                     Map<AbstractSpell, Float> valueMap) {
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

        for (var entry : Config.getExtraSpellValue().entrySet()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(entry.getKey()))
                .ifPresent(spell -> valueMap.put(spell, entry.getValue()));
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
     * 获取配置里给定的法术伤害先验
     *
     * @param spell 法术
     * @return 1 级时一次完整施放的预期总伤害；配置未给出时返回 0
     */
    public float getSpellValue(AbstractSpell spell) {
        return spellValueMap.getOrDefault(spell, 0F);
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
