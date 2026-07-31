package net.magicterra.winefoxsspellbooks.magic;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.mixin.SummonManagerAccessor;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;

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

        // 缓存里的空值有两种来源：实体确实不是召唤物，或当初主人离线/未加载没解析出来。
        // 后者不能当结论用，对仍然宣称有主人的实体重新解析（存档里被写坏的缓存也在这里自愈）
        if (entity.hasData(WsbAttachments.ROOT_SUMMONER_UUID.get())) {
            Optional<UUID> cached = entity.getData(WsbAttachments.ROOT_SUMMONER_UUID.get());
            if (cached.isPresent() || !claimsOwner(entity)) {
                return cached.orElse(null);
            }
        }

        // 没有可信缓存，遍历召唤链
        RootLookup lookup = lookupRootSummoner(entity);
        // 链没走到顶就不要落盘：附件随实体存档且永不失效，一旦把"没有主人"定死，
        // 主人回来后这只女仆就永远不再被认作自家人（护盾、酒池回魔、酒雾豁免全部失效）。
        if (lookup.resolved()) {
            entity.setData(WsbAttachments.ROOT_SUMMONER_UUID.get(), Optional.ofNullable(lookup.rootUUID()));
        }
        return lookup.rootUUID();
    }

    /**
     * 召唤链追溯结果
     *
     * @param root     顶层召唤者；实体自身不是召唤物时为 null
     * @param resolved 是否走到了真正的顶层。中途因主人离线/未加载而断链时为 false， 此时 {@code root} 只是能查到的最上一层，不可作为结论缓存
     */
    private record RootLookup(@Nullable Entity root, boolean resolved) {
        @Nullable
        UUID rootUUID() {
            return root != null ? root.getUUID() : null;
        }
    }

    /**
     * 追溯召唤链并附带"是否走到顶"的判定
     */
    private static RootLookup lookupRootSummoner(Entity entity) {
        Entity current = entity;
        int depth = 0;

        while (depth < MAX_SUMMON_DEPTH) {
            Entity summoner = getSummonerOrOwner(current);
            if (summoner == null) {
                // current 查不出上一级：要么它本来就是顶层，要么它的主人此刻不可达
                Entity root = current == entity ? null : current;
                return new RootLookup(root, !claimsOwner(current));
            }
            current = summoner;
            depth++;
        }

        // 达到最大深度，返回当前实体
        return new RootLookup(current, true);
    }

    /**
     * 实体是否仍然宣称有主人/召唤者：用来区分"确实到顶了"与"主人此刻不可达"。
     */
    private static boolean claimsOwner(Entity entity) {
        if (entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return true;
        }
        return ((SummonManagerAccessor) SummonManager.INSTANCE).getSummonToOwner().containsKey(entity.getUUID());
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
        UUID rootUUID = getEffectiveRootUUID(entity);
        if (rootUUID != null && entity.level() instanceof ServerLevel serverLevel) {
            Entity root = serverLevel.getEntity(rootUUID);
            if (root != null) {
                return root;
            }
        }
        // 顶层 UUID 查不到实体（跨维度/未加载）时退回实体链，链上最后一个可达的祖先也好过没有
        Entity reachable = lookupRootSummoner(entity).root();
        return reachable != null ? reachable : entity;
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
