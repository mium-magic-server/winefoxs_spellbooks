package net.magicterra.winefoxsspellbooks.mixin;

import com.elfmcys.yesstevemodel.o000o0ooOOOOoooOoo0o0oOo;
import com.elfmcys.yesstevemodel.o00Ooooo0o0o0oOOoO00o0Oo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入动画控制器2
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-25 02:02
 */
@Mixin(o000o0ooOOOOoooOoo0o0oOo.class)
public interface YsmACStateAccessor2 {
    @Accessor("ooo00O0o000OOoOO00o00ooo")
    o00Ooooo0o0o0oOOoO00o0Oo getRealController();
}
