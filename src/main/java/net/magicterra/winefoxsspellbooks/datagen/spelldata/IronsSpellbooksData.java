package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;

public final class IronsSpellbooksData {

    private IronsSpellbooksData() {}

    public static void contribute(SpellDataContext ctx) {
        // ========== 近战攻击法术 ==========
        // DIVINE_SMITE: 在施法者前方 1.7 格位置触发 AOE，范围 2.2*2 = 4.4 格
        ctx.range().add(SpellRegistry.DIVINE_SMITE_SPELL.get().getSpellResource(), 2F);
        // FLAMING_STRIKE: 在施法者前方 1.9 格位置触发，检测距离 3.25 格
        ctx.range().add(SpellRegistry.FLAMING_STRIKE_SPELL.get().getSpellResource(), 2F);

        // ========== 支援法术范围限制 ==========
        ctx.range().add(SpellRegistry.CLEANSE_SPELL.get().getSpellResource(), 3F);
        ctx.range().add(SpellRegistry.HEALING_CIRCLE_SPELL.get().getSpellResource(), 5F);

        // ========== 默认远程距离（无对应法术 ID 时的兜底） ==========
        ctx.range().add(SpellRegistry.none().getSpellResource(), 15F);

        // ========== Defense ==========
        ctx.effect().add(SpellRegistry.HEARTSTOP_SPELL.get().getSpellResource(), MobEffectRegistry.HEARTSTOP.getId());
        ctx.effect().add(SpellRegistry.ECHOING_STRIKES_SPELL.get().getSpellResource(), MobEffectRegistry.ECHOING_STRIKES.getId());
        ctx.effect().add(SpellRegistry.EVASION_SPELL.get().getSpellResource(), MobEffectRegistry.EVASION.getId());
        ctx.effect().add(SpellRegistry.INVISIBILITY_SPELL.get().getSpellResource(), MobEffectRegistry.TRUE_INVISIBILITY.getId());
        ctx.effect().add(SpellRegistry.CHARGE_SPELL.get().getSpellResource(), MobEffectRegistry.CHARGED.getId());
        ctx.effect().add(SpellRegistry.SPIDER_ASPECT_SPELL.get().getSpellResource(), MobEffectRegistry.SPIDER_ASPECT.getId());
        ctx.effect().add(SpellRegistry.OAKSKIN_SPELL.get().getSpellResource(), MobEffectRegistry.OAKSKIN.getId());
        ctx.effect().add(SpellRegistry.ABYSSAL_SHROUD_SPELL.get().getSpellResource(), MobEffectRegistry.ABYSSAL_SHROUD.getId());

        // ========== Support ==========
        ctx.effect().add(SpellRegistry.GLUTTONY_SPELL.get().getSpellResource(), MobEffectRegistry.GLUTTONY.getId());

        // ========== Positive Effect ==========
        ctx.effect().add(SpellRegistry.FORTIFY_SPELL.get().getSpellResource(), MobEffectRegistry.FORTIFY.getId());
        ctx.effect().add(SpellRegistry.HASTE_SPELL.get().getSpellResource(), MobEffectRegistry.HASTENED.getId());

        // ========== Negative Effect ==========
        ctx.effect().add(SpellRegistry.SLOW_SPELL.get().getSpellResource(), MobEffectRegistry.SLOWED.getId());
        ctx.effect().add(SpellRegistry.HEAT_SURGE_SPELL.get().getSpellResource(), MobEffectRegistry.REND.getId());

        // ========== Attack（带 MobEffect 的攻击法术）==========
        ctx.effect().add(SpellRegistry.FROSTWAVE_SPELL.get().getSpellResource(), MobEffectRegistry.CHILLED.getId());
        ctx.effect().add(SpellRegistry.BLIGHT_SPELL.get().getSpellResource(), MobEffectRegistry.BLIGHT.getId());
        ctx.effect().add(SpellRegistry.FROSTBITE_SPELL.get().getSpellResource(), MobEffectRegistry.FROSTBITTEN_STRIKES.getId());
    }
}
