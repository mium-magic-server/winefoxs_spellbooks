package net.magicterra.winefoxsspellbooks.magic.catalog;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.api.event.RegisterSpellTypeEvent;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.api.spell.SpellCategory;
import net.magicterra.winefoxsspellbooks.api.spell.SpellFlag;
import net.magicterra.winefoxsspellbooks.event.MaidEventHandler;
import net.magicterra.winefoxsspellbooks.magic.data.SpellDataManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * 女仆法术目录：把「哪些法术女仆能用、分别归哪一类」的答案缓存下来。
 * <p>
 * 先读法术标签，再并入服务端配置里的额外法术；类别集合由 {@link MaidSpellAction} 与
 * {@link SpellFlag} 驱动。在 {@code ServerStartingEvent} 与 {@code TagsUpdatedEvent} 上整体重建。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 23:14
 */
public final class MaidSpellCatalog {
    private MaidSpellCatalog() {
    }

    /**
     * 类别 → 该类法术集合。每次重建都是 clear + refill，<b>不是</b>换 Set 实例—— {@link RegisterSpellTypeEvent} 把这些 Set 直接交给外部模组修改，换实例会让持有旧引用的 监听方对着孤儿集合写入。
     */
    private static final Map<MaidSpellAction, Set<AbstractSpell>> BY_ACTION = newCatalog(MaidSpellAction.class);

    private static final Map<SpellFlag, Set<AbstractSpell>> BY_FLAG = newCatalog(SpellFlag.class);

    private static <T extends Enum<T> & SpellCategory> Map<T, Set<AbstractSpell>> newCatalog(Class<T> type) {
        Map<T, Set<AbstractSpell>> map = new EnumMap<>(type);
        for (T category : type.getEnumConstants()) {
            map.put(category, new HashSet<>());
        }
        return map;
    }

    public static void registerSpell(ServerStartingEvent event) {
        refreshSpellTypes();
    }

    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (!event.shouldUpdateStaticData()) {
            return;
        }
        refreshSpellTypes();
    }

    private static void refreshSpellTypes() {
        BY_ACTION.forEach((category, spells) -> reloadFromTag(category, spells));
        BY_FLAG.forEach((category, spells) -> reloadFromTag(category, spells));

        NeoForge.EVENT_BUS.post(new RegisterSpellTypeEvent(BY_ACTION, BY_FLAG));

        // 初次进世界时本方法会被 WorldLoader 里的 TagsUpdatedEvent 触发一次，那时 SERVER 配置还没加载，
        // 读了会抛异常。跳过即可——ServerStartingEvent 会在配置加载后再整体刷一遍，配置项不会丢。
        // 那一轮世界都还没建出来，也就没有女仆需要重扫，直接返回不会漏。
        if (!Config.isServerConfigLoaded()) {
            WinefoxsSpellbooks.LOGGER.debug("Server config not loaded yet, skipping configured spell merge; "
                + "it will be redone on ServerStartingEvent");
            return;
        }

        BY_ACTION.forEach(MaidSpellCatalog::mergeConfigured);
        BY_FLAG.forEach(MaidSpellCatalog::mergeConfigured);

        // 目录变了，存量女仆手上的法术快照就过期了。重建完在这里收口通知，而不是让下游再去
        // 订阅一遍同样的两个事件、靠 EventPriority 排在本类之后——那种顺序耦合编译期看不见，
        // 将来这里多一个重建触发点，下游就会悄悄漏掉。
        MaidEventHandler.refreshLoadedMaidSpellData();
    }

    private static void reloadFromTag(SpellCategory category, Set<AbstractSpell> target) {
        target.clear();
        SpellRegistry.REGISTRY.getOrCreateTag(category.tag()).forEach(holder -> target.add(holder.value()));
    }

    private static void mergeConfigured(SpellCategory category, Set<AbstractSpell> target) {
        List<String> extraSpellIds = Config.getExtraSpells(category);
        for (String extraSpellId : extraSpellIds) {
            if (extraSpellId == null) {
                WinefoxsSpellbooks.LOGGER.warn("Invalid null spell id in config '{}'", category.configName());
                continue;
            }
            ResourceLocation spellId = ResourceLocation.tryParse(extraSpellId);
            if (spellId == null) {
                WinefoxsSpellbooks.LOGGER.warn("Invalid spell id '{}' in config '{}'", extraSpellId, category.configName());
                continue;
            }
            SpellRegistry.REGISTRY.getOptional(spellId).ifPresent(target::add);
        }
    }

    /**
     * 取某一行为类别下的全部法术。返回的是<b>活的</b>集合，勿在外部修改—— 要增删请监听 {@link RegisterSpellTypeEvent}。
     */
    public static Set<AbstractSpell> spellsOf(MaidSpellAction action) {
        return BY_ACTION.get(action);
    }

    /**
     * 取某个标记下的全部法术。同样是活集合，见 {@link #spellsOf}。
     */
    public static Set<AbstractSpell> spellsOf(SpellFlag flag) {
        return BY_FLAG.get(flag);
    }

    /**
     * 该法术属于哪个行为类别；都不属于时返回 {@code null}。
     * <p>
     * 行为类别互斥，故首个命中即答案。
     */
    public static MaidSpellAction actionOf(AbstractSpell spell) {
        for (MaidSpellAction action : MaidSpellAction.ALL) {
            if (BY_ACTION.get(action).contains(spell)) {
                return action;
            }
        }
        return null;
    }

    /**
     * 该法术是否带某个标记。
     */
    public static boolean hasFlag(AbstractSpell spell, SpellFlag flag) {
        return BY_FLAG.get(flag).contains(spell);
    }

    /**
     * 是否为召唤法术。
     */
    public static boolean isSummonSpell(AbstractSpell spell) {
        return hasFlag(spell, SpellFlag.SUMMON);
    }

    /**
     * 女仆是否应当对该法术进行二段咏唱。
     */
    public static boolean maidShouldRecast(AbstractSpell spell) {
        return hasFlag(spell, SpellFlag.SHOULD_RECAST);
    }

    /**
     * 已有未完成的二段咏唱时，女仆能否继续施放该法术。
     * <p>
     * 召唤类即便标了逐发也不行：把 recast 次数用光会走 {@code USED_ALL_RECASTS} 分支收回刚召出来的东西， 等 recast 自然超时才安全（超时分支不收回）。选术与施法两端必须同口径，否则女仆会反复选中 一个到了施法环节必被拒绝的法术，白白空转。
     */
    public static boolean maidMayRepeatCast(AbstractSpell spell) {
        return maidShouldRecast(spell) && !isSummonSpell(spell);
    }

    /**
     * 法术施法范围，数据由 {@link SpellDataManager} 从数据包加载。
     */
    public static float getSpellRange(AbstractSpell spell) {
        return SpellDataManager.getInstance().getSpellRange(spell);
    }

    /**
     * 法术附带效果，数据由 {@link SpellDataManager} 从数据包加载；可能为 null。
     */
    public static Holder<MobEffect> getSpellCausedEffect(AbstractSpell spell) {
        return SpellDataManager.getInstance().getSpellCausedEffect(spell);
    }
}
