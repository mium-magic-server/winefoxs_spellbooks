package net.magicterra.winefoxsspellbooks.magic.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 残月引爆发射器的选项：只需要 AoE 半径，客户端据此把环波铺到实际伤害边界上，让女仆更大的引爆范围也看得见。 发射器自身不渲染，出生点即引爆点，故不用带实体 ID。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-31
 */
public record CrescentBurstOption(ParticleType<CrescentBurstOption> particleType, int radius) implements ParticleOptions {

    public static MapCodec<CrescentBurstOption> codec(ParticleType<CrescentBurstOption> particleType) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("radius").forGetter(CrescentBurstOption::radius)
        ).apply(instance, radius -> new CrescentBurstOption(particleType, radius)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, CrescentBurstOption> streamCodec(
        ParticleType<CrescentBurstOption> particleType) {
        return StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CrescentBurstOption::radius,
            radius -> new CrescentBurstOption(particleType, radius));
    }

    @Override
    public ParticleType<CrescentBurstOption> getType() {
        return this.particleType;
    }
}
