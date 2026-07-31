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
 * 残月烙印粒子族的选项。一个粒子类型下有四种形态，{@link #mode} 选形态，其余字段按形态取用：
 * <ul>
 *   <li>{@link #MODE_STAMP} / {@link #MODE_ORBIT}：走网络，要 {@link #brandedEntityId} 逐 tick 跟踪被烙印者，
 *       绕身还要 {@link #remainingTicks} 驱动临期脉动；</li>
 *   <li>{@link #MODE_SHARD} / {@link #MODE_FLASH}：仅由引爆发射器在客户端本地生成，位置与速度由发射器给定，
 *       实体与时长字段不用。</li>
 * </ul>
 * {@link #amplifier} 四种形态里只有落印/绕身按它微调尺寸。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-31
 */
public record CrescentBrandOption(ParticleType<CrescentBrandOption> particleType, int mode, int brandedEntityId,
                                  int remainingTicks, int amplifier) implements ParticleOptions {

    /**
     * 落印：胸口盖章式收缩。
     */
    public static final int MODE_STAMP = 0;

    /**
     * 绕身：常驻公转印记，服务端定期补发续命。
     */
    public static final int MODE_ORBIT = 1;

    /**
     * 碎片：引爆甩出的月牙残片。
     */
    public static final int MODE_SHARD = 2;

    /**
     * 闪光：引爆瞬间的白金亮月。
     */
    public static final int MODE_FLASH = 3;

    public static MapCodec<CrescentBrandOption> codec(ParticleType<CrescentBrandOption> particleType) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("mode").forGetter(CrescentBrandOption::mode),
            Codec.INT.fieldOf("branded_entity_id").forGetter(CrescentBrandOption::brandedEntityId),
            Codec.INT.fieldOf("remaining_ticks").forGetter(CrescentBrandOption::remainingTicks),
            Codec.INT.fieldOf("amplifier").forGetter(CrescentBrandOption::amplifier)
        ).apply(instance, (mode, entityId, remaining, amplifier) ->
            new CrescentBrandOption(particleType, mode, entityId, remaining, amplifier)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, CrescentBrandOption> streamCodec(
        ParticleType<CrescentBrandOption> particleType) {
        return StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CrescentBrandOption::mode,
            ByteBufCodecs.VAR_INT, CrescentBrandOption::brandedEntityId,
            ByteBufCodecs.VAR_INT, CrescentBrandOption::remainingTicks,
            ByteBufCodecs.VAR_INT, CrescentBrandOption::amplifier,
            (mode, entityId, remaining, amplifier) ->
                new CrescentBrandOption(particleType, mode, entityId, remaining, amplifier));
    }

    @Override
    public ParticleType<CrescentBrandOption> getType() {
        return this.particleType;
    }
}
