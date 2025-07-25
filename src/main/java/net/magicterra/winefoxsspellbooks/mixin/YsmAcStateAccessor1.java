package net.magicterra.winefoxsspellbooks.mixin;

import com.elfmcys.yesstevemodel.o000o0ooOOOOoooOoo0o0oOo;
import com.elfmcys.yesstevemodel.oooo00oOO0o0oO0OoOoO0o0o;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入动画控制器1
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 01:57
 */
@Mixin(oooo00oOO0o0oO0OoOoO0o0o.class)
public interface YsmAcStateAccessor1 {
    @Accessor("ooo00O0o000OOoOO00o00ooo")
    o000o0ooOOOOoooOoo0o0oOo<?> getWrappedAnimateController();
}
