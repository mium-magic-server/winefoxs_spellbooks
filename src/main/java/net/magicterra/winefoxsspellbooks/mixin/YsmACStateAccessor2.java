package net.magicterra.winefoxsspellbooks.mixin;

import com.elfmcys.yesstevemodel.OOOO0oo0O0o0Oo00O0o0oOOO;
import com.elfmcys.yesstevemodel.Oo0oOOOOo00OoOO00ooooO0o;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入动画控制器2
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 02:02
 */
@Mixin(Oo0oOOOOo00OoOO00ooooO0o.class)
public interface YsmACStateAccessor2 {
    @Accessor("O00OOO0O000oO00O0oOo0O0o")
    OOOO0oo0O0o0Oo00O0o0oOOO getRealController();
}
