package net.magicterra.winefoxsspellbooks.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.ConcatenatedListView;

/**
 * 女仆当前可用的法术，按 {@link MaidSpellAction} 行为类别分组。
 * <p>
 * NBT/JSON 字段名取自 {@link MaidSpellAction#catalogName()}，空列表省略；网络包按枚举声明顺序写。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-09-11 00:41
 */
public class MaidSpellDataHolder {
    /**
     * 法术列表 Codec
     */
    private static final Codec<List<SpellData>> SPELL_LIST_CODEC = SpellData.CODEC.listOf();

    /**
     * 法术列表 StreamCodec
     */
    private static final StreamCodec<ByteBuf, List<SpellData>> SPELL_LIST_STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, (SpellData spellData) -> spellData.getSpell().getSpellResource(),
        ByteBufCodecs.INT, SpellData::getLevel,
        ByteBufCodecs.BOOL, SpellData::isLocked,
        SpellData::new
    ).apply(ByteBufCodecs.list());

    /**
     * 落盘字段名 → 行为类别的查表。
     */
    private static final Map<String, MaidSpellAction> CATALOG_NAMES = Arrays.stream(MaidSpellAction.ALL)
        .collect(Collectors.toUnmodifiableMap(MaidSpellAction::catalogName, Function.identity()));

    /**
     * 行为类别 ↔ 其 {@code catalogName}（即落盘字段名）。
     * <p>
     * 用 {@code comapFlatMap} 而非 {@code xmap}，认不出的字段名要报错而不是抛异常： 玩家把本模组降级到还没有某个类别的版本时，存档里会留着未来版本写的字段， 那时该跳过它继续读，不能崩在实体加载上。
     */
    private static final Codec<MaidSpellAction> ACTION_CODEC = Codec.STRING.comapFlatMap(
        name -> {
            MaidSpellAction action = CATALOG_NAMES.get(name);
            return action != null
                   ? DataResult.success(action)
                   : DataResult.error(() -> "Unknown maid spell category '" + name + "'");
        },
        MaidSpellAction::catalogName
    );

    /**
     * MaidSpellDataHolder 的 Codec。键集固定为枚举，用 {@code unboundedMap} 产出的
     * JSON/NBT 结构与逐字段写相同，加一类法术零改动。
     */
    public static final Codec<MaidSpellDataHolder> CODEC =
        Codec.unboundedMap(ACTION_CODEC, SPELL_LIST_CODEC).xmap(
            map -> {
                MaidSpellDataHolder holder = new MaidSpellDataHolder();
                map.forEach(holder::update);
                return holder;
            },
            holder -> {
                // 空列表不写
                Map<MaidSpellAction, List<SpellData>> map = new LinkedHashMap<>();
                for (MaidSpellAction action : MaidSpellAction.ALL) {
                    List<SpellData> spells = holder.get(action);
                    if (!spells.isEmpty()) {
                        map.put(action, spells);
                    }
                }
                return map;
            }
        );

    public static final StreamCodec<ByteBuf, MaidSpellDataHolder> STREAM_CODEC = new StreamCodec<>() {
        @Nonnull
        @Override
        public MaidSpellDataHolder decode(@Nonnull ByteBuf b) {
            MaidSpellDataHolder holder = new MaidSpellDataHolder();
            // 读写顺序都是 MaidSpellAction 的声明顺序，严格对称
            for (MaidSpellAction action : MaidSpellAction.ALL) {
                holder.update(action, SPELL_LIST_STREAM_CODEC.decode(b));
            }
            return holder;
        }

        @Override
        public void encode(@Nonnull ByteBuf b, @Nonnull MaidSpellDataHolder h) {
            for (MaidSpellAction action : MaidSpellAction.ALL) {
                SPELL_LIST_STREAM_CODEC.encode(b, h.get(action));
            }
        }
    };

    /**
     * 类别 → 该类可用法术。七个桶在构造时建好，之后只清空重填，从不换实例。
     */
    private final Map<MaidSpellAction, ArrayList<SpellData>> spells = new EnumMap<>(MaidSpellAction.class);

    /**
     * 全类别的拼接视图。因为底层 ArrayList 实例恒定，这个视图建一次即可长期有效， 不必每次 {@link #getAllSpells()} 重新拼。
     */
    private final Collection<SpellData> allSpellsView;

    /**
     * 默认构造函数
     */
    public MaidSpellDataHolder() {
        for (MaidSpellAction action : MaidSpellAction.ALL) {
            spells.put(action, new ArrayList<>());
        }
        this.allSpellsView = ConcatenatedListView.of(List.copyOf(spells.values()));
    }

    // ==================== 读取 ====================

    /**
     * 取某一类别的可用法术。返回活引用——调用方多是热路径上的只读遍历，不做防御性拷贝。
     *
     * @param action 行为类别
     * @return 该类别的法术列表
     */
    public List<SpellData> get(MaidSpellAction action) {
        return spells.get(action);
    }

    // ==================== 判断方法 ====================

    /**
     * 是否持有任意可用法术。
     */
    public boolean hasAnySpells() {
        return hasAnyOf(MaidSpellAction.ALL);
    }

    /**
     * 施法任务（{@code MaidCastingTask}）用得上的法术是否至少有一个。
     */
    public boolean hasAnyCastingTaskSpells() {
        return hasAnyOf(MaidSpellAction.ATTACK, MaidSpellAction.DEFENSE, MaidSpellAction.MOVEMENT,
            MaidSpellAction.SUPPORT, MaidSpellAction.NEGATIVE);
    }

    /**
     * 支援任务（{@code MaidMagicSupportTask}）用得上的法术是否至少有一个。
     */
    public boolean hasAnyMagicSupportTaskSpells() {
        return hasAnyOf(MaidSpellAction.DEFENSE, MaidSpellAction.SUPPORT, MaidSpellAction.POSITIVE,
            MaidSpellAction.NEGATIVE, MaidSpellAction.SUPPORT_OTHER);
    }

    private boolean hasAnyOf(MaidSpellAction... actions) {
        for (MaidSpellAction action : actions) {
            if (!spells.get(action).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // ==================== 更新方法 ====================

    /**
     * 整体替换某一类别的法术。
     *
     * @param action 行为类别
     * @param source 新的法术集合（内容被拷入，不持有其引用）
     */
    public void update(MaidSpellAction action, Collection<SpellData> source) {
        ArrayList<SpellData> target = spells.get(action);
        target.clear();
        target.addAll(source);
    }

    /**
     * 全部类别的法术拼接视图（惰性，不拷贝）。
     */
    public Collection<SpellData> getAllSpells() {
        return allSpellsView;
    }

    public void clear() {
        spells.values().forEach(ArrayList::clear);
    }
}
