package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.grandsigil.registry.GsEffects;
import com.grandsigil.registry.GsSpells;
import net.minecraft.world.effect.MobEffects;

/**
 * Apex Arcana（grandsigil）：法术施放后挂在敌人身上的主要效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class ApexArcanaData {

    private ApexArcanaData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 攻击：下列效果均挂在敌人身上，敌我判定依赖修复后的 EntityAllyMixin
        ctx.effect().add(GsSpells.GRAND_SIGIL.get().getSpellResource(), GsEffects.BOUND.getId());
        // BLACK_COFFIN: 棺内敌人被 BOUND 束缚
        ctx.effect().add(GsSpells.BLACK_COFFIN.get().getSpellResource(), GsEffects.BOUND.getId());
        // THE_GAZE: 领域内敌人被 TRANSFIXED 定住并持续刷新
        ctx.effect().add(GsSpells.THE_GAZE.get().getSpellResource(), GsEffects.TRANSFIXED.getId());
        // CRIMSON_THREADS / DIMENSION_CUT / ICE_AGE: 敌人持续被施加缓慢
        ctx.effect().add(GsSpells.CRIMSON_THREADS.get().getSpellResource(), MobEffects.MOVEMENT_SLOWDOWN.getKey().location());
        ctx.effect().add(GsSpells.DIMENSION_CUT.get().getSpellResource(), MobEffects.MOVEMENT_SLOWDOWN.getKey().location());
        ctx.effect().add(GsSpells.ICE_AGE.get().getSpellResource(), MobEffects.MOVEMENT_SLOWDOWN.getKey().location());
        // FULL_BLOOM: 敌人持续中毒（同时减速），盟友获得再生
        ctx.effect().add(GsSpells.FULL_BLOOM.get().getSpellResource(), MobEffects.POISON.getKey().location());
        // RECKONING 的药水效果只给盟友，STILLED_WORLD 的缓慢只对 Boss 生效，均不挂在普通敌人身上，故不登记
    }
}
