package net.magicterra.winefoxsspellbooks.mixin;


import com.elfmcys.yesstevemodel.O00o0O0oo000oO00o00oOo0o;
import com.elfmcys.yesstevemodel.O0O0OO0oOoO0o00OO0oOo0OO;
import com.elfmcys.yesstevemodel.O0OoOOO0o00oooO0ooOO0oOO;
import com.elfmcys.yesstevemodel.O0OooooooOO0oOOo00O0oo0o;
import com.elfmcys.yesstevemodel.OOOO00OoOoooO00oo0oOo00O;
import com.elfmcys.yesstevemodel.OOOO0oo0O0o0Oo00O0o0oOOO;
import com.elfmcys.yesstevemodel.Oo0oOOOOo00OoOO00ooooO0o;
import com.elfmcys.yesstevemodel.OoO0000OO0Oo0OooO0O00OO0;
import com.elfmcys.yesstevemodel.o00oOo000oOoO000oOOoOOOo;
import com.elfmcys.yesstevemodel.o0o00o0OO0o0ooo0oO0O0oOo;
import com.elfmcys.yesstevemodel.oOO00OOoo000O0o0OoOooooO;
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntity;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
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
 * YSM 支持
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-23 00:52
 */
@Mixin(OOOO00OoOoooO00oo0oOo00O.class)
public abstract class YsmMaidEntityMixin extends O00o0O0oo000oO00o00oOo0o<EntityMaid> implements IGeoEntity {
    @Unique
    private AbstractSpell lastCastSpellType = SpellRegistry.none();

    @Unique
    private final OoO0000OO0Oo0OooO0O00OO0<O00o0O0oo000oO00o00oOo0o<EntityMaid>> animationControllerOtherCast = new OoO0000OO0Oo0OooO0O00OO0<>(this, "other_casting", 0f, this::otherCastingPredicate);

    @Unique
    private final OoO0000OO0Oo0OooO0O00OO0<O00o0O0oo000oO00o00oOo0o<EntityMaid>> animationControllerInstantCast = new OoO0000OO0Oo0OooO0O00OO0<>(this, "instant_casting", 0f, this::instantCastingPredicate);

    @Unique
    private final OoO0000OO0Oo0OooO0O00OO0<O00o0O0oo000oO00o00oOo0o<EntityMaid>> animationControllerLongCast = new OoO0000OO0Oo0OooO0O00OO0<>(this, "long_casting", 0f, this::longCastingPredicate);


    public YsmMaidEntityMixin(EntityMaid entity, boolean var2) {
        super(entity, var2);
    }

    @Inject(method = "OOOoOo0OooOo0ooO0OO0ooO0()V", at = @At("TAIL"))
    public void afterRegisterControllers(CallbackInfo ci) {
        // registerController
        OO0O0o000O0o0OOO000oOOO0(animationControllerOtherCast);
        OO0O0o000O0o0OOO000oOOO0(animationControllerInstantCast);
        OO0O0o000O0o0OOO000oOOO0(animationControllerLongCast);
    }

    @Unique
    private O0OooooooOO0oOOo00O0oo0o instantCastingPredicate(O0OoOOO0o00oooO0ooOO0oOO<O00o0O0oo000oO00o00oOo0o<EntityMaid>> event, o00oOo000oOoO000oOOoOOOo<?> var2) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();

        if (accessor.winefoxsSpellbooks$getCancelCastAnimation()) {
            // STOP
            return O0OooooooOO0oOOo00O0oo0o.OOOoOo0OooOo0ooO0OO0ooO0;
        }

        // getController()
        var controller = getRealAnimationController(event.OOo0OOo00OOOo00OO0oo00o0());
        // o0o00o0OO0o0ooo0oO0O0oOo.o0OOO0oOooOO0O0O0O0OOo0o: STOPPED
        if (accessor.winefoxsSpellbooks$getInstantCastSpellType() != SpellRegistry.none() && controller.O00OOO0O000oO00O0oOo0O0o() == o0o00o0OO0o0ooo0oO0O0oOo.OO0O0o000O0o0OOO000oOOO0) {
            setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getInstantCastSpellType());
            accessor.winefoxsSpellbooks$setInstantCastSpellType(SpellRegistry.none());
        }
        // CONTINUE
        return O0OooooooOO0oOOo00O0oo0o.OO0O0o000O0o0OOO000oOOO0;
    }

    @Unique
    private O0OooooooOO0oOOo00O0oo0o longCastingPredicate(O0OoOOO0o00oooO0ooOO0oOO<O00o0O0oo000oO00o00oOo0o<EntityMaid>> event, o00oOo000oOoO000oOOoOOOo<?> var2) {
        IMagicEntity maid = (IMagicEntity) getMaid();
        MaidMagicEntity accessor = (MaidMagicEntity) maid;

        // getController()
        var controller = getRealAnimationController(event.OOo0OOo00OOOo00OO0oo00o0());
        if (accessor.winefoxsSpellbooks$getCancelCastAnimation() || (controller.O00OOO0O000oO00O0oOo0O0o() == o0o00o0OO0o0ooo0oO0O0oOo.OO0O0o000O0o0OOO000oOOO0 &&
            !(maid.isCasting() && accessor.winefoxsSpellbooks$getCastingSpell() != null &&
                accessor.winefoxsSpellbooks$getCastingSpell().getSpell().getCastType() == CastType.LONG))) {
            // STOP
            return O0OooooooOO0oOOo00O0oo0o.OOOoOo0OooOo0ooO0OO0ooO0;
        }

        if (maid.isCasting()) {
            if (controller.O00OOO0O000oO00O0oOo0O0o() == o0o00o0OO0o0ooo0oO0O0oOo.OO0O0o000O0o0OOO000oOOO0) {
                setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getCastingSpell().getSpell());
            }
        } else if (lastCastSpellType.getCastType() == CastType.LONG) {
            setStartFinishAnimationFromSpell(controller, lastCastSpellType, true);
        }

        // CONTINUE
        return O0OooooooOO0oOOo00O0oo0o.OO0O0o000O0o0OOO000oOOO0;
    }

    @Unique
    private O0OooooooOO0oOOo00O0oo0o otherCastingPredicate(O0OoOOO0o00oooO0ooOO0oOO<O00o0O0oo000oO00o00oOo0o<EntityMaid>> event, o00oOo000oOoO000oOOoOOOo<?> var2) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();
        if (accessor.winefoxsSpellbooks$getCancelCastAnimation()) {
            // STOP
            return O0OooooooOO0oOOo00O0oo0o.OOOoOo0OooOo0ooO0OO0ooO0;
        }

        IMagicEntity maid = (IMagicEntity) accessor;

        // getController()
        var controller = getRealAnimationController(event.OOo0OOo00OOOo00OO0oo00o0());
        if (maid.isCasting() && accessor.winefoxsSpellbooks$getCastingSpell() != null &&
            controller.O00OOO0O000oO00O0oOo0O0o() == o0o00o0OO0o0ooo0oO0O0oOo.OO0O0o000O0o0OOO000oOOO0) {
            if (accessor.winefoxsSpellbooks$getCastingSpell().getSpell().getCastType() == CastType.CONTINUOUS) {
                setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getCastingSpell().getSpell());
            }
            // CONTINUE
            return O0OooooooOO0oOOo00O0oo0o.OO0O0o000O0o0OOO000oOOO0;
        }

        if (maid.isCasting()) {
            // CONTINUE
            return O0OooooooOO0oOOo00O0oo0o.OO0O0o000O0o0OOO000oOOO0;
        } else {
            // STOP
            return O0OooooooOO0oOOo00O0oo0o.OOOoOo0OooOo0ooO0OO0ooO0;
        }
    }

    @Unique
    private void setStartAnimationFromSpell(OOOO0oo0O0o0Oo00O0o0oOOO controller, AbstractSpell spell) {
        setStartFinishAnimationFromSpell(controller, spell, false);
    }

    @Unique
    private void setStartFinishAnimationFromSpell(OOOO0oo0O0o0Oo00O0o0oOOO controller, AbstractSpell spell, boolean finish) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();
        AnimationHolder animationHolder;
        if (finish) {
            animationHolder = spell.getCastFinishAnimation();
            if (animationHolder.isPass) {
                accessor.winefoxsSpellbooks$setCancelCastAnimation(false);
                return;
            }
        } else {
            animationHolder = spell.getCastStartAnimation();
        }
        animationHolder.getForMob().ifPresentOrElse(animationBuilder -> {
            controller.OOo0OOo00OOOo00OO0oo00o0();
            for (RawAnimation.Stage animationStage : animationBuilder.getAnimationStages()) {
                if (animationStage.loopType() == Animation.LoopType.LOOP) {
                    controller.OO0O0o000O0o0OOO000oOOO0(animationStage.animationName(), O0O0OO0oOoO0o00OO0oOo0OO.OO0O0o000O0o0OOO000oOOO0);
                } else if (animationStage.loopType() == Animation.LoopType.PLAY_ONCE) {
                    controller.OO0O0o000O0o0OOO000oOOO0(animationStage.animationName(), O0O0OO0oOoO0o00OO0oOo0OO.OOOoOo0OooOo0ooO0OO0ooO0);
                } else if (animationStage.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) {
                    controller.OO0O0o000O0o0OOO000oOOO0(animationStage.animationName(), O0O0OO0oOoO0o00OO0oOo0OO.O00OOO0O000oO00O0oOo0O0o);
                }
            }
            if (finish) {
                lastCastSpellType = SpellRegistry.none();
            } else {
                lastCastSpellType = spell;
            }
            accessor.winefoxsSpellbooks$setCancelCastAnimation(false);
        }, () -> accessor.winefoxsSpellbooks$setCancelCastAnimation(true));
    }

    @Unique
    public boolean isAnimating() {
        IMagicEntity maid = (IMagicEntity) getMaid();
        return maid.isCasting()
            || (getAnimationState(animationControllerLongCast) == o0o00o0OO0o0ooo0oO0O0oOo.O00OOO0O000oO00O0oOo0O0o)
            || (getAnimationState(animationControllerOtherCast) == o0o00o0OO0o0ooo0oO0O0oOo.O00OOO0O000oO00O0oOo0O0o)
            || (getAnimationState(animationControllerInstantCast) == o0o00o0OO0o0ooo0oO0O0oOo.O00OOO0O000oO00O0oOo0O0o);
    }

    @Unique
    private static OOOO0oo0O0o0Oo00O0o0oOOO getRealAnimationController(oOO00OOoo000O0o0OoOooooO<?> controller) {
        Oo0oOOOOo00OoOO00ooooO0o<?> wrappedAnimateController;
        if (controller instanceof OoO0000OO0Oo0OooO0O00OO0) {
            YsmAcStateAccessor1 accessor = (YsmAcStateAccessor1) controller;
            wrappedAnimateController = accessor.getWrappedAnimateController();
        } else if (controller instanceof Oo0oOOOOo00OoOO00ooooO0o) {
            wrappedAnimateController = (Oo0oOOOOo00OoOO00ooooO0o<?>) controller;
        } else {
            throw new IllegalStateException("Unknown controller");
        }
        YsmACStateAccessor2 accessor2 = (YsmACStateAccessor2) wrappedAnimateController;
        return accessor2.getRealController();
    }

    @Unique
    private static o0o00o0OO0o0ooo0oO0O0oOo getAnimationState(OoO0000OO0Oo0OooO0O00OO0<?> controller) {
        return getRealAnimationController(controller).O00OOO0O000oO00O0oOo0O0o();
    }

    @Shadow
    public abstract IMaid getMaid();
}
