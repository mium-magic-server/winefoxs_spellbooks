package net.magicterra.winefoxsspellbooks.client;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityBroomRender;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 客户端设置
 * <p>
 * 注册 SummonedEntityMaid 的渲染器，复用 EntityMaidRenderer。
 * 由于 SummonedEntityMaid 实现了 IMaid 接口，EntityMaidRenderer 可以直接渲染。
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
        // 复用 EntityMaidRenderer，因为 SummonedEntityMaid 实现了 IMaid 接口
        // EntityMaidRenderer.render() 方法会调用 IMaid.convert() 获取 IMaid 接口
        evt.registerEntityRenderer(SummonedEntityMaid.TYPE, EntityMaidRenderer::new);

        // 注册 SummonedMaidBroom 渲染器，复用 EntityBroomRender
        // SummonedMaidBroom 继承自 EntityBroom，可以直接使用原版渲染器
        evt.registerEntityRenderer(SummonedMaidBroom.TYPE, EntityBroomRender::new);
    }
}
