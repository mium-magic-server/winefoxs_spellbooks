package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import net.iriri.cursespells.registry.CurseEffectRegistry;
import net.iriri.cursespells.registry.CurseSpellRegistry;
import net.minecraft.world.effect.MobEffects;

/**
 * CS Curse Spells（诅咒法术）的法术数据：施法距离与附带效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class CsCurseSpellsData {

    private CsCurseSpellsData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // ========== 近战攻击法术 ==========
        // STAGNANT_SPIRAL: 飞刃环绕半径 3.2 格，命中判定再外扩 2.1 格，需要敌人贴身
        ctx.range().add(CurseSpellRegistry.STAGNANT_SPIRAL.get().getSpellResource(), 2F);

        // ========== 自身光环 ==========
        // 效果挂在施法者身上，周期性向周围敌人降下伤害
        ctx.effect().add(CurseSpellRegistry.ABHORRENT_STAR.get().getSpellResource(),
            CurseEffectRegistry.ABHORRENT_STAR.getId());
        ctx.effect().add(CurseSpellRegistry.ALL_VANITY.get().getSpellResource(),
            CurseEffectRegistry.ALL_VANITY.getId());
        ctx.effect().add(CurseSpellRegistry.INFERNO.get().getSpellResource(),
            CurseEffectRegistry.INFERNO.getId());

        // ========== 攻击 / 凋零 ==========
        // 以下法术在命中时按法术等级配置凋零的时长与强度；其余一并附带的诅咒、冰冻等效果来自可选模组 l2complements，不登记
        ctx.effect().add(CurseSpellRegistry.A_CALL_BEYOND.get().getSpellResource(),
            MobEffects.WITHER.getKey().location());
        ctx.effect().add(CurseSpellRegistry.CURSED_SLASH.get().getSpellResource(),
            MobEffects.WITHER.getKey().location());
        ctx.effect().add(CurseSpellRegistry.DROPLET_OF_STAGNATION.get().getSpellResource(),
            MobEffects.WITHER.getKey().location());
        ctx.effect().add(CurseSpellRegistry.HOMING_SOULMASS.get().getSpellResource(),
            MobEffects.WITHER.getKey().location());
        ctx.effect().add(CurseSpellRegistry.PILLAR_OF_SIN.get().getSpellResource(),
            MobEffects.WITHER.getKey().location());
        ctx.effect().add(CurseSpellRegistry.WANDERING_GRUDGE.get().getSpellResource(),
            MobEffects.WITHER.getKey().location());
    }
}
