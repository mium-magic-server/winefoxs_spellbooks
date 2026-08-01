package net.magicterra.winefoxsspellbooks.mixin;

import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入 recast 实例，开放其剩余计时。
 * <p>
 * {@code remainingTicks} 虽是 protected，但 {@code MaidRecasts} 并非它的子类，隔包够不着，且没留 setter，
 * 故在这里开一个口子。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-31
 */
@Mixin(RecastInstance.class)
public interface RecastInstanceAccessor {
    @Accessor
    void setRemainingTicks(int remainingTicks);
}
