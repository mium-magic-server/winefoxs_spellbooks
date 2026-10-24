package net.magicterra.winefoxsspellbooks.client;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.magicterra.winefoxsspellbooks.ClientConfig;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.registry.WsbAttachments;
import net.magicterra.winefoxsspellbooks.task.MaidCastingTask;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * 女仆身旁的浮动法力条
 * <p>
 * 只给自己的、正在执行法术攻击或法术支援任务的女仆显示（与 Jade 的法力显示口径一致）。弧形条始终朝向镜头，呈「)」形绕在女仆身体靠镜头右侧的一边，从下端沿弧往上涨，
 * 法力回满后渐隐，再掉下去时渐显。可在客户端配置里关闭，见 {@link ClientConfig#isMaidManaBarShown()}。
 * <p>
 * 召唤女仆不显示：其召唤者只记录在 Iron's 的服务端 SummonManager 里，客户端无法判断是不是自己的。
 * <p>
 * 不挂在 TLM 的女仆渲染器上，而是在世界渲染阶段统一绘制，不受女仆使用哪种模型渲染器影响。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = WinefoxsSpellbooks.MODID)
public final class MaidManaBarRenderer {
    /**
     * 纯色半透明、全亮、不剔除背面的四边形
     * <p>
     * 几层弧带贴得极近，靠深度区分前后会在远处互相争深度、出网纹，所以不写深度、上传时也不按深度重排，
     * 严格按提交顺序叠：先画的外框在底，后画的填充在上；多只女仆之间从远到近画。深度测试仍保留，方块和实体照样能挡住
     */
    private static final RenderType BAR = RenderType.create(WinefoxsSpellbooks.MODID + ":maid_mana_bar",
        DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, false,
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.NO_CULL)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .createCompositeState(false));
    /**
     * 一个「像素」的边长（格），比原版名牌（0.025）略小，整条弧随之等比缩放
     */
    private static final float PIXEL = 0.021F;
    /**
     * 弧形条：1/4 圆环，圆心在女仆一侧，弧向外鼓成「)」形，两端沿半径方向截断。弧长约与身体等高
     */
    private static final float ARC_RADIUS = 24.0F;
    private static final float ARC_HALF_ANGLE = (float) Math.toRadians(45.0);
    /**
     * 槽宽（像素），外面再包 1 像素边框
     */
    private static final float BAND = 2.5F;
    /**
     * 整条弧分多少段折线逼近，太少近看会出折角
     */
    private static final int SEGMENTS = 48;
    /**
     * 边缘羽化宽度（像素）：外框对着背景用宽的，里层叠在外框上用窄的
     */
    private static final float OUTER_FEATHER = 0.8F;
    private static final float INNER_FEATHER = 0.4F;
    /**
     * 弧顶离碰撞箱侧面的距离（格）。女仆袖子比碰撞箱宽，太近会被手臂挡住
     */
    private static final float SIDE_GAP = 0.4F;
    private static final double MAX_DISTANCE_SQR = 32.0 * 32.0;
    /**
     * 渐隐、渐显耗时（毫秒）
     */
    private static final float FADE_MS = 600.0F;
    /**
     * 显示值追赶实际值的速度（每秒），法力按回复间隔跳变，这里让条平滑地涨落
     */
    private static final float FOLLOW_SPEED = 6.0F;
    private static final int BORDER_COLOR = 0x0B0F1E;
    private static final int EMPTY_COLOR = 0x1C2648;
    private static final int FILL_COLOR = 0x3E8CF0;
    private static final int HIGHLIGHT_COLOR = 0x8FC4FF;

    private static final Map<EntityMaid, BarState> STATES = new WeakHashMap<>();

    private MaidManaBarRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null || mc.options.hideGui || !ClientConfig.isMaidManaBarShown()) {
            STATES.clear();
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        long now = Util.getMillis();
        List<EntityMaid> visible = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof EntityMaid maid) || !shouldShow(maid, player)) {
                continue;
            }
            if (maid.distanceToSqr(camera) > MAX_DISTANCE_SQR || maid.isInvisibleTo(player)
                || !event.getFrustum().isVisible(maid.getBoundingBox().inflate(0.5))) {
                continue;
            }
            float maxMana = (float) MaidMagicManager.getMaxMana(maid);
            if (maxMana <= 0.0F) {
                continue;
            }
            float ratio = Mth.clamp(maid.getData(WsbAttachments.MAID_MANA) / maxMana, 0.0F, 1.0F);
            BarState state = STATES.computeIfAbsent(maid, m -> new BarState(ratio, now));
            state.update(ratio, now);
            if (state.alpha > 0.01F) {
                visible.add(maid);
            }
        }
        if (visible.isEmpty()) {
            return;
        }
        // 不写深度，多只女仆的条之间没有遮挡关系，只能从远到近画，近的压在上面
        visible.sort(Comparator.comparingDouble((EntityMaid maid) -> maid.distanceToSqr(camera)).reversed());
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(BAR);
        PoseStack poseStack = event.getPoseStack();
        for (EntityMaid maid : visible) {
            renderBar(poseStack, consumer, maid, camera, partialTick, STATES.get(maid));
        }
        buffers.endBatch(BAR);
    }

    private static boolean shouldShow(EntityMaid maid, LocalPlayer player) {
        if (!maid.isOwnedBy(player)) {
            return false;
        }
        // 法术支援任务继承自 MaidCastingTask，一并涵盖
        return maid.getTask() instanceof MaidCastingTask;
    }

    private static void renderBar(PoseStack poseStack, VertexConsumer consumer, EntityMaid maid, Vec3 camera,
                                  float partialTick, BarState state) {
        Vec3 pos = maid.getPosition(partialTick);
        poseStack.pushPose();
        poseStack.translate(pos.x - camera.x, pos.y - camera.y + maid.getBbHeight() * 0.5, pos.z - camera.z);
        // 同名牌：先转到朝向镜头，再翻转 y 轴，之后 x 向右、y 向下，按像素画
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.translate(maid.getBbWidth() * 0.5F + SIDE_GAP, 0.0F, 0.0F);
        poseStack.scale(PIXEL, -PIXEL, PIXEL);
        Matrix4f matrix = poseStack.last().pose();

        int alpha = Mth.floor(state.alpha * 255.0F);
        float inner = ARC_RADIUS - BAND * 0.5F;
        float outer = ARC_RADIUS + BAND * 0.5F;
        // 边框在两端各多出 1 像素弧长
        float borderPad = 1.0F / (2.0F * ARC_HALF_ANGLE * ARC_RADIUS);
        // t 沿弧从上端 0 到下端 1，法力从下端往上涨；有法力时至少留 1 像素，免得快空时看不出还有
        float fillStart = state.shown <= 0.0F ? 1.0F : Math.min(1.0F - state.shown, 1.0F - borderPad);

        // 按提交顺序叠，后画的在上面，见 BAR 的说明
        // 外框对着背景，羽化宽一些；里面几层叠在外框上，略作羽化即可。外框与空槽透明度不宜太低，否则底下草地等纹理会透上来成竖纹
        arc(consumer, matrix, inner - 1.0F, outer + 1.0F, -borderPad, 1.0F + borderPad, 0.0F, BORDER_COLOR, alpha * 235 / 255,
            OUTER_FEATHER);
        arc(consumer, matrix, inner, outer, 0.0F, 1.0F, 0.0F, EMPTY_COLOR, alpha * 220 / 255, INNER_FEATHER);
        if (fillStart < 1.0F) {
            arc(consumer, matrix, inner, outer, fillStart, 1.0F, 0.0F, FILL_COLOR, alpha, INNER_FEATHER);
            // 靠女仆一侧的内圈描一道亮边
            arc(consumer, matrix, inner, inner + 1.0F, fillStart, 1.0F, 0.0F, HIGHLIGHT_COLOR, alpha, INNER_FEATHER);
        }
        poseStack.popPose();
    }

    /**
     * 画一段弧带：半径 rIn 到 rOut，沿弧参数 t0 到 t1。圆心在 (-ARC_RADIUS, 0)，弧中点正好落在锚点上
     * <p>
     * 游戏默认不开多重采样，多边形边缘会按屏幕像素截出台阶。这里四周各加一圈宽 feather 的透明度渐变，边缘在不到一个像素内淡出，相当于手工抗锯齿
     */
    private static void arc(VertexConsumer consumer, Matrix4f matrix, float rIn, float rOut, float t0, float t1, float z,
                            int rgb, int alpha, float feather) {
        // 弧长方向的羽化换算成 t
        float ft = feather / (2.0F * ARC_HALF_ANGLE * ARC_RADIUS);
        float rInF = rIn - feather;
        float rOutF = rOut + feather;
        float t0F = t0 - ft;
        float t1F = t1 + ft;
        // 本体
        strip(consumer, matrix, rIn, rOut, t0, t1, z, rgb, alpha, alpha, alpha, alpha);
        // 内外两侧
        strip(consumer, matrix, rInF, rIn, t0, t1, z, rgb, 0, alpha, 0, alpha);
        strip(consumer, matrix, rOut, rOutF, t0, t1, z, rgb, alpha, 0, alpha, 0);
        // 两端切口
        strip(consumer, matrix, rIn, rOut, t0F, t0, z, rgb, 0, 0, alpha, alpha);
        strip(consumer, matrix, rIn, rOut, t1, t1F, z, rgb, alpha, alpha, 0, 0);
        // 四个角，只有贴着本体的那个顶点不透明
        strip(consumer, matrix, rInF, rIn, t0F, t0, z, rgb, 0, 0, 0, alpha);
        strip(consumer, matrix, rOut, rOutF, t0F, t0, z, rgb, 0, 0, alpha, 0);
        strip(consumer, matrix, rInF, rIn, t1, t1F, z, rgb, 0, alpha, 0, 0);
        strip(consumer, matrix, rOut, rOutF, t1, t1F, z, rgb, alpha, 0, 0, 0);
    }

    /**
     * 弧带上的一条四边形带，四角透明度分别给出，沿弧向线性插值
     *
     * @param alphaA0 半径 rA、参数 tA 处的透明度，其余三个同理
     */
    private static void strip(VertexConsumer consumer, Matrix4f matrix, float rA, float rB, float tA, float tB, float z,
                              int rgb, int alphaA0, int alphaB0, int alphaA1, int alphaB1) {
        int segments = Math.max(1, Mth.ceil(SEGMENTS * (tB - tA)));
        for (int i = 0; i < segments; i++) {
            float k0 = (float) i / segments;
            float k1 = (float) (i + 1) / segments;
            float ta = Mth.lerp(k0, tA, tB);
            float tb = Mth.lerp(k1, tA, tB);
            arcVertex(consumer, matrix, rA, ta, z, FastColor.ARGB32.color(Mth.lerpInt(k0, alphaA0, alphaA1), rgb));
            arcVertex(consumer, matrix, rB, ta, z, FastColor.ARGB32.color(Mth.lerpInt(k0, alphaB0, alphaB1), rgb));
            arcVertex(consumer, matrix, rB, tb, z, FastColor.ARGB32.color(Mth.lerpInt(k1, alphaB0, alphaB1), rgb));
            arcVertex(consumer, matrix, rA, tb, z, FastColor.ARGB32.color(Mth.lerpInt(k1, alphaA0, alphaA1), rgb));
        }
    }

    private static void arcVertex(VertexConsumer consumer, Matrix4f matrix, float radius, float t, float z, int color) {
        // y 向下为正：t=0 在上端，t=1 在下端；角度只取决于 t，两端切口沿半径方向
        float angle = ARC_HALF_ANGLE * (2.0F * t - 1.0F);
        float x = -ARC_RADIUS + radius * Mth.cos(angle);
        float y = radius * Mth.sin(angle);
        consumer.addVertex(matrix, x, y, z).setColor(color);
    }

    /**
     * 每只女仆的显示状态：平滑后的法力比例与淡入淡出透明度
     */
    private static final class BarState {
        float shown;
        float alpha;
        long lastMillis;

        BarState(float ratio, long now) {
            this.shown = ratio;
            this.lastMillis = now;
        }

        void update(float ratio, long now) {
            // 限制单帧步长，切回游戏或卡顿后不会一下子跳完
            float dt = Math.min((now - lastMillis) / 1000.0F, 0.25F);
            lastMillis = now;
            shown += (ratio - shown) * Math.min(1.0F, dt * FOLLOW_SPEED);
            float target = ratio >= 0.999F ? 0.0F : 1.0F;
            float step = dt * 1000.0F / FADE_MS;
            alpha = alpha < target ? Math.min(target, alpha + step) : Math.max(target, alpha - step);
        }
    }
}
