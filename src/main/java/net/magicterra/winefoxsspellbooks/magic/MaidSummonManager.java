package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.magicterra.winefoxsspellbooks.mixin.SummonManagerAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * 额外的召唤物管理器方法
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-29 00:18
 */
public class MaidSummonManager {
    public static void onMaidPlaced(LivingEntity maid) {
        if (maid.level instanceof ServerLevel serverLevel) {
            IronsDataStorage.INSTANCE.setDirty();
            SummonManagerAccessor manager = (SummonManagerAccessor) SummonManager.INSTANCE;
            var savedSummons = manager.getOfflineSummonersToSavedEntities().remove(maid.getUUID());
            if (savedSummons != null) {
                Set<UUID> summonsSet = new HashSet<>();
                UUID ownerUUID = maid.getUUID();
                for (CompoundTag summon : savedSummons) {
                    var summonedEntity = EntityType.create(summon, serverLevel).orElse(null);
                    if (summonedEntity != null) {
                        Vec3 position = maid.position();
                        summonedEntity.teleportTo(position.x, position.y, position.z);
                        serverLevel.addFreshEntityWithPassengers(summonedEntity);
                        var summonUUID = summonedEntity.getUUID();
                        summonsSet.add(summonUUID);
                        manager.getSummonToOwner().put(summonUUID, ownerUUID);
                        SummonManager.setDuration(summonedEntity, summon.getInt("summon_duration_remaining"));
                    }
                    manager.getOwnerToSummons().put(ownerUUID, summonsSet);
                }
            }
        }
    }

    public static void onMaidRemoved(LivingEntity maid) {
        SummonManagerAccessor manager = (SummonManagerAccessor) SummonManager.INSTANCE;
        Set<UUID> summons = manager.getOwnerToSummons().get(maid.getUUID());
        if (summons == null) {
            return;
        }
        if (maid.level.isClientSide) {
            return;
        }
        var serverLevel = ((ServerLevel) maid.level);
        var savedSummons = new ArrayList<CompoundTag>();
        for (UUID uuid : summons) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity != null) {
                CompoundTag saveData = new CompoundTag();
                entity.save(saveData);
                int durationRemaining = manager.invokeGetExpirationTick(entity.getUUID()) - serverLevel.getServer().getTickCount();
                saveData.putInt("summon_duration_remaining", durationRemaining);
                entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                savedSummons.add(saveData);
            }
        }
        IronsDataStorage.INSTANCE.setDirty();
        manager.getOfflineSummonersToSavedEntities().put(maid.getUUID(), savedSummons);
        manager.invokeStopTrackingSummonerAndSummons(maid);
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof EntityMaid maid) {
                    onMaidRemoved(maid);
                }
            }
        }
    }
}
