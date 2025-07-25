package net.magicterra.winefoxsspellbooks.mixin;

import com.elfmcys.yesstevemodel.O0ooOoOoooooO000OOO0oOoo;
import com.elfmcys.yesstevemodel.OOoOOo0oo0OO00oOo0OOO0O0;
import com.elfmcys.yesstevemodel.Ooo0oOOo0Ooo0OO00o0O0oOo;
import com.elfmcys.yesstevemodel.OooOOoo00oOoOOoOOOoO0ooO;
import com.elfmcys.yesstevemodel.o000o0ooOOOOoooOoo0o0oOo;
import com.elfmcys.yesstevemodel.o00OOOOoo0OOo000oo0oo0oo;
import com.elfmcys.yesstevemodel.o00Ooooo0o0o0oOOoO00o0Oo;
import com.elfmcys.yesstevemodel.o0o000o0OoOO0OoO00O0oOoo;
import com.elfmcys.yesstevemodel.o0oOOOOooO00O00OooOO0OO0;
import com.elfmcys.yesstevemodel.ooOO0OO00000O0oo0oo0oOoO;
import com.elfmcys.yesstevemodel.oooo00oOO0o0oO0OoOoO0o0o;
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
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
 * YSM 支持
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-23 00:52
 */
@Mixin(ooOO0OO00000O0oo0oo0oOoO.class)
public abstract class YsmMaidEntityMixin extends Ooo0oOOo0Ooo0OO00o0O0oOo<EntityMaid> implements IGeoEntity {
    @Unique
    private AbstractSpell lastCastSpellType = SpellRegistry.none();

    @Unique
    private final oooo00oOO0o0oO0OoOoO0o0o<?> animationControllerOtherCast = new oooo00oOO0o0oO0OoOoO0o0o<>(this, "other_casting", 0f, this::otherCastingPredicate);

    @Unique
    private final oooo00oOO0o0oO0OoOoO0o0o<?> animationControllerInstantCast = new oooo00oOO0o0oO0OoOoO0o0o<>(this, "instant_casting", 0f, this::instantCastingPredicate);

    @Unique
    private final oooo00oOO0o0oO0OoOoO0o0o<?> animationControllerLongCast = new oooo00oOO0o0oO0OoOoO0o0o<>(this, "long_casting", 0f, this::longCastingPredicate);


    public YsmMaidEntityMixin(EntityMaid entity, boolean var2) {
        super(entity, var2);
    }

    @Inject(method = "Ooo000oOo0000oo00o00o000()V", at = @At("TAIL"))
    public void afterRegisterControllers(CallbackInfo ci) {
        // registerController
        oOO0OOoOOOO0OoooOO0oO000(animationControllerOtherCast);
        oOO0OOoOOOO0OoooOO0oO000(animationControllerInstantCast);
        oOO0OOoOOOO0OoooOO0oO000(animationControllerLongCast);
    }

    @Unique
    private o00OOOOoo0OOo000oo0oo0oo instantCastingPredicate(O0ooOoOoooooO000OOO0oOoo<Ooo0oOOo0Ooo0OO00o0O0oOo<EntityMaid>> event, o0o000o0OoOO0OoO00O0oOoo<?> var2) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();

        if (accessor.winefoxsSpellbooks$getCancelCastAnimation()) {
            // STOP
            return o00OOOOoo0OOo000oo0oo0oo.Ooo000oOo0000oo00o00o000;
        }

        // getController()
        o00Ooooo0o0o0oOOoO00o0Oo controller = getRealAnimationController(event.oOooOo0Oooo0OOO00000o000());
        // OOoOOo0oo0OO00oOo0OOO0O0.ooo00O0o000OOoOO00o00ooo: STOPPED
        if (accessor.winefoxsSpellbooks$getInstantCastSpellType() != SpellRegistry.none() && controller.Ooo000oOo0000oo00o00o000() == OOoOOo0oo0OO00oOo0OOO0O0.ooo00O0o000OOoOO00o00ooo) {
            setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getInstantCastSpellType());
            accessor.winefoxsSpellbooks$setInstantCastSpellType(SpellRegistry.none());
        }
        // CONTINUE
        return o00OOOOoo0OOo000oo0oo0oo.oOO0OOoOOOO0OoooOO0oO000;
    }

    @Unique
    private o00OOOOoo0OOo000oo0oo0oo longCastingPredicate(O0ooOoOoooooO000OOO0oOoo<Ooo0oOOo0Ooo0OO00o0O0oOo<EntityMaid>> event, o0o000o0OoOO0OoO00O0oOoo<?> var2) {
        IMagicEntity maid = (IMagicEntity) getMaid();
        MaidMagicEntity accessor = (MaidMagicEntity) maid;

        // getController()
        o00Ooooo0o0o0oOOoO00o0Oo controller = getRealAnimationController(event.oOooOo0Oooo0OOO00000o000());
        if (accessor.winefoxsSpellbooks$getCancelCastAnimation() || (controller.Ooo000oOo0000oo00o00o000() == OOoOOo0oo0OO00oOo0OOO0O0.ooo00O0o000OOoOO00o00ooo &&
            !(maid.isCasting() && accessor.winefoxsSpellbooks$getCastingSpell() != null &&
                accessor.winefoxsSpellbooks$getCastingSpell().getSpell().getCastType() == CastType.LONG))) {
            // STOP
            return o00OOOOoo0OOo000oo0oo0oo.Ooo000oOo0000oo00o00o000;
        }

        if (maid.isCasting()) {
            if (controller.Ooo000oOo0000oo00o00o000() == OOoOOo0oo0OO00oOo0OOO0O0.ooo00O0o000OOoOO00o00ooo) {
                setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getCastingSpell().getSpell());
            }
        } else if (lastCastSpellType.getCastType() == CastType.LONG) {
            setFinishAnimationFromSpell(controller, lastCastSpellType);
        }

        // CONTINUE
        return o00OOOOoo0OOo000oo0oo0oo.oOO0OOoOOOO0OoooOO0oO000;
    }

    @Unique
    private o00OOOOoo0OOo000oo0oo0oo otherCastingPredicate(O0ooOoOoooooO000OOO0oOoo<Ooo0oOOo0Ooo0OO00o0O0oOo<EntityMaid>> event, o0o000o0OoOO0OoO00O0oOoo<?> var2) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();
        if (accessor.winefoxsSpellbooks$getCancelCastAnimation()) {
            // STOP
            return o00OOOOoo0OOo000oo0oo0oo.Ooo000oOo0000oo00o00o000;
        }

        IMagicEntity maid = (IMagicEntity) accessor;

        // getController()
        o00Ooooo0o0o0oOOoO00o0Oo controller = getRealAnimationController(event.oOooOo0Oooo0OOO00000o000());
        if (maid.isCasting() && accessor.winefoxsSpellbooks$getCastingSpell() != null &&
            controller.Ooo000oOo0000oo00o00o000() == OOoOOo0oo0OO00oOo0OOO0O0.ooo00O0o000OOoOO00o00ooo) {
            if (accessor.winefoxsSpellbooks$getCastingSpell().getSpell().getCastType() == CastType.CONTINUOUS) {
                setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getCastingSpell().getSpell());
            }
            // CONTINUE
            return o00OOOOoo0OOo000oo0oo0oo.oOO0OOoOOOO0OoooOO0oO000;
        }

        if (maid.isCasting()) {
            // CONTINUE
            return o00OOOOoo0OOo000oo0oo0oo.oOO0OOoOOOO0OoooOO0oO000;
        } else {
            // STOP
            return o00OOOOoo0OOo000oo0oo0oo.Ooo000oOo0000oo00o00o000;
        }
    }

    @Unique
    private void setStartAnimationFromSpell(o00Ooooo0o0o0oOOoO00o0Oo controller, AbstractSpell spell) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();
        spell.getCastStartAnimation().getForMob().ifPresentOrElse(animationBuilder -> {
            if (Log.SPELL_DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("ASCM.setStartAnimationFromSpell {}", animationBuilder);
            }
            controller.o0OO000o0oo0o0oOoOoo0O0o();
            o0oOOOOooO00O00OooOO0OO0 builder = new o0oOOOOooO00O00OooOO0OO0();
            for (RawAnimation.Stage animationStage : animationBuilder.getAnimationStages()) {
                if (animationStage.loopType() == Animation.LoopType.LOOP) {
                    builder.ooo00O0o000OOoOO00o00ooo(animationStage.animationName());
                } else if (animationStage.loopType() == Animation.LoopType.PLAY_ONCE) {
                    builder.Ooo000oOo0000oo00o00o000(animationStage.animationName());
                } else if (animationStage.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) {
                    builder.OOo00OoO0000OOO00ooOOOO0(animationStage.animationName());
                }
            }
            controller.oOO0OOoOOOO0OoooOO0oO000(builder);
            lastCastSpellType = spell;
            accessor.winefoxsSpellbooks$setCancelCastAnimation(false);
        }, () -> {
            if (Log.SPELL_DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("ASCM.setStartAnimationFromSpell cancelCastAnimation");
            }
            accessor.winefoxsSpellbooks$setCancelCastAnimation(true);
        });
    }

    @Unique
    private void setFinishAnimationFromSpell(o00Ooooo0o0o0oOOoO00o0Oo controller, AbstractSpell spell) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();
        if (spell.getCastFinishAnimation().isPass) {
            accessor.winefoxsSpellbooks$setCancelCastAnimation(false);
            return;
        }
        spell.getCastFinishAnimation().getForMob().ifPresentOrElse(animationBuilder -> {
            if (Log.SPELL_DEBUG) {
                WinefoxsSpellbooks.LOGGER.debug("ASCM.setFinishAnimationFromSpell {}", animationBuilder);
            }
            controller.o0OO000o0oo0o0oOoOoo0O0o();
            o0oOOOOooO00O00OooOO0OO0 builder = new o0oOOOOooO00O00OooOO0OO0();
            for (RawAnimation.Stage animationStage : animationBuilder.getAnimationStages()) {
                if (animationStage.loopType() == Animation.LoopType.LOOP) {
                    builder.ooo00O0o000OOoOO00o00ooo(animationStage.animationName());
                } else if (animationStage.loopType() == Animation.LoopType.PLAY_ONCE) {
                    builder.Ooo000oOo0000oo00o00o000(animationStage.animationName());
                } else if (animationStage.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) {
                    builder.OOo00OoO0000OOO00ooOOOO0(animationStage.animationName());
                }
            }

            controller.oOO0OOoOOOO0OoooOO0oO000(builder);
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
            || (getAnimationState(animationControllerLongCast) == OOoOOo0oo0OO00oOo0OOO0O0.oOO0OOoOOOO0OoooOO0oO000)
            || (getAnimationState(animationControllerOtherCast) == OOoOOo0oo0OO00oOo0OOO0O0.oOO0OOoOOOO0OoooOO0oO000)
            || (getAnimationState(animationControllerInstantCast) == OOoOOo0oo0OO00oOo0OOO0O0.oOO0OOoOOOO0OoooOO0oO000);
    }

    @Unique
    private static o00Ooooo0o0o0oOOoO00o0Oo getRealAnimationController(OooOOoo00oOoOOoOOOoO0ooO<?> controller) {
        o000o0ooOOOOoooOoo0o0oOo<?> wrappedAnimateController;
        if (controller instanceof oooo00oOO0o0oO0OoOoO0o0o) {
            YsmAcStateAccessor1 accessor = (YsmAcStateAccessor1) controller;
            wrappedAnimateController = accessor.getWrappedAnimateController();
        } else if (controller instanceof o000o0ooOOOOoooOoo0o0oOo) {
            wrappedAnimateController = (o000o0ooOOOOoooOoo0o0oOo<?>) controller;
        } else {
            throw new IllegalStateException("Unknown controller");
        }
        YsmACStateAccessor2 accessor2 = (YsmACStateAccessor2) wrappedAnimateController;
        return accessor2.getRealController();
    }

    @Unique
    private static OOoOOo0oo0OO00oOo0OOO0O0 getAnimationState(OooOOoo00oOoOOoOOOoO0ooO<?> controller) {
        return getRealAnimationController(controller).Ooo000oOo0000oo00o00o000();
    }

    @Shadow
    public abstract IMaid getMaid();
}
