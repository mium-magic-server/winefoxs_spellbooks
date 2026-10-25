package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffects;
import net.redreaper.twilight_spellbooks.init.ModMobEffects;
import net.redreaper.twilight_spellbooks.init.ModSpells;

/**
 * Spellbooks of Twilight（暮色法术书，暮色森林联动）的法术数据贡献者：已造成的效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class SpellbooksOfTwilightData {

    private SpellbooksOfTwilightData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 攻击 / 负面：效果挂在敌人身上
        ctx.effect().add(ModSpells.AURORA_MISSILE.get().getSpellResource(), MobEffectRegistry.GUIDING_BOLT.getId());
        ctx.effect().add(ModSpells.DRUID_BOLT.get().getSpellResource(), MobEffects.POISON.getKey().location());
        ctx.effect().add(ModSpells.EXANIMATED_FIREBALL.get().getSpellResource(), ModMobEffects.OMINOUS_BURN.getId());
        ctx.effect().add(ModSpells.EXANIMATED_RAY.get().getSpellResource(), ModMobEffects.OMINOUS_BURN.getId());
        // HYDRA_MORTAR_SHOT: 落点生成的火场给站在其中的目标叠加 HYDRA_FIRE
        ctx.effect().add(ModSpells.HYDRA_MORTAR_SHOT.get().getSpellResource(), ModMobEffects.HYDRA_FIRE.getId());
    }
}
