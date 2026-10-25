package net.magicterra.winefoxsspellbooks.client;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityBroomRender;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.client.particle.CrescentBrandParticle;
import net.magicterra.winefoxsspellbooks.client.particle.CrescentBurstEmitter;
import net.magicterra.winefoxsspellbooks.client.particle.DecantCleanseEmitter;
import net.magicterra.winefoxsspellbooks.client.particle.FoxshadeSparkParticle;
import net.magicterra.winefoxsspellbooks.client.particle.ManaStreamParticle;
import net.magicterra.winefoxsspellbooks.client.particle.MistSpeckParticle;
import net.magicterra.winefoxsspellbooks.client.particle.TransferStreamEmitter;
import net.magicterra.winefoxsspellbooks.client.particle.WineBubbleParticle;
import net.magicterra.winefoxsspellbooks.entity.cloud.TipsyMistCloud;
import net.magicterra.winefoxsspellbooks.entity.cloud.WinePoolCloud;
import net.magicterra.winefoxsspellbooks.entity.projectile.FoxshadeWispProjectile;
import net.magicterra.winefoxsspellbooks.entity.projectile.WineBombProjectile;
import net.magicterra.winefoxsspellbooks.entity.summon.FoxshadeBiterEntity;
import net.magicterra.winefoxsspellbooks.entity.summon.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.summon.SummonedMaidBroom;
import net.magicterra.winefoxsspellbooks.item.VixensFlightItem;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * 客户端设置：注册本模组各实体的渲染器、粒子与物品着色。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-12-29
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = WinefoxsSpellbooks.MODID)
public class ClientSetup {

    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        // SummonedEntityMaid 实现了 IMaid 接口，EntityMaidRenderer 经 IMaid.convert() 可直接渲染
        evt.registerEntityRenderer(SummonedEntityMaid.TYPE, EntityMaidRenderer::new);

        // SummonedMaidBroom 继承自 EntityBroom，直接复用 TLM 的扫帚渲染器
        evt.registerEntityRenderer(SummonedMaidBroom.TYPE, EntityBroomRender::new);

        // 酒杯狐影弹：GeckoLib 雪狐模型 + 狐火螺旋层
        evt.registerEntityRenderer(FoxshadeWispProjectile.TYPE, FoxshadeWispRenderer::new);

        // 狐影伏击落地狐：复用狐影 littlefox 模型的地面奔袭渲染
        evt.registerEntityRenderer(FoxshadeBiterEntity.TYPE, FoxshadeBiterRenderer::new);

        // 醉狐之雾的雾团：本体不绘模型，视觉完全走 AoeEntity 自带的粒子（ambientParticles）
        evt.registerEntityRenderer(TipsyMistCloud.TYPE, NoopRenderer::new);

        // 狐酿炸弹：GeckoLib 翻滚的紫葡萄串模型
        evt.registerEntityRenderer(WineBombProjectile.TYPE, WineBombRenderer::new);

        evt.registerEntityRenderer(WinePoolCloud.TYPE, NoopRenderer::new);
    }

    /**
     * 品酒排四支酒签按各自药水颜色着色
     */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item evt) {
        evt.register(VixensFlightItem::getTintColor, WsbItems.VIXENS_FLIGHT.get());
    }

    /**
     * 注册自定义粒子的渲染提供器。
     */
    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent evt) {
        evt.registerSpriteSet(WsbParticles.MANA_BOLT.get(), ManaStreamParticle.BoltProvider::new);
        evt.registerSpriteSet(WsbParticles.GOLD_CROSS.get(), ManaStreamParticle.CrossProvider::new);
        evt.registerSpriteSet(WsbParticles.WINE_BUBBLE.get(), WineBubbleParticle.Provider::new);
        evt.registerSpriteSet(WsbParticles.MIST_SPECK.get(), MistSpeckParticle.Provider::new);
        evt.registerSpriteSet(WsbParticles.FOXSHADE_SPARK.get(), FoxshadeSparkParticle.Provider::new);
        evt.registerSpriteSet(WsbParticles.CRESCENT_BRAND.get(), CrescentBrandParticle.Provider::new);
        // 发射器类不渲染、无贴图,走 special 注册
        evt.registerSpecial(WsbParticles.TRANSFER_STREAM.get(), new TransferStreamEmitter.Provider());
        evt.registerSpecial(WsbParticles.CRESCENT_BURST.get(), new CrescentBurstEmitter.Provider());
        evt.registerSpecial(WsbParticles.DECANT_CLEANSE.get(), new DecantCleanseEmitter.Provider());
    }
}
