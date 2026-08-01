package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffects;
import net.redreaper.monsterspellbooks.init.ModMobEffects;
import net.redreaper.monsterspellbooks.init.ModSpellRegistry;

public final class MonstersSpellbooksData {

    private MonstersSpellbooksData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 近身结算的法术：判定盒就在身前几格，射程不压到 2F 女仆会隔着半个战场空放
        ctx.range().add(ModSpellRegistry.CAUTERIZING_TOUCH.get().getSpellResource(), 2F);
        ctx.range().add(ModSpellRegistry.ENDERSENT_SMASH.get().getSpellResource(), 2F);
        ctx.range().add(ModSpellRegistry.GRAVITY_WELL.get().getSpellResource(), 2F);
        ctx.range().add(ModSpellRegistry.PRIMORDIAL_FLASH.get().getSpellResource(), 2F);
        ctx.range().add(ModSpellRegistry.SANGUINITE_EVISCERATION.get().getSpellResource(), 2F);
        ctx.range().add(ModSpellRegistry.SPACE_BREAKER.get().getSpellResource(), 2F);
        ctx.range().add(ModSpellRegistry.SPIRIT_STRIKE.get().getSpellResource(), 2F);
        ctx.range().add(ModSpellRegistry.VILE_SLASH.get().getSpellResource(), 2F);

        // 自我强化：效果挂在女仆自己身上
        ctx.effect().add(ModSpellRegistry.ENDERSENT_FORM.get().getSpellResource(),
            ModMobEffects.ENDERSENT_STRENGTH.getId());
        ctx.effect().add(ModSpellRegistry.HYSTERIA.get().getSpellResource(), ModMobEffects.BLOODLUST.getId());
        ctx.effect().add(ModSpellRegistry.LICHDOM.get().getSpellResource(), ModMobEffects.LICHDOM.getId());
        ctx.effect().add(ModSpellRegistry.OVERHEAT.get().getSpellResource(), ModMobEffects.OVERHEAT.getId());
        ctx.effect().add(ModSpellRegistry.REAPER_ASPECT.get().getSpellResource(),
            ModMobEffects.REAPER_ASPECT.getId());
        ctx.effect().add(ModSpellRegistry.SOUL_FORM.get().getSpellResource(), ModMobEffects.SOUL_FORM.getId());
        ctx.effect().add(ModSpellRegistry.SPELL_OF_UNDYING.get().getSpellResource(),
            ModMobEffects.EFFECT_OF_UNDYING.getId());

        // 增益队友：效果挂在被支援的目标身上
        ctx.effect().add(ModSpellRegistry.AIR_BUBBLE.get().getSpellResource(),
            MobEffects.WATER_BREATHING.getKey().location());
        ctx.effect().add(ModSpellRegistry.ENCHANTERS_BOOST.get().getSpellResource(),
            MobEffects.DAMAGE_BOOST.getKey().location());
        ctx.effect().add(ModSpellRegistry.ENCHANTERS_PROTECTION.get().getSpellResource(),
            MobEffects.DAMAGE_RESISTANCE.getKey().location());
        ctx.effect().add(ModSpellRegistry.FROST_COATING.get().getSpellResource(),
            ModMobEffects.FROST_COATING.getId());
        ctx.effect().add(ModSpellRegistry.SOUL_CASTING_FIELD.get().getSpellResource(),
            ModMobEffects.SOUL_BOOSTED.getId());

        // 攻击 / 负面：效果挂在敌人身上
        ctx.effect().add(ModSpellRegistry.ANTICOAGULATION.get().getSpellResource(),
            ModMobEffects.HEAL_CUT.getId());
        ctx.effect().add(ModSpellRegistry.BANSHEE_SCREAM.get().getSpellResource(),
            MobEffects.WITHER.getKey().location());
        ctx.effect().add(ModSpellRegistry.BLOOD_PIERCE.get().getSpellResource(), MobEffectRegistry.REND.getId());
        ctx.effect().add(ModSpellRegistry.BRIMSTONE_WRATH.get().getSpellResource(),
            ModMobEffects.BRIMSTONE_FLAME.getId());
        ctx.effect().add(ModSpellRegistry.CAUTERIZING_TOUCH.get().getSpellResource(),
            ModMobEffects.HEAL_CUT.getId());
        ctx.effect().add(ModSpellRegistry.CRUSH.get().getSpellResource(), ModMobEffects.HEAVY_BODY.getId());
        ctx.effect().add(ModSpellRegistry.ENDERSENT_SMASH.get().getSpellResource(),
            MobEffectRegistry.AIRBORNE.getId());
        ctx.effect().add(ModSpellRegistry.FALL_CURSE.get().getSpellResource(), ModMobEffects.PARALYSIS.getId());
        ctx.effect().add(ModSpellRegistry.FRENZIED_BURST.get().getSpellResource(), ModMobEffects.MADNESS.getId());
        ctx.effect().add(ModSpellRegistry.FRENZY_SURGE.get().getSpellResource(), ModMobEffects.MADNESS.getId());
        ctx.effect().add(ModSpellRegistry.GLOW_INK_RELEASE.get().getSpellResource(),
            MobEffectRegistry.GUIDING_BOLT.getId());
        ctx.effect().add(ModSpellRegistry.GRAVITY_WELL.get().getSpellResource(), ModMobEffects.PARALYSIS.getId());
        ctx.effect().add(ModSpellRegistry.GUARDIANS_NEUTRALIZER.get().getSpellResource(),
            ModMobEffects.PARALYSIS.getId());
        ctx.effect().add(ModSpellRegistry.SANGUINITE_EVISCERATION.get().getSpellResource(),
            ModMobEffects.BLEEDING.getId());
        ctx.effect().add(ModSpellRegistry.SOUL_CHAIN.get().getSpellResource(),
            ModMobEffects.SPACE_ANCHORED.getId());
        ctx.effect().add(ModSpellRegistry.SPACE_BREAKER.get().getSpellResource(), ModMobEffects.PARALYSIS.getId());
        ctx.effect().add(ModSpellRegistry.SPECTRAL_BLAST.get().getSpellResource(), ModMobEffects.LETHARGY.getId());
        ctx.effect().add(ModSpellRegistry.SPIRIT_STRIKE.get().getSpellResource(), ModMobEffects.SOUL_REND.getId());
        ctx.effect().add(ModSpellRegistry.SUFFOCATE.get().getSpellResource(), ModMobEffects.SUFFOCATION.getId());
        ctx.effect().add(ModSpellRegistry.WIND_WAVE.get().getSpellResource(), MobEffectRegistry.AIRBORNE.getId());
        ctx.effect().add(ModSpellRegistry.WITHER_NOVA.get().getSpellResource(), ModMobEffects.SOUL_REND.getId());
    }
}
