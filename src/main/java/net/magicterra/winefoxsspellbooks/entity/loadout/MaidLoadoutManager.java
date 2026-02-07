package net.magicterra.winefoxsspellbooks.entity.loadout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.loadout.codec.LoadoutCodecs;
import net.magicterra.winefoxsspellbooks.entity.loadout.data.MaidLoadout;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * 女仆装备配置管理器
 * 负责从数据包加载和管理所有装备配置
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public class MaidLoadoutManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIRECTORY = "summoned_maid_loadouts";

    /** 单例实例 */
    private static MaidLoadoutManager instance;

    /** 所有加载的配置 */
    private Map<ResourceLocation, MaidLoadout> loadouts = new HashMap<>();

    /** 可选择的配置（权重 > 0） */
    private List<MaidLoadout> selectableLoadouts = new ArrayList<>();

    /** 总权重 */
    private int totalWeight = 0;

    public MaidLoadoutManager() {
        super(GSON, DIRECTORY);
        instance = this;
    }

    /** 获取单例实例 */
    public static MaidLoadoutManager getInstance() {
        if (instance == null) {
            instance = new MaidLoadoutManager();
        }
        return instance;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, MaidLoadout> newLoadouts = new HashMap<>();
        List<MaidLoadout> newSelectableLoadouts = new ArrayList<>();
        int newTotalWeight = 0;

        WinefoxsSpellbooks.LOGGER.info("Loading maid loadout configurations...");
        long startTime = System.currentTimeMillis();

        var ops = this.makeConditionalOps();

        for (var entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();

            var result = LoadoutCodecs.MAID_LOADOUT_DATA.parse(ops, json)
                .resultOrPartial(err -> WinefoxsSpellbooks.LOGGER.warn("Failed to parse maid loadout {}: {}", id, err));

            if (result.isEmpty()) {
                continue;
            }

            MaidLoadout loadout = result.get().toLoadout(id);
            newLoadouts.put(id, loadout);

            // 只有权重 > 0 的配置参与选择
            if (loadout.isSelectable()) {
                newSelectableLoadouts.add(loadout);
                newTotalWeight += loadout.weight();
            }

            WinefoxsSpellbooks.LOGGER.debug("Loaded maid loadout: {} (weight: {}, tiers: {})",
                id, loadout.weight(), loadout.tiers().size());
        }

        this.loadouts = newLoadouts;
        this.selectableLoadouts = newSelectableLoadouts;
        this.totalWeight = newTotalWeight;

        long elapsed = System.currentTimeMillis() - startTime;
        WinefoxsSpellbooks.LOGGER.info("Loaded {} maid loadout configurations ({} selectable) in {}ms",
            loadouts.size(), selectableLoadouts.size(), elapsed);
    }

    /**
     * 获取指定 ID 的配置
     *
     * @param id 配置 ID
     * @return 配置，如果不存在返回 empty
     */
    public Optional<MaidLoadout> getLoadout(ResourceLocation id) {
        return Optional.ofNullable(loadouts.get(id));
    }

    /**
     * 获取所有配置
     *
     * @return 所有配置的不可变集合
     */
    public Collection<MaidLoadout> getAllLoadouts() {
        return Collections.unmodifiableCollection(loadouts.values());
    }

    /**
     * 按权重随机选择一个配置
     *
     * @param random 随机数生成器
     * @return 选中的配置，如果没有可选配置则返回 null
     */
    @Nullable
    public MaidLoadout selectLoadout(RandomSource random) {
        if (selectableLoadouts.isEmpty() || totalWeight <= 0) {
            WinefoxsSpellbooks.LOGGER.warn("No maid loadout configurations available! " +
                "Please ensure data/*/summoned_maid_loadouts/*.json files exist.");
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;

        for (MaidLoadout loadout : selectableLoadouts) {
            cumulative += loadout.weight();
            if (roll < cumulative) {
                return loadout;
            }
        }

        // 理论上不会到这里，但作为回退
        return selectableLoadouts.getFirst();
    }

    /**
     * 检查是否有加载的配置
     *
     * @return 如果有配置返回 true
     */
    public boolean hasLoadouts() {
        return !loadouts.isEmpty();
    }

    /**
     * 获取配置数量
     *
     * @return 配置数量
     */
    public int getLoadoutCount() {
        return loadouts.size();
    }

    /**
     * 获取可选择配置数量
     *
     * @return 可选择配置数量
     */
    public int getSelectableCount() {
        return selectableLoadouts.size();
    }
}
