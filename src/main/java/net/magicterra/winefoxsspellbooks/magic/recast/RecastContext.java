package net.magicterra.winefoxsspellbooks.magic.recast;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 一次 recast 收尾所需的全部上下文。
 *
 * @param caster   施法者（女仆或召唤女仆）
 * @param level    施法者所在服务端世界
 * @param spell    本次 recast 对应的法术
 * @param instance recast 实例（含等级、来源、剩余次数）
 * @param result   本次 recast 的结束原因（超时 / 用尽 / 反制 …）
 * @param castData 施法时存下的数据（目标列表、召唤物列表、锚点等），可能为 {@code null}
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public record RecastContext(
    LivingEntity caster,
    ServerLevel level,
    AbstractSpell spell,
    RecastInstance instance,
    RecastResult result,
    ICastDataSerializable castData
) {
    /**
     * 本次 recast 的法术等级。
     */
    public int spellLevel() {
        return instance.getSpellLevel();
    }

    /**
     * 计入施法者属性后的法术强度。
     */
    public float spellPower() {
        return spell.getSpellPower(spellLevel(), caster);
    }

    /**
     * 给施法者挂上本法术的冷却。几乎每个处理器都要做这一步。
     */
    public void addCooldown() {
        MaidMagicManager.addCooldown(caster, spell, instance.getCastSource());
    }

    /**
     * 施法者眼前一点点的位置，多用作弹体/音效的原点。
     */
    public Vec3 muzzle() {
        return caster.getEyePosition().add(caster.getForward().normalize().scale(0.2));
    }
}
