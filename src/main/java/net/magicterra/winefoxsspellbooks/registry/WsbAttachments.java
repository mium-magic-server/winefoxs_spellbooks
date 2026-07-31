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
import org.apache.commons.lang3.mutable.MutableInt;

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
     * 用于快速判断召唤链关系，避免每次都遍历召唤链。 附件不存在表示尚未查询过；存在但为 {@code Optional.empty()} 表示实体不是召唤物。
     * 追溯中途主人不可达时不写入，留待下次重试。
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
     * 女仆晨间礼物（灵狐精魂）的"上次赠送游戏时间"
     * <p>
     * 跨女仆共享：哪只满好感度女仆送过都会写到玩家身上，防多女仆刷魂。
     * <p>
     * 单位：tick（{@link net.minecraft.world.level.Level#getGameTime()}）。0L 表示从未触发。 选 gameTime 而不是 dayTime，是因为 dayTime 可被 {@code /time set} 随意拨动， 而 gameTime 单调递增、不受玩家命令影响。
     */
    public static final Supplier<AttachmentType<Long>> LAST_VULPINE_GIFT_TIME = ATTACHMENT_TYPES.register(
        "last_vulpine_gift_time", () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).build()
    );

    /**
     * 本维度内存活的召唤女仆数（仅逻辑服务端维护，不入存档）。
     * <p>
     * 挂在 {@link net.minecraft.world.level.Level} 上而不是另开静态表：计数随世界对象一起回收， 维度卸载后既不会留下把 {@code ServerLevel} 钉死在进程里的条目，也不用再补一路卸载事件去擦。
     */
    public static final Supplier<AttachmentType<MutableInt>> SUMMONED_MAID_COUNT = ATTACHMENT_TYPES.register(
        "summoned_maid_count", () -> AttachmentType.builder(() -> new MutableInt()).build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
