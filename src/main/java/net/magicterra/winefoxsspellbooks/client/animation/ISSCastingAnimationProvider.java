package net.magicterra.winefoxsspellbooks.client.animation;

import com.github.tartaricacid.touhoulittlemaid.api.animation.IMagicCastingAnimationProvider;
import com.github.tartaricacid.touhoulittlemaid.api.animation.IMagicCastingState;
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.AnimationBuilder;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.file.AnimationFile;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.resource.GeckoLibCache;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 铁魔法施法动画适配
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-03 10:09
 */
public class ISSCastingAnimationProvider implements IMagicCastingAnimationProvider {
    private static final String ANIMATION_NAME_PREFIX = "iss:";

    @Override
    public @Nullable IMagicCastingState getMagicCastingState(IMaid maid) {
        if (!(maid.asEntity() instanceof MaidMagicEntity maidMagicEntity)) {
            return null;
        }
        return maidMagicEntity.winefoxSpellbooks$getMagicCastingState();
    }

    @Override
    public @Nullable AnimationBuilder getAnimationBuilder(IMaid maid, IMagicCastingState state) {
        if (!(state instanceof MagicCastingAnimateStateHolder animateState)) {
            return null;
        }
        IMagicCastingState.CastingPhase currentPhase = animateState.getCurrentPhase();
        if (currentPhase == null || currentPhase == IMagicCastingState.CastingPhase.NONE) {
            return null;
        }

        MaidModelInfo maidModelInfo = CustomPackLoader.MAID_MODELS.getInfo(maid.getModelId()).orElse(null);
        AnimationFile animationFile = maidModelInfo == null ? null : GeckoLibCache.getInstance().getAnimations().get(maidModelInfo.getModelId());
        AbstractSpell castingSpell = animateState.getCastingSpellType();
        AbstractSpell spell;
        if (castingSpell == null || castingSpell == SpellRegistry.none()) {
            spell = animateState.getInstantCastSpellType();
        } else {
            spell = castingSpell;
        }
        if (spell == null || spell == SpellRegistry.none()) {
            return null;
        }
        if (currentPhase == IMagicCastingState.CastingPhase.START
            || currentPhase == IMagicCastingState.CastingPhase.CASTING
            || currentPhase == IMagicCastingState.CastingPhase.INSTANT) {
            return getStartAnimationFromSpell(animationFile, spell, animateState);
        } else if (currentPhase == IMagicCastingState.CastingPhase.END) {
            return getFinishAnimationFromSpell(animationFile, spell, animateState);
        }
        return null;
    }

    private AnimationBuilder getStartAnimationFromSpell(AnimationFile animationFile, AbstractSpell spell, MagicCastingAnimateStateHolder animateState) {
        Optional<RawAnimation> opRawAnimation = spell.getCastStartAnimation().getForMob();
        if (opRawAnimation.isPresent()) {
            RawAnimation rawAnimation = opRawAnimation.get();
            AnimationBuilder builder = toTlmAnimation(animationFile, rawAnimation);
            animateState.setCancelled(false);
            if (spell.getCastType() == CastType.INSTANT) {
                animateState.clearInstantCastSpellType();
            }
            return builder;
        } else {
            animateState.setCancelled(true);
            return null;
        }
    }

    private AnimationBuilder getFinishAnimationFromSpell(AnimationFile animationFile, AbstractSpell spell, @UnknownNullability MagicCastingAnimateStateHolder animateState) {
        animateState.clearInstantCastSpellType();
        if (spell.getCastFinishAnimation().isPass) {
            animateState.setCancelled(false);
            return null;
        }
        Optional<RawAnimation> opRawAnimation = spell.getCastFinishAnimation().getForMob();
        if (opRawAnimation.isPresent()) {
            RawAnimation rawAnimation = opRawAnimation.get();
            AnimationBuilder builder = toTlmAnimation(animationFile, rawAnimation);
            animateState.setCancelled(false);
            return builder;
        } else {
            animateState.setCancelled(true);
            return null;
        }
    }

    private static AnimationBuilder toTlmAnimation(AnimationFile animationFile, RawAnimation rawAnimation) {
        AnimationBuilder builder = new AnimationBuilder();
        for (RawAnimation.Stage animationStage : rawAnimation.getAnimationStages()) {
            String animationName = ANIMATION_NAME_PREFIX + animationStage.animationName();
            com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.Animation customAnimation = animationFile.getAnimation(animationName);
            ILoopType loopType = null;
            if (customAnimation != null) {
                loopType = customAnimation.loop;
            } else {
                if (animationStage.loopType() == Animation.LoopType.LOOP) {
                    loopType = ILoopType.EDefaultLoopTypes.LOOP;
                } else if (animationStage.loopType() == Animation.LoopType.PLAY_ONCE) {
                    loopType = ILoopType.EDefaultLoopTypes.PLAY_ONCE;
                } else if (animationStage.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) {
                    loopType = ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME;
                }
            }
            builder.addAnimation(animationName, loopType);
        }
        return builder;
    }
}
