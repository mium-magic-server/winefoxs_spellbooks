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
 * 魔力传输流的粒子选项，由传输发射器、追踪魔力弹与金十字星芒三种粒子共用——它们描述的是同一道光束， 字段语义一致，别再拿它当通用四字段载荷派给别的效果（那样字段名会与实际含义脱节）：
 * <ul>
 *   <li>{@code casterId} / {@code targetId}：施法者与目标的实体 ID。客户端每 tick 从本地
 *       实体的实时位置与朝向重算光束两端——比传坐标 + 速度外推更准，转头不滞后。 金十字星芒另有一种脱离光束的形态，见 {@link #DETACHED_CASTER}；</li>
 *   <li>{@code time}：动画时基（tick）。发射器用它推导弹幕相位与配色轮转并跨包衔接；
 *       星芒把它当作出生时刻，公转角 = (time + age) × 角速度；</li>
 *   <li>{@code dist}：星芒在光束轴上的固定距离（格），符号编码螺旋臂：负值表示相差
 *       180° 的第二条臂。发射器自身不用此字段，恒传 0。</li>
 * </ul>
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-19
 */
public final class TransferStreamOption implements ParticleOptions {

    /**
     * {@code casterId} 取此值表示金十字星芒不挂在光束上，而是原地闪一下（{@code dist} 改作尺寸倍率）， 供各法术做点睛一闪。实体 ID 非负，不会与之相撞。
     */
    public static final int DETACHED_CASTER = -1;

    private final ParticleType<TransferStreamOption> type;

    private final int casterId;

    private final int targetId;

    private final float time;

    private final float dist;

    public TransferStreamOption(ParticleType<TransferStreamOption> type,
                                int casterId, int targetId, float time, float dist) {
        this.type = type;
        this.casterId = casterId;
        this.targetId = targetId;
        this.time = time;
        this.dist = dist;
    }

    public static MapCodec<TransferStreamOption> codec(ParticleType<TransferStreamOption> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("caster_id").forGetter(TransferStreamOption::casterId),
            Codec.INT.fieldOf("target_id").forGetter(TransferStreamOption::targetId),
            Codec.FLOAT.fieldOf("time").forGetter(TransferStreamOption::time),
            Codec.FLOAT.fieldOf("dist").forGetter(TransferStreamOption::dist)
        ).apply(instance, (casterId, targetId, time, dist) ->
            new TransferStreamOption(type, casterId, targetId, time, dist)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, TransferStreamOption> streamCodec(ParticleType<TransferStreamOption> type) {
        return StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TransferStreamOption::casterId,
            ByteBufCodecs.VAR_INT, TransferStreamOption::targetId,
            ByteBufCodecs.FLOAT, TransferStreamOption::time,
            ByteBufCodecs.FLOAT, TransferStreamOption::dist,
            (casterId, targetId, time, dist) -> new TransferStreamOption(type, casterId, targetId, time, dist));
    }

    public int casterId() {
        return this.casterId;
    }

    public int targetId() {
        return this.targetId;
    }

    public float time() {
        return this.time;
    }

    public float dist() {
        return this.dist;
    }

    @Override
    public ParticleType<TransferStreamOption> getType() {
        return this.type;
    }
}
