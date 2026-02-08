package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.magicterra.winefoxsspellbooks.mixin.SummonManagerAccessor;
import net.magicterra.winefoxsspellbooks.registry.InitAttachments;
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
            SummonManagerAccessor manager = (SummonManagerAccessor) SummonManager.INSTANCE;
            var savedSummons = maid.getExistingDataOrNull(InitAttachments.SAVED_SUMMONS);
            if (savedSummons != null) {
                Set<UUID> summonsSet = new HashSet<>();
                UUID ownerUUID = maid.getUUID();
                for (CompoundTag summon : savedSummons) {
                    var summonedEntity = EntityType.create(summon, serverLevel).orElse(null);
                    if (summonedEntity != null) {
                        Vec3 position = maid.position();
                        if (summonedEntity instanceof SummonedMaidBroom) {
                            summonedEntity.getSelfAndPassengers().forEach(entity -> {
                                serverLevel.addFreshEntity(entity);
                                var summonUUID = entity.getUUID();
                                summonsSet.add(summonUUID);
                                manager.getSummonToOwner().put(summonUUID, ownerUUID);
                                SummonManager.setDuration(entity, summon.getInt("summon_duration_remaining"));
                            });
                        } else {
                            serverLevel.addFreshEntityWithPassengers(summonedEntity);
                            var summonUUID = summonedEntity.getUUID();
                            summonsSet.add(summonUUID);
                            manager.getSummonToOwner().put(summonUUID, ownerUUID);
                            SummonManager.setDuration(summonedEntity, summon.getInt("summon_duration_remaining"));
                        }
                        summonedEntity.teleportTo(position.x, position.y, position.z);
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
                if (entity instanceof SummonedEntityMaid) {
                    entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                } else {
                    CompoundTag saveData = new CompoundTag();
                    entity.save(saveData);
                    int durationRemaining = manager.invokeGetExpirationTick(entity.getUUID()) - serverLevel.getServer().getTickCount();
                    saveData.putInt("summon_duration_remaining", durationRemaining);
                    entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                    savedSummons.add(saveData);
                }
            }
        }
        maid.setData(InitAttachments.SAVED_SUMMONS, savedSummons);
        manager.invokeStopTrackingSummonerAndSummons(maid);
    }

    public static void saveSummons(ServerLevel serverLevel, LivingEntity maid) {
        if (!(maid instanceof EntityMaid || maid instanceof SummonedEntityMaid || maid instanceof SummonedMaidBroom)) {
            return;
        }
        Set<UUID> summons = SummonManager.getSummons(maid);
        for (UUID summon : summons) {
            Entity entity = serverLevel.getEntity(summon);
            if (entity instanceof LivingEntity livingEntity) {
                saveSummons(serverLevel, livingEntity);
            }
        }
        onMaidRemoved(maid);
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity livingEntity) {
                    saveSummons(level, livingEntity);
                }
            }
        }
    }
}
