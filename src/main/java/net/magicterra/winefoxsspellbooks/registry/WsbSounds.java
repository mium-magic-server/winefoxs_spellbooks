package net.magicterra.winefoxsspellbooks.registry;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 本模组自有音效事件注册表。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-06
 */
public final class WsbSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(Registries.SOUND_EVENT, WinefoxsSpellbooks.MODID);

    /**
     * 酒狐巫法学派音（{@code SpellSchool#getCastSound}）。
     * <p>
     * 咏唱段不在这里：咏唱型法术在各自 {@code getCastStartSound()} 里返回 Iron's 的循环音。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> WINEFOX_HEX_CAST =
        register("cast.winefox_hex");

    /**
     * 扑咬狐现身：窜草声在先、狐鸣在后，四变体随机。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> FOXSHADE_BITER_SPAWN =
        register("entity.foxshade_biter.spawn");

    /**
     * 狐影弹发射：0.4–0.5 s 短「咻」，三变体随机。逐发 recast 触发，必须短否则连发会糊。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> FOXSHADE_WISPS_FIRE =
        register("cast.foxshade_wisps.fire");

    /**
     * 狐影伏击施法：低沉的「上膛」感。效果要等下次命中才兑现，此处刻意克制。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> FOXSHADE_AMBUSH_CAST =
        register("cast.foxshade_ambush.cast");

    /**
     * 扑咬狐咬中：软组织闷响。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> FOXSHADE_BITER_BITE =
        register("entity.foxshade_biter.bite");

    /**
     * 狐影弹命中：狐鸣溃散尾，三变体随机。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> FOXSHADE_WISPS_IMPACT =
        register("impact.foxshade_wisps");

    /**
     * 醉狐之雾雾团环境音。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> TIPSY_MIST_AMBIENT =
        register("ambient.tipsy_mist");

    /**
     * 醉狐之雾落地：低频一记「噗」，延后 0.16 s 接弥散气声散开。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> TIPSY_MIST_BURST =
        register("cast.tipsy_mist.burst");

    /**
     * 狐酿炸弹脱手：短促「嗖」+ 瓶内液体晃动。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> VINTNERS_BOMB_THROW =
        register("cast.vintners_bomb.throw");

    /**
     * 狐酿炸弹炸裂：玻璃碎裂 + 湿性爆破 + 酒液泼洒尾，三变体随机。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> VINTNERS_BOMB_EXPLODE =
        register("cast.vintners_bomb.explode");

    /**
     * 召唤女仆到场：衣袍翻动在先，0.55 s 后落一记单击钟。
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> SUMMON_MAID_ARRIVE =
        register("cast.summon_maid.arrive");

    private WsbSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, name)));
    }

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }
}
