package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import java.util.Optional;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.projectile.WineBombProjectile;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.magicterra.winefoxsspellbooks.registry.WsbSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 狐酿炸弹:投掷一瓶粉色酒瓶({@link WineBombProjectile}),直击打出酒狐巫法学派伤害, 落点铺开酒池——自家女仆泡在其中回魔,敌人反复中微醺。出手位置/初速仿 Iron's {@code AcidOrbSpell.onCast}(眼高出手 + 视线方向 + 微上抛)。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-06
 */
public class VintnersBombSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "vintners_bomb");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
        .setMaxLevel(8)
        .setCooldownSeconds(8.0)
        .build();

    public VintnersBombSpell() {
        this.baseManaCost = 20;
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    /**
     * 施法动画与 Iron's 苦力怕迫击炮(lob_creeper)一致:瞬发投掷。显式钉死,不随基类默认漂移。
     */
    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_INSTANT_CAST;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    /**
     * 脱手音：瓶子离手正对应施法收尾这一刻。爆炸另由 {@link WineBombProjectile#getImpactSound()} 在落点触发，两者不重叠。
     */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(WsbSounds.VINTNERS_BOMB_THROW.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        WineBombProjectile bomb = new WineBombProjectile(level, entity);
        bomb.setPos(entity.position()
            .add(0.0, entity.getEyeHeight() - bomb.getBoundingBox().getYsize() * 0.5, 0.0)
            .add(entity.getForward()));
        bomb.shoot(entity.getLookAngle());
        bomb.setDeltaMovement(bomb.getDeltaMovement().add(0.0, 0.2, 0.0));
        bomb.setDamage(getSpellPower(spellLevel, entity));
        level.addFreshEntity(bomb);

        WinefoxHexHelper.spawnCastBurst(serverLevel, entity);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
