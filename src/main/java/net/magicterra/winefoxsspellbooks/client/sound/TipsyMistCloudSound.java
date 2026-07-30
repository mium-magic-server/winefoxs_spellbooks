package net.magicterra.winefoxsspellbooks.client.sound;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.cloud.TipsyMistCloud;
import net.magicterra.winefoxsspellbooks.registry.WsbSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * 雾团环境音：跟随 {@link TipsyMistCloud} 循环播放，雾团消失即淡出自停。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-27
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = WinefoxsSpellbooks.MODID)
public class TipsyMistCloudSound extends AbstractTickableSoundInstance {

    /**
     * 淡入淡出时长(ticks)。雾团存续几十秒，可以比咏唱循环从容些。
     */
    private static final int FADE_TICKS = 10;

    /**
     * 满音量。环境层，压在落地音与战斗音之下。
     */
    private static final float MAX_VOLUME = 0.6f;

    private final TipsyMistCloud cloud;

    private int fade;

    private TipsyMistCloudSound(TipsyMistCloud cloud) {
        // 用 NEUTRAL 而非 AMBIENT：这是法术产物不是环境音，挂在 AMBIENT 分类下
        // 会被玩家的「环境/氛围」音量滑块单独压掉，与本模组其余法术音行为不一致
        super(WsbSounds.TIPSY_MIST_AMBIENT.get(), SoundSource.NEUTRAL,
            SoundInstance.createUnseededRandom());
        this.cloud = cloud;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
        this.x = cloud.getX();
        this.y = cloud.getY();
        this.z = cloud.getZ();
    }

    @Override
    public void tick() {
        if (this.cloud.isRemoved()) {
            // 雾团没了就淡出；淡到 0 再真正停止，避免突然静音
            this.fade--;
            if (this.fade <= 0) {
                stop();
                return;
            }
        } else {
            this.fade = Math.min(this.fade + 1, FADE_TICKS);
            this.x = this.cloud.getX();
            this.y = this.cloud.getY();
            this.z = this.cloud.getZ();
        }
        this.volume = MAX_VOLUME * this.fade / FADE_TICKS;
    }

    /**
     * 音量从 0 起，必须允许静音启动，否则实例会被声音引擎立刻丢弃。
     */
    @Override
    public boolean canStartSilent() {
        return true;
    }

    /**
     * 雾团进入客户端世界时挂上循环音。
     * <p>
     * 用 {@code EntityJoinLevelEvent} 而不是在 {@code TipsyMistCloud#tick} 里判断—— 后者会让这个仅客户端的类被通用实体类引用到，专用服务器上有类加载风险。
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof TipsyMistCloud cloud) {
            Minecraft.getInstance().getSoundManager().play(new TipsyMistCloudSound(cloud));
        }
    }
}
