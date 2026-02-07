package net.magicterra.winefoxsspellbooks.magic.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * 法术导致的药水效果数据
 * 定义法术施放后导致的主要药水效果，用于判断法术是否需要重新施放
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-31
 */
public record SpellCausedEffectData(List<Entry> entries) {

    /**
     * 单个法术效果条目
     *
     * @param spellId  法术 ID
     * @param effectId 药水效果 ID
     */
    public record Entry(ResourceLocation spellId, ResourceLocation effectId) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("spell").forGetter(Entry::spellId),
            ResourceLocation.CODEC.fieldOf("effect").forGetter(Entry::effectId)
        ).apply(instance, Entry::new));
    }

    public static final Codec<SpellCausedEffectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Entry.CODEC.listOf().fieldOf("values").forGetter(SpellCausedEffectData::entries)
    ).apply(instance, SpellCausedEffectData::new));

    /**
     * 创建构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final java.util.ArrayList<Entry> entries = new java.util.ArrayList<>();

        public Builder add(String spellId, String effectId) {
            entries.add(new Entry(ResourceLocation.parse(spellId), ResourceLocation.parse(effectId)));
            return this;
        }

        public Builder add(ResourceLocation spellId, ResourceLocation effectId) {
            entries.add(new Entry(spellId, effectId));
            return this;
        }

        public SpellCausedEffectData build() {
            return new SpellCausedEffectData(List.copyOf(entries));
        }
    }
}
