package net.magicterra.winefoxsspellbooks.entity.ai.behavior.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.api.spell.MaidSpellAction;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * 跟随模式（未开启 Home）下女仆的活动范围，口径与 TLM 的 {@code MaidFollowOwnerTask} 一致。
 * <p>
 * {@code MaidFollowOwnerTask} 在 core 活动里运行，战斗中同样生效：女仆离主人达到 {@code (int) getRestrictRadius() - 2} 格，它就把行走目标和视线目标都改成主人。战斗行为若把女仆推出这个范围， 两边就会互相拉扯：女仆被拉回主人身边、视线被锁在主人身上，走位变成绕着主人转圈，范围外的目标也永远打不到。 所以接战点、走位和索敌都以这个范围为界。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
public final class MaidLeash {
    /**
     * 相对跟随触发距离留出的余量，避免女仆停在边界上反复触发跟随
     */
    private static final double MARGIN = 1.0;

    private MaidLeash() {
    }

    /**
     * 跟随模式下约束女仆活动范围的主人。
     *
     * @param mob 实体
     * @return 主人；非女仆、开启了 Home、无法自主移动（坐下、骑乘等）、没有主人或主人不在同一维度时为 null
     */
    @Nullable
    public static LivingEntity leashOwner(Mob mob) {
        if (!(mob instanceof EntityMaid maid) || maid.isHomeModeEnable() || !maid.canBrainMoving()) {
            return null;
        }
        LivingEntity owner = maid.getOwner();
        if (owner == null || owner.isSpectator() || owner.isDeadOrDying() || owner.level() != maid.level()) {
            return null;
        }
        return owner;
    }

    /**
     * 女仆在不触发跟随的前提下，离主人最远能走到的距离。
     */
    public static double radius(EntityMaid maid) {
        return Math.max(0, (int) maid.getRestrictRadius() - 2 - MARGIN);
    }

    /**
     * {@code pos} 是否在以主人为圆心、{@code radius} 为半径的活动范围内。
     */
    public static boolean contains(LivingEntity owner, double radius, Vec3 pos) {
        return pos.distanceToSqr(owner.position()) < radius * radius;
    }

    /**
     * 跟随模式下目标在活动范围外时的接战点：活动范围内离目标最近的可站立位置。
     *
     * @return 接战点；不受跟随约束、或目标就在活动范围内（可以直接走向目标）时为 null
     */
    @Nullable
    public static Vec3 approach(Mob mob, LivingEntity target) {
        LivingEntity owner = leashOwner(mob);
        if (owner == null) {
            return null;
        }
        double radius = radius((EntityMaid) mob);
        if (contains(owner, radius, target.position())) {
            return null;
        }
        Vec3 center = new Vec3(owner.getX(), mob.getY(), owner.getZ());
        return SpellAttackWalkToTarget.approachWithin(mob, target, center, radius,
            pos -> contains(owner, radius, Vec3.atBottomCenterOf(pos)));
    }

    /**
     * 索敌过滤：跟随模式下，女仆站在活动范围边缘也够不着的目标不予锁定。
     * <p>
     * 施法距离取女仆针对敌人的法术（攻击、负面效果）里最远的一个；没有这类法术时取起手施法距离。 返回的谓词在创建时就算好可及距离，供逐个候选调用。
     *
     * @param maid 女仆
     * @return 目标是否可以接战
     */
    public static Predicate<LivingEntity> canEngage(EntityMaid maid) {
        LivingEntity owner = leashOwner(maid);
        if (owner == null) {
            return target -> true;
        }
        double reach = radius(maid) + engageRange(maid);
        double reachSqr = reach * reach;
        return target -> target.distanceToSqr(owner) <= reachSqr;
    }

    private static final MaidSpellAction[] ENGAGE_ACTIONS = {MaidSpellAction.ATTACK, MaidSpellAction.NEGATIVE};

    private static float engageRange(EntityMaid maid) {
        float range = 0;
        if (maid instanceof MaidMagicEntity magicEntity) {
            MaidSpellDataHolder holder = magicEntity.winefoxsSpellbooks$getSpellDataHolder();
            for (MaidSpellAction action : ENGAGE_ACTIONS) {
                for (SpellData spellData : holder.get(action)) {
                    range = Math.max(range, MaidSpellCatalog.getSpellRange(spellData.getSpell()));
                }
            }
        }
        return range > 0 ? range : Config.getStartSpellRange();
    }
}
