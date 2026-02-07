package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.mixin.SummonManagerAccessor;
import net.magicterra.winefoxsspellbooks.registry.InitAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * 额外的召唤物管理器方法
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-29 00:18
 */
public class MaidSummonManager {

    /**
     * 递归追溯的最大深度，防止无限循环
     */
    private static final int MAX_SUMMON_DEPTH = 10;

    /**
     * 初始化并缓存实体的顶层召唤者 UUID
     * <p>
     * 应在召唤物创建后调用一次，后续查询直接读取缓存。
     *
     * @param entity 要初始化缓存的实体
     */
    public static void initRootSummonerCache(Entity entity) {
        if (entity == null) {
            return;
        }
        Entity root = getRootSummoner(entity);
        UUID rootUUID = root != null ? root.getUUID() : null;
        entity.setData(InitAttachments.ROOT_SUMMONER_UUID.get(), Optional.ofNullable(rootUUID));
    }

    /**
     * 获取顶层召唤者的 UUID（优先使用缓存）
     * <p>
     * 如果实体有缓存的 UUID，直接返回；否则遍历召唤链并缓存结果。
     *
     * @param entity 要追溯的实体
     * @return 顶层召唤者的 UUID，如果不是召唤物则返回 null
     */
    @Nullable
    public static UUID getRootSummonerUUID(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }

        // 检查是否有缓存
        if (entity.hasData(InitAttachments.ROOT_SUMMONER_UUID.get())) {
            Optional<UUID> cached = entity.getData(InitAttachments.ROOT_SUMMONER_UUID.get());
            // Optional 已存在说明已初始化，直接返回（可能是 null 表示不是召唤物）
            return cached.orElse(null);
        }

        // 没有缓存，遍历召唤链并缓存
        Entity root = getRootSummoner(entity);
        UUID rootUUID = root != null ? root.getUUID() : null;
        entity.setData(InitAttachments.ROOT_SUMMONER_UUID.get(), Optional.ofNullable(rootUUID));
        return rootUUID;
    }

    /**
     * 获取实体的有效顶层 UUID（顶层召唤者 UUID 或自身 UUID）
     *
     * @param entity 实体
     * @return 有效的顶层 UUID
     */
    @Nullable
    public static UUID getEffectiveRootUUID(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        UUID rootUUID = getRootSummonerUUID(entity);
        return rootUUID != null ? rootUUID : entity.getUUID();
    }

    /**
     * 获取顶层召唤者（递归追溯召唤链）
     * <p>
     * 对于嵌套召唤（玩家 → 女仆 → 召唤物），返回最顶层的召唤者（玩家）。
     * 如果实体不是召唤物，返回 null。
     *
     * @param entity 要追溯的实体
     * @return 顶层召唤者，如果不是召唤物则返回 null
     */
    @Nullable
    public static Entity getRootSummoner(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }

        Entity current = entity;
        int depth = 0;

        while (depth < MAX_SUMMON_DEPTH) {
            Entity summoner = getSummonerOrOwner(current);
            if (summoner == null) {
                // current 不是召唤物，它就是顶层
                // 但如果 current == entity，说明 entity 本身不是召唤物
                return current == entity ? null : current;
            }
            current = summoner;
            depth++;
        }

        // 达到最大深度，返回当前实体
        return current;
    }

    /**
     * 获取实体的直接召唤者或主人
     *
     * @param entity 实体
     * @return 召唤者或主人，如果没有则返回 null
     */
    @Nullable
    private static Entity getSummonerOrOwner(@Nullable Entity entity) {
        return switch (entity) {
            case IMagicSummon summon -> summon.getSummoner();
            case OwnableEntity ownable -> ownable.getOwner();
            case null, default -> null;
        };
    }

    /**
     * 获取实体的有效顶层实体（顶层召唤者或自身）
     * <p>
     * 如果实体是召唤物，返回顶层召唤者；否则返回实体本身。
     *
     * @param entity 实体
     * @return 有效的顶层实体
     */
    @Nullable
    public static Entity getEffectiveRoot(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        Entity root = getRootSummoner(entity);
        return root != null ? root : entity;
    }

    /**
     * 检查两个实体是否属于同一召唤链（共享相同的顶层召唤者）
     * <p>
     * 使用 UUID 比较，支持 Attachment 缓存优化。
     * 注意：此方法只检查召唤链，不检查同盟关系（避免递归）。
     *
     * @param entity1 第一个实体
     * @param entity2 第二个实体
     * @return 如果属于同一召唤链返回 true
     */
    public static boolean isSameSummonChain(@Nullable Entity entity1, @Nullable Entity entity2) {
        if (entity1 == null || entity2 == null) {
            return false;
        }
        if (entity1 == entity2) {
            return true;
        }

        // 使用 UUID 比较（支持缓存）
        UUID rootUUID1 = getEffectiveRootUUID(entity1);
        UUID rootUUID2 = getEffectiveRootUUID(entity2);

        return rootUUID1 != null && Objects.equals(rootUUID1, rootUUID2);
    }
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
