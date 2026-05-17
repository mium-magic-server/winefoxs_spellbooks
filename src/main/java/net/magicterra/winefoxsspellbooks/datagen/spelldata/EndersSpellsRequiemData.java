package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.ender.ess_requiem.registries.GGEffectRegistry;
import net.ender.ess_requiem.registries.GGSpellRegistry;

public final class EndersSpellsRequiemData {

    private EndersSpellsRequiemData() {}

    public static void contribute(SpellDataContext ctx) {
        // ========== 近战攻击法术 ==========
        // CLAW (Rip and Tear): 在施法者前方 1.9 格位置触发，检测距离 3.25 格
        ctx.range().add(GGSpellRegistry.CLAW.get().getSpellResource(), 2F);

        // Defense
        ctx.effect().add(GGSpellRegistry.UNDEAD_PACT.get().getSpellResource(),
            GGEffectRegistry.UNDEAD_PACT.getId());
        ctx.effect().add(GGSpellRegistry.STRAIN.get().getSpellResource(),
            GGEffectRegistry.STRAINED.getId());
        ctx.effect().add(GGSpellRegistry.REAPER.get().getSpellResource(),
            GGEffectRegistry.REAPER.getId());
        ctx.effect().add(GGSpellRegistry.EBONY_ARMOR.get().getSpellResource(),
            GGEffectRegistry.EBONY_ARMOR.getId());
        ctx.effect().add(GGSpellRegistry.PROTECTION_OF_THE_FALLEN.get().getSpellResource(),
            GGEffectRegistry.PROTECTION_OF_ASHES.getId());
        ctx.effect().add(GGSpellRegistry.CURSED_IMMORTALITY.get().getSpellResource(),
            GGEffectRegistry.CURSED_IMMORTALITY.getId());
        ctx.effect().add(GGSpellRegistry.EBONY_CATAPHRACT.get().getSpellResource(),
            GGEffectRegistry.EBONY_CATAPHRACT.getId());
        ctx.effect().add(GGSpellRegistry.BASTION_OF_LIGHT.get().getSpellResource(),
            GGEffectRegistry.BASTION_OF_LIGHT.getId());

        // Negative Effect
        ctx.effect().add(GGSpellRegistry.FINALITY_OF_DECAY.get().getSpellResource(),
            GGEffectRegistry.FINALITY_OF_DECAY.getId());
        // ETERNAL_BATTLEFIELD 通过 EternalBattlefield 实体给范围内目标施加 CURSED_IMMORTALITY 效果
        ctx.effect().add(GGSpellRegistry.ETERNAL_BATTLEFIELD.get().getSpellResource(),
            GGEffectRegistry.CURSED_IMMORTALITY.getId());

        // DISAPPEARING_ACT 给施法者/友军/目标施加 Iron's 核心的 TRUE_INVISIBILITY 效果
        ctx.effect().add(GGSpellRegistry.DISAPPEARING_ACT.get().getSpellResource(),
            MobEffectRegistry.TRUE_INVISIBILITY.getId());

        // Positive Effect (Spellblade)
        ctx.effect().add(GGSpellRegistry.HONE_EDGE.get().getSpellResource(),
            GGEffectRegistry.HONED_EDGE.getId());
        ctx.effect().add(GGSpellRegistry.UNDYING_DREAD.get().getSpellResource(),
            GGEffectRegistry.SOUL_STRENGTH.getId());
        ctx.effect().add(GGSpellRegistry.ADRENALINE_RUSH.get().getSpellResource(),
            GGEffectRegistry.ADRENALINE_RUSH.getId());
        ctx.effect().add(GGSpellRegistry.NIGHT_VEIL.get().getSpellResource(),
            GGEffectRegistry.NIGHT_VEIL.getId());
        ctx.effect().add(GGSpellRegistry.OVERWHELMING_LIGHT.get().getSpellResource(),
            GGEffectRegistry.BASTION_OF_LIGHT.getId());
    }
}
