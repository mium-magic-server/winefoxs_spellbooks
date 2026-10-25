package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.github.astralight37.ironsspellsandunstablespellbooks.SpellsRegister;
import net.minecraft.world.effect.MobEffects;

/**
 * Iron's Spells 'n Unstable Spellbooks（不稳定魔法）的法术数据贡献者：已造成的效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class UnstableMagicData {

    private UnstableMagicData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 自我强化：效果挂在女仆自己身上；PONDER_HORN 同时给速度与力量，取力量作为代表
        ctx.effect().add(SpellsRegister.PONDER_HORN.get().getSpellResource(),
            MobEffects.DAMAGE_BOOST.getKey().location());
    }
}
