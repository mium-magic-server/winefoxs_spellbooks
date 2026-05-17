package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.rinko1231.SnowWaifuSpell.init.ModSpellRegistry;

public final class SnowWaifuSpellTags {

    private SnowWaifuSpellTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(ModSpellRegistry.SUMMON_SNOW_QUEEN_SPELL.get().getSpellResource());
        ctx.summon().addOptional(ModSpellRegistry.SUMMON_SNOW_QUEEN_SPELL.get().getSpellResource());
        // SUMMON_SNOW_QUEEN: getRecastCount() = 2，多段召唤
        ctx.maidShouldRecast().addOptional(ModSpellRegistry.SUMMON_SNOW_QUEEN_SPELL.get().getSpellResource());
    }
}
