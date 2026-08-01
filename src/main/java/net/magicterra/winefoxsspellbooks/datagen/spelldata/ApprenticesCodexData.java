package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import jp.aquafactory.apprenticecodex.registry.EffectRegistry;
import jp.aquafactory.apprenticecodex.registry.SpellRegistry;

public final class ApprenticesCodexData {

    private ApprenticesCodexData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 近身结算的法术：具现刀剑类都在身前 1~2 格判定
        ctx.range().add(SpellRegistry.BOUND_SWORD.get().getSpellResource(), 2F);
        ctx.range().add(SpellRegistry.HIGANBANA.get().getSpellResource(), 2F);
        ctx.range().add(SpellRegistry.MANTIS_LEAP.get().getSpellResource(), 2F);
        ctx.range().add(SpellRegistry.MOON_LIGHT.get().getSpellResource(), 2F);
        ctx.range().add(SpellRegistry.PHALANX_CHARGE.get().getSpellResource(), 2F);
        ctx.range().add(SpellRegistry.PRECISION_JACK.get().getSpellResource(), 2F);
        ctx.range().add(SpellRegistry.SLASH_BLADE.get().getSpellResource(), 2F);

        // 自我强化
        ctx.effect().add(SpellRegistry.DIVINE_POSSESSION.get().getSpellResource(),
            EffectRegistry.DIVINE_POSSESSION.getId());
        ctx.effect().add(SpellRegistry.ECHO_CAST.get().getSpellResource(), EffectRegistry.ECHO_SPELL.getId());
        ctx.effect().add(SpellRegistry.PALETTE_SHIFT.get().getSpellResource(),
            EffectRegistry.PALETTE_RECEPTION.getId());

        // 攻击 / 负面
        ctx.effect().add(SpellRegistry.THERMAL_PROCESS.get().getSpellResource(),
            EffectRegistry.THERMAL_PROCESSING.getId());
    }
}
