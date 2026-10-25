package net.magicterra.winefoxsspellbooks.event;

import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.summon.FoxshadeBiterEntity;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.magic.spell.FoxshadeAmbushSpell;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * 狐影伏击（Foxshade Ambush）触发系统事件监听器。
 * <p>
 * 持有 {@link WsbEffects#FOXSHADE_AMBUSH} 标记的施法者首次造成任意伤害时（近战、法术、 名下 AoE 的每 tick 伤害一概算数）： 消费标记 → 在目标附近生成一只落地狐狸（{@link FoxshadeBiterEntity}），寻路跑向目标、腾身扑咬，按法术等级连咬 1～4 口。 咬击打出酒狐巫法学派伤害；命中带残月烙印的敌人时经 {@link WinefoxDetonationHandler} 自动引爆。
 * <p>
 * 「先消费再召唤」+「跳过狐咬自身伤害」双重防重入：标记在触发时即移除，且扑咬狐的伤害 （directEntity 为狐本体）被显式跳过，绝不自我链式召唤；同 tick 命中多个目标也只有首个触发（标记单次）。
 * <p>
 * 两道不触发的闸门，均在消费标记之前判定，因此标记完整留到下一次出手：
 * <ul>
 *   <li>目标是自己人或打不疼（{@link WinefoxHexHelper#isAimTarget}）—— 自家女仆、召唤物、队友一概不算；</li>
 *   <li>目标悬空或周身找不到落脚点 —— 狐没地方落地，召出来也只是原地空放。</li>
 * </ul>
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-23
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class FoxshadeAmbushHandler {

    /**
     * 扑咬狐生成点距目标的水平距离(格)：略远处落地，跑几步再腾身扑咬，读作一次伏击突袭。
     */
    private static final double SPAWN_DISTANCE = 5.0;

    /**
     * 目标脚下的最大探地深度(格)：探不到地面即视作悬空（飞行/高空坠落中），不触发。
     */
    private static final int GROUND_PROBE_DEPTH = 4;

    /**
     * 生成点候选角度数：绕目标均匀取样，取第一个站得住的方位。
     */
    private static final int SPAWN_ANGLE_SAMPLES = 8;

    /**
     * 生成点与目标落脚面的最大高差(格)：超出即认为隔坑/隔崖，狐够不到。
     */
    private static final int SPAWN_Y_TOLERANCE = 3;

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        // 本监听器挂在全局伤害事件上，服务器里每一次伤害都会进来一趟，所以判定顺序按
        // 「最便宜且最能排除」优先：先取攻击者（一次字段读），再查标记（一次 Map 查）。
        // 绝大多数伤害在这两步就出去了，后面的世界类型判断、instanceof、敌我判定都省了。
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        MobEffectInstance ambush = attacker.getEffect(WsbEffects.FOXSHADE_AMBUSH);
        if (ambush == null) return;

        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)) return;
        // 跳过扑咬狐自身造成的咬击伤害（directEntity 为狐本体）→ 杜绝链式自召唤
        if (event.getSource().getDirectEntity() instanceof FoxshadeBiterEntity) return;
        // 伏击等的是标记生效后的第一次见血，不问这一下是怎么打出来的：近战、法术、
        // 挂在施法者名下的雾团/酒池每 tick 的伤害都算数。判据用「指向目标」（已含
        // 「不是自己」「还活着」），此处不该再问目标是不是 Enemy——那会让猪、铁傀儡
        // 这类非敌对生物永远打不出扑咬。自家女仆与队友仍被阵营判定挡住，
        // 打到它们既不触发也不消费标记
        if (!WinefoxHexHelper.isAimTarget(target, attacker)) return;

        // 落脚点先于消费判定：目标悬空或周身无处落地时不生成，标记完整留给下一次出手
        Vec3 spawnPos = findSpawnPos(level, target, attacker.getRandom());
        if (spawnPos == null) return;

        // 再消费标记：单次触发，防同 tick 多命中重复召唤
        attacker.removeEffect(WsbEffects.FOXSHADE_AMBUSH);

        int amplifier = ambush.getAmplifier();
        float biteDamage = Math.max(1.0f, FoxshadeAmbushSpell.decodePower(amplifier) * FoxshadeAmbushSpell.BITE_DAMAGE_MULTIPLIER);
        summonFoxshadeBiter(level, attacker, target, biteDamage, FoxshadeAmbushSpell.decodeBites(amplifier), spawnPos);
    }

    /**
     * 找一个扑咬狐站得住、且跑得到目标的落地点。
     * <p>
     * 先探目标脚下 {@link #GROUND_PROBE_DEPTH} 格：探不到地面说明目标在飞或在高空坠落，直接放弃 （狐是地面寻路生物，召出来也够不着）。再绕目标以 {@link #SPAWN_DISTANCE} 格半径均匀取 {@link #SPAWN_ANGLE_SAMPLES} 个方位，要求候选点有落脚面、与目标落脚面高差不超过 {@link #SPAWN_Y_TOLERANCE} 格（隔坑/隔崖排除），且狐的碰撞箱放得下。一圈都不合格则返回
     * {@code null}，本次伏击不触发。
     *
     * @return 可用的落地点；目标悬空或一圈都没有落脚点时返回 {@code null}
     */
    @Nullable
    private static Vec3 findSpawnPos(ServerLevel level, LivingEntity target, RandomSource rng) {
        Integer targetGroundY = probeGroundY(level, target.blockPosition(), GROUND_PROBE_DEPTH);
        if (targetGroundY == null) return null;

        float width = FoxshadeBiterEntity.TYPE.getWidth();
        float height = FoxshadeBiterEntity.TYPE.getHeight();
        // 随机起始角：不让狐每次都从同一侧扑来
        double angleOffset = rng.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < SPAWN_ANGLE_SAMPLES; i++) {
            double angle = angleOffset + i * (Math.PI * 2.0 / SPAWN_ANGLE_SAMPLES);
            double x = target.getX() + Math.cos(angle) * SPAWN_DISTANCE;
            double z = target.getZ() + Math.sin(angle) * SPAWN_DISTANCE;

            // 自「目标落脚面 + 容差」向下探，向上向下各留 SPAWN_Y_TOLERANCE 格的搜索窗
            BlockPos probeFrom = BlockPos.containing(x, targetGroundY + SPAWN_Y_TOLERANCE, z);
            Integer groundY = probeGroundY(level, probeFrom, SPAWN_Y_TOLERANCE * 2);
            if (groundY == null || Math.abs(groundY - targetGroundY) > SPAWN_Y_TOLERANCE) continue;

            AABB box = new AABB(x - width * 0.5, groundY, z - width * 0.5,
                x + width * 0.5, groundY + height, z + width * 0.5);
            if (level.noCollision(box)) {
                return new Vec3(x, groundY, z);
            }
        }
        return null;
    }

    /**
     * 自 {@code from} 逐格向下探地，返回第一个实心方块之上的落脚 Y。 水与草丛之类无碰撞箱的方块会被穿过（狐会浮水），探满 {@code maxDrop} 格仍无落脚面则返回 {@code null}。
     */
    @Nullable
    private static Integer probeGroundY(ServerLevel level, BlockPos from, int maxDrop) {
        BlockPos.MutableBlockPos cursor = from.mutable();
        for (int i = 0; i <= maxDrop; i++) {
            if (!level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
                return cursor.getY() + 1;
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    /**
     * 在给定落地点生成一只扑咬狐，绑定召唤者与目标后放入世界，由其 AI 跑向目标扑咬。 owner 设为施法者，保证咬击伤害归因正确（女仆 ×1.5 / 玩家 ×1.0 引爆分支照旧）。
     */
    private static void summonFoxshadeBiter(ServerLevel level, LivingEntity caster, LivingEntity target,
                                            float damage, int bites, Vec3 spawnPos) {
        // 落地即朝向目标，省掉出生后第一帧的转身
        float yaw = (float) (Mth.atan2(target.getZ() - spawnPos.z, target.getX() - spawnPos.x)
            * (180.0 / Math.PI)) - 90.0f;

        FoxshadeBiterEntity fox = new FoxshadeBiterEntity(FoxshadeBiterEntity.TYPE, level);
        fox.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, yaw, 0.0f);
        fox.setup(caster, target, damage, bites);
        level.addFreshEntity(fox);
    }
}
