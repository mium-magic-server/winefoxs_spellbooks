package net.magicterra.winefoxsspellbooks.datagen.spelldata;

import com.birdie.asterismarcanum.registries.ASARSpellRegistry;
import net.minecraft.world.effect.MobEffects;

/**
 * Asterism Arcanum 附属：法术附带的主要状态效果。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class AsterismArcanumData {

    private AsterismArcanumData() {
    }

    public static void contribute(SpellDataContext ctx) {
        // 增益队友：SILVERY_BARBS 的减伤免疫由幸运效果与标签共同触发，效果存在期间才有效
        ctx.effect().add(ASARSpellRegistry.SILVERY_BARBS.get().getSpellResource(),
            MobEffects.LUCK.getKey().location());
    }
}
