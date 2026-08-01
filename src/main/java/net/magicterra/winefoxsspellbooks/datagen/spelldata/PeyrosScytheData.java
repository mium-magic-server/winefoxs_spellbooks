package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.rinko1231.peyroscythe.init.MobEffectRegistry;
import com.rinko1231.peyroscythe.init.NewSpellRegistry;

public final class PeyrosScytheData {

    private PeyrosScytheData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 自我强化
        ctx.effect().add(NewSpellRegistry.BLACK_FLAME_WINGS.get().getSpellResource(),
            MobEffectRegistry.BLACK_FLAME_WINGS.getId());
        // CRIMSON_MOON: 血月本体给拥有者挂 MOON_FRENZY
        ctx.effect().add(NewSpellRegistry.CRIMSON_MOON_SPELL.get().getSpellResource(),
            MobEffectRegistry.MOON_FRENZY.getId());
        ctx.effect().add(NewSpellRegistry.SINFIRE_EMBRACE_SPELL.get().getSpellResource(),
            MobEffectRegistry.SINFIRE_EMBRACE.getId());

        // 攻击 / 负面
        ctx.effect().add(NewSpellRegistry.DEATH_SMOKE_SPELL.get().getSpellResource(),
            MobEffectRegistry.DEATH_SMOKE_EROSION.getId());
        ctx.effect().add(NewSpellRegistry.FROZEN_WORLD_SPELL.get().getSpellResource(),
            io.redspace.ironsspellbooks.registries.MobEffectRegistry.CHILLED.getId());
        // HOLY_BELL: 金钟实体给非友方目标挂减攻减速
        ctx.effect().add(NewSpellRegistry.HOLY_BELL_SPELL.get().getSpellResource(),
            MobEffectRegistry.HOLY_BELL_SUPPRESSION.getId());
        ctx.effect().add(NewSpellRegistry.ILLUSION_CRAZY.get().getSpellResource(),
            MobEffectRegistry.ILLUSION_CRAZY.getId());
        ctx.effect().add(NewSpellRegistry.ILLUSION_FEAR.get().getSpellResource(),
            MobEffectRegistry.ILLUSION_FEAR.getId());
    }
}
