package net.magicterra.winefoxsspellbooks.ai;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * 女仆咒语注册表
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 23:14
 */
public final class MaidSpellRegistry {
    public static final TagKey<AbstractSpell> ATTACK_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "attack_spells")
    );
    public static final TagKey<AbstractSpell> DEFENSE_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "defense_spells")
    );
    public static final TagKey<AbstractSpell> MOVEMENT_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "movement_spells")
    );
    public static final TagKey<AbstractSpell> SUPPORT_SPELLS_TAG = TagKey.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "support_spells")
    );

    public static final Set<AbstractSpell> ATTACK_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> DEFENSE_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> MOVEMENT_SPELLS = new HashSet<>();
    public static final Set<AbstractSpell> SUPPORT_SPELLS = new HashSet<>();
    private static final Map<AbstractSpell, Float> SPELL_RANGE_MAP = new HashMap<>();

    static {
        registerSpell();
    }

    private static void registerSpell() {
        SpellRegistry.REGISTRY.getOrCreateTag(ATTACK_SPELLS_TAG).stream().forEach(s -> ATTACK_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(DEFENSE_SPELLS_TAG).stream().forEach(s -> DEFENSE_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(MOVEMENT_SPELLS_TAG).stream().forEach(s -> MOVEMENT_SPELLS.add(s.value()));
        SpellRegistry.REGISTRY.getOrCreateTag(SUPPORT_SPELLS_TAG).stream().forEach(s -> SUPPORT_SPELLS.add(s.value()));

        SPELL_RANGE_MAP.put(SpellRegistry.DIVINE_SMITE_SPELL.get(), 1.7F);
        SPELL_RANGE_MAP.put(SpellRegistry.CLEANSE_SPELL.get(), 3.0F);
        SPELL_RANGE_MAP.put(SpellRegistry.HEALING_CIRCLE_SPELL.get(), 5.0F);
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
}
