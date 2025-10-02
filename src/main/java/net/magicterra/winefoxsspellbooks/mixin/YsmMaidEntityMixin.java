package net.magicterra.winefoxsspellbooks.mixin;


import com.elfmcys.yesstevemodel.O0000oo0ooO0ooo000o00ooo;
import com.elfmcys.yesstevemodel.O000oO0oOOoO0oOooO00oOOo;
import com.elfmcys.yesstevemodel.OOoOO0OOoo00O0oo0oO0Oooo;
import com.elfmcys.yesstevemodel.o0O0oO0o0oOo0O0O0O0OoO00;
import com.elfmcys.yesstevemodel.o0OOOooo000O0OO0OOOOoOOO;
import com.elfmcys.yesstevemodel.o0OoOOO00o0OooOoo0O00OoO;
import com.elfmcys.yesstevemodel.o0oOoOoOoOO0Ooo000OO0ooo;
import com.elfmcys.yesstevemodel.oOO0O000000o0O0o0oO00oO0;
import com.elfmcys.yesstevemodel.oOo0OooO0oOo0oOOOoo00o0O;
import com.elfmcys.yesstevemodel.oo0o0ooOOo0OO0O0OO000OOO;
import com.elfmcys.yesstevemodel.oooOo0o0O0oooo00O0OOooOO;
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
@Mixin(o0O0oO0o0oOo0O0O0O0OoO00.class)
public abstract class YsmMaidEntityMixin extends oooOo0o0O0oooo00O0OOooOO<EntityMaid> implements IGeoEntity {
    @Unique
    private AbstractSpell lastCastSpellType = SpellRegistry.none();

    @Unique
    private final oOo0OooO0oOo0oOOOoo00o0O<oooOo0o0O0oooo00O0OOooOO<EntityMaid>> animationControllerOtherCast = new oOo0OooO0oOo0oOOOoo00o0O<>(this, "other_casting", 0f, this::otherCastingPredicate);

    @Unique
    private final oOo0OooO0oOo0oOOOoo00o0O<oooOo0o0O0oooo00O0OOooOO<EntityMaid>> animationControllerInstantCast = new oOo0OooO0oOo0oOOOoo00o0O<>(this, "instant_casting", 0f, this::instantCastingPredicate);

    @Unique
    private final oOo0OooO0oOo0oOOOoo00o0O<oooOo0o0O0oooo00O0OOooOO<EntityMaid>> animationControllerLongCast = new oOo0OooO0oOo0oOOOoo00o0O<>(this, "long_casting", 0f, this::longCastingPredicate);


    public YsmMaidEntityMixin(EntityMaid entity, boolean var2) {
        super(entity, var2);
    }

    @Inject(method = "o0ooOOOooo0o0oOooOO00O00()V", at = @At("TAIL"))
    public void afterRegisterControllers(CallbackInfo ci) {
        // registerController
        OOOoO0oo0000oO0ooOOoO000(animationControllerOtherCast);
        OOOoO0oo0000oO0ooOOoO000(animationControllerInstantCast);
        OOOoO0oo0000oO0ooOOoO000(animationControllerLongCast);
    }

    @Unique
    private oo0o0ooOOo0OO0O0OO000OOO instantCastingPredicate(o0OOOooo000O0OO0OOOOoOOO<oooOo0o0O0oooo00O0OOooOO<EntityMaid>> event, oOO0O000000o0O0o0oO00oO0<?> var2) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();

        if (accessor.winefoxsSpellbooks$getCancelCastAnimation()) {
            // STOP
            return oo0o0ooOOo0OO0O0OO000OOO.o0ooOOOooo0o0oOooOO00O00;
        }

        // getController()
        var controller = getRealAnimationController(event.OoOoo0OO000o00OOO000000O());
        // O0000oo0ooO0ooo000o00ooo.o0OOO0oOooOO0O0O0O0OOo0o: STOPPED
        if (accessor.winefoxsSpellbooks$getInstantCastSpellType() != SpellRegistry.none() && controller.O0OOooOOOo0O0o0oOO00OOoo() == O0000oo0ooO0ooo000o00ooo.OOOoO0oo0000oO0ooOOoO000) {
            setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getInstantCastSpellType());
            accessor.winefoxsSpellbooks$setInstantCastSpellType(SpellRegistry.none());
        }
        // CONTINUE
        return oo0o0ooOOo0OO0O0OO000OOO.OOOoO0oo0000oO0ooOOoO000;
    }

    @Unique
    private oo0o0ooOOo0OO0O0OO000OOO longCastingPredicate(o0OOOooo000O0OO0OOOOoOOO<oooOo0o0O0oooo00O0OOooOO<EntityMaid>> event, oOO0O000000o0O0o0oO00oO0<?> var2) {
        IMagicEntity maid = (IMagicEntity) getMaid();
        MaidMagicEntity accessor = (MaidMagicEntity) maid;

        // getController()
        var controller = getRealAnimationController(event.OoOoo0OO000o00OOO000000O());
        if (accessor.winefoxsSpellbooks$getCancelCastAnimation() || (controller.O0OOooOOOo0O0o0oOO00OOoo() == O0000oo0ooO0ooo000o00ooo.OOOoO0oo0000oO0ooOOoO000 &&
            !(maid.isCasting() && accessor.winefoxsSpellbooks$getCastingSpell() != null &&
                accessor.winefoxsSpellbooks$getCastingSpell().getSpell().getCastType() == CastType.LONG))) {
            // STOP
            return oo0o0ooOOo0OO0O0OO000OOO.o0ooOOOooo0o0oOooOO00O00;
        }

        if (maid.isCasting()) {
            if (controller.O0OOooOOOo0O0o0oOO00OOoo() == O0000oo0ooO0ooo000o00ooo.OOOoO0oo0000oO0ooOOoO000) {
                setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getCastingSpell().getSpell());
            }
        } else if (lastCastSpellType.getCastType() == CastType.LONG) {
            setStartFinishAnimationFromSpell(controller, lastCastSpellType, true);
        }

        // CONTINUE
        return oo0o0ooOOo0OO0O0OO000OOO.OOOoO0oo0000oO0ooOOoO000;
    }

    @Unique
    private oo0o0ooOOo0OO0O0OO000OOO otherCastingPredicate(o0OOOooo000O0OO0OOOOoOOO<oooOo0o0O0oooo00O0OOooOO<EntityMaid>> event, oOO0O000000o0O0o0oO00oO0<?> var2) {
        MaidMagicEntity accessor = (MaidMagicEntity) getMaid();
        if (accessor.winefoxsSpellbooks$getCancelCastAnimation()) {
            // STOP
            return oo0o0ooOOo0OO0O0OO000OOO.o0ooOOOooo0o0oOooOO00O00;
        }

        IMagicEntity maid = (IMagicEntity) accessor;

        // getController()
        var controller = getRealAnimationController(event.OoOoo0OO000o00OOO000000O());
        if (maid.isCasting() && accessor.winefoxsSpellbooks$getCastingSpell() != null &&
            controller.O0OOooOOOo0O0o0oOO00OOoo() == O0000oo0ooO0ooo000o00ooo.OOOoO0oo0000oO0ooOOoO000) {
            if (accessor.winefoxsSpellbooks$getCastingSpell().getSpell().getCastType() == CastType.CONTINUOUS) {
                setStartAnimationFromSpell(controller, accessor.winefoxsSpellbooks$getCastingSpell().getSpell());
            }
            // CONTINUE
            return oo0o0ooOOo0OO0O0OO000OOO.OOOoO0oo0000oO0ooOOoO000;
        }

        if (maid.isCasting()) {
            // CONTINUE
            return oo0o0ooOOo0OO0O0OO000OOO.OOOoO0oo0000oO0ooOOoO000;
        } else {
            // STOP
            return oo0o0ooOOo0OO0O0OO000OOO.o0ooOOOooo0o0oOooOO00O00;
        }
    }

    @Unique
    private void setStartAnimationFromSpell(o0OoOOO00o0OooOoo0O00OoO controller, AbstractSpell spell) {
        setStartFinishAnimationFromSpell(controller, spell, false);
    }

    @Unique
    private void setStartFinishAnimationFromSpell(o0OoOOO00o0OooOoo0O00OoO controller, AbstractSpell spell, boolean finish) {
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
            controller.OoOoo0OO000o00OOO000000O();
            for (RawAnimation.Stage animationStage : animationBuilder.getAnimationStages()) {
                if (animationStage.loopType() == Animation.LoopType.LOOP) {
                    controller.OOOoO0oo0000oO0ooOOoO000(animationStage.animationName(), O000oO0oOOoO0oOooO00oOOo.OOOoO0oo0000oO0ooOOoO000);
                } else if (animationStage.loopType() == Animation.LoopType.PLAY_ONCE) {
                    controller.OOOoO0oo0000oO0ooOOoO000(animationStage.animationName(), O000oO0oOOoO0oOooO00oOOo.o0ooOOOooo0o0oOooOO00O00);
                } else if (animationStage.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) {
                    controller.OOOoO0oo0000oO0ooOOoO000(animationStage.animationName(), O000oO0oOOoO0oOooO00oOOo.O0OOooOOOo0O0o0oOO00OOoo);
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
            || (getAnimationState(animationControllerLongCast) == O0000oo0ooO0ooo000o00ooo.O0OOooOOOo0O0o0oOO00OOoo)
            || (getAnimationState(animationControllerOtherCast) == O0000oo0ooO0ooo000o00ooo.O0OOooOOOo0O0o0oOO00OOoo)
            || (getAnimationState(animationControllerInstantCast) == O0000oo0ooO0ooo000o00ooo.O0OOooOOOo0O0o0oOO00OOoo);
    }

    @Unique
    private static o0OoOOO00o0OooOoo0O00OoO getRealAnimationController(OOoOO0OOoo00O0oo0oO0Oooo<?> controller) {
        o0oOoOoOoOO0Ooo000OO0ooo<?> wrappedAnimateController;
        if (controller instanceof oOo0OooO0oOo0oOOOoo00o0O) {
            YsmAcStateAccessor1 accessor = (YsmAcStateAccessor1) controller;
            wrappedAnimateController = accessor.getWrappedAnimateController();
        } else if (controller instanceof o0oOoOoOoOO0Ooo000OO0ooo) {
            wrappedAnimateController = (o0oOoOoOoOO0Ooo000OO0ooo<?>) controller;
        } else {
            throw new IllegalStateException("Unknown controller");
        }
        YsmACStateAccessor2 accessor2 = (YsmACStateAccessor2) wrappedAnimateController;
        return accessor2.getRealController();
    }

    @Unique
    private static O0000oo0ooO0ooo000o00ooo getAnimationState(oOo0OooO0oOo0oOOOoo00o0O<?> controller) {
        return getRealAnimationController(controller).O0OOooOOOo0O0o0oOO00OOoo();
    }

    @Shadow
    public abstract IMaid getMaid();
}
