package net.magicterra.winefoxsspellbooks.registry;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.particle.CrescentBrandOption;
import net.magicterra.winefoxsspellbooks.magic.particle.CrescentBurstOption;
import net.magicterra.winefoxsspellbooks.magic.particle.DecantCleanseOption;
import net.magicterra.winefoxsspellbooks.magic.particle.TransferStreamOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 自定义粒子类型注册。带自定义选项的类型各有各的记录类（字段名即语义），客户端逐 tick 从本地实体重算行为。
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
     * 残月印记。四种形态由 {@link CrescentBrandOption#mode()} 选:落印/绕身走网络, 碎片/闪光仅由爆发发射器在客户端本地生成。
     */
    public static final Supplier<ParticleType<CrescentBrandOption>> CRESCENT_BRAND =
        PARTICLES.register("crescent_brand",
            () -> optionType(CrescentBrandOption::codec, CrescentBrandOption::streamCodec));

    /**
     * 残月引爆发射器。不渲染,一包一发:客户端按 AoE 半径铺闪光、月牙碎片弧、扩散环波与酒泡点缀。
     */
    public static final Supplier<ParticleType<CrescentBurstOption>> CRESCENT_BURST =
        PARTICLES.register("crescent_burst",
            () -> optionType(CrescentBurstOption::codec, CrescentBurstOption::streamCodec));

    /**
     * 醒酒净化发射器。不渲染,一包一发:客户端铺浊滴上滗、清醒环洗落、金星与酒泡收尾。
     */
    public static final Supplier<ParticleType<DecantCleanseOption>> DECANT_CLEANSE =
        PARTICLES.register("decant_cleanse",
            () -> optionType(DecantCleanseOption::codec, DecantCleanseOption::streamCodec));

    private static ParticleType<TransferStreamOption> streamType() {
        return optionType(TransferStreamOption::codec, TransferStreamOption::streamCodec);
    }

    /**
     * 建一个带自定义选项的粒子类型:两个编解码器都要拿到类型自身,故由这里回传 {@code this}。
     * <p>
     * 编解码器建好即存下:原版粒子包的调度编解码器在每次编码/解码时都会重新调 {@code codec()} /
     * {@code streamCodec()},现建现扔等于每发一个粒子包就重新组装一遍编解码器。
     */
    private static <T extends ParticleOptions> ParticleType<T> optionType(
        Function<ParticleType<T>, MapCodec<T>> codec,
        Function<ParticleType<T>, StreamCodec<RegistryFriendlyByteBuf, T>> streamCodec) {
        return new ParticleType<T>(false) {
            private final MapCodec<T> mapCodec = codec.apply(this);

            private final StreamCodec<RegistryFriendlyByteBuf, T> networkCodec = streamCodec.apply(this);

            @Override
            public MapCodec<T> codec() {
                return this.mapCodec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return this.networkCodec;
            }
        };
    }

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
