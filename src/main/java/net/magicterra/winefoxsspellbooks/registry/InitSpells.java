package net.magicterra.winefoxsspellbooks.registry;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.function.Supplier;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.spell.SummonMaidSpell;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * InitSpells
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-02 22:17
 */
public class InitSpells {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, WinefoxsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }

    private static Supplier<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static final Supplier<AbstractSpell> SUMMON_MAID_SPELL = registerSpell(new SummonMaidSpell());
}
