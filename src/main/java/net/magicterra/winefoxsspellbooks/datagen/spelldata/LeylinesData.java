package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import dev.semeth.leylines.registry.LeySpellRegistry;
import dev.semeth.leylines.registry.ModEffects;
import net.minecraft.world.effect.MobEffects;

/**
 * Leylines（leylines）的法术数据：施放后的主要效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class LeylinesData {

    private LeylinesData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 自我强化
        ctx.effect().add(LeySpellRegistry.LEY_VEIL.get().getSpellResource(),
            MobEffects.INVISIBILITY.getKey().location());

        // 攻击 / 控制
        ctx.effect().add(LeySpellRegistry.CHRONO_TETHER.get().getSpellResource(),
            MobEffects.MOVEMENT_SLOWDOWN.getKey().location());
        ctx.effect().add(LeySpellRegistry.LEY_ECLIPSE.get().getSpellResource(),
            MobEffects.DARKNESS.getKey().location());
        ctx.effect().add(LeySpellRegistry.TEMPORAL_STUTTER.get().getSpellResource(),
            ModEffects.TIME_STOP.getId());
        ctx.effect().add(LeySpellRegistry.TETHER_GAZE.get().getSpellResource(), ModEffects.TIME_STOP.getId());
    }
}
