package net.magicterra.winefoxsspellbooks.magic.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * 法术施法范围数据
 * 定义女仆在使用某些法术时需要靠近到指定距离再施法
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-31
 */
public record SpellCastingRangeData(List<Entry> entries) {

    /**
     * 单个法术施法范围条目
     *
     * @param spellId 法术 ID
     * @param range   施法范围（方块）
     */
    public record Entry(ResourceLocation spellId, float range) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("spell").forGetter(Entry::spellId),
            Codec.FLOAT.fieldOf("range").forGetter(Entry::range)
        ).apply(instance, Entry::new));
    }

    public static final Codec<SpellCastingRangeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Entry.CODEC.listOf().fieldOf("values").forGetter(SpellCastingRangeData::entries)
    ).apply(instance, SpellCastingRangeData::new));

    /**
     * 创建构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final java.util.ArrayList<Entry> entries = new java.util.ArrayList<>();

        public Builder add(String spellId, float range) {
            entries.add(new Entry(ResourceLocation.parse(spellId), range));
            return this;
        }

        public Builder add(ResourceLocation spellId, float range) {
            entries.add(new Entry(spellId, range));
            return this;
        }

        public SpellCastingRangeData build() {
            return new SpellCastingRangeData(List.copyOf(entries));
        }
    }
}
