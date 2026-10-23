package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidBegTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.magicterra.winefoxsspellbooks.task.MaidCastingTask;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 法术战斗、支援期间不讨食。
 * <p>
 * TLM 的讨食行为在 work 活动里运行，战斗中同样会触发：主人手持诱惑物品时，女仆会播放讨食动作， 并把行走和视线目标改成主人，打断走位和瞄准。{@code canStillUse} 也调用这里，所以进入战斗时正在讨食会随即停止。 战斗状态取自 {@code MagicMaidAdapter#syncSpellCombatFlag} 维护的 aggressive 标志。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
@Mixin(MaidBegTask.class)
public abstract class MaidBegTaskMixin {
    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)Z",
        at = @At("HEAD"), cancellable = true)
    private void winefoxsSpellbooks$skipBegInSpellCombat(ServerLevel level, EntityMaid maid,
                                                         CallbackInfoReturnable<Boolean> cir) {
        if (maid.isAggressive() && maid.getTask() instanceof MaidCastingTask) {
            cir.setReturnValue(false);
        }
    }
}
