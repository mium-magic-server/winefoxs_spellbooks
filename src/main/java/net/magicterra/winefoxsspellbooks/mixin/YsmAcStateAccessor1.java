package net.magicterra.winefoxsspellbooks.mixin;

import com.elfmcys.yesstevemodel.o0oOoOoOoOO0Ooo000OO0ooo;
import com.elfmcys.yesstevemodel.oOo0OooO0oOo0oOOOoo00o0O;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入动画控制器1
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 01:57
 */
@Mixin(oOo0OooO0oOo0oOOOoo00o0O.class)
public interface YsmAcStateAccessor1 {
    @Accessor("O0OOooOOOo0O0o0oOO00OOoo")
    o0oOoOoOoOO0Ooo000OO0ooo<?> getWrappedAnimateController();
}
