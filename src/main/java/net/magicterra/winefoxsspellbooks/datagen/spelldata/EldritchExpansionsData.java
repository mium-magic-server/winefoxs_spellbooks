package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import dev.morsecodeguy.eldritchexpansion.spell.EldritchExpansionSpells;
import net.minecraft.world.effect.MobEffects;

/**
 * Eldritch Expansions 附属：法术附带的主要状态效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class EldritchExpansionsData {

    private EldritchExpansionsData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 自我强化
        ctx.effect().add(EldritchExpansionSpells.FERAL_DISTORTION.get().getSpellResource(),
            MobEffects.DAMAGE_BOOST.getKey().location());
        ctx.effect().add(EldritchExpansionSpells.PALE_COVENANT.get().getSpellResource(),
            MobEffects.REGENERATION.getKey().location());
        ctx.effect().add(EldritchExpansionSpells.PHANTOM_STRIDE.get().getSpellResource(),
            MobEffects.INVISIBILITY.getKey().location());

        // 攻击 / 负面
        ctx.effect().add(EldritchExpansionSpells.GRAVEHOOK.get().getSpellResource(),
            MobEffects.MOVEMENT_SLOWDOWN.getKey().location());
        ctx.effect().add(EldritchExpansionSpells.NULL_REND.get().getSpellResource(),
            MobEffects.WEAKNESS.getKey().location());
    }
}
