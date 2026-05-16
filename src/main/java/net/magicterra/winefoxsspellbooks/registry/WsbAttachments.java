package net.magicterra.winefoxsspellbooks.registry;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 初始化附件对象
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-23 02:34
 */
public class WsbAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WinefoxsSpellbooks.MODID);


    public static final Supplier<AttachmentType<MaidSpellDataHolder>> MAID_SPELL_DATA = ATTACHMENT_TYPES.register(
        "iron_spell_data", () -> AttachmentType.builder(MaidSpellDataHolder::new).serialize(MaidSpellDataHolder.CODEC).sync(MaidSpellDataHolder.STREAM_CODEC).build()
    );

    public static final Supplier<AttachmentType<Float>> MAID_MANA = ATTACHMENT_TYPES.register(
        "iron_mana", () -> AttachmentType.builder(() -> 0F).serialize(Codec.FLOAT).sync(ByteBufCodecs.FLOAT).build()
    );

    /**
     * 缓存的顶层召唤者 UUID
     * <p>
     * 用于快速判断召唤链关系，避免每次都遍历召唤链。
     * 使用 Optional 包装，空 Optional 表示未初始化，Optional.empty() 内容表示不是召唤物。
     */
    public static final Supplier<AttachmentType<Optional<UUID>>> ROOT_SUMMONER_UUID = ATTACHMENT_TYPES.register(
        "root_summoner_uuid", () -> AttachmentType.<Optional<UUID>>builder(Optional::empty)
            .serialize(UUIDUtil.CODEC.optionalFieldOf("uuid").codec())
            .build()
    );

    public static final Supplier<AttachmentType<List<CompoundTag>>> SAVED_SUMMONS = ATTACHMENT_TYPES.register(
        "saved_summons", () -> AttachmentType.<List<CompoundTag>>builder(Collections::emptyList)
            .serialize(CompoundTag.CODEC.listOf().fieldOf("saved_summons").codec())
            .build()
    );

    /**
     * 女仆晨赠灵狐精魂的"上次赠送游戏时间"
     * <p>
     * 跨女仆共享：哪只满好感度女仆送过都会写到玩家身上，防多女仆刷魂。
     * <p>
     * 单位：tick（{@link net.minecraft.world.level.Level#getGameTime()}）。0L 表示从未触发。
     * 选 gameTime 而不是 dayTime，是因为 dayTime 可被 {@code /time set} 随意拨动，
     * 而 gameTime 单调递增、不受玩家命令影响。
     */
    public static final Supplier<AttachmentType<Long>> LAST_VULPINE_GIFT_TIME = ATTACHMENT_TYPES.register(
        "last_vulpine_gift_time", () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
