package net.magicterra.winefoxsspellbooks.registry;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
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
    private static final Map<AbstractSpell, Float> SPELL_RANGE_MAP = new HashMap<>();

    private static final Map<AbstractSpell, Holder<MobEffect>> SPELL_EFFECT_MAP = new HashMap<>();

    public static void registerSpell(ServerStartingEvent event) {
        ATTACK_SPELLS.clear();
        DEFENSE_SPELLS.clear();
        MOVEMENT_SPELLS.clear();
        SUPPORT_SPELLS.clear();
        POSITIVE_EFFECT_SPELLS.clear();
        SUPPORT_EFFECT_SPELLS.clear();
        NEGATIVE_EFFECT_SPELLS.clear();
        SUMMON_SPELLS.clear();
        MAID_SHOULD_RECAST_SPELLS.clear();
        SPELL_RANGE_MAP.clear();
        SPELL_EFFECT_MAP.clear();
        SpellRegistry.REGISTRY.getOrCreateTag(ATTACK_SPELLS_TAG).stream().forEach(s -> ATTACK_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(DEFENSE_SPELLS_TAG).stream().forEach(s -> DEFENSE_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(MOVEMENT_SPELLS_TAG).stream().forEach(s -> MOVEMENT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(SUPPORT_SPELLS_TAG).stream().forEach(s -> SUPPORT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(POSITIVE_EFFECT_SPELLS_TAG).stream().forEach(s -> POSITIVE_EFFECT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(SUPPORT_EFFECT_SPELLS_TAG).stream().forEach(s -> SUPPORT_EFFECT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(NEGATIVE_EFFECT_SPELLS_TAG).stream().forEach(s -> NEGATIVE_EFFECT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(SUMMON_SPELLS_TAG).stream().forEach(s -> SUMMON_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(MAID_SHOULD_RECAST_SPELLS_TAG).stream().forEach(s -> MAID_SHOULD_RECAST_SPELLS.add(s.value()));

        for (String extraSpellId : Config.getExtraAttackSpells()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(extraSpellId)).ifPresent(ATTACK_SPELLS::add);
        }
        for (String extraSpellId : Config.getExtraDefenseSpells()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(extraSpellId)).ifPresent(DEFENSE_SPELLS::add);
        }
        for (String extraSpellId : Config.getExtraMovementSpells()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(extraSpellId)).ifPresent(MOVEMENT_SPELLS::add);
        }
        for (String extraSpellId : Config.getExtraSupportSpells()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(extraSpellId)).ifPresent(SUPPORT_SPELLS::add);
        }
        for (String extraSpellId : Config.getExtraPositiveEffectSpells()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(extraSpellId)).ifPresent(POSITIVE_EFFECT_SPELLS::add);
        }
        for (String extraSpellId : Config.getExtraNegativeEffectSpells()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(extraSpellId)).ifPresent(NEGATIVE_EFFECT_SPELLS::add);
        }
        for (String extraSpellId : Config.getExtraSupportEffectSpells()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(extraSpellId)).ifPresent(SUPPORT_EFFECT_SPELLS::add);
        }
        for (String extraSpellId : Config.getExtraSummonSpells()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(extraSpellId)).ifPresent(SUMMON_SPELLS::add);
        }
        for (String extraSpellId : Config.getMaidShouldRecastSpells()) {
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(extraSpellId)).ifPresent(MAID_SHOULD_RECAST_SPELLS::add);
        }

        // 配置攻击范围，女仆在使用这些法术时会尝试靠近到小于指定的距离再发动法术
        SPELL_RANGE_MAP.put(SpellRegistry.none(), 15.0F);
        Map<String, Float> spellStartCastingRangeInConfig = Config.getSpellStartCastingRange();
        if (spellStartCastingRangeInConfig.isEmpty()) {
            spellStartCastingRangeInConfig = Config.DEFAULT_SPELL_CASTING_RANGE_RAW;
        }
        for (Map.Entry<String, Float> entry : spellStartCastingRangeInConfig.entrySet()) {
            String key = entry.getKey();
            SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(key)).ifPresent(s -> SPELL_RANGE_MAP.put(s, entry.getValue()));
        }

        // 配置法术导致的药水效果，这里仅考虑主要效果，如果有法术导致多个效果可能会出问题
        Map<String, String> extraSpellCausedEffects = Config.getExtraSpellCausedEffects();
        if (extraSpellCausedEffects.isEmpty()) {
            extraSpellCausedEffects = Config.DEFAULT_SPELL_CAUSED_EFFECTS_RAW;
        }
        for (Map.Entry<String, String> entry : extraSpellCausedEffects.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Optional<AbstractSpell> spellOptional = SpellRegistry.REGISTRY.getOptional(ResourceLocation.parse(key));
            Optional<MobEffect> effectOptional = BuiltInRegistries.MOB_EFFECT.getOptional(ResourceLocation.parse(value));
            if (spellOptional.isPresent() && effectOptional.isPresent()) {
                AbstractSpell spell = spellOptional.get();
                MobEffect effect = effectOptional.get();
                SPELL_EFFECT_MAP.put(spell, Holder.direct(effect));
            }
        }
    }

    public static float getSpellRange(AbstractSpell spell) {
        return SPELL_RANGE_MAP.getOrDefault(spell, 10F);
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

    public static Holder<MobEffect> getSpellCausedEffect(AbstractSpell spell) {
        return SPELL_EFFECT_MAP.get(spell);
    }
}
