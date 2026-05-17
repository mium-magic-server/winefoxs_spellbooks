package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.hazen.hazennstuff.Registries.HnSEffects;
import net.hazen.hazennstuff.Spells.HnSSpellRegistries;

public final class HazenNStuffData {

    private HazenNStuffData() {}

    public static void contribute(SpellDataContext ctx) {
        // ========== 近战攻击法术 ==========
        // SCORCHING_SLASH: 在施法者前方 1.9 格位置触发，检测距离 3.25 格（同时发射投射物）
        ctx.range().add(HnSSpellRegistries.SCORCHING_SLASH.get().getSpellResource(), 2F);
        // NIGHTS_EDGE_STRIKE: 在施法者前方 1.9 格位置触发，检测距离 3.25 格（同时发射投射物）
        ctx.range().add(HnSSpellRegistries.NIGHTS_EDGE_STRIKE.get().getSpellResource(), 2F);

        // Negative Effect
        // GOLDEN_SHOWER 发射 IchorStream 弹射物，命中目标施加 ICHOR 效果（降低护甲、护甲韧性、法术抗性）
        ctx.effect().add(HnSSpellRegistries.GOLDEN_SHOWER.get().getSpellResource(),
            HnSEffects.ICHOR.getId());

        // Positive Effect
        // MOONKISSED 给施法者施加 MOONKISSED 增益效果（水下采矿速度、水中移动效率、宇宙/水系法术威能）
        ctx.effect().add(HnSSpellRegistries.MOONKISSED.get().getSpellResource(),
            HnSEffects.MOONKISSED.getId());
    }
}
