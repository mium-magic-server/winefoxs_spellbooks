package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.somake.somakespells.registries.ModSpells;

public final class SomakeSpellsTags {

    private SomakeSpellsTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // RITUAL_FLAME 只用来点燃祭坛、转化烈焰人，战斗中没有用处。
        // audit:ignore RITUAL_FLAME

        // ========== Fire ==========
        ctx.attack().addOptional(ModSpells.ABYSSAL_BURN.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.APOCALYPTIC_BURST.get().getSpellResource());
        // DAMNED_DEMOMANS: 自身增益，增益期间由 handler 持续放出爆发伤害，交战时施放
        ctx.attack().addOptional(ModSpells.DAMNED_DEMOMANS.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.COMBUSTION.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.ERUPTION.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FIRE_BLAST.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FIRE_ORBS.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FIRESTORM_VORTEX.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.IGNIS_SHIELD.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.INCINERATOR_SLASH.get().getSpellResource());

        // ========== Blood ==========
        ctx.defense().addOptional(ModSpells.BLOOD_CUT.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.BLOODMARK.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.BLOOD_RUSH.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.BLOODY_LEGACY.get().getSpellResource());
        // CURSED_CONNECTION: 在两个敌人之间建链，伤害互相镜像
        ctx.attack().addOptional(ModSpells.CURSED_CONNECTION.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.FRAGMENTED_REQUIEM.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.ROSE_SECRET.get().getSpellResource());

        // ========== Holy ==========
        // BLESSED_CONNECTION: 建链后两端共享治疗量
        ctx.supportEffect().addOptional(ModSpells.BLESSED_CONNECTION.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.BLESSING.get().getSpellResource());
        ctx.positiveEffect().addOptional(ModSpells.CUSTODIA_CAELI.get().getSpellResource());
        // GUARDIAN_CONNECTION: 把队友承受的伤害转移到自己身上
        ctx.positiveEffect().addOptional(ModSpells.GUARDIAN_CONNECTION.get().getSpellResource());

        // ========== Lightning ==========
        ctx.attack().addOptional(ModSpells.LIGHTNING_BALL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_CUT.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_DANCE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_FIELD.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_LASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_SPARK.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_SPEAR.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_STRIKE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.LIGHTNING_SWARM.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.STORM_AURA.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.THUNDER_CLOUD.get().getSpellResource());

        // ========== Aqua ==========
        ctx.attack().addOptional(ModSpells.HYDRO_SLASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SEA_SERPENT.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SEA_SERPENT_JET.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.SUBMERGE.get().getSpellResource());
        // TIDAL_DASH: 锚点把施法者拽向目标并撞击，主用途是打伤害
        ctx.attack().addOptional(ModSpells.TIDAL_DASH.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.TIDAL_GRASP.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.TSUNAMI.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.WATER_BALL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.WATER_CONTROL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.WATER_SPEAR.get().getSpellResource());

        // ========== Symmetry ==========
        ctx.attack().addOptional(ModSpells.AXE_CLEAVE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.DESERT_WRATH.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.HALBERD_STRIKE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.MIRROR_STRIKE.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.SYMMETRY_EMPOWERMENT.get().getSpellResource());

        // ========== Ender / Eldritch / Evocation / Geo ==========
        ctx.attack().addOptional(ModSpells.ABYSSAL_ORBS.get().getSpellResource());
        // CHAIN_CONNECTION: 锁链把两个目标拴在一起并互相拉扯
        ctx.attack().addOptional(ModSpells.CHAIN_CONNECTION.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.EARTHBOUND.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.ELDRITCH_GAMBIT.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.ENDER_CORRUPTION.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.EVOCATION_FORTITUDE.get().getSpellResource());
        ctx.movement().addOptional(ModSpells.RENDER_RUSH.get().getSpellResource());

        // ========== Nature / Ice ==========
        ctx.defense().addOptional(ModSpells.OVERGROWTH.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.PERMAFROST.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.PUMPKIN_BOMB.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SUMMON_ZOMBIE.get().getSpellResource());
        ctx.summon().addOptional(ModSpells.SUMMON_ZOMBIE.get().getSpellResource());

        // ========== Sound / Spirit ==========
        ctx.attack().addOptional(ModSpells.JINGLE_BELL.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.PHANTOM_BARRAGE.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.RAM_TCHUM.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.RESONANT_PULSE.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.REVERBERATION.get().getSpellResource());
        ctx.negativeEffect().addOptional(ModSpells.SLUMBER_MELODY.get().getSpellResource());
        ctx.attack().addOptional(ModSpells.SOUL_GRAB.get().getSpellResource());
        ctx.defense().addOptional(ModSpells.SPIRIT_EMPOWERMENT.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(ModSpells.BLESSED_CONNECTION.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.BLOODMARK.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.CHAIN_CONNECTION.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.CURSED_CONNECTION.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.FIRE_ORBS.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.HYDRO_SLASH.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.LIGHTNING_CUT.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.PHANTOM_BARRAGE.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(ModSpells.THUNDER_CLOUD.get().getSpellResource());
    }
}
