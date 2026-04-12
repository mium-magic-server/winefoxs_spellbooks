package net.magicterra.winefoxsspellbooks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.magicterra.winefoxsspellbooks.magic.MaidAllyHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 增强 Entity.isAlliedTo() 以支持通过召唤链解析同盟关系
 * <p>
 * 将两个实体分别解析到顶层实体（通过 IMagicSummon / OwnableEntity 链）， 然后委托顶层实体之间的 isAlliedTo 检查。 这使得其他模组（如 Teams Friendly Fire）的队伍检查能正确覆盖召唤物。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-03-18
 */
@Mixin(Entity.class)
public class EntityAllyMixin {

    /**
     * 通过召唤链解析增强同盟检查
     * <p>
     * 如果原始检查已返回 true，直接传递。 否则，将两个实体解析到各自的顶层召唤者， 委托顶层实体之间的 isAlliedTo 检查（让其他模组在玩家层级判断队伍关系）。
     */
    @ModifyReturnValue(
        method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z",
        at = @At("RETURN")
    )
    private boolean wsb$checkAllyViaSummonChain(boolean original, Entity other) {
        if (original) {
            return true;
        }

        Entity self = (Entity) (Object) this;

        // 仅在服务端处理
        if (self.level().isClientSide()) {
            return false;
        }

        // 解析两个实体的顶层实体（通过 IMagicSummon / OwnableEntity 链追溯）
        Entity selfRoot = MaidAllyHelper.getEffectiveRoot(self);
        Entity otherRoot = MaidAllyHelper.getEffectiveRoot(other);

        // 如果两者都没有被解析到不同的实体，跳过以避免递归
        if (selfRoot == self && otherRoot == other) {
            return false;
        }

        // 委托顶层实体之间的同盟检查
        // 其他模组（如 Teams Friendly Fire）的 mixin 会在此处对玩家做 FTB Teams 检查
        return selfRoot.isAlliedTo(otherRoot);
    }
}
