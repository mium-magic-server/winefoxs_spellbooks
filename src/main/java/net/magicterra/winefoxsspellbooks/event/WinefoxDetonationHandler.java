package net.magicterra.winefoxsspellbooks.event;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbSpells;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * 残月烙印（Crescent Brand）引爆系统事件监听器：
 * <ul>
 *   <li>受到酒狐巫法伤害时触发引爆（移除烙印 + AoE 爆发）</li>
 * </ul>
 * 烙印自然到期的弱自爆不在此处，而在 {@link net.magicterra.winefoxsspellbooks.effect.CrescentBrandEffect}
 * 的效果 tick 内实现（详见该类说明）。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class WinefoxDetonationHandler {

    /**
     * 连锁深度计数器（ThreadLocal 防止递归无限触发）。
     * 女仆分支允许深度 1，玩家分支允许深度 0（即不连锁）。
     */
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (!target.hasEffect(WsbEffects.CRESCENT_BRAND)) return;
        if (!WinefoxHexHelper.isWinefoxHexDamage(event.getSource())) return;

        detonate(target, event.getSource().getEntity());
    }

    /**
     * 执行引爆逻辑：移除烙印，对周围存活敌对实体造成 AoE 爆发伤害。
     *
     * @param target   持有烙印的目标（爆心）
     * @param attacker 造成触发伤害的实体（可为 null）
     */
    private static void detonate(LivingEntity target, Entity attacker) {
        boolean isMaid = WinefoxHexHelper.isOwnedMaid(attacker);
        int maxDepth = isMaid ? 1 : 0;
        if (DEPTH.get() > maxDepth) return;

        // 女仆分支伤害 ×1.5；半径在主人 Brewsurge 满层时 +1
        float damage = isMaid ? WinefoxHexHelper.DETONATE_BASE * WinefoxHexHelper.MAID_MULT
            : WinefoxHexHelper.DETONATE_BASE;
        int radius = isMaid
            ? (WinefoxHexHelper.commanderBrewsurgeStacks(attacker) >= WinefoxHexHelper.BREWSURGE_MAX_STACKS
                ? WinefoxHexHelper.MAID_RADIUS_SURGE : WinefoxHexHelper.MAID_RADIUS)
            : WinefoxHexHelper.PLAYER_RADIUS;

        // 移除烙印（可能触发 MobEffectEvent.Remove，不触发 Expired）
        target.removeEffect(WsbEffects.CRESCENT_BRAND);

        // 用残月烙印法术的学派伤害源制造 AoE
        var brand = WsbSpells.CRESCENT_BRAND_SPELL.get();
        AABB box = target.getBoundingBox().inflate(radius);

        DEPTH.set(DEPTH.get() + 1);
        try {
            // 伤害源以 attacker 为直接实体。不能把被引爆的 target 填进直接实体位:
            // Iron's preHitEffects 按 directEntity instanceof IMagicSummon 走召唤物分支,
            // 若 target 恰是敌方召唤物,会错用敌方召唤者的召唤伤害加成,且其召唤者免伤。
            var src = brand.getDamageSource(attacker != null ? attacker : target);
            // attacker 可为 null（环境 WH 伤害 / 无 by 的测试注入）；Entity#isAlliedTo 会对其参数调用
            // getTeam()，故 attacker 为 null 时必须短路，否则过滤器 NPE 崩 tick。
            WinefoxHexHelper.applyAoeBurst(target.level(), box, damage, src,
                le -> le != attacker && le.isAlive() && (attacker == null || !le.isAlliedTo(attacker)));
        } finally {
            DEPTH.set(DEPTH.get() - 1);
        }
    }
}
