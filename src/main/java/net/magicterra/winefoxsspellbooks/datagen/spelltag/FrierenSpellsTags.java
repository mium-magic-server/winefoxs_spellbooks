package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.frierenvoid.FrierenVoid;

/**
 * Frieren Spells（frierenvoid）的法术分类：Zoltraak 光束、奇点与防御魔法。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class FrierenSpellsTags {

    private FrierenSpellsTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 本插件的四个法术均可由女仆正常施放，无需排除。

        // ========== 攻击 ==========
        // BLACK_ZOLTRAAK / ZOLTRAAK: 女仆没有模式选择，按默认单发光束施放
        // SINGULARITY: 在手掌位置生成吸引并持续伤害范围内敌人的奇点，默认不伤害玩家与被动生物
        ctx.attack().addOptional(FrierenVoid.BLACK_ZOLTRAAK.get().getSpellResource());
        ctx.attack().addOptional(FrierenVoid.SINGULARITY.get().getSpellResource());
        ctx.attack().addOptional(FrierenVoid.ZOLTRAAK.get().getSpellResource());

        // ========== 自我强化 ==========
        // DEFENSE: 在施法者面前生成吸收伤害的六边形魔法屏障，持续 4 秒
        ctx.defense().addOptional(FrierenVoid.DEFENSE.get().getSpellResource());
    }
}
