package net.magicterra.winefoxsspellbooks.entity.ai.brain;

import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.MaidDeliverGiftBehavior;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/**
 * 女仆施法 AI 注册
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-05 01:08
 */
public class MaidMagicBrain implements IExtraMaidBrain {
    /**
     * 晨间礼物交付优先级
     * <p>
     * 高于普通 idle 行为（看向、随机走），保证拿到 GIFT_DELIVERY 后能优先送达。
     */
    private static final int GIFT_DELIVERY_PRIORITY = 1;

    @Override
    public List<MemoryModuleType<?>> getExtraMemoryTypes() {
        return List.of(
            MaidCastingMemoryModuleTypes.CURRENT_SPELL.get(),
            MaidCastingMemoryModuleTypes.CURRENT_SPELL_ACTION.get(),
            MaidCastingMemoryModuleTypes.SUPPORT_TARGET.get(),
            MaidCastingMemoryModuleTypes.GIFT_DELIVERY.get()
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> getIdleBehaviors() {
        return giftDeliveryBehaviors();
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> getWorkBehaviors() {
        return giftDeliveryBehaviors();
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> getRestBehaviors() {
        return giftDeliveryBehaviors();
    }

    /**
     * Brain 会按 schedule 分别持有 idle/work/rest 行为实例（各自维护运行状态），
     * 因此这里每次返回新的 Behavior 实例而不是共享单例。
     */
    private static List<Pair<Integer, BehaviorControl<? super EntityMaid>>> giftDeliveryBehaviors() {
        return List.of(Pair.of(GIFT_DELIVERY_PRIORITY, new MaidDeliverGiftBehavior()));
    }
}
