package net.magicterra.winefoxsspellbooks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * 灵狐精魂 - 酒狐巫法学派 Focus 物品
 * <p>
 * 三重身份：学派图鉴 icon、Scroll Forge 卷轴锻造材料、高阶法术媒介。
 * 获取方式三路并行：女仆击杀妖精、宝箱战利品、满好感度女仆晨赠。
 * <p>
 * hover tooltip 与 JEI 信息页由 {@code WsbItemDescTooltipHandler} 与 {@code WsbJeiPlugin}
 * 通过 {@code item.winefoxs_spellbooks.vulpine_anima.desc} 翻译键自动注入。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public class VulpineAnima extends Item {
    public VulpineAnima() {
        super(new Properties().stacksTo(64).rarity(Rarity.RARE));
    }
}
