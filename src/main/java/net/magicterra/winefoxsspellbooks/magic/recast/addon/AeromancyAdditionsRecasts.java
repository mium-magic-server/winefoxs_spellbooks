package net.magicterra.winefoxsspellbooks.magic.recast.addon;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.magicterra.winefoxsspellbooks.magic.recast.AddonReflection;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastContext;
import net.magicterra.winefoxsspellbooks.magic.recast.RecastRegistrar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Aeromancy Additions 的 recast 收尾。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-08-01
 */
public final class AeromancyAdditionsRecasts {
    private AeromancyAdditionsRecasts() {
    }

    public static final String MOD_ID = "aero_additions";

    /**
     * 该模组的实体注册表，上升气流的视觉实体挂在它下面。嵌套类，二进制名用 {@code $} 连接。
     */
    private static final String ENTITY_REGISTRY = "com.snackpirate.aeromancy.spells.AASpells$Entities";

    /**
     * 上升气流视觉实体在该注册表里的字段名。
     */
    private static final String UPDRAFT_VISUAL_FIELD = "UPDRAFT_VISUAL_ENTITY";

    /**
     * 掀飞目标的上冲初速度。
     */
    private static final Vec3 KNOCK_UP = new Vec3(0.0, 0.9, 0.0);

    /**
     * 落地后的缓慢：5 秒、4 级。原实现写死不随法术等级变化（与 tooltip 里按等级增长的说法对不上，这里照代码走）。
     */
    private static final int SLOWDOWN_DURATION = 100;

    private static final int SLOWDOWN_AMPLIFIER = 3;

    /**
     * 特效实体的落位取的插值位置，与原实现一致。
     */
    private static final float VISUAL_POSITION_PARTIAL_TICK = 0.2f;

    /**
     * 登记本模组的收尾动作与反射目标。
     */
    public static void contribute(RecastRegistrar registrar) {
        AddonReflection.declareField(MOD_ID, ENTITY_REGISTRY, UPDRAFT_VISUAL_FIELD);

        registrar.register("updraft", AeromancyAdditionsRecasts::updraft);
    }

    /**
     * UPDRAFT（上升气流）：把 recast 期间标记过的目标一并掀上天，各挂一层缓慢，并在脚下放一团气流特效。
     */
    public static void updraft(RecastContext ctx) {
        if (!ctx.beginMultiTargetFinish()) {
            return;
        }

        ServerLevel level = ctx.level();
        ctx.playAtMuzzle(SoundRegistry.GUST_CAST.get(), 2.0f);

        // 特效实体类型只解析一次；解析不到就整轮只走原版部分，击飞与缓慢照常。
        // 走注册表 Holder 而不是直接 new，省掉对实体类构造器的反射
        EntityType<?> visualType =
            AddonReflection.holderValue(ENTITY_REGISTRY, UPDRAFT_VISUAL_FIELD, EntityType.class).orElse(null);

        for (LivingEntity target : ctx.markedTargets()) {
            target.addDeltaMovement(KNOCK_UP);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                SLOWDOWN_DURATION, SLOWDOWN_AMPLIFIER));
            if (visualType != null) {
                spawnVisual(level, visualType, target);
            }
        }
    }

    /**
     * 在目标脚下放一团气流特效。纯装饰，生成失败不影响任何战斗结算。
     */
    private static void spawnVisual(ServerLevel level, EntityType<?> visualType, LivingEntity target) {
        Entity visual = visualType.create(level);
        if (visual == null) {
            return;
        }
        visual.setPos(target.getPosition(VISUAL_POSITION_PARTIAL_TICK));
        // 入场前先 tick 一次：特效实体自己会把位置写回去，这样第一帧就停在目标脚下
        visual.tick();
        level.addFreshEntity(visual);
    }
}
