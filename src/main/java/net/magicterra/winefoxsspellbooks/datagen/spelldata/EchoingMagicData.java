package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.hazen.echoing_magic.Spells.EMSpellRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Echoing Magic（回响魔法）的法术数据：施法距离与附带效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class EchoingMagicData {

    private EchoingMagicData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // ========== 近战攻击法术 ==========
        // ECHOING_EXPLOSION: 以施法者为中心、半径 3.0 + 0.2 * 等级（最高 4.6）格的爆发，需要敌人贴身
        ctx.range().add(EMSpellRegistries.ECHOING_EXPLOSION.get().getSpellResource(), 2F);

        // ========== 附带效果 ==========
        // ECHOING_EXPLOSION: 给范围内目标挂 Archaion 的护甲破碎（Archaion 仅运行时依赖，按 ID 构造）
        ctx.effect().add(EMSpellRegistries.ECHOING_EXPLOSION.get().getSpellResource(),
            ResourceLocation.fromNamespaceAndPath("archaion", "armor_break"));
    }
}
