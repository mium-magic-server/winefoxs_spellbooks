package net.magicterra.winefoxsspellbooks.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 女仆法力值
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-23 02:20
 */
public class MaidMana {
    public static final Supplier<MaidMana> DEFAULT_FACTORY = () -> new MaidMana(0F);

    public static final Codec<MaidMana> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.optionalFieldOf("mana", 0F).forGetter(h -> h.mana)
    ).apply(instance, MaidMana::new));

    public static final StreamCodec<ByteBuf, MaidMana> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, MaidMana::getMana, MaidMana::new);

    private float mana;

    public MaidMana(float mana) {
        this.mana = mana;
    }

    public float getMana() {
        return mana;
    }

    public void setMana(float mana) {
        this.mana = mana;
    }

    @Override
    public String toString() {
        return String.format("mana=%.2f", mana);
    }
}
