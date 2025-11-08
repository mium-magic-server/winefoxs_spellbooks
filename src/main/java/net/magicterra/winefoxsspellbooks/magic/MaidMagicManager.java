package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.TextChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import java.util.Objects;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

public class MaidMagicManager {
    public static final int MANA_REGEN_TICKS = 10;
    public static final int CONTINUOUS_CAST_TICK_INTERVAL = 10;

    public static boolean regenMaidMana(EntityMaid maid, MagicData magicData) {
        int maidMaxMana = (int) getMaxMana(maid);
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

    public static double getMaxMana(EntityMaid maid) {
        double maxMana = maid.getAttributeValue(AttributeRegistry.MAX_MANA);
        return Config.getMaxManaMultiplier() * maxMana;
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

    public static int getLevelFor(EntityMaid maid, AbstractSpell spell, int level) {
        int addition = 0;
        if (maid != null) {
            IItemHandler invWrapper = new CombinedInvWrapper(maid.getArmorInvWrapper(), new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, maid.getMainHandItem())), maid.getMaidBauble());
            for (int i = 0; i < invWrapper.getSlots(); i++) {
                ItemStack stackInSlot = invWrapper.getStackInSlot(i);
                AffinityData affinityData = AffinityData.getAffinityData(stackInSlot);
                if (affinityData == AffinityData.NONE) {
                    continue;
                }
                addition += affinityData.getBonusFor(spell);
            }
        }
        var levelEvent = new ModifySpellLevelEvent(spell, maid, level, level + addition);
        NeoForge.EVENT_BUS.post(levelEvent);
        return levelEvent.getLevel();
    }

    public static boolean isSpellUsable(EntityMaid maid, SpellData spellData) {
        MaidMagicEntity magicMaid = (MaidMagicEntity) maid;
        IMagicEntity spellCastingMob = (IMagicEntity) maid;
        PlayerRecasts playerRecasts = spellCastingMob.getMagicData().getPlayerRecasts();
        float mana = spellCastingMob.getMagicData().getMana();
        int manaCost = magicMaid.winefoxsSpellbooks$getManaCost(spellData.getSpell(), spellData.getLevel());
        if (playerRecasts.hasRecastForSpell(spellData.getSpell())) {
            // 二段咏唱，例如:
            // 炽焰追踪弹幕、火墙术 需要二段咏唱才能施放
            // 而召唤术二段咏唱会收回召唤物
            if (!MaidSpellRegistry.maidShouldRecast(spellData.getSpell())) {
                // 不能二段咏唱
                return false;
            }
        }
        if (mana < manaCost) {
            // 魔力不足
            return false;
        }
        if (spellCastingMob.getMagicData().getPlayerCooldowns().isOnCooldown(spellData.getSpell())) {
            // 冷却中
            return false;
        }
        return true;
    }

    public static void showCurrentSpellInChatBubble(EntityMaid maid, SpellData spellData, boolean explainFailure) {
        if (!Config.getShowChatBubbles()) {
            return;
        }
        if (Objects.equals(spellData, SpellData.EMPTY)) {
            if (explainFailure) {
                maid.getChatBubbleManager().getChatBubbleDataCollection().chatBubbles().clear();
                maid.getChatBubbleManager().addChatBubble(TextChatBubbleData.create(60, Component.translatable("chat_bubble.winefoxs_spellbooks.casting_task.no_available_spell"),
                    IChatBubbleData.TYPE_2, IChatBubbleData.DEFAULT_PRIORITY - 100));
            }
        } else {
            AbstractSpell spell = spellData.getSpell();
            maid.getChatBubbleManager().getChatBubbleDataCollection().chatBubbles().clear();
            maid.getChatBubbleManager().addChatBubble(TextChatBubbleData.create(40,
                Component.translatable(spell.getComponentId()).append("!"),
                IChatBubbleData.TYPE_2, IChatBubbleData.DEFAULT_PRIORITY - 100));
        }
    }
}
