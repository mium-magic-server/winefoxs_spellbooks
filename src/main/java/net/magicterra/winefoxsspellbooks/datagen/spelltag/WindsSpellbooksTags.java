package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.raptorzizi.wind_spellbooks.registries.ModSpellRegistry;

/**
 * Wind's Spellbooks 附属：为其 Wind 学派法术分类。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class WindsSpellbooksTags {

    private WindsSpellbooksTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 本附属的全部法术对女仆均可正常施放，没有需要排除的法术。
        // ACROBATICS_SPELL 的二段连冲（recast）仅在效果回调中对 ServerPlayer 登记，女仆只能完成首次冲刺，故不打 recast 标记。

        // ========== 攻击 ==========
        // IRON_SLASH_SPELL: 沿视线方向冲刺并对路径上的非友方造成斩击伤害（伤害含武器伤害）
        ctx.attack().addOptional(ModSpellRegistry.IRON_SLASH_SPELL.get().getSpellResource());
        // TORNADO_SPELL: 在 16 格内的准星处生成龙卷风，持续对范围内非友方造成伤害
        ctx.attack().addOptional(ModSpellRegistry.TORNADO_SPELL.get().getSpellResource());
        ctx.attack().addOptional(ModSpellRegistry.WIND_BLADE_SPELL.get().getSpellResource());

        // ========== 自我强化 ==========
        // ALMIGHTY_PUSH_SPELL: 以施法者为中心的斥力球，击退范围内非友方并拦截投射物，自身不造成伤害
        ctx.defense().addOptional(ModSpellRegistry.ALMIGHTY_PUSH_SPELL.get().getSpellResource());
        // TAILWIND_SPELL: 提升跳跃与空中机动，并免疫摔落
        ctx.defense().addOptional(ModSpellRegistry.TAILWIND_SPELL.get().getSpellResource());

        // ========== 位移 ==========
        // ACROBATICS_SPELL: 向视线方向冲刺，途中撞到实体会造成伤害并爆发弹起
        ctx.movement().addOptional(ModSpellRegistry.ACROBATICS_SPELL.get().getSpellResource());
        // WIND_JUMP_SPELL: 朝视线方向向前上方跃起，落点附近附带阵风击退，并获得缓降
        ctx.movement().addOptional(ModSpellRegistry.WIND_JUMP_SPELL.get().getSpellResource());
    }
}
