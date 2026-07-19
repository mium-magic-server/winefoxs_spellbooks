package net.magicterra.winefoxsspellbooks.event;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 监听 Iron's SpellOnCastEvent：玩家施放任意 Winefox Hex 法术 → 累/刷新 Brewsurge
 * <br>
 * SpellOnCastEvent 仅在玩家施法时触发，正合 Brewsurge 的玩家专属定位
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
@EventBusSubscriber(modid = WinefoxsSpellbooks.MODID)
public class WinefoxCastListener {
    @SubscribeEvent
    public static void onSpellCast(SpellOnCastEvent event) {
        if (!WinefoxHexHelper.isWinefoxHexSchool(event.getSchoolType())) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        var cur = player.getEffect(WsbEffects.BREWSURGE);
        int next = Math.min((cur == null ? -1 : cur.getAmplifier()) + 1,
                WinefoxHexHelper.BREWSURGE_MAX_STACKS - 1);
        player.addEffect(new MobEffectInstance(WsbEffects.BREWSURGE,
                WinefoxHexHelper.BREWSURGE_DURATION, next));
    }
}
