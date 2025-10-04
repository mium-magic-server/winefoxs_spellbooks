package net.magicterra.winefoxsspellbooks.mixin;

import com.elfmcys.yesstevemodel.Oo0oOOOOo00OoOO00ooooO0o;
import com.elfmcys.yesstevemodel.OoO0000OO0Oo0OooO0O00OO0;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入动画控制器1
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 01:57
 */
@Mixin(OoO0000OO0Oo0OooO0O00OO0.class)
public interface YsmAcStateAccessor1 {
    @Accessor("O00OOO0O000oO00O0oOo0O0o")
    Oo0oOOOOo00OoOO00ooooO0o<?> getWrappedAnimateController();
}
