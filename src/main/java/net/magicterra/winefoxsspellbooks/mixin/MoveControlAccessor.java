package net.magicterra.winefoxsspellbooks.mixin;

import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 读写 {@link MoveControl} 的速度倍率
 * <p>
 * 原版 {@link MoveControl#strafe} 把速度倍率固定为 0.25，女仆后撤时须在其后改回自己的倍率。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-26
 */
@Mixin(MoveControl.class)
public interface MoveControlAccessor {
    @Accessor("speedModifier")
    void winefoxsSpellbooks$setSpeedModifier(double speedModifier);
}
