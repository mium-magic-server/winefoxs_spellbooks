package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.aetherspellbooks.registry.ASSpells;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;

/**
 * Aether Spellbooks（以太法术书，Aether 联动）的法术数据贡献者：近身射程与已造成的效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class AetherSpellbooksData {

    private AetherSpellbooksData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 近身结算的法术：以施法者为中心的爆发，半径仅 3.5 至 6 格，射程压到 2F 避免隔着半个战场空放
        ctx.range().add(ASSpells.GRAVITITE_SURGE.get().getSpellResource(), 2F);

        // 攻击 / 负面：效果挂在敌人身上
        ctx.effect().add(ASSpells.ICESTONE_METEOR.get().getSpellResource(), MobEffectRegistry.CHILLED.getId());
    }
}
