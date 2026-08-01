package net.magicterra.winefoxsspellbooks.mixin;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 混入 recast 管理器，开放其内部表。
 * <p>
 * 该表是 private final，而摘表的公开入口 {@code removeRecast(String)} 带 {@code @OnlyIn(Dist.CLIENT)}、
 * 专服上会被剥掉，女仆结算时无从下手，故在这里开一个口子。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-31
 */
@Mixin(PlayerRecasts.class)
public interface PlayerRecastsAccessor {
    @Accessor
    Map<String, RecastInstance> getRecastLookup();
}
