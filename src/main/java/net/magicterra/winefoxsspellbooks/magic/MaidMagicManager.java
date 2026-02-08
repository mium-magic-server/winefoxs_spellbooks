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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
        return regenMana(maid, magicData);
    }

    /**
     * 恢复魔力值 (通用方法)
     *
     * @param entity    实体
     * @param magicData 魔法数据
     * @return 是否有恢复
     */
    public static boolean regenMana(LivingEntity entity, MagicData magicData) {
        int maxMana = (int) getMaxMana(entity);
        var mana = magicData.getMana();
        if (mana < maxMana) {
            float manaRegenMultiplier;
            if (entity instanceof EntityMaid maid && maid.isStruckByLightning()) {
                manaRegenMultiplier = 100; // 雷击加速恢复
            } else {
                manaRegenMultiplier = (float) entity.getAttributeValue(AttributeRegistry.MANA_REGEN);
            }
            var increment = maxMana * manaRegenMultiplier * .01f * ServerConfigs.MANA_REGEN_MULTIPLIER.get().floatValue();
            float newMana = Mth.clamp(magicData.getMana() + increment, 0, maxMana);
            if (entity instanceof MaidMagicEntity maidMagicEntity) {
                maidMagicEntity.winefoxsSpellbooks$setMana(newMana);
            } else {
                magicData.setMana(newMana);
            }

            return true;
        } else {
            return false;
        }
    }

    public static double getMaxMana(EntityMaid maid) {
        return getMaxMana((LivingEntity) maid);
    }

    /**
     * 获取最大魔力值 (通用方法)
     *
     * @param entity 实体
     * @return 最大魔力值
     */
    public static double getMaxMana(LivingEntity entity) {
        double maxMana = entity.getAttributeValue(AttributeRegistry.MAX_MANA);
        return Config.getMaxManaMultiplier() * maxMana;
    }

    public static void addCooldown(LivingEntity entity, AbstractSpell spell, CastSource castSource) {
        if (castSource == CastSource.SCROLL)
            return;
        if (!(entity instanceof IMagicEntity magicEntity)) {
            return;
        }
        int effectiveCooldown = getEffectiveSpellCooldown(spell, entity, castSource);
        MagicData magicData = magicEntity.getMagicData();
        magicData.getPlayerCooldowns().addCooldown(spell, effectiveCooldown);
    }

    public static void clearCooldowns(LivingEntity entity) {
        if (!(entity instanceof IMagicEntity magicEntity)) {
            return;
        }
        MagicData magicData = magicEntity.getMagicData();
        magicData.getPlayerCooldowns().clearCooldowns();
    }

    public static int getEffectiveSpellCooldown(AbstractSpell spell, LivingEntity entity, CastSource castSource) {
        double playerCooldownModifier = entity.getAttributeValue(AttributeRegistry.COOLDOWN_REDUCTION);

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

    public static boolean isSpellUsable(Mob mob, SpellData spellData) {
        if (!(mob instanceof IMagicEntity spellCastingMob)) {
            return false;
        }
        if (!(mob instanceof MaidMagicEntity magicMaid)) {
            return false;
        }
        PlayerRecasts playerRecasts = spellCastingMob.getMagicData().getPlayerRecasts();
        float mana = spellCastingMob.getMagicData().getMana();
        int manaCost = magicMaid.winefoxsSpellbooks$getManaCost(spellData.getSpell(), spellData.getLevel());
        boolean hasRecastForSpell = playerRecasts.hasRecastForSpell(spellData.getSpell());
        if (hasRecastForSpell) {
            // 二段咏唱，例如:
            // 炽焰追踪弹幕、火墙术 需要二段咏唱才能施放
            // 而召唤术二段咏唱会收回召唤物
            if (!MaidSpellRegistry.maidShouldRecast(spellData.getSpell())) {
                // 不能二段咏唱
                return false;
            }
        }
        if (!hasRecastForSpell && mana < manaCost) {
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
