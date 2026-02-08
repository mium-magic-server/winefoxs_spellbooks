package net.magicterra.winefoxsspellbooks.registry;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.HashSet;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.api.event.RegisterSpellTypeEvent;
import net.magicterra.winefoxsspellbooks.magic.data.SpellDataManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * 女仆咒语注册表
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 23:14
 */
public final class MaidSpellRegistry {
    /**
     * 攻击法术，法术攻击模式下，对敌人施放
     */
    public static final TagKey<AbstractSpell> ATTACK_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "attack_spells")
    );
    /**
     * 自我强化法术，所有模式下，对自身施放
     */
    public static final TagKey<AbstractSpell> DEFENSE_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "defense_spells")
    );
    /**
     * 移动法术，法术攻击模式下，用于移动到目标位置
     */
    public static final TagKey<AbstractSpell> MOVEMENT_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "movement_spells")
    );
    /**
     * 治疗法术，所有模式下，对自身施放
     */
    public static final TagKey<AbstractSpell> SUPPORT_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "support_spells")
    );
    /**
     * 正面效果法术，法术支援模式下，对队友施放
     */
    public static final TagKey<AbstractSpell> POSITIVE_EFFECT_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "positive_effect_spells")
    );
    /**
     * 治疗效果法术，法术支援模式下，对队友施放
     */
    public static final TagKey<AbstractSpell> SUPPORT_EFFECT_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "support_effect_spells")
    );
    /**
     * 负面效果法术，所有模式下，对敌人施放
     */
    public static final TagKey<AbstractSpell> NEGATIVE_EFFECT_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "negative_effect_spells")
    );
    /**
     * 召唤法术，需要标记以避免重新咏唱
     */
    public static final TagKey<AbstractSpell> SUMMON_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "summon_spells")
    );
    /**
     * 需要二重咏唱的法术，例如 炽焰追踪弹幕
     */
    public static final TagKey<AbstractSpell> MAID_SHOULD_RECAST_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "maid_should_recast_spells")
    );

    public static final Set<AbstractSpell> ATTACK_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> DEFENSE_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> MOVEMENT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> SUPPORT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> POSITIVE_EFFECT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> SUPPORT_EFFECT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> NEGATIVE_EFFECT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> SUMMON_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> MAID_SHOULD_RECAST_SPELLS = new HashSet<>();

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
        ATTACK_SPELLS.clear();
        DEFENSE_SPELLS.clear();
        MOVEMENT_SPELLS.clear();
        SUPPORT_SPELLS.clear();
        POSITIVE_EFFECT_SPELLS.clear();
        SUPPORT_EFFECT_SPELLS.clear();
        NEGATIVE_EFFECT_SPELLS.clear();
        SUMMON_SPELLS.clear();
        MAID_SHOULD_RECAST_SPELLS.clear();

        SpellRegistry.REGISTRY.getOrCreateTag(ATTACK_SPELLS_TAG).stream().forEach(s -> ATTACK_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(DEFENSE_SPELLS_TAG).stream().forEach(s -> DEFENSE_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(MOVEMENT_SPELLS_TAG).stream().forEach(s -> MOVEMENT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(SUPPORT_SPELLS_TAG).stream().forEach(s -> SUPPORT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(POSITIVE_EFFECT_SPELLS_TAG).stream().forEach(s -> POSITIVE_EFFECT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(SUPPORT_EFFECT_SPELLS_TAG).stream().forEach(s -> SUPPORT_EFFECT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(NEGATIVE_EFFECT_SPELLS_TAG).stream().forEach(s -> NEGATIVE_EFFECT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(SUMMON_SPELLS_TAG).stream().forEach(s -> SUMMON_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(MAID_SHOULD_RECAST_SPELLS_TAG).stream().forEach(s -> MAID_SHOULD_RECAST_SPELLS.add(s.value()));

        // 触发事件，允许其他模组修改法术类型集合
        NeoForge.EVENT_BUS.post(new RegisterSpellTypeEvent(
            ATTACK_SPELLS,
            DEFENSE_SPELLS,
            MOVEMENT_SPELLS,
            SUPPORT_SPELLS,
            POSITIVE_EFFECT_SPELLS,
            SUPPORT_EFFECT_SPELLS,
            NEGATIVE_EFFECT_SPELLS,
            SUMMON_SPELLS,
            MAID_SHOULD_RECAST_SPELLS
        ));

        addConfiguredSpells(ATTACK_SPELLS, Config.getExtraAttackSpells(), "spell_compat.extra_attack_spells");
        addConfiguredSpells(DEFENSE_SPELLS, Config.getExtraDefenseSpells(), "spell_compat.extra_defense_spells");
        addConfiguredSpells(MOVEMENT_SPELLS, Config.getExtraMovementSpells(), "spell_compat.extra_movement_spells");
        addConfiguredSpells(SUPPORT_SPELLS, Config.getExtraSupportSpells(), "spell_compat.extra_support_spells");
        addConfiguredSpells(POSITIVE_EFFECT_SPELLS, Config.getExtraPositiveEffectSpells(), "spell_compat.extra_positive_effect_spells");
        addConfiguredSpells(NEGATIVE_EFFECT_SPELLS, Config.getExtraNegativeEffectSpells(), "spell_compat.extra_negative_effect_spells");
        addConfiguredSpells(SUPPORT_EFFECT_SPELLS, Config.getExtraSupportEffectSpells(), "spell_compat.extra_support_effect_spells");
        addConfiguredSpells(SUMMON_SPELLS, Config.getExtraSummonSpells(), "spell_compat.extra_summon_spells");
        addConfiguredSpells(MAID_SHOULD_RECAST_SPELLS, Config.getMaidShouldRecastSpells(), "spell_compat.maid_should_recast_spells");
    }

    private static void addConfiguredSpells(Set<AbstractSpell> spellSet, Iterable<String> extraSpellIds, String configKey) {
        for (String extraSpellId : extraSpellIds) {
            if (extraSpellId == null) {
                WinefoxsSpellbooks.LOGGER.warn("Invalid null spell id in config '{}'", configKey);
                continue;
            }
            ResourceLocation spellId = ResourceLocation.tryParse(extraSpellId);
            if (spellId == null) {
                WinefoxsSpellbooks.LOGGER.warn("Invalid spell id '{}' in config '{}'", extraSpellId, configKey);
                continue;
            }
            SpellRegistry.REGISTRY.getOptional(spellId).ifPresent(spellSet::add);
        }
    }

    /**
     * 获取法术施法范围
     * 数据由 {@link SpellDataManager} 从数据包加载
     *
     * @param spell 法术
     * @return 施法范围
     */
    public static float getSpellRange(AbstractSpell spell) {
        return SpellDataManager.getInstance().getSpellRange(spell);
    }

    public static boolean isAttackSpell(AbstractSpell spell) {
        return ATTACK_SPELLS.contains(spell);
    }

    public static boolean isDefenseSpell(AbstractSpell spell) {
        return DEFENSE_SPELLS.contains(spell);
    }

    public static boolean isMovementSpell(AbstractSpell spell) {
        return MOVEMENT_SPELLS.contains(spell);
    }

    public static boolean isSupportSpell(AbstractSpell spell) {
        return SUPPORT_SPELLS.contains(spell);
    }

    public static boolean isPositiveEffectSpell(AbstractSpell spell) {
        return POSITIVE_EFFECT_SPELLS.contains(spell);
    }

    public static boolean isSupportEffectSpell(AbstractSpell spell) {
        return SUPPORT_EFFECT_SPELLS.contains(spell);
    }

    public static boolean isNegativeEffectSpell(AbstractSpell spell) {
        return NEGATIVE_EFFECT_SPELLS.contains(spell);
    }

    public static boolean isSummonSpell(AbstractSpell spell) {
        return SUMMON_SPELLS.contains(spell);
    }

    public static boolean maidShouldRecast(AbstractSpell spell) {
        return MAID_SHOULD_RECAST_SPELLS.contains(spell);
    }

    /**
     * 获取法术导致的效果
     * 数据由 {@link SpellDataManager} 从数据包加载
     *
     * @param spell 法术
     * @return 效果 Holder，可能为 null
     */
    public static Holder<MobEffect> getSpellCausedEffect(AbstractSpell spell) {
        return SpellDataManager.getInstance().getSpellCausedEffect(spell);
    }
}
