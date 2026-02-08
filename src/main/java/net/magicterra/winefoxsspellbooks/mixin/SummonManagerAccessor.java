package net.magicterra.winefoxsspellbooks.mixin;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 混入召唤物管理器
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-29 02:27
 */
@Mixin(SummonManager.class)
public interface SummonManagerAccessor {
    @Accessor
    HashMap<UUID, List<CompoundTag>> getOfflineSummonersToSavedEntities();

    @Accessor
    HashMap<UUID, UUID> getSummonToOwner();

    @Accessor
    HashMap<UUID, Set<UUID>> getOwnerToSummons();

    @Accessor
    PriorityQueue<Object> getSummonExpirations();

    @Invoker("getExpirationTick")
    int invokeGetExpirationTick(UUID uuid);

    @Invoker("stopTrackingSummonerAndSummons")
    void invokeStopTrackingSummonerAndSummons(Entity maid);
}
