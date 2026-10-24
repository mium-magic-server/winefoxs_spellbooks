package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.somake.somakespells.registries.ModMobEffects;
import com.somake.somakespells.registries.ModSpells;

public final class SomakeSpellsData {

    private SomakeSpellsData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 近身结算的法术
        ctx.range().add(ModSpells.HYDRO_SLASH.get().getSpellResource(), 2F);
        ctx.range().add(ModSpells.INCINERATOR_SLASH.get().getSpellResource(), 2F);
        ctx.range().add(ModSpells.LIGHTNING_CUT.get().getSpellResource(), 2F);
        ctx.range().add(ModSpells.MIRROR_STRIKE.get().getSpellResource(), 2F);
        ctx.range().add(ModSpells.RAM_TCHUM.get().getSpellResource(), 2F);

        // 自我强化
        ctx.effect().add(ModSpells.BLESSING.get().getSpellResource(), ModMobEffects.BLESSED.getId());
        ctx.effect().add(ModSpells.BLOOD_CUT.get().getSpellResource(), ModMobEffects.BLOOD_CUT.getId());
        ctx.effect().add(ModSpells.BLOOD_RUSH.get().getSpellResource(), ModMobEffects.BLOOD_RUSH.getId());
        ctx.effect().add(ModSpells.COMBUSTION.get().getSpellResource(), ModMobEffects.COMBUSTIONED.getId());
        ctx.effect().add(ModSpells.EARTHBOUND.get().getSpellResource(), ModMobEffects.EARTHBOUND.getId());
        ctx.effect().add(ModSpells.ELDRITCH_GAMBIT.get().getSpellResource(),
            ModMobEffects.ELDRITCH_GAMBIT.getId());
        ctx.effect().add(ModSpells.ENDER_CORRUPTION.get().getSpellResource(),
            ModMobEffects.ENDER_CORRUPTION.getId());
        ctx.effect().add(ModSpells.EVOCATION_FORTITUDE.get().getSpellResource(),
            ModMobEffects.EVOCATION_FORTITUDE.getId());
        ctx.effect().add(ModSpells.OVERGROWTH.get().getSpellResource(), ModMobEffects.OVERGROWN.getId());
        ctx.effect().add(ModSpells.PERMAFROST.get().getSpellResource(), ModMobEffects.PERMAFROST.getId());
        ctx.effect().add(ModSpells.REVERBERATION.get().getSpellResource(), ModMobEffects.REVERBERATION.getId());
        ctx.effect().add(ModSpells.ROSE_SECRET.get().getSpellResource(), ModMobEffects.ROSE_SECRET.getId());
        ctx.effect().add(ModSpells.SPIRIT_EMPOWERMENT.get().getSpellResource(),
            ModMobEffects.SPIRIT_EMPOWERMENT.getId());
        ctx.effect().add(ModSpells.SUBMERGE.get().getSpellResource(), ModMobEffects.SUBMERGED.getId());
        ctx.effect().add(ModSpells.SYMMETRY_EMPOWERMENT.get().getSpellResource(),
            ModMobEffects.SYMMETRY_EMPOWERMENT.getId());

        // 增益队友：CUSTODIA_CAELI 的旗印给范围内友军上生命 / 护甲加成
        ctx.effect().add(ModSpells.CUSTODIA_CAELI.get().getSpellResource(),
            ModMobEffects.CUSTODIA_CAELI_BLESSING.getId());
        // 增益队友：GUARDIAN_CONNECTION 建立守护连接，效果只挂在被保护的一方（施法目标）身上
        ctx.effect().add(ModSpells.GUARDIAN_CONNECTION.get().getSpellResource(),
            ModMobEffects.GUARDIAN_CONNECTION.getId());

        // 攻击 / 负面
        ctx.effect().add(ModSpells.LIGHTNING_CUT.get().getSpellResource(), ModMobEffects.ELECTROCUTED.getId());
        ctx.effect().add(ModSpells.LIGHTNING_LASH.get().getSpellResource(), ModMobEffects.ELECTROCUTED.getId());
        ctx.effect().add(ModSpells.LIGHTNING_STRIKE.get().getSpellResource(), ModMobEffects.ELECTROCUTED.getId());
        ctx.effect().add(ModSpells.STORM_AURA.get().getSpellResource(), ModMobEffects.WET.getId());
        ctx.effect().add(ModSpells.THUNDER_CLOUD.get().getSpellResource(), ModMobEffects.WET.getId());
    }
}
