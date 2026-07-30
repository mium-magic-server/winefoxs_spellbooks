package net.magicterra.winefoxsspellbooks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.magicterra.winefoxsspellbooks.client.layer.FoxshadeWispOrbitLayer;
import net.magicterra.winefoxsspellbooks.client.model.SnowFoxModel;
import net.magicterra.winefoxsspellbooks.entity.projectile.FoxshadeWispProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 酒杯狐影弹渲染器（GeckoLib）。
 * <p>
 * 渲染一只冰狐配色的小狐狸弹体，飞行中叠加翻滚与行走循环。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-21
 */
public class FoxshadeWispRenderer extends GeoEntityRenderer<FoxshadeWispProjectile> {

    /**
     * 弹体缩放：全身狐模型比旧狐头大得多，整体缩小。
     */
    private static final float WISP_SCALE = 0.45f;

    public FoxshadeWispRenderer(EntityRendererProvider.Context context) {
        super(context, new SnowFoxModel<>());
        this.addRenderLayer(new FoxshadeWispOrbitLayer(this));
    }

    @Override
    public void preRender(PoseStack poseStack, FoxshadeWispProjectile animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        float s = WISP_SCALE;
        this.scaleWidth = s;
        this.scaleHeight = s;
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
            partialTick, packedLight, packedOverlay, colour);
    }

    /**
     * 按弹体自身的 yRot/xRot 定向；偏航补 +180° 把模型局部 -Z 转到飞行方向。
     */
    @Override
    protected void applyRotations(FoxshadeWispProjectile animatable, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick, float nativeScale) {
        float yaw = Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot());
        float pitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
    }
}
