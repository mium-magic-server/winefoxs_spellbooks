package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.snackpirate.aeromancy.spells.AASpells;

public final class AeromancyAdditionsTags {

    private AeromancyAdditionsTags() {}

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(AASpells.WIND_CHARGE.get().getSpellResource());
        ctx.attack().addOptional(AASpells.ASPHYXIATE.get().getSpellResource());
        ctx.attack().addOptional(AASpells.WIND_BLADE.get().getSpellResource());
        ctx.defense().addOptional(AASpells.AIRSTEP.get().getSpellResource());
        ctx.defense().addOptional(AASpells.WIND_SHIELD.get().getSpellResource());
        ctx.defense().addOptional(AASpells.AIRBLAST.get().getSpellResource());
        ctx.defense().addOptional(AASpells.FLUSH.get().getSpellResource());
        ctx.movement().addOptional(AASpells.DASH.get().getSpellResource());
        ctx.positiveEffect().addOptional(AASpells.FEATHER_FALL.get().getSpellResource());

        // UPDRAFT: 多次施放，将敌人击飞并施加 MOVEMENT_SLOWDOWN（眩晕）
        ctx.attack().addOptional(AASpells.UPDRAFT.get().getSpellResource());
        ctx.negativeEffect().addOptional(AASpells.UPDRAFT.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(AASpells.UPDRAFT.get().getSpellResource());
    }
}
