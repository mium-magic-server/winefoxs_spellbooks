package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.jastkub.fdspells.registry.FDSRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffects;

/**
 * Frozen Dominion Spells（frozen_dominion_spells）：施放距离覆盖与法术挂在敌人身上的主要效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class FrozenDominionSpellsData {

    private FrozenDominionSpellsData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 以施法者为中心的近身冰刺圈，需要敌人贴近
        ctx.range().add(FDSRegistry.WINTERS_SORROW.get().getSpellResource(), 3F);

        // 攻击 / 控制
        ctx.effect().add(FDSRegistry.FROST_JAVELIN.get().getSpellResource(), MobEffectRegistry.CHILLED.getId());
        ctx.effect().add(FDSRegistry.FROST_SHACKLES.get().getSpellResource(),
            MobEffects.MOVEMENT_SLOWDOWN.getKey().location());
        ctx.effect().add(FDSRegistry.ICICLE_RAIN.get().getSpellResource(), MobEffectRegistry.CHILLED.getId());
    }
}
