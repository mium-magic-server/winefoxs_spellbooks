package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.chronomancy.registry.ChronoSpellRegistry;

/**
 * Chronomancy：时间系法术。多数法术只对玩家（ServerPlayer）或本模组自带的 ChronoMobCaster 生效，女仆施放会无效果，
 * 因此仅归类对任意 LivingEntity 施法者都能结算的法术。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class ChronomancyTags {

    private ChronomancyTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：ACCELERATED_ZONE 对区域内除施法者外的所有生物生效（含主人与友方），加速、增伤并在进出边界时造成伤害，且没有敌我过滤。
        // audit:ignore ACCELERATED_ZONE_SPELL
        // 原因：BORROWED_FUTURE、CHRONO_DOUBLE、REWIND、THE_WORLD_STOP、TIME_WALK 的 onCast 只处理 ServerPlayer（或模组自带的 ChronoMobCaster），女仆施放无任何效果。
        // audit:ignore BORROWED_FUTURE_SPELL, CHRONO_DOUBLE_SPELL, REWIND_SPELL, THE_WORLD_STOP_SPELL, TIME_WALK_SPELL

        // ========== 攻击 ==========
        // BACKTRACK_SPELL: 时间回溯弹，命中后把目标拉回数秒前的位置并在一秒后结算伤害
        ctx.attack().addOptional(ChronoSpellRegistry.BACKTRACK_SPELL.getSpellResource());
        // RIFT_SPELL: 撕开时间裂隙，复制目标生物并让复制体攻击原体（需要目标，不能复制首领），不可通过战利品或合成获得
        ctx.attack().addOptional(ChronoSpellRegistry.RIFT_SPELL.getSpellResource());
        ctx.summon().addOptional(ChronoSpellRegistry.RIFT_SPELL.getSpellResource());
        // SANDS_OF_TIME_SPELL: 持续施法的沙流锥，持续伤害并逐步减缓目标
        ctx.attack().addOptional(ChronoSpellRegistry.SANDS_OF_TIME_SPELL.getSpellResource());
        // TIME_PIERCING_NEEDLE_SPELL: 一秒后沿施法瞬间的朝向射出时间针，多针叠加后崩解结算伤害
        ctx.attack().addOptional(ChronoSpellRegistry.TIME_PIERCING_NEEDLE_SPELL.getSpellResource());

        // ========== 控制 ==========
        // TEMPORAL_STASIS_BEAM_SPELL: 射线命中后将目标时间停滞数秒，期间受到的伤害有上限
        ctx.negativeEffect().addOptional(ChronoSpellRegistry.TEMPORAL_STASIS_BEAM_SPELL.getSpellResource());
        // TIME_DILATION_FIELD_SPELL: 投出时间球，落点形成减速球域（施法者本人免疫，其余生物都会被减速）
        ctx.negativeEffect().addOptional(ChronoSpellRegistry.TIME_DILATION_FIELD_SPELL.getSpellResource());
    }
}
