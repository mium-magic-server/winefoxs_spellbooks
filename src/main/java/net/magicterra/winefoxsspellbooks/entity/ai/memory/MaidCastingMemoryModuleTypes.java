package net.magicterra.winefoxsspellbooks.entity.ai.memory;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellAction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 法术记忆
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-03 01:25
 */
public class MaidCastingMemoryModuleTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MAID_CASTING_MEMORY_MODULE_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, WinefoxsSpellbooks.MODID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<SpellData>> CURRENT_SPELL = MAID_CASTING_MEMORY_MODULE_TYPES.register("current_spell", (key) -> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<MaidSpellAction>> CURRENT_SPELL_ACTION = MAID_CASTING_MEMORY_MODULE_TYPES.register("current_spell_action", (key) -> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<LivingEntity>> SUPPORT_TARGET = MAID_CASTING_MEMORY_MODULE_TYPES.register("support_target", (key) -> new MemoryModuleType<>(Optional.empty()));
}
