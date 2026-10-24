package net.magicterra.winefoxsspellbooks.entity.ai.memory;

import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/**
 * 施法目标解析：攻击目标与支援目标可以并存，按当前法术类别决定朝谁施法
 * <p>
 * 选中增益 / 治疗队友时只写入 {@link MaidCastingMemoryModuleTypes#SUPPORT_TARGET}，不擦除攻击目标；
 * 支援法术放完后 SpellChooseTask 清掉支援目标，女仆接着打原来的攻击目标。
 * 走位、朝向、施法、瞄准都必须经由这里取目标，否则支援期间会有一部分行为仍对着敌人。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
public final class MaidSpellTargets {
    private MaidSpellTargets() {
    }

    /**
     * 当前法术应当指向的目标
     * <p>
     * 增益 / 治疗队友取支援目标，其余类别取攻击目标；首选为空时退回另一个，与旧的 {@code ATTACK_TARGET.or(SUPPORT_TARGET)} 兼容
     * （例如紧急支援只写了支援目标、还没选出法术时）。
     *
     * @param brain 施法者的 Brain，需注册了攻击目标与支援目标两个记忆
     * @return 目标；两者皆无时为 null
     */
    @Nullable
    public static LivingEntity current(Brain<?> brain) {
        LivingEntity attackTarget = memory(brain, MemoryModuleType.ATTACK_TARGET);
        LivingEntity supportTarget = memory(brain, MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get());
        if (isSupportAction(brain)) {
            return supportTarget != null ? supportTarget : attackTarget;
        }
        return attackTarget != null ? attackTarget : supportTarget;
    }

    /**
     * 当前法术是否为支援队友的类别（增益 / 治疗队友）
     */
    public static boolean isSupportAction(Brain<?> brain) {
        if (!brain.hasMemoryValue(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get())) {
            return false;
        }
        MaidSpellAction action = brain.getMemory(MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get()).orElse(null);
        return action == MaidSpellAction.POSITIVE || action == MaidSpellAction.SUPPORT_OTHER;
    }

    /**
     * hasMemoryValue 在前：记忆模块没注册时 getMemory 直接抛 ISE
     */
    @Nullable
    private static LivingEntity memory(Brain<?> brain, MemoryModuleType<LivingEntity> type) {
        return brain.hasMemoryValue(type) ? brain.getMemory(type).orElse(null) : null;
    }
}
