package net.magicterra.winefoxsspellbooks.registry;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

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

    public static final Set<AbstractSpell> ATTACK_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> DEFENSE_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> MOVEMENT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> SUPPORT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> POSITIVE_EFFECT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> SUPPORT_EFFECT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> NEGATIVE_EFFECT_SPELLS = new HashSet<>();
    private static final Map<AbstractSpell, Float> SPELL_RANGE_MAP = new HashMap<>();

    private static final Map<AbstractSpell, Holder<MobEffect>> SPELL_EFFECT_MAP = new HashMap<>();

    static {
        registerSpell();
    }

    private static void registerSpell() {
        SpellRegistry.REGISTRY.getOrCreateTag(ATTACK_SPELLS_TAG).stream().forEach(s -> ATTACK_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(DEFENSE_SPELLS_TAG).stream().forEach(s -> DEFENSE_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(MOVEMENT_SPELLS_TAG).stream().forEach(s -> MOVEMENT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(SUPPORT_SPELLS_TAG).stream().forEach(s -> SUPPORT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(POSITIVE_EFFECT_SPELLS_TAG).stream().forEach(s -> POSITIVE_EFFECT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(SUPPORT_EFFECT_SPELLS_TAG).stream().forEach(s -> SUPPORT_EFFECT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(NEGATIVE_EFFECT_SPELLS_TAG).stream().forEach(s -> NEGATIVE_EFFECT_SPELLS.add(s.value()));

        // 配置攻击范围，女仆在使用这些法术时会尝试靠近到小于指定的距离再发动法术
        SPELL_RANGE_MAP.put(SpellRegistry.DIVINE_SMITE_SPELL.get(), 1.7F);
        SPELL_RANGE_MAP.put(SpellRegistry.CLEANSE_SPELL.get(), 3.0F);
        SPELL_RANGE_MAP.put(SpellRegistry.HEALING_CIRCLE_SPELL.get(), 5.0F);
        SPELL_RANGE_MAP.put(SpellRegistry.none(), 15.0F);

        // 配置法术导致的药水效果，这里仅考虑主要效果，如果有法术导致多个效果可能会出问题
        SPELL_EFFECT_MAP.put(SpellRegistry.HEARTSTOP_SPELL.get(), MobEffectRegistry.HEARTSTOP);
        SPELL_EFFECT_MAP.put(SpellRegistry.ECHOING_STRIKES_SPELL.get(), MobEffectRegistry.ECHOING_STRIKES);
        SPELL_EFFECT_MAP.put(SpellRegistry.INVISIBILITY_SPELL.get(), MobEffects.INVISIBILITY);
        SPELL_EFFECT_MAP.put(SpellRegistry.CHARGE_SPELL.get(), MobEffectRegistry.CHARGED);
        SPELL_EFFECT_MAP.put(SpellRegistry.SPIDER_ASPECT_SPELL.get(), MobEffectRegistry.SPIDER_ASPECT);
        SPELL_EFFECT_MAP.put(SpellRegistry.OAKSKIN_SPELL.get(), MobEffectRegistry.OAKSKIN);
        SPELL_EFFECT_MAP.put(SpellRegistry.GLUTTONY_SPELL.get(), MobEffectRegistry.GLUTTONY);
        SPELL_EFFECT_MAP.put(SpellRegistry.ABYSSAL_SHROUD_SPELL.get(), MobEffectRegistry.ABYSSAL_SHROUD);
        SPELL_EFFECT_MAP.put(SpellRegistry.SLOW_SPELL.get(), MobEffects.MOVEMENT_SLOWDOWN);
        SPELL_EFFECT_MAP.put(SpellRegistry.HEAT_SURGE_SPELL.get(), MobEffectRegistry.REND);
        SPELL_EFFECT_MAP.put(SpellRegistry.FROSTWAVE_SPELL.get(), MobEffectRegistry.CHILLED);
        SPELL_EFFECT_MAP.put(SpellRegistry.BLIGHT_SPELL.get(), MobEffectRegistry.BLIGHT);
        SPELL_EFFECT_MAP.put(SpellRegistry.FORTIFY_SPELL.get(), MobEffectRegistry.FORTIFY);
        SPELL_EFFECT_MAP.put(SpellRegistry.HASTE_SPELL.get(), MobEffectRegistry.HASTENED);
        SPELL_EFFECT_MAP.put(SpellRegistry.FROSTBITE_SPELL.get(), MobEffectRegistry.FROSTBITTEN_STRIKES);
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

    public static Holder<MobEffect> getSpellCausedEffect(AbstractSpell spell) {
        return SPELL_EFFECT_MAP.get(spell);
    }
}
