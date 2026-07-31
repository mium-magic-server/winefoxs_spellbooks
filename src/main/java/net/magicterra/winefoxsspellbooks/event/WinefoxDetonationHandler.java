package net.magicterra.winefoxsspellbooks.event;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.magic.particle.CrescentBurstOption;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbParticles;
import net.magicterra.winefoxsspellbooks.registry.WsbSpells;
import net.minecraft.server.level.ServerLevel;
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
 *   <li>每次引爆为指挥者叠一层酒涌（酒涌唯一的产生途径）</li>
 * </ul>
 * 烙印自然到期的弱自爆在 {@link net.magicterra.winefoxsspellbooks.effect.CrescentBrandEffect} 内实现。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class WinefoxDetonationHandler {

    /**
     * 连锁深度计数器（女仆分支允许深度 1，玩家分支 0）。仅服务端主线程访问，无需 ThreadLocal。
     */
    private static int depth;

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide)
            return;
        if (!target.hasEffect(WsbEffects.CRESCENT_BRAND))
            return;
        if (!WinefoxHexHelper.isWinefoxHexDamage(event.getSource()))
            return;

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
        if (depth > maxDepth)
            return;

        // 烙印 amplifier = 挂印时的法术强度（含施法者属性），换算为引爆额外伤害；
        // 指令/数据包挂的 amplifier 0 即退回基础值
        var brandInstance = target.getEffect(WsbEffects.CRESCENT_BRAND);
        float bonus = brandInstance != null ? brandInstance.getAmplifier() : 0.0f;
        // 女仆分支伤害 ×1.5；半径在主人 Brewsurge 满层时 +1
        float damage = (WinefoxHexHelper.DETONATE_BASE + bonus) * (isMaid ? WinefoxHexHelper.MAID_MULT : 1.0f);
        int radius = isMaid
                     ? (WinefoxHexHelper.commanderBrewsurgeStacks(attacker) >= WinefoxHexHelper.BREWSURGE_MAX_STACKS
                        ? WinefoxHexHelper.MAID_RADIUS_SURGE : WinefoxHexHelper.MAID_RADIUS)
                     : WinefoxHexHelper.PLAYER_RADIUS;

        // 移除烙印（可能触发 MobEffectEvent.Remove，不触发 Expired）
        target.removeEffect(WsbEffects.CRESCENT_BRAND);

        // 引爆视觉:一包爆发发射器,客户端铺闪光/月牙碎片/按实际半径扩散的环波
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                new CrescentBurstOption(WsbParticles.CRESCENT_BURST.get(), radius),
                target.getX(), target.getY(), target.getZ(), 1, 0, 0, 0, 0);
        }

        // 用残月烙印法术的学派伤害源制造 AoE
        var brand = WsbSpells.CRESCENT_BRAND_SPELL.get();
        AABB box = target.getBoundingBox().inflate(radius);

        // 为指挥者叠一层酒涌。半径按叠层前的层数结算（radius 已在上方取定），新层作用于下一轮
        var commander = WinefoxHexHelper.commanderOf(attacker);
        if (commander != null) {
            WinefoxHexHelper.addBrewsurgeStack(commander);
        }

        depth++;
        try {
            // 伤害源以 attacker 为直接实体。不能把被引爆的 target 填进直接实体位:
            // Iron's preHitEffects 按 directEntity instanceof IMagicSummon 走召唤物分支,
            // 若 target 恰是敌方召唤物,会错用敌方召唤者的召唤伤害加成,且其召唤者免伤。
            var src = brand.getDamageSource(attacker != null ? attacker : target);
            // AoE 波及用阵营敌我判定，只把自己人摘出去
            WinefoxHexHelper.applyAoeBurst(target.level(), box, damage, src,
                le -> WinefoxHexHelper.isFactionHostile(le, attacker));
        } finally {
            depth--;
        }
    }
}
