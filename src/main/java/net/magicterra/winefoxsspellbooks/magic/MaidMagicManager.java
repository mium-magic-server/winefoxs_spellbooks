package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class MaidMagicManager {
    public static final int MANA_REGEN_TICKS = 10;
    public static final int CONTINUOUS_CAST_TICK_INTERVAL = 10;

    public static boolean regenMaidMana(EntityMaid maid, MagicData magicData) {
        int maidMaxMana = (int) maid.getAttributeValue(AttributeRegistry.MAX_MANA);
        var mana = magicData.getMana();
        if (mana < maidMaxMana) {
            float manaRegenMultiplier;
            if (maid.isStruckByLightning()) {
                manaRegenMultiplier = 100;
            } else {
                manaRegenMultiplier = (float) maid.getAttributeValue(AttributeRegistry.MANA_REGEN);
            }
            var increment = maidMaxMana * manaRegenMultiplier * .01f * ServerConfigs.MANA_REGEN_MULTIPLIER.get().floatValue();
            magicData.setMana(Mth.clamp(magicData.getMana() + increment, 0, maidMaxMana));
            return true;
        } else {
            return false;
        }
    }

    public static void addCooldown(EntityMaid maid, AbstractSpell spell, CastSource castSource) {
        if (castSource == CastSource.SCROLL)
            return;
        int effectiveCooldown = getEffectiveSpellCooldown(spell, maid, castSource);

        IMagicEntity magicEntity = (IMagicEntity) maid;
        MagicData magicData = magicEntity.getMagicData();
        magicData.getPlayerCooldowns().addCooldown(spell, effectiveCooldown);
    }

    public static void clearCooldowns(EntityMaid maid) {
        IMagicEntity magicEntity = (IMagicEntity) maid;
        MagicData magicData = magicEntity.getMagicData();
        magicData.getPlayerCooldowns().clearCooldowns();
    }

    public static int getEffectiveSpellCooldown(AbstractSpell spell, EntityMaid maid, CastSource castSource) {
        double playerCooldownModifier = maid.getAttributeValue(AttributeRegistry.COOLDOWN_REDUCTION);

        float itemCoolDownModifer = 1;
        if (castSource == CastSource.SWORD) {
            itemCoolDownModifer = ServerConfigs.SWORDS_CD_MULTIPLIER.get().floatValue();
        }
        return (int) (spell.getSpellCooldown() * (2 - Utils.softCapFormula(playerCooldownModifier)) * itemCoolDownModifer);
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, particle, force, x, y, z, count, deltaX, deltaY, deltaZ, speed));
    }
}
