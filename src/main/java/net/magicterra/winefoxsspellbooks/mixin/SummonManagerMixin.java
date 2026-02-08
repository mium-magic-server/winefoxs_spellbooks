package net.magicterra.winefoxsspellbooks.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 处理召唤的女仆持久化问题
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-02-08 16:01
 */
@Mixin(SummonManager.class)
public abstract class SummonManagerMixin {
    @Shadow
    protected abstract int getExpirationTick(UUID uuid);

    @Expression("entity != null")
    @Definition(id = "entity", local = @Local(name = "entity", type = Entity.class))
    @ModifyExpressionValue(method = "handlePlayerDisconnect", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean injectHandlePlayerDisconnect(
        boolean original,
        ServerPlayer serverPlayer,
        @Local(name = "serverLevel") ServerLevel serverLevel,
        @Local(name = "savedSummons") ArrayList<CompoundTag> savedSummons,
        @Local(name = "entity") Entity entity) {
        if (!original) {
            return false;
        }
        if (entity instanceof SummonedEntityMaid maid && maid.isAirForce()) {
            return false;
        } else if (entity instanceof SummonedMaidBroom) {
            CompoundTag saveData = new CompoundTag();
            entity.save(saveData);
            int durationRemaining = getExpirationTick(entity.getUUID()) - serverLevel.getServer().getTickCount();
            saveData.putInt("summon_duration_remaining", durationRemaining);
            entity.getSelfAndPassengers().forEach(e -> e.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
            savedSummons.add(saveData);
            return false;
        }
        return true;
    }

    @Expression("summonedEntity != null")
    @Definition(id = "summonedEntity", local = @Local(name = "summonedEntity", type = Entity.class))
    @ModifyExpressionValue(method = "onPlayerLogin", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static boolean injectOnPlayerLogin(
        boolean original,
        PlayerEvent.PlayerLoggedInEvent event,
        @Local(name = "serverLevel") ServerLevel serverLevel,
        @Local(name = "summonsSet") Set<UUID> summonsSet,
        @Local(name = "summon") CompoundTag summon,
        @Local(name = "summonedEntity") Entity summonedEntity) {
        if (!original) {
            return false;
        }
        Player player = event.getEntity();
        UUID ownerUUID = player.getUUID();
        SummonManagerAccessor manager = (SummonManagerAccessor) SummonManager.INSTANCE;
        if (summonedEntity instanceof SummonedMaidBroom) {
            summonedEntity.getSelfAndPassengers().forEach(entity -> {
                serverLevel.addFreshEntity(entity);
                var summonUUID = entity.getUUID();
                summonsSet.add(summonUUID);
                manager.getSummonToOwner().put(summonUUID, ownerUUID);
                SummonManager.setDuration(entity, summon.getInt("summon_duration_remaining"));
            });
            return false;
        }
        return true;
    }
}
