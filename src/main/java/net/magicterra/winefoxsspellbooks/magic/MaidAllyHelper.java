package net.magicterra.winefoxsspellbooks.magic;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.registry.InitAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;

/**
 * 结盟处理
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-02-08 18:14
 */
public class MaidAllyHelper {
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
     * 对于嵌套召唤（玩家 → 女仆 → 召唤物），返回最顶层的召唤者（玩家）。 如果实体不是召唤物，返回 null。
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
        UUID effectiveRootUUID = getEffectiveRootUUID(entity);
        Level level = entity.level();
        Entity root = null;
        if (effectiveRootUUID != null && level instanceof ServerLevel serverLevel) {
            root = serverLevel.getEntity(effectiveRootUUID);
        }
        if (root == null) {
            root = getRootSummoner(entity);
        }
        return root != null ? root : entity;
    }

    /**
     * 检查两个实体是否属于同一召唤链（共享相同的顶层召唤者）
     * <p>
     * 使用 UUID 比较，支持 Attachment 缓存优化。 注意：此方法只检查召唤链，不检查同盟关系（避免递归）。
     *
     * @param entity1 第一个实体
     * @param entity2 第二个实体
     * @return 如果属于同一召唤链返回 true
     */
    public static boolean isAllied(@Nullable Entity entity1, @Nullable Entity entity2) {
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
}
