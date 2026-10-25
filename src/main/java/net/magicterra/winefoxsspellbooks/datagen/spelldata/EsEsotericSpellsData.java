package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.inolia_zaicek.barrier_spell.registries.BSMobEffectRegistry;
import com.inolia_zaicek.barrier_spell.registries.BSSpellRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;

/**
 * Esoteric Spells（秘法法术）的施法范围与已造成效果贡献者。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class EsEsotericSpellsData {

    private EsEsotericSpellsData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 近身结算的法术
        // IMPACT_MAGIC: 以施法者为中心的冲击波，需要敌人贴近才能命中
        ctx.range().add(BSSpellRegistry.IMPACT_MAGIC.get().getSpellResource(), 2F);
        // MAGIC_BLADE_ENCHANT_SLASH: 约 3.25 格的前方挥砍
        ctx.range().add(BSSpellRegistry.MAGIC_BLADE_ENCHANT_SLASH.get().getSpellResource(), 2F);

        // 结界：效果由结界实体周期性刷新给范围内友军（含施法者）
        // CAST_BARRIER: 使用 Iron's 的 HASTENED
        ctx.effect().add(BSSpellRegistry.CAST_BARRIER.get().getSpellResource(), MobEffectRegistry.HASTENED.getId());
        ctx.effect().add(BSSpellRegistry.ASPIS_SOUL_BARRIER.get().getSpellResource(),
            BSMobEffectRegistry.ASPIS_SOUL.getId());
        ctx.effect().add(BSSpellRegistry.ETHER_BARRIER.get().getSpellResource(), BSMobEffectRegistry.ETHER.getId());
        ctx.effect().add(BSSpellRegistry.FLOWING_BARRIER.get().getSpellResource(),
            BSMobEffectRegistry.FLOWING_BARRIER.getId());
        ctx.effect().add(BSSpellRegistry.GLORIA_BARRIER.get().getSpellResource(), BSMobEffectRegistry.GLORIA.getId());
        ctx.effect().add(BSSpellRegistry.GUARDIAN_BARRIER.get().getSpellResource(),
            BSMobEffectRegistry.GUARDING.getId());
        ctx.effect().add(BSSpellRegistry.PRAYER_BARRIER.get().getSpellResource(), BSMobEffectRegistry.PRAYER.getId());

        // 光环：效果由光环实体周期性刷新给范围内友军（含施法者）
        // BARRIER_AURA: 效果为 PROTECTION
        ctx.effect().add(BSSpellRegistry.BARRIER_AURA.get().getSpellResource(),
            BSMobEffectRegistry.PROTECTION.getId());
        ctx.effect().add(BSSpellRegistry.BRAVE_AURA.get().getSpellResource(), BSMobEffectRegistry.BRAVE.getId());
        ctx.effect().add(BSSpellRegistry.CYCLE_AURA.get().getSpellResource(), BSMobEffectRegistry.CYCLE.getId());
        ctx.effect().add(BSSpellRegistry.QUICK_MOTION_AURA.get().getSpellResource(),
            BSMobEffectRegistry.QUICK_MOTION.getId());

        // 自我强化
        ctx.effect().add(BSSpellRegistry.DUAL_BRINGER.get().getSpellResource(),
            BSMobEffectRegistry.DUAL_BRINGER.getId());
        ctx.effect().add(BSSpellRegistry.LIMIT_BREAK.get().getSpellResource(),
            BSMobEffectRegistry.LIMIT_BREAK.getId());
        ctx.effect().add(BSSpellRegistry.MANA_RESONANCE.get().getSpellResource(),
            BSMobEffectRegistry.MANA_RESONANCE.getId());
        ctx.effect().add(BSSpellRegistry.RESURRECTION.get().getSpellResource(),
            BSMobEffectRegistry.RESURRECTION.getId());
        ctx.effect().add(BSSpellRegistry.SPELL_CALIBRATION.get().getSpellResource(),
            BSMobEffectRegistry.SPELL_CALIBRATION.getId());

        // 使魔与装置
        // AUTO_DEVICE: 效果时长与装置存续时间一致
        ctx.effect().add(BSSpellRegistry.AUTO_DEVICE.get().getSpellResource(),
            BSMobEffectRegistry.AUTO_DEVICE_DURATION.getId());
        // FAMILIA_CAT_SUMMON: 由黑猫使魔每刻刷新给施法者
        ctx.effect().add(BSSpellRegistry.FAMILIA_CAT_SUMMON.get().getSpellResource(),
            BSMobEffectRegistry.FAMILIA_BLACK_CAT.getId());
        ctx.effect().add(BSSpellRegistry.FAMILIA_RESEARCH.get().getSpellResource(),
            BSMobEffectRegistry.MAGIC_CIRCUIT.getId());
        ctx.effect().add(BSSpellRegistry.FINALE_GUIDE.get().getSpellResource(), BSMobEffectRegistry.GUIDE.getId());
        ctx.effect().add(BSSpellRegistry.FINALE_STONE_BARRIER.get().getSpellResource(),
            BSMobEffectRegistry.STONE_SKIN.getId());
    }
}
