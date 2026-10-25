package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.cursedschool.registry.ModEffects;
import com.cursedschool.registry.ModSpells;
import net.minecraft.world.effect.MobEffects;

/**
 * Cursed School 附属模组的施法距离与法术附带效果贡献。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class CursedSchoolData {

    private CursedSchoolData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // ========== 施法距离 ==========
        // 近身爆发、短扫与贴身射线，需要敌人贴近女仆才会命中
        ctx.range().add(ModSpells.BONE_TOLL.get().getSpellResource(), 2F);
        ctx.range().add(ModSpells.DECAY_FIELD.get().getSpellResource(), 2F);
        ctx.range().add(ModSpells.GRAVE_DUST.get().getSpellResource(), 2F);
        ctx.range().add(ModSpells.RUIN_SLASH.get().getSpellResource(), 2F);
        ctx.range().add(ModSpells.WITHERING_TOUCH.get().getSpellResource(), 2F);
        // ECLIPSE_MAW: 判定盒中心在视线前方 4 格，有效范围约 2.5 至 8.5 格
        ctx.range().add(ModSpells.ECLIPSE_MAW.get().getSpellResource(), 5F);
        // SEVERED_TITHE: 射线长度仅 6 格，超出默认施法距离后会落空
        ctx.range().add(ModSpells.SEVERED_TITHE.get().getSpellResource(), 5F);

        // ========== 自我强化 ==========
        ctx.effect().add(ModSpells.APOSTATE_CROWN.get().getSpellResource(), ModEffects.APOSTATE_CROWN.getId());
        ctx.effect().add(ModSpells.ASH_GUARD.get().getSpellResource(), ModEffects.ASH_GUARD.getId());
        ctx.effect().add(ModSpells.MARGIN_PACT.get().getSpellResource(), ModEffects.MARGIN_PACT.getId());
        ctx.effect().add(ModSpells.NIGHT_LEDGER.get().getSpellResource(), ModEffects.NIGHT_LEDGER.getId());
        ctx.effect().add(ModSpells.SOOT_SHROUD.get().getSpellResource(), ModEffects.SOOT_SHROUD.getId());

        // ========== 控制 / 削弱 ==========
        ctx.effect().add(ModSpells.BLACK_ROOT.get().getSpellResource(), ModEffects.BLACK_ROOT.getId());
        // BLACK_VERSE: 领域内每次脉冲都会刷新虚弱
        ctx.effect().add(ModSpells.BLACK_VERSE.get().getSpellResource(), MobEffects.WEAKNESS.getKey().location());
        ctx.effect().add(ModSpells.CHAIN_OF_REMORSE.get().getSpellResource(), ModEffects.REMORSE_LINK.getId());
        ctx.effect().add(ModSpells.INVOICE.get().getSpellResource(), ModEffects.INVOICE.getId());
        ctx.effect().add(ModSpells.STAIN_BRAND.get().getSpellResource(), ModEffects.STAIN_BRAND.getId());
        ctx.effect().add(ModSpells.UMBRAL_COFFIN.get().getSpellResource(), ModEffects.UMBRAL_CAGE.getId());

        // ========== 攻击 ==========
        ctx.effect().add(ModSpells.DEBT_SPIKE.get().getSpellResource(), ModEffects.OWED.getId());
        ctx.effect().add(ModSpells.HEX_BARRAGE.get().getSpellResource(), ModEffects.HEX_SEAL.getId());
        ctx.effect().add(ModSpells.MARROW_DRAIN.get().getSpellResource(), ModEffects.MARROW_WOUND.getId());
        ctx.effect().add(ModSpells.RUIN_SLASH.get().getSpellResource(), ModEffects.ARMOR_SHRED.getId());
        ctx.effect().add(ModSpells.WAILING_DIRGE.get().getSpellResource(), ModEffects.DIRGE_FEAR.getId());
        ctx.effect().add(ModSpells.WITHERING_TOUCH.get().getSpellResource(), MobEffects.WITHER.getKey().location());
    }
}
