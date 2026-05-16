package net.magicterra.winefoxsspellbooks.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidGiftDelivery;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.magicterra.winefoxsspellbooks.registry.WsbLootTables;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

/**
 * 满好感度女仆的晨间礼物：灵狐精魂
 * <p>
 * 仿 vanilla 猫的晨间礼物 ({@code Cat$CatRelaxOnOwnerGoal#giveMorningGift}) 但更"主动"：
 * 这里只负责 "选女仆 + 抽战利品 + 写入 Brain 记忆 + 写冷却"，
 * 真正的"走过去、挥手、抛出"由 {@code MaidDeliverGiftBehavior} 在 Brain tick 中完成。
 * <p>
 * 战利品表 {@link WsbLootTables#MAID_MORNING_GIFT} 可由数据包整张覆盖。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class MaidGiftOnWakeHandler {
    /** TLM 好感度等级阈值：3 = 满好感度（≥384 点） */
    private static final int MIN_FAVORABILITY_FOR_GIFT = 3;

    /**
     * 赠送冷却（tick）：略小于一整日（24000 tick），允许"睡到天明后下一晚再睡"也能触发。
     * 选 gameTime 是因为它单调递增、不受 {@code /time set} 影响，比 dayTime 可靠。
     */
    private static final long GIFT_COOLDOWN_TICKS = 22000L;

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!Config.isVulpineGiftEnabled()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // 被怪/雷暴吵醒不算
        if (Config.isVulpineGiftRequireFullSleep() && event.wakeImmediately()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        long lastGift = player.getData(WsbAttachments.LAST_VULPINE_GIFT_TIME);
        if (now < lastGift + GIFT_COOLDOWN_TICKS) {
            return;
        }

        EntityMaid gifter = findGifter(player, level);
        if (gifter == null) {
            return;
        }

        List<ItemStack> drops = rollGiftTable(level, gifter);
        // 即使空表也写冷却，避免每次"醒来即抽"刷爆
        player.setData(WsbAttachments.LAST_VULPINE_GIFT_TIME, now);
        if (drops.isEmpty()) {
            return;
        }

        // 把投递任务交给女仆 Brain；走/挥手/抛出在 MaidDeliverGiftBehavior 中执行
        gifter.getBrain().setMemory(
            MaidCastingMemoryModuleTypes.GIFT_DELIVERY.get(),
            new MaidGiftDelivery(player, drops)
        );
    }

    /**
     * 在半径内挑出"满好感度 + 玩家驯化"女仆中距离最近的一只。
     * <p>
     * {@code SummonedEntityMaid} 是 {@code PathfinderMob}，不是 {@code EntityMaid} 子类，
     * 所以 {@link ServerLevel#getEntitiesOfClass} 限定 {@code EntityMaid.class} 时自然不会被纳入。
     */
    private static EntityMaid findGifter(ServerPlayer player, ServerLevel level) {
        double radius = Config.getVulpineGiftRadius();
        AABB box = player.getBoundingBox().inflate(radius);
        return level.getEntitiesOfClass(EntityMaid.class, box, maid ->
                maid.isTame()
                    && Objects.equals(maid.getOwnerUUID(), player.getUUID())
                    && maid.getFavorabilityManager().getLevel() >= MIN_FAVORABILITY_FOR_GIFT)
            .stream()
            .min(Comparator.comparingDouble(maid -> maid.distanceToSqr(player)))
            .orElse(null);
    }

    private static List<ItemStack> rollGiftTable(ServerLevel level, EntityMaid gifter) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(WsbLootTables.MAID_MORNING_GIFT);
        LootParams params = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, gifter.position())
            .withParameter(LootContextParams.THIS_ENTITY, gifter)
            .create(LootContextParamSets.GIFT);
        return table.getRandomItems(params);
    }
}
