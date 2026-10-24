package net.magicterra.winefoxsspellbooks.mixin.touhou_little_maid_spell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yimeng261.maidspell.item.bauble.enderPocket.EnderPocketService;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.List;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.EnderPocketManaHolder;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.TlmSpellCompat;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 末影腰包女仆列表生成后，为正在使用法力的女仆填入法力，显示条件与浮动法力条一致（见 {@link TlmSpellCompat#usesMana}）
 * <p>
 * 列表供 HUD 刷新、女仆选择界面与背包侧栏共用，填在这里各处的包都带得上。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
@Pseudo
@Mixin(targets = "com.github.yimeng261.maidspell.item.bauble.enderPocket.EnderPocketService", remap = false)
public abstract class EnderPocketServiceMixin {
    @ModifyReturnValue(method = "getPlayerEnderPocketMaids", at = @At("RETURN"), require = 0)
    private static List<EnderPocketService.EnderPocketMaidInfo> winefoxsSpellbooks$fillMana(
        List<EnderPocketService.EnderPocketMaidInfo> infos, ServerPlayer player) {
        for (EnderPocketService.EnderPocketMaidInfo info : infos) {
            ServerLevel level = player.server.getLevel(info.levelKey);
            if (level != null && level.getEntity(info.maidEntityId) instanceof EntityMaid maid
                && maid.getUUID().equals(info.maidUUID) && TlmSpellCompat.usesMana(maid)) {
                ((EnderPocketManaHolder) (Object) info).winefoxsSpellbooks$setMana(
                    MaidMagicManager.getMana(maid), (float) MaidMagicManager.getMaxMana(maid));
            }
        }
        return infos;
    }
}
