package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import org.wizardshelp.registry.WizardsHelpSpellRegistry;

/**
 * Wizard's Help 附属模组的法术标签贡献。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class WizardsHelpTags {

    private WizardsHelpTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // ========== 召唤 ==========
        // 九个召唤物都以施法者为主人，沿用 Iron's 的 GenericOwner* 目标选择与跟随目标，会帮女仆作战。
        // SUMMON_APOTHECARIST: 药剂师，向友方投掷治疗药水，同时用毒箭、酸球等攻击敌人，攻守兼具但以输出为主
        ctx.attack().addOptional(WizardsHelpSpellRegistry.SUMMON_APOTHECARIST.get().getSpellResource());
        ctx.attack().addOptional(WizardsHelpSpellRegistry.SUMMON_ARCHEVOKER.get().getSpellResource());
        ctx.attack().addOptional(WizardsHelpSpellRegistry.SUMMON_CRYOMANCER.get().getSpellResource());
        ctx.attack().addOptional(WizardsHelpSpellRegistry.SUMMON_CULTIST.get().getSpellResource());
        ctx.attack().addOptional(WizardsHelpSpellRegistry.SUMMON_ICE_SPIDER.get().getSpellResource());
        ctx.attack().addOptional(WizardsHelpSpellRegistry.SUMMON_KEEPER.get().getSpellResource());
        ctx.attack().addOptional(WizardsHelpSpellRegistry.SUMMON_NECROMANCER.get().getSpellResource());
        ctx.attack().addOptional(WizardsHelpSpellRegistry.SUMMON_PYROMANCER.get().getSpellResource());
        // SUMMON_PRIEST: 牧师，主要职责是治疗、祝福与强化主人，输出只是附带，故归为防御类召唤
        ctx.defense().addOptional(WizardsHelpSpellRegistry.SUMMON_PRIEST.get().getSpellResource());

        ctx.summon().addOptional(WizardsHelpSpellRegistry.SUMMON_APOTHECARIST.get().getSpellResource());
        ctx.summon().addOptional(WizardsHelpSpellRegistry.SUMMON_ARCHEVOKER.get().getSpellResource());
        ctx.summon().addOptional(WizardsHelpSpellRegistry.SUMMON_CRYOMANCER.get().getSpellResource());
        ctx.summon().addOptional(WizardsHelpSpellRegistry.SUMMON_CULTIST.get().getSpellResource());
        ctx.summon().addOptional(WizardsHelpSpellRegistry.SUMMON_ICE_SPIDER.get().getSpellResource());
        ctx.summon().addOptional(WizardsHelpSpellRegistry.SUMMON_KEEPER.get().getSpellResource());
        ctx.summon().addOptional(WizardsHelpSpellRegistry.SUMMON_NECROMANCER.get().getSpellResource());
        ctx.summon().addOptional(WizardsHelpSpellRegistry.SUMMON_PRIEST.get().getSpellResource());
        ctx.summon().addOptional(WizardsHelpSpellRegistry.SUMMON_PYROMANCER.get().getSpellResource());
    }
}
