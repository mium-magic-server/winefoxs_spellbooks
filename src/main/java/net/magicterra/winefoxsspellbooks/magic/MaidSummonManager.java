package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.magicterra.winefoxsspellbooks.entity.summon.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.summon.SummonedMaidBroom;
import net.magicterra.winefoxsspellbooks.mixin.SummonManagerAccessor;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
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
        if (!(maid.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        var savedSummons = maid.getExistingDataOrNull(WsbAttachments.SAVED_SUMMONS);
        if (savedSummons == null) {
            return;
        }

        SummonManagerAccessor manager = (SummonManagerAccessor) SummonManager.INSTANCE;
        Set<UUID> summonsSet = new HashSet<>();
        // 这一轮没能反序列化出来的原样留着,下次加载再试
        var unrestored = new ArrayList<CompoundTag>();
        UUID ownerUUID = maid.getUUID();
        Vec3 position = maid.position();
        for (CompoundTag summon : savedSummons) {
            var summonedEntity = EntityType.create(summon, serverLevel).orElse(null);
            if (summonedEntity == null) {
                unrestored.add(summon);
                continue;
            }
            if (summonedEntity instanceof SummonedMaidBroom) {
                // 扫帚要连同骑乘者一起注册，否则骑在上面的女仆不会被记进召唤链
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
        // 并入而非覆盖：整表 put 会把这只女仆名下已有的追踪抹掉，被抹掉的召唤物不再被计时解除，
        // 会永久留在世界里；逐条登记的 summonToOwner 本就是并入语义，两张表在这里保持一致。
        // 一个都没恢复出来时也不建空条目：空 Set 会让这只女仆被当成"有召唤物的召唤者"带进
        // IronsDataStorage 的离线召唤表，还会让 SummonManager.onSummonerLogin 失去 isEmpty 快速返回。
        if (!summonsSet.isEmpty()) {
            manager.getOwnerToSummons().computeIfAbsent(ownerUUID, key -> new HashSet<>()).addAll(summonsSet);
        }
        // 附件只是一次性的搬运凭证，恢复出来的用完即弃。留着的话：召唤物到期解除后追踪表已空，女仆下次卸载时
        // onMaidRemoved 会提前返回而不覆盖附件，再加载就照着旧 NBT 把早该消失的召唤物重新生成出来。
        // 但没恢复出来的不能跟着删：实体 id 认不出来多半是附属被临时禁用，直接清掉等于把召唤物永久丢弃。
        if (unrestored.isEmpty()) {
            maid.removeData(WsbAttachments.SAVED_SUMMONS);
        } else {
            maid.setData(WsbAttachments.SAVED_SUMMONS, unrestored);
        }
    }

    public static void onMaidRemoved(LivingEntity maid) {
        // 服务端判定必须在读召唤表之前：单人模式下客户端女仆与服务端同 UUID，
        // 客户端线程去读服务端线程正在改的普通 HashMap，并发扩容时会自旋
        if (!(maid.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        SummonManagerAccessor manager = (SummonManagerAccessor) SummonManager.INSTANCE;
        Set<UUID> summons = manager.getOwnerToSummons().get(maid.getUUID());
        if (summons == null) {
            return;
        }
        var savedSummons = new ArrayList<CompoundTag>();
        // 附件此刻只可能剩上次没恢复出来的条目(onAddedToLevel 必先于 onRemovedFromLevel,恢复成功的已被摘掉),
        // 带上它们一起写回，否则这只女仆期间新召唤的东西一存档就把它们顶掉
        var unrestored = maid.getExistingDataOrNull(WsbAttachments.SAVED_SUMMONS);
        if (unrestored != null) {
            savedSummons.addAll(unrestored);
        }
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
        maid.setData(WsbAttachments.SAVED_SUMMONS, savedSummons);
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
            // getAllEntities() 是实体表的活视图，而 saveSummons 会经 onMaidRemoved 把召唤物 setRemoved，
            // 直接从这张表里摘节点。边遍历边删会跳过排在后面的女仆（她们的召唤物就此丢失），
            // 先快照再处理。
            for (Entity entity : Lists.newArrayList(level.getAllEntities())) {
                if (entity instanceof LivingEntity livingEntity) {
                    saveSummons(level, livingEntity);
                }
            }
        }
    }
}
