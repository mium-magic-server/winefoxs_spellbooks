package net.magicterra.winefoxsspellbooks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.magicterra.winefoxsspellbooks.client.model.SnowFoxModel;
import net.magicterra.winefoxsspellbooks.entity.summon.FoxshadeBiterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 狐影伏击落地狐狸渲染器（GeckoLib）。
 * <p>
 * 使用公共雪狐狸模型/材质/动画（资产名 {@code snow_fox}），作为地面奔袭的小狐狸渲染。 geo 根骨骼保持标准朝向（飞行弹渲染器自行补偿），故本体作为普通生物按 {@link GeoEntityRenderer} 基类身体偏航渲染即朝向正确，无需覆写 {@code applyRotations}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-23
 */
public class FoxshadeBiterRenderer extends GeoEntityRenderer<FoxshadeBiterEntity> {

    public FoxshadeBiterRenderer(EntityRendererProvider.Context context) {
        super(context, new SnowFoxModel<>());
    }

    /**
     * 原始 1.0 尺寸渲染 littlefox 全身狐模型。
     */
    @Override
    public void preRender(PoseStack poseStack, FoxshadeBiterEntity animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        this.scaleWidth = 1.0f;
        this.scaleHeight = 1.0f;
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
            partialTick, packedLight, packedOverlay, colour);
    }
}
