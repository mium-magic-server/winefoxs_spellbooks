package net.magicterra.winefoxsspellbooks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.spells.FoxshadeWispProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 酒杯狐影弹渲染器（GeckoLib）。
 * <p>
 * 渲染一颗以 littlefox 狐头为参照、紫粉重调的拖焰狐头模型。资产路径由
 * {@link DefaultedEntityGeoModel} 按名称 {@code foxshade_wisp} 自动解析，且固定带 entity/ 子目录：
 * geo/entity/foxshade_wisp.geo.json、animations/entity/foxshade_wisp.animation.json、
 * textures/entity/foxshade_wisp.png。资产不在该路径下编译期查不出，首次渲染时才会崩溃。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-21
 */
public class FoxshadeWispRenderer extends GeoEntityRenderer<FoxshadeWispProjectile> {

    public FoxshadeWispRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
            ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "foxshade_wisp")));
    }

    /**
     * finisher 弹体（第 6 发）渲染放大至 0.85；普通弹保持 0.55。
     *
     * @author Gardel &lt;gardel741@outlook.com&gt;
     * @since 2026-06-21
     */
    @Override
    public void preRender(PoseStack poseStack, FoxshadeWispProjectile animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        float s = animatable.isFinisher() ? 0.85f : 0.55f;
        this.scaleWidth = s;
        this.scaleHeight = s;
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
            partialTick, packedLight, packedOverlay, colour);
    }
}
