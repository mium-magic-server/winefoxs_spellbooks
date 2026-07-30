package net.magicterra.winefoxsspellbooks.registry;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.particle.TransferStreamOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 自定义粒子类型注册。共用 {@link TransferStreamOption} 的类型由客户端逐 tick 从本地实体重算行为。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-19
 */
public class WsbParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
        DeferredRegister.create(Registries.PARTICLE_TYPE, WinefoxsSpellbooks.MODID);

    /**
     * 彩色追踪魔力弹。
     */
    public static final Supplier<ParticleType<TransferStreamOption>> MANA_BOLT =
        PARTICLES.register("mana_bolt", WsbParticles::streamType);

    /**
     * 金光星芒。
     */
    public static final Supplier<ParticleType<TransferStreamOption>> GOLD_CROSS =
        PARTICLES.register("gold_cross", WsbParticles::streamType);

    /**
     * 魔力传输流发射器。
     */
    public static final Supplier<ParticleType<TransferStreamOption>> TRANSFER_STREAM =
        PARTICLES.register("transfer_stream", WsbParticles::streamType);

    /**
     * 酒泡:酒池/狐酿炸弹的肥皂泡粒子,上浮摇摆后破裂,无附加数据。
     */
    public static final Supplier<SimpleParticleType> WINE_BUBBLE =
        PARTICLES.register("wine_bubble", () -> new SimpleParticleType(false));

    /**
     * 水汽斑点:醉狐之雾的淡白蒸气粒子,升腾飘摆淡出,无附加数据。
     */
    public static final Supplier<SimpleParticleType> MIST_SPECK =
        PARTICLES.register("mist_speck", () -> new SimpleParticleType(false));

    /**
     * 狐影螺旋火星:两种颜色由入参速度的符号选择,客户端随 GeckoLib 骨骼生成。
     */
    public static final Supplier<SimpleParticleType> FOXSHADE_SPARK =
        PARTICLES.register("foxshade_spark", () -> new SimpleParticleType(false));

    /**
     * 残月印记。TransferStreamOption 字段在此复用为:casterId=形态(0 落印/1 绕身/2 碎片/3 闪光), targetId=被烙印实体,time=挂印剩余 tick(绕身脉动用),dist=烙印 amplifier(尺寸微增)。 落印/绕身走网络,碎片/闪光仅由爆发发射器在客户端本地生成。
     */
    public static final Supplier<ParticleType<TransferStreamOption>> CRESCENT_BRAND =
        PARTICLES.register("crescent_brand", WsbParticles::streamType);

    /**
     * 残月引爆发射器。不渲染,一包一发:dist=引爆 AoE 半径;客户端铺闪光、 月牙碎片弧、扩散环波与酒泡点缀。
     */
    public static final Supplier<ParticleType<TransferStreamOption>> CRESCENT_BURST =
        PARTICLES.register("crescent_burst", WsbParticles::streamType);

    /**
     * 醒酒净化发射器。不渲染,一包一发:targetId=净化目标,time=净除效果数,dist=模式(1 减半/2 净除); 客户端铺浊滴上滗、清醒环洗落、金星与酒泡收尾。
     */
    public static final Supplier<ParticleType<TransferStreamOption>> DECANT_CLEANSE =
        PARTICLES.register("decant_cleanse", WsbParticles::streamType);

    private static ParticleType<TransferStreamOption> streamType() {
        return new ParticleType<TransferStreamOption>(false) {
            @Override
            public MapCodec<TransferStreamOption> codec() {
                return TransferStreamOption.codec(this);
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, TransferStreamOption> streamCodec() {
                return TransferStreamOption.streamCodec(this);
            }
        };
    }

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
