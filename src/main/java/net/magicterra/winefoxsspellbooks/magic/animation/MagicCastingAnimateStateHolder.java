package net.magicterra.winefoxsspellbooks.magic.animation;

import com.github.tartaricacid.touhoulittlemaid.api.animation.IMagicCastingState;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import net.minecraft.world.entity.LivingEntity;

/**
 * 咏唱动画状态机：把「同步下来的施法数据」翻译成女仆动画系统认得的 {@link IMagicCastingState.CastingPhase}（起手 / 持续 / 收招 / 瞬发）。
 * <p>
 * 双端共用（由 {@code MagicMaidAdapter} 持有），但 {@link #updateState} 开头有 {@code isClientSide}
 * 闸门，只有客户端会推进阶段。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-11
 */
public class MagicCastingAnimateStateHolder implements IMagicCastingState {
    private CastingPhase phase;

    private boolean cancelled;

    private AbstractSpell castingSpellType = SpellRegistry.none();

    private AbstractSpell instantCastSpellType = SpellRegistry.none();

    private boolean clientIsCasting = false;

    public MagicCastingAnimateStateHolder(CastingPhase phase) {
        this(phase, false);
    }

    public MagicCastingAnimateStateHolder(CastingPhase phase, boolean cancelled) {
        this.phase = phase;
        this.cancelled = cancelled;
    }

    @Override
    public CastingPhase getCurrentPhase() {
        return phase;
    }

    public void setCurrentPhase(CastingPhase phase) {
        this.phase = phase;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public AbstractSpell getInstantCastSpellType() {
        return instantCastSpellType;
    }

    public void clearInstantCastSpellType() {
        this.instantCastSpellType = SpellRegistry.none();
        this.phase = CastingPhase.NONE;
    }

    public AbstractSpell getCastingSpellType() {
        return castingSpellType;
    }

    public void updateState(LivingEntity maid, SyncedSpellData syncedSpellData) {
        if (!maid.level().isClientSide) {
            return;
        }

        boolean oldIsCasting = clientIsCasting;
        AbstractSpell lastCastingSpell = castingSpellType;
        castingSpellType = SpellRegistry.getSpell(syncedSpellData.getCastingSpellId());
        clientIsCasting = syncedSpellData.isCasting();

        if (castingSpellType == SpellRegistry.none() && lastCastingSpell == SpellRegistry.none()) {
            if (phase != CastingPhase.INSTANT) {
                phase = CastingPhase.NONE;
            }
            return;
        }

        if (!clientIsCasting && oldIsCasting) {
            castingSpellType = lastCastingSpell;
            phase = CastingPhase.END;
            instantCastSpellType = SpellRegistry.none();
        } else if (clientIsCasting && !oldIsCasting) {
            phase = CastingPhase.START;
            if (castingSpellType.getCastType() == CastType.INSTANT) {
                instantCastSpellType = castingSpellType;
                castingSpellType = SpellRegistry.none();
                phase = CastingPhase.INSTANT;
            } else {
                instantCastSpellType = SpellRegistry.none();
            }
        } else if (clientIsCasting) {
            phase = CastingPhase.CASTING;
        } else {
            castingSpellType = SpellRegistry.none();
            phase = CastingPhase.NONE;
            instantCastSpellType = SpellRegistry.none();
        }
    }

    public void clearLastCastSpellType() {
        castingSpellType = SpellRegistry.none();
        phase = CastingPhase.NONE;
    }
}
