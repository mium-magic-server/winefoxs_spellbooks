package net.magicterra.winefoxsspellbooks.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.magicterra.winefoxsspellbooks.entity.projectile.FoxshadeWispProjectile;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * 狐影追踪弹的双股狐火螺旋。
 * <p>
 * 两个空骨骼随 {@code rotate} 动画绕弹体翻滚，本层读取动画完成后的世界坐标生成粒子， 使粒子相位、旋转方向和模型翻滚完全共用 GeckoLib 动画。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-25
 */
public class FoxshadeWispOrbitLayer extends GeoRenderLayer<FoxshadeWispProjectile> {

    private static final String PRIMARY_BONE = "foxshade_orbit_primary";

    private static final String HIGHLIGHT_BONE = "foxshade_orbit_highlight";

    private static final double PRIMARY_MARKER = 1.0e-4;

    private static final double HIGHLIGHT_MARKER = -1.0e-4;

    private int lastEntityId = Integer.MIN_VALUE;

    private int lastEmissionTick = Integer.MIN_VALUE;

    public FoxshadeWispOrbitLayer(GeoRenderer<FoxshadeWispProjectile> renderer) {
        super(renderer);
    }

    @Override
    public void preRender(PoseStack poseStack, FoxshadeWispProjectile animatable, BakedGeoModel bakedModel,
                          RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                          float partialTick, int packedLight, int packedOverlay) {
        bakedModel.getBone(PRIMARY_BONE).ifPresent(bone -> bone.setTrackingMatrices(true));
        bakedModel.getBone(HIGHLIGHT_BONE).ifPresent(bone -> bone.setTrackingMatrices(true));
    }

    @Override
    public void render(PoseStack poseStack, FoxshadeWispProjectile animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        if (buffer == null || !animatable.isAlive()) {
            return;
        }
        if (animatable.getId() == this.lastEntityId && animatable.tickCount == this.lastEmissionTick) {
            return;
        }

        GeoBone primary = bakedModel.getBone(PRIMARY_BONE).orElse(null);
        GeoBone highlight = bakedModel.getBone(HIGHLIGHT_BONE).orElse(null);
        if (primary == null || highlight == null) {
            return;
        }

        this.lastEntityId = animatable.getId();
        this.lastEmissionTick = animatable.tickCount;
        addParticle(animatable, position(primary), PRIMARY_MARKER);
        addParticle(animatable, position(highlight), HIGHLIGHT_MARKER);
    }

    private static Vec3 position(GeoBone bone) {
        Vector3d position = bone.getWorldPosition();

        return new Vec3(position.x, position.y, position.z);
    }

    private static void addParticle(FoxshadeWispProjectile animatable, Vec3 position, double colorMarker) {
        animatable.level().addParticle(WsbParticles.FOXSHADE_SPARK.get(),
            position.x, position.y, position.z, colorMarker, 0.0, 0.0);
    }
}
