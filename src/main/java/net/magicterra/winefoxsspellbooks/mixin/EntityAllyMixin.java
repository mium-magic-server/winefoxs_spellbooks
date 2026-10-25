package net.magicterra.winefoxsspellbooks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.magicterra.winefoxsspellbooks.magic.MaidAllyHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 增强 Entity.isAlliedTo() 以支持通过召唤链解析同盟关系
 * <p>
 * 两个实体解析到同一个顶层实体（主人、主人的女仆、驯服宠物、各自的召唤物）时互为盟友，双向成立；
 * 解析到不同顶层时，委托顶层实体之间的 isAlliedTo 检查。这使得其他模组（如 Teams Friendly Fire）的队伍检查能正确覆盖召唤物。
 * <p>
 * 实体与自身<b>不</b>算盟友（与原版一致）：Iron's 的 {@code IMagicSummon#isAlliedHelper} 里有一处笔误
 * {@code otherOwner.isAlliedTo(otherOwner)}（本意应是 {@code owner.isAlliedTo(otherOwner)}），
 * 一旦「自己与自己」为真，任何有主人的召唤物 / 宠物都会被它判成「与所有带主人的实体互为盟友」。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-03-18
 */
@Mixin(Entity.class)
public class EntityAllyMixin {
    /**
     * 防重入标志：委托顶层实体判定的那一次嵌套调用里，本钩子直接放行原结果，不再继续解析
     */
    @Unique
    private static final ThreadLocal<Boolean> WSB_DELEGATING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /**
     * 通过召唤链解析增强同盟检查
     * <p>
     * 如果原始检查已返回 true，直接传递。否则按顺序：
     * <ol>
     *   <li>两者是不同实体、且解析到同一顶层（含「一方正是顶层」，如主人与其女仆）：返回 true。
     *       此前这一步缺失——主人这一侧解析后仍是自己，落入「无变化则跳过」的分支而恒为 false，导致 {@code master.isAlliedTo(maid)} 与反向不对称；</li>
     *   <li>两者都没有被解析到别的实体：返回 false（无召唤关系，避免递归）；</li>
     *   <li>否则委托顶层实体之间的 isAlliedTo（让其他模组在玩家层级判断队伍关系）。</li>
     * </ol>
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

        // 嵌套的顶层委托调用：直接放行原结果，防止递归
        if (WSB_DELEGATING.get()) {
            return false;
        }

        // 同一召唤链（共享顶层 UUID）且不是同一个实体：互为盟友，主人 / 女仆 / 宠物 / 各自的召唤物之间双向成立。
        // 排除实体自身，理由见类注释
        if (self != other && MaidAllyHelper.isAllied(self, other)) {
            return true;
        }

        // 解析两个实体的顶层实体（通过 IMagicSummon / OwnableEntity 链追溯）
        Entity selfRoot = MaidAllyHelper.getEffectiveRoot(self);
        Entity otherRoot = MaidAllyHelper.getEffectiveRoot(other);

        // 如果两者都没有被解析到不同的实体，跳过以避免递归
        if (selfRoot == self && otherRoot == other) {
            return false;
        }

        // 顶层是同一实体（上面按 UUID 比对没命中的兜底，如跨维度时按可达祖先解析）：
        // 不同实体互为盟友；实体与自身仍保持原版的 false
        if (selfRoot == otherRoot) {
            return self != other;
        }

        // 委托顶层实体之间的同盟检查
        // 其他模组（如 Teams Friendly Fire）的 mixin 会在此处对玩家做 FTB Teams 检查
        WSB_DELEGATING.set(Boolean.TRUE);
        try {
            return selfRoot.isAlliedTo(otherRoot);
        } finally {
            WSB_DELEGATING.set(Boolean.FALSE);
        }
    }
}
