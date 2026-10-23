package net.magicterra.winefoxsspellbooks.client.animation;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.AnimationManager;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.AnimationState;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 法术战斗、支援期间站定时不播放待机动作。
 * <p>
 * TLM 的 {@code idle} 是一段很长的待机序列（以 winefox 为例：前 16 秒静立，之后低头、抬手等小动作）， 走位间隙和两次施法之间站定时也会播到，战斗中看起来像在打哈欠、捡东西。这里在 {@code idle} 之前插入一个状态： 战斗中且站定时播放 {@code "empty"}（TLM 自己用来重置控制器的空动画名），主控制器不驱动骨骼，模型保持绑定姿势， 也就是各模型的静立姿势。眨眼、耳朵、尾巴等并行动画在其他控制器上，不受影响。
 * <p>
 * 战斗状态取自原版已同步的 aggressive 标志，由 {@code MagicMaidAdapter#syncSpellCombatFlag} 在服务端维护。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
@OnlyIn(Dist.CLIENT)
public final class SpellCombatIdleAnimation {
    /**
     * 与 TLM {@code AnimationRegister} 的 walk 判定同一阈值，两者互斥
     */
    private static final double MIN_SPEED = 0.05;

    /**
     * 与 walk、run 同一优先级段，低于受击、跳跃、游泳，高于 idle
     */
    private static final int PRIORITY = 3;

    private static boolean registered = false;

    private SpellCombatIdleAnimation() {
    }

    /**
     * 注册到 TLM 的主动画管理器；重复调用只注册一次。
     */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        AnimationManager.getInstance().register(new AnimationState("empty", ILoopType.EDefaultLoopTypes.LOOP,
            PRIORITY, SpellCombatIdleAnimation::test));
    }

    private static boolean test(IMaid maid, AnimationEvent<?> event) {
        Mob mob = maid.asEntity();
        // 判定与 walk、run 互斥，不依赖注册先后：移动或疾跑时仍按原样播放行走动画
        return mob instanceof MaidMagicEntity && mob.isAggressive() && mob.onGround() && !mob.isSprinting()
            && event.getLimbSwingAmount() <= MIN_SPEED;
    }
}
