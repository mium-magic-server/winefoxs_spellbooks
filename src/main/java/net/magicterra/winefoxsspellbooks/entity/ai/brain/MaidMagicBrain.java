package net.magicterra.winefoxsspellbooks.entity.ai.brain;

import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import java.util.List;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/**
 * 女仆施法 AI 注册
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-05 01:08
 */
public class MaidMagicBrain implements IExtraMaidBrain {
    @Override
    public List<MemoryModuleType<?>> getExtraMemoryTypes() {
        return List.of(
            MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(),
            MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(),
            MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get()
        );
    }
}
