package net.magicterra.winefoxsspellbooks.mixin;

import com.elfmcys.yesstevemodel.o0OoOOO00o0OooOoo0O00OoO;
import com.elfmcys.yesstevemodel.o0oOoOoOoOO0Ooo000OO0ooo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入动画控制器2
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 02:02
 */
@Mixin(o0oOoOoOoOO0Ooo000OO0ooo.class)
public interface YsmACStateAccessor2 {
    @Accessor("O0OOooOOOo0O0o0oOO00OOoo")
    o0OoOOO00o0OooOoo0O00OoO getRealController();
}
