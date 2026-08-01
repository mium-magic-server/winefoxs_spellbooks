package net.magicterra.winefoxsspellbooks.magic.recast;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.MultiTargetEntityCastData;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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

    /**
     * 在 {@link #muzzle()} 处播一声，音源 PLAYERS、音调固定 1.0。收尾的起手音都走这里。
     */
    public void playAtMuzzle(SoundEvent sound, float volume) {
        Vec3 origin = muzzle();
        level.playSound(null, origin.x, origin.y, origin.z, sound, SoundSource.PLAYERS, volume, 1.0f);
    }

    /**
     * 本次咏唱存的是不是多目标标记数据。不是的话 {@link #markedTargets()} 恒为空表，收尾无事可做。
     */
    public boolean isMultiTargetCast() {
        return castData instanceof MultiTargetEntityCastData;
    }

    /**
     * 多目标收尾的统一闸门：先把冷却挂上（无论后面做不做事），再判断这次 recast 还有没有活可干—— 正常结束、且咏唱期间确实存了目标标记。
     *
     * @return 是否继续执行收尾
     */
    public boolean beginMultiTargetFinish() {
        addCooldown();
        return !result.isFailure() && isMultiTargetCast();
    }

    /**
     * 咏唱期间标记下来、此刻仍在世的目标，按标记顺序排列。
     * <p>
     * 标记存的是 UUID，收尾时目标可能已死或已卸载，解析不到的直接跳过；{@code castData} 不是多目标数据时返回空表。
     */
    public List<LivingEntity> markedTargets() {
        if (!(castData instanceof MultiTargetEntityCastData targetingData)) {
            return List.of();
        }
        List<LivingEntity> targets = new ArrayList<>(targetingData.getTargets().size());
        for (UUID uuid : targetingData.getTargets()) {
            if (level.getEntity(uuid) instanceof LivingEntity target) {
                targets.add(target);
            }
        }
        return targets;
    }
}
