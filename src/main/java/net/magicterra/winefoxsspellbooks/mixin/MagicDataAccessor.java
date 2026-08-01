package net.magicterra.winefoxsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入魔力数据，开放其 recast 管理器字段。
 * <p>
 * Iron's 自带的同名 setter 带 {@code @OnlyIn(Dist.CLIENT)}、专服上会被剥掉，故在这里另开一个口子；
 * 客户端那侧原方法还在，方法名也就不能再叫 {@code setPlayerRecasts}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-31
 */
@Mixin(MagicData.class)
public interface MagicDataAccessor {
    @Accessor("playerRecasts")
    void winefoxsSpellbooks$setPlayerRecasts(PlayerRecasts playerRecasts);
}
