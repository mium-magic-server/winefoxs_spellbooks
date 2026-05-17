package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.warphan.iss_magicfromtheeast.registries.MFTEEffectRegistries;
import net.warphan.iss_magicfromtheeast.registries.MFTESpellRegistries;

public final class MagicFromTheEastData {

    private MagicFromTheEastData() {}

    public static void contribute(SpellDataContext ctx) {
        // Defense
        // BAGUA_ARRAY_CIRCLE 创建八卦阵区域，给施法者施加 REVERSAL_HEALING 效果（伤害转化为治疗）
        ctx.effect().add(MFTESpellRegistries.BAGUA_ARRAY_CIRCLE_SPELL.get().getSpellResource(),
            MFTEEffectRegistries.REVERSAL_HEALING.getId());

        // Negative Effect
        // SOUL_BURST 命中目标施加 SOULBURN 灵魂燃烧 debuff
        ctx.effect().add(MFTESpellRegistries.SOUL_BURST_SPELL.get().getSpellResource(),
            MFTEEffectRegistries.SOULBURN.getId());
    }
}
