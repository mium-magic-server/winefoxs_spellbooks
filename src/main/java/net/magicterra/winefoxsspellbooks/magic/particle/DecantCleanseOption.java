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
 * 醒酒净化发射器的选项。
 * <ul>
 *   <li>{@link #targetId}：被净化者，客户端逐帧从它身上重算浊滴与清醒环的位置，目标移动也贴身；</li>
 *   <li>{@link #cleansedCount}：这次实际处理掉的负面效果数，浊滴数量正比于它——法术做了多少事直接画出来；</li>
 *   <li>{@link #purge}：满级整列净除为真，环更亮更密、收尾金星 3 枚；低酒龄的减半为假。</li>
 * </ul>
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-31
 */
public record DecantCleanseOption(ParticleType<DecantCleanseOption> particleType, int targetId, int cleansedCount,
                                  boolean purge) implements ParticleOptions {

    public static MapCodec<DecantCleanseOption> codec(ParticleType<DecantCleanseOption> particleType) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("target_id").forGetter(DecantCleanseOption::targetId),
            Codec.INT.fieldOf("cleansed_count").forGetter(DecantCleanseOption::cleansedCount),
            Codec.BOOL.fieldOf("purge").forGetter(DecantCleanseOption::purge)
        ).apply(instance, (targetId, cleansedCount, purge) ->
            new DecantCleanseOption(particleType, targetId, cleansedCount, purge)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, DecantCleanseOption> streamCodec(
        ParticleType<DecantCleanseOption> particleType) {
        return StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DecantCleanseOption::targetId,
            ByteBufCodecs.VAR_INT, DecantCleanseOption::cleansedCount,
            ByteBufCodecs.BOOL, DecantCleanseOption::purge,
            (targetId, cleansedCount, purge) ->
                new DecantCleanseOption(particleType, targetId, cleansedCount, purge));
    }

    @Override
    public ParticleType<DecantCleanseOption> getType() {
        return this.particleType;
    }
}
