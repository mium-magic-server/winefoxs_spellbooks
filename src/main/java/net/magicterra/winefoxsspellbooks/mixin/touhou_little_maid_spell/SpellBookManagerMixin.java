package net.magicterra.winefoxsspellbooks.mixin.touhou_little_maid_spell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yimeng261.maidspell.api.ISpellBookProvider;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.TlmSpellCompat;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.WsbIronsSpellbooksProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 万法皆通在 {@code SpellBookManager} 静态初始化时按硬编码名单实例化各 provider；这里在其后把铁魔法那一项换成 {@link WsbIronsSpellbooksProvider}
 * <p>
 * 换在 provider 表首次被读取（{@code getProviders} 生成不可变快照）之前，其余模组的 provider 不动。
 * <p>
 * 另在其逐个 provider 施法时做互斥：铁魔法占着施法优先权时（见 {@link TlmSpellCompat#hasIronsSpellPriority}），
 * 其余占用施法的 provider 本轮让出；铁魔法冷却、缺蓝或本轮无可选时它们照原有节奏施放。拔刀剑与普攻并行，不受限制。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "com.github.yimeng261.maidspell.spell.manager.SpellBookManager", remap = false)
public abstract class SpellBookManagerMixin {
    @Shadow
    @Final
    private static Map<String, ISpellBookProvider<?, ?>> providerMap;

    @Inject(method = "initializeProviderFactories", at = @At("TAIL"), require = 0)
    private static void winefoxsSpellbooks$replaceIronsProvider(CallbackInfo ci) {
        if (!providerMap.containsKey(WsbIronsSpellbooksProvider.IRONS_MOD_ID)) {
            // 万法皆通自己没装上铁魔法 provider（例如铁魔法版本不兼容），不越俎代庖
            return;
        }
        providerMap.put(WsbIronsSpellbooksProvider.IRONS_MOD_ID, new WsbIronsSpellbooksProvider());
        WinefoxsSpellbooks.LOGGER.info("Touhou Little Maid: Spell detected; Iron's spellcasting for maids is handled by Winefox's Spellbooks");
    }

    @WrapOperation(method = "castSpell",
        at = @At(value = "INVOKE", target = "Lcom/github/yimeng261/maidspell/api/ISpellBookProvider;castSpell(Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)V"),
        require = 0)
    private void winefoxsSpellbooks$yieldToIronsSpell(ISpellBookProvider<?, ?> provider, EntityMaid maid, Operation<Void> original) {
        if (provider instanceof WsbIronsSpellbooksProvider || !winefoxsSpellbooks$occupiesCasting(provider)
            || !TlmSpellCompat.hasIronsSpellPriority(maid)) {
            original.call(provider, maid);
        }
    }

    /**
     * 按类名判断，不加载拔刀剑的类
     */
    @Unique
    private static boolean winefoxsSpellbooks$occupiesCasting(ISpellBookProvider<?, ?> provider) {
        return !"SlashBladeProvider".equals(provider.getClass().getSimpleName());
    }
}
