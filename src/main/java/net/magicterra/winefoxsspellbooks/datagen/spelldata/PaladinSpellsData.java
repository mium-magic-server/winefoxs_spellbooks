package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import eu.kaufko.paladin_spells.registry.PaladinEffectsRegistry;
import eu.kaufko.paladin_spells.registry.PaladinSpellRegistry;

/**
 * Paladin Spells（paladin_spells）的法术数据：近身结算范围与施放后的主要效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class PaladinSpellsData {

    private PaladinSpellsData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 近身结算的法术：冲撞伤害只在施放瞬间结算于冲撞盒内
        ctx.range().add(PaladinSpellRegistry.RAM_SPELL.get().getSpellResource(), 2F);

        // 自我强化
        ctx.effect().add(PaladinSpellRegistry.BEDROCK_SKIN_SPELL.get().getSpellResource(),
            PaladinEffectsRegistry.BEDROCK_SKIN_EFFECT.getId());
        ctx.effect().add(PaladinSpellRegistry.BULWARK_SPELL.get().getSpellResource(),
            PaladinEffectsRegistry.BULWARK_EFFECT.getId());
    }
}
