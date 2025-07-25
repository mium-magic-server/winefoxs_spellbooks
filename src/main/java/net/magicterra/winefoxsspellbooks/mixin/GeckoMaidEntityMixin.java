package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.AnimatableEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.AnimationState;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.PlayState;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.AnimationBuilder;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.controller.AnimationController;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntity;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.util.Log;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * GeckoLib 动画支持
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-23 00:52
 */
@Mixin(GeckoMaidEntity.class)
public abstract class GeckoMaidEntityMixin extends AnimatableEntity<EntityMaid> implements IGeoEntity {
    @Unique
    private AbstractSpell lastCastSpellType = SpellRegistry.none();

    @Unique
    private boolean animatingLegs = false;

    @Unique
    private final AnimationController<?> animationControllerOtherCast = new AnimationController<>(this, "other_casting", 2, this::otherCastingPredicate);

    @Unique
    private final AnimationController<?> animationControllerInstantCast = new AnimationController<>(this, "instant_casting", 2, this::instantCastingPredicate);

    @Unique
    private final AnimationController<?> animationControllerLongCast = new AnimationController<>(this, "long_casting", 2, this::longCastingPredicate);


    public GeckoMaidEntityMixin(EntityMaid entity, int fps) {
        super(entity, fps);
    }

    @Inject(method = "registerControllers", at = @At("TAIL"))
    public void afterRegisterControllers(CallbackInfo ci) {
        addAnimationController(animationControllerOtherCast);
        addAnimationController(animationControllerInstantCast);
        addAnimationController(animationControllerLongCast);
    }

    @Unique
    private PlayState instantCastingPredicate(AnimationEvent<AnimatableEntity<EntityMaid>> event) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();

        if (accessor.winefoxsSpellbooks$getCancelCastAnimation()) {
            return PlayState.STOP;
        }

        var controller = event.getController();
        if (accessor.winefoxsSpellbooks$getInstantCastSpellType() != SpellRegistry.none() && controller.getAnimationState() == AnimationState.STOPPED) {
            setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getInstantCastSpellType());
            accessor.winefoxsSpellbooks$setInstantCastSpellType(SpellRegistry.none());
        }
        return PlayState.CONTINUE;
    }

    @Unique
    private PlayState longCastingPredicate(AnimationEvent<AnimatableEntity<EntityMaid>> event) {
        var controller = event.getController();

        IMagicEntity maid = (IMagicEntity) getMaid();
        MaidMagicEntity accessor = (MaidMagicEntity) maid;

        if (accessor.winefoxsSpellbooks$getCancelCastAnimation() || (controller.getAnimationState() == AnimationState.STOPPED &&
            !(maid.isCasting() && accessor.winefoxsSpellbooks$getCastingSpell() != null &&
                accessor.winefoxsSpellbooks$getCastingSpell().getSpell().getCastType() == CastType.LONG))) {
            return PlayState.STOP;
        }

        if (maid.isCasting()) {
            if (controller.getAnimationState() == AnimationState.STOPPED) {
                setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getCastingSpell().getSpell());
            }
        } else if (lastCastSpellType.getCastType() == CastType.LONG) {
            setFinishAnimationFromSpell(controller, lastCastSpellType);
        }

        return PlayState.CONTINUE;
    }

    @Unique
    private PlayState otherCastingPredicate(AnimationEvent<AnimatableEntity<EntityMaid>> event) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();
        if (accessor.winefoxsSpellbooks$getCancelCastAnimation()) {
            return PlayState.STOP;
        }

        var controller = event.getController();

        IMagicEntity maid = (IMagicEntity) accessor;

        if (maid.isCasting() && accessor.winefoxsSpellbooks$getCastingSpell() != null &&
            controller.getAnimationState() == AnimationState.STOPPED) {
            if (accessor.winefoxsSpellbooks$getCastingSpell().getSpell().getCastType() == CastType.CONTINUOUS) {
                setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getCastingSpell().getSpell());
            }
            return PlayState.CONTINUE;
        }

        if (maid.isCasting()) {
            return PlayState.CONTINUE;
        } else {
            return PlayState.STOP;
        }
    }

    @Unique
    private void setStartAnimationFromSpell(AnimationController controller, AbstractSpell spell) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();
        spell.getCastStartAnimation().getForMob().ifPresentOrElse(animationBuilder -> {
            if (Log.SPELL_DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("ASCM.setStartAnimationFromSpell {}", animationBuilder);
            }
            controller.markNeedsReload();
            AnimationBuilder builder = new AnimationBuilder();
            for (RawAnimation.Stage animationStage : animationBuilder.getAnimationStages()) {
                if (animationStage.loopType() == Animation.LoopType.LOOP) {
                    builder.loop(animationStage.animationName());
                } else if (animationStage.loopType() == Animation.LoopType.PLAY_ONCE) {
                    builder.playOnce(animationStage.animationName());
                } else if (animationStage.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) {
                    builder.playAndHold(animationStage.animationName());
                }
            }
            controller.setAnimation(builder);
            lastCastSpellType = spell;
            accessor.winefoxsSpellbooks$setCancelCastAnimation(false);
            animatingLegs = spell.getCastStartAnimation().animatesLegs;
        }, () -> {
            if (Log.SPELL_DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("ASCM.setStartAnimationFromSpell cancelCastAnimation");
            }
            accessor.winefoxsSpellbooks$setCancelCastAnimation(true);
        });
    }

    @Unique
    private void setFinishAnimationFromSpell(AnimationController controller, AbstractSpell spell) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();
        if (spell.getCastFinishAnimation().isPass) {
            accessor.winefoxsSpellbooks$setCancelCastAnimation(false);
            return;
        }
        spell.getCastFinishAnimation().getForMob().ifPresentOrElse(animationBuilder -> {
            if (Log.SPELL_DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("ASCM.setFinishAnimationFromSpell {}", animationBuilder);
            }
            controller.markNeedsReload();
            AnimationBuilder builder = new AnimationBuilder();
            for (RawAnimation.Stage animationStage : animationBuilder.getAnimationStages()) {
                if (animationStage.loopType() == Animation.LoopType.LOOP) {
                    builder.loop(animationStage.animationName());
                } else if (animationStage.loopType() == Animation.LoopType.PLAY_ONCE) {
                    builder.playOnce(animationStage.animationName());
                } else if (animationStage.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) {
                    builder.playAndHold(animationStage.animationName());
                }
            }

            controller.setAnimation(builder);
            lastCastSpellType = SpellRegistry.none();
            accessor.winefoxsSpellbooks$setCancelCastAnimation(false);
        }, () -> {
            if (Log.SPELL_DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("ASCM.setFinishAnimationFromSpell cancelCastAnimation");
            }
            accessor.winefoxsSpellbooks$setCancelCastAnimation(true);
        });
    }

    @Unique
    public boolean isAnimating() {
        IMagicEntity maid = (IMagicEntity) getMaid();
        return maid.isCasting()
            || (animationControllerLongCast.getAnimationState() == AnimationState.RUNNING)
            || (animationControllerOtherCast.getAnimationState() == AnimationState.RUNNING)
            || (animationControllerInstantCast.getAnimationState() == AnimationState.RUNNING);
    }

    @Unique
    public boolean shouldBeExtraAnimated() {
        return true;
    }

    @Unique
    public boolean shouldAlwaysAnimateHead() {
        return true;
    }

    @Unique
    public boolean shouldAlwaysAnimateLegs() {
        return !animatingLegs;
    }

    @Unique
    public boolean shouldPointArmsWhileCasting() {
        return true;
    }

    @Unique
    public boolean bobBodyWhileWalking() {
        return true;
    }

    @Unique
    public boolean shouldSheathSword() {
        return false;
    }

    @Shadow
    public abstract IMaid getMaid();
}
