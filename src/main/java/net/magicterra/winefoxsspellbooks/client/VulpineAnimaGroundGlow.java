package net.magicterra.winefoxsspellbooks.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.registry.WsbItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * 灵狐精魂掉落物的地面光斑
 * <p>
 * 纯客户端假光：不放光源方块、不改光照等级、不依赖动态光源模组，只在掉落物下方的方块顶面叠一层加色混合的火光贴图，并随时间闪烁。
 * 贴到哪些方块、如何按方块顶面裁切照搬原版实体阴影（{@code EntityRenderDispatcher.renderShadow}），所以光斑只落在实心方块上，不会悬空伸出边缘。
 * 物品本身的全亮由物品模型的 emissive 层负责，见 {@code ItemModelGenerator}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = WinefoxsSpellbooks.MODID)
public final class VulpineAnimaGroundGlow {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "textures/misc/vulpine_anima_glow.png");
    /**
     * 加色混合：贴图亮度直接叠到地面上，黑色即无光。照搬 eyes 的着色器（不吃光照图、只乘顶点色），加上阴影的 Z 偏移防止和方块顶面抢深度
     */
    private static final RenderType GLOW = RenderType.create(WinefoxsSpellbooks.MODID + ":vulpine_anima_glow",
        DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, false,
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_EYES_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
            .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
            .createCompositeState(false));
    /**
     * 光斑半径（格）
     */
    private static final float RADIUS = 1.1F;
    /**
     * 掉落物离地多高时光斑完全消失（格），弹跳、下落过程中随高度渐隐
     */
    private static final float MAX_HEIGHT = 2.0F;
    /**
     * 超过这个距离不再绘制
     */
    private static final double MAX_DISTANCE_SQR = 48.0 * 48.0;
    /**
     * 光斑峰值亮度；周围越亮越淡，最亮处保留的比例见 {@link #DAYLIGHT_KEEP}
     */
    private static final float BASE_STRENGTH = 1.0F;
    private static final float DAYLIGHT_KEEP = 0.5F;
    /**
     * 呼吸的亮度起伏幅度与半径起伏比例
     */
    private static final float BREATH_DEPTH = 0.14F;
    private static final float BREATH_SPREAD = 0.08F;

    private VulpineAnimaGroundGlow() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float time = level.getGameTime() + partialTick;
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = null;
        // 此阶段 PoseStack 为单位阵，镜头旋转在 ModelView 里，顶点按相对镜头的坐标写
        PoseStack.Pose pose = event.getPoseStack().last();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity item) || !item.getItem().is(WsbItems.VULPINE_ANIMA.get())) {
                continue;
            }
            if (entity.distanceToSqr(camera) > MAX_DISTANCE_SQR
                || !event.getFrustum().isVisible(entity.getBoundingBox().inflate(RADIUS, MAX_HEIGHT, RADIUS))) {
                continue;
            }
            if (consumer == null) {
                consumer = buffers.getBuffer(GLOW);
            }
            renderGlow(pose, consumer, level, item, camera, partialTick, time);
        }
        if (consumer != null) {
            buffers.endBatch(GLOW);
        }
    }

    private static void renderGlow(PoseStack.Pose pose, VertexConsumer consumer, ClientLevel level, ItemEntity item,
                                   Vec3 camera, float partialTick, float time) {
        double x = Mth.lerp(partialTick, item.xOld, item.getX());
        double y = Mth.lerp(partialTick, item.yOld, item.getY());
        double z = Mth.lerp(partialTick, item.zOld, item.getZ());
        // 呼吸与物品上下浮动同频同相，公式同 ItemEntityRenderer：bob 为 1 时物品在最高处。
        // 物品越高光斑越暗、越散，落到最低处最亮、最聚拢，像光源离地远近的变化。bobOffs 每个掉落物随机，一地精魂自然错开
        float bob = Mth.sin((item.getAge() + partialTick) / 10.0F + item.bobOffs);
        // 再叠一层小幅快速抖动，像火苗跳动
        float phase = item.getId() * 1.618F;
        float jitter = 0.03F * Mth.sin(time * 1.13F + phase) + 0.02F * Mth.sin(time * 2.7F + phase * 0.7F);
        float flicker = 0.86F - BREATH_DEPTH * bob + jitter;
        float radius = RADIUS * (1.0F + BREATH_SPREAD * bob);

        int minX = Mth.floor(x - radius);
        int maxX = Mth.floor(x + radius);
        int minY = Mth.floor(y - MAX_HEIGHT);
        int maxY = Mth.floor(y);
        int minZ = Mth.floor(z - radius);
        int maxZ = Mth.floor(z + radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int bz = minZ; bz <= maxZ; bz++) {
            for (int bx = minX; bx <= maxX; bx++) {
                for (int by = minY; by <= maxY; by++) {
                    pos.set(bx, by, bz);
                    renderOnBlock(pose, consumer, level, pos, x, y, z, camera, radius, flicker);
                }
            }
        }
    }

    /**
     * 在 pos 下方方块的顶面画一块光斑。判定条件同原版阴影：下方方块可见且碰撞箱为完整方块
     */
    private static void renderOnBlock(PoseStack.Pose pose, VertexConsumer consumer, ClientLevel level, BlockPos pos,
                                      double x, double y, double z, Vec3 camera, float radius, float flicker) {
        BlockPos below = pos.below();
        BlockState state = level.getBlockState(below);
        if (state.getRenderShape() == RenderShape.INVISIBLE || !state.isCollisionShapeFullBlock(level, below)) {
            return;
        }
        VoxelShape shape = state.getShape(level, below);
        if (shape.isEmpty()) {
            return;
        }
        float height = (float) (y - pos.getY());
        if (height < 0.0F || height > MAX_HEIGHT) {
            return;
        }
        // 环境越暗光斑越明显；白天也保留一部分，不至于完全看不见
        float ambient = level.getMaxLocalRawBrightness(pos) / 15.0F;
        float strength = BASE_STRENGTH * flicker * (1.0F - height / MAX_HEIGHT) * (1.0F - (1.0F - DAYLIGHT_KEEP) * ambient);
        if (strength <= 0.0F) {
            return;
        }
        int c = Mth.floor(Mth.clamp(strength, 0.0F, 1.0F) * 255.0F);
        int color = FastColor.ARGB32.color(255, c, c, c);

        AABB bounds = shape.bounds();
        double wx0 = pos.getX() + bounds.minX;
        double wx1 = pos.getX() + bounds.maxX;
        double wz0 = pos.getZ() + bounds.minZ;
        double wz1 = pos.getZ() + bounds.maxZ;
        float fx0 = (float) (wx0 - camera.x);
        float fx1 = (float) (wx1 - camera.x);
        float fy = (float) (pos.getY() + bounds.minY - camera.y);
        float fz0 = (float) (wz0 - camera.z);
        float fz1 = (float) (wz1 - camera.z);
        float u0 = (float) ((wx0 - x) / (2.0F * radius) + 0.5);
        float u1 = (float) ((wx1 - x) / (2.0F * radius) + 0.5);
        float v0 = (float) ((wz0 - z) / (2.0F * radius) + 0.5);
        float v1 = (float) ((wz1 - z) / (2.0F * radius) + 0.5);
        // 顶点顺序同原版阴影，正面朝上
        vertex(pose, consumer, color, fx0, fy, fz0, u0, v0);
        vertex(pose, consumer, color, fx0, fy, fz1, u0, v1);
        vertex(pose, consumer, color, fx1, fy, fz1, u1, v1);
        vertex(pose, consumer, color, fx1, fy, fz0, u1, v0);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int color, float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
