package net.magicterra.winefoxsspellbooks.client;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityBroomRender;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.spells.FoxshadeWispProjectile;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 客户端设置：注册本模组各实体的渲染器。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-12-29
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = WinefoxsSpellbooks.MODID)
public class ClientSetup {

    /**
     * 注册实体渲染器
     *
     * @param evt 渲染器注册事件
     */
    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        // SummonedEntityMaid 实现了 IMaid 接口，EntityMaidRenderer 经 IMaid.convert() 可直接渲染
        evt.registerEntityRenderer(SummonedEntityMaid.TYPE, EntityMaidRenderer::new);

        // SummonedMaidBroom 继承自 EntityBroom，直接复用 TLM 的扫帚渲染器
        evt.registerEntityRenderer(SummonedMaidBroom.TYPE, EntityBroomRender::new);

        // 酒杯狐影弹：GeckoLib 狐头模型 + 拖尾粒子
        evt.registerEntityRenderer(FoxshadeWispProjectile.TYPE, FoxshadeWispRenderer::new);
    }
}
