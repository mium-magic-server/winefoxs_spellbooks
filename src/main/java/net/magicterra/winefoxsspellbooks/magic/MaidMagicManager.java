package net.magicterra.winefoxsspellbooks.magic;

import com.github.tartaricacid.touhoulittlemaid.compat.curios.CuriosCompat;
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
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import java.util.Objects;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.compat.tlmspell.TlmSpellCompat;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class MaidMagicManager {
    public static final int MANA_REGEN_TICKS = 10;

    public static final int CONTINUOUS_CAST_TICK_INTERVAL = 10;

    /**
     * 雷击女仆的魔力恢复倍率。
     * <p>
     * 按 {@link #regenMana} 的公式，100 意味着每 {@link #MANA_REGEN_TICKS} tick 回满整条蓝条（0.5 秒一满）， 等同于无限法力；而 TLM 的 {@code isStruckByLightning} 是<b>持久化的永久标志</b>（存 NBT，一次雷劈终身有效）， 所以这是个一次性触发、不可逆的巨大增益。默认关，由 {@link Config#isLightningManaRegenEnabled()} 控制。
     */
    private static final float LIGHTNING_MANA_REGEN_MULTIPLIER = 100.0f;

    /**
     * 恢复魔力值 (通用方法)
     *
     * @param entity    实体
     * @param magicData 魔法数据
     * @return 是否有恢复
     */
    public static boolean regenMana(LivingEntity entity, MagicData magicData) {
        // 魔力紊乱效果：禁用魔力恢复
        if (entity.hasEffect(WsbEffects.MANA_DISRUPTION)) {
            return false;
        }
        int maxMana = (int) getMaxMana(entity);
        var mana = magicData.getMana();
        if (mana < maxMana) {
            var increment = getManaRegenPerInterval(entity);
            setMana(entity, Mth.clamp(magicData.getMana() + increment, 0, maxMana));
            return true;
        } else {
            return false;
        }
    }

    /**
     * 每个 {@link #MANA_REGEN_TICKS} 周期能恢复的魔力量，公式与 {@link #regenMana} 保持一致（含魔力紊乱短路）。
     * <p>
     * 供 {@link SpellCastPlanner} 推演「再等几个周期才能凑够某个法术的魔力」用，不产生任何副作用。
     *
     * @param entity 实体
     * @return 每周期恢复量，被禁用回魔时为 0
     */
    public static float getManaRegenPerInterval(LivingEntity entity) {
        if (entity.hasEffect(WsbEffects.MANA_DISRUPTION)) {
            return 0f;
        }
        int maxMana = (int) getMaxMana(entity);
        float manaRegenMultiplier;
        if (Config.isLightningManaRegenEnabled()
            && entity instanceof EntityMaid maid && maid.isStruckByLightning()) {
            manaRegenMultiplier = LIGHTNING_MANA_REGEN_MULTIPLIER; // 雷击加速恢复
        } else {
            manaRegenMultiplier = (float) entity.getAttributeValue(AttributeRegistry.MANA_REGEN);
        }
        return maxMana * manaRegenMultiplier * .01f * ServerConfigs.MANA_REGEN_MULTIPLIER.get().floatValue();
    }

    /**
     * 读实体当前魔力：女仆走适配器，其余走 Iron's 的 {@link MagicData}。
     * <p>
     * 女仆的真实魔力存在 {@link net.magicterra.winefoxsspellbooks.entity.adapter.MagicMaidAdapter} 里， {@link MagicData#getPlayerMagicData} 对女仆返回的是无人读写的孤儿附件，从那里读恒为 0。
     *
     * @param entity 实体
     * @return 当前魔力值
     */
    public static float getMana(LivingEntity entity) {
        if (entity instanceof MaidMagicEntity maidMagicEntity) {
            return maidMagicEntity.winefoxsSpellbooks$getMana();
        }
        if (entity instanceof IMagicEntity magicEntity) {
            return magicEntity.getMagicData().getMana();
        }
        return MagicData.getPlayerMagicData(entity).getMana();
    }

    /**
     * 写实体魔力，路由同 {@link #getMana}。
     * <p>
     * 女仆必须走 {@code winefoxsSpellbooks$setMana}：它在写 {@link MagicData} 之外还同步 {@code MAID_MANA} 附件，只写前者的话魔力条与可用性判定都不会变。
     *
     * @param entity 实体
     * @param mana   目标魔力值
     */
    public static void setMana(LivingEntity entity, float mana) {
        if (entity instanceof MaidMagicEntity maidMagicEntity) {
            maidMagicEntity.winefoxsSpellbooks$setMana(mana);
        } else if (entity instanceof IMagicEntity magicEntity) {
            magicEntity.getMagicData().setMana(mana);
        } else {
            MagicData magicData = MagicData.getPlayerMagicData(entity);
            magicData.setMana(mana);
            // MagicData 是纯服务端状态，客户端的魔力条与法术可用性判定读的是 ClientMagicData。
            // Iron's 只在自然回魔（且未满）时每 10 tick 补一次同步包，靠它兜不住这里的外部写入：
            // 一旦把玩家补满，客户端会永远停在写入前的旧值。
            if (entity instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(magicData));
            }
        }
    }

    /**
     * 获取最大魔力值 (通用方法)
     * <p>
     * {@code maxManaMultiplier} 的语义是「女仆魔力上限相对玩家的倍率」，只对女仆生效。 套到玩家身上会连锁改写 Iron's 的真实上限：回魔封顶、魔力条读数、法术可用性判定全跟着偏， 倍率大于 1 时甚至能把玩家的魔力写到真实上限之上（{@link MagicData} 不会自行截断）。
     *
     * @param entity 实体
     * @return 最大魔力值
     */
    public static double getMaxMana(LivingEntity entity) {
        double maxMana = entity.getAttributeValue(AttributeRegistry.MAX_MANA);
        return entity instanceof MaidMagicEntity ? Config.getMaxManaMultiplier() * maxMana : maxMana;
    }

    public static void addCooldown(LivingEntity entity, AbstractSpell spell, CastSource castSource) {
        if (castSource == CastSource.SCROLL)
            return;
        if (TlmSpellCompat.usesTlmSpellCooldown(entity)) {
            // 装了万法皆通：玩家女仆的冷却记在其女仆数据里，受其冷却倍率与饰品（时痕之戒、梦云水晶、春花-返）影响
            TlmSpellCompat.addTlmSpellCooldown((EntityMaid) entity, spell);
            return;
        }
        if (!(entity instanceof IMagicEntity magicEntity)) {
            return;
        }
        int effectiveCooldown = getEffectiveSpellCooldown(spell, entity, castSource);
        MagicData magicData = magicEntity.getMagicData();
        magicData.getPlayerCooldowns().addCooldown(spell, effectiveCooldown);
    }

    /**
     * 法术是否在冷却中。装了万法皆通时玩家女仆查万法皆通的冷却，否则查 Iron's 的冷却表。选术与施法前的冷却判断都经由这里
     *
     * @param entity 施法者，须为 {@link IMagicEntity}
     * @param spell  法术
     * @return 是否冷却中
     */
    public static boolean isOnCooldown(LivingEntity entity, AbstractSpell spell) {
        if (TlmSpellCompat.usesTlmSpellCooldown(entity)) {
            return TlmSpellCompat.isOnTlmSpellCooldown((EntityMaid) entity, spell);
        }
        return entity instanceof IMagicEntity magicEntity && magicEntity.getMagicData().getPlayerCooldowns().isOnCooldown(spell);
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

    /**
     * 女仆的施法装备视图：盔甲栏 + 主手 + TLM 饰品栏 + Curios 饰品栏。
     * <p>
     * 找法术书和算饰品加成必须看同一批槽位，否则会出现「书读得到、戒指读不到」的偏差：姻缘戒指这类饰品的原生槽位在 Curios 那边，漏掉它等于加成静默失效。
     *
     * @param maid 女仆
     * @return 合并后的只读视图
     */
    public static IItemHandler getSpellGearView(EntityMaid maid) {
        IItemHandlerModifiable curiosItems = (IItemHandlerModifiable) EmptyItemHandler.INSTANCE;
        if (CuriosCompat.isLoadedOrEnable()) {
            curiosItems = CuriosApi.getCuriosInventory(maid).map(ICuriosItemHandler::getEquippedCurios).orElse(curiosItems);
        }
        return new CombinedInvWrapper(maid.getArmorInvWrapper(),
            new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, maid.getMainHandItem())),
            maid.getMaidBauble(), curiosItems);
    }

    /**
     * 计入饰品亲和加成后的实际施法等级。
     *
     * @param gearView {@link #getSpellGearView} 的结果。视图由调用方建一次传进来： 一次重算要跑一整本法术书，每个法术各建一遍的话，Curios 那边每次都会现场新建一个合并 wrapper
     */
    public static int getLevelFor(EntityMaid maid, IItemHandler gearView, AbstractSpell spell, int level) {
        int addition = 0;
        for (int i = 0; i < gearView.getSlots(); i++) {
            ItemStack stackInSlot = gearView.getStackInSlot(i);
            AffinityData affinityData = AffinityData.getAffinityData(stackInSlot);
            if (affinityData == AffinityData.NONE) {
                continue;
            }
            addition += affinityData.getBonusFor(spell);
        }
        var levelEvent = new ModifySpellLevelEvent(spell, maid, level, level + addition);
        NeoForge.EVENT_BUS.post(levelEvent);
        return levelEvent.getLevel();
    }

    /**
     * 法术是否就绪：二段咏唱状态允许、且不在冷却中——唯独不看魔力够不够。
     * <p>
     * 供 {@link SpellCastPlanner} 判断「这法术攒蓝等得到吗」：还在冷却的法术， 魔力攒够了也没用，不该进规划的候选池；只是魔力不够的法术则相反，值得等。
     *
     * @param mob       施法者
     * @param spellData 待检查的法术
     * @return 法术是否就绪（不考虑魔力）
     */
    public static boolean isSpellReady(Mob mob, SpellData spellData) {
        if (!(mob instanceof IMagicEntity)) {
            return false;
        }
        if (!(mob instanceof MaidMagicEntity)) {
            return false;
        }
        boolean hasRecastForSpell = hasRecast(mob, spellData);
        if (hasRecastForSpell && !MaidSpellCatalog.maidMayRepeatCast(spellData.getSpell())) {
            // 二段咏唱，例如:
            // 炽焰追踪弹幕、火墙术 需要二段咏唱才能施放
            // 而召唤术二段咏唱会收回召唤物
            return false;
        }
        return !isOnCooldown(mob, spellData.getSpell());
    }

    public static boolean isSpellUsable(Mob mob, SpellData spellData) {
        return isSpellReady(mob, spellData) && hasManaFor(mob, spellData);
    }

    /**
     * 当前魔力是否够完整放出这个法术（二段咏唱中途不扣蓝，恒为够）。调用方需先确认 {@link #isSpellReady} 为真。 持续施法按整段引导计价，见 {@link #getEffectiveManaCost}。
     *
     * @param mob       施法者
     * @param spellData 待检查的法术
     * @return 魔力是否足够
     */
    public static boolean hasManaFor(Mob mob, SpellData spellData) {
        return ((IMagicEntity) mob).getMagicData().getMana() >= getEffectiveManaCost(mob, spellData);
    }

    /**
     * 完整施放一次实际要扣的魔力：二段咏唱中途不二次扣魔（见 {@code MagicMaidAdapter#castSpell}），为 0。 持续施法每个引导间隔扣一次，按整段引导的总价算，超过魔力上限时按上限算。
     * <p>
     * 只按单跳计价时，余额刚够一两跳的持续法术也会被当作可立即施放：起手后余额不足两跳即提前收尾并进入冷却， 既打不出伤害，也不会交给规划器攒蓝。
     *
     * @param mob       施法者，须为 {@link MaidMagicEntity}
     * @param spellData 法术
     * @return 实际蓝耗
     */
    public static float getEffectiveManaCost(Mob mob, SpellData spellData) {
        if (hasRecast(mob, spellData)) {
            return 0;
        }
        AbstractSpell spell = spellData.getSpell();
        int perCast = ((MaidMagicEntity) mob).winefoxsSpellbooks$getManaCost(spell, spellData.getLevel());
        if (spell.getCastType() != CastType.CONTINUOUS) {
            return perCast;
        }
        int castTicks = Math.max(0, spell.getEffectiveCastTime(spellData.getLevel(), mob));
        return fullChannelManaCost(perCast, castTicks, (float) getMaxMana(mob));
    }

    /**
     * 持续施法整段引导的总蓝耗：每 {@link #CONTINUOUS_CAST_TICK_INTERVAL} 刻扣一次单跳蓝耗， 超过魔力上限时按上限算（攒满蓝就是能引导的最长一段）。
     *
     * @param perCast   单跳蓝耗
     * @param castTicks 引导总时长
     * @param maxMana   魔力上限
     * @return 整段引导的总蓝耗
     */
    public static float fullChannelManaCost(float perCast, int castTicks, float maxMana) {
        int intervals = Math.max(1, Mth.ceil((float) castTicks / CONTINUOUS_CAST_TICK_INTERVAL));
        return Math.min(Math.max(1f, maxMana), perCast * intervals);
    }

    /**
     * 法术是否正处于二段咏唱中途。
     */
    public static boolean hasRecast(Mob mob, SpellData spellData) {
        return mob instanceof IMagicEntity magicEntity
            && magicEntity.getMagicData().getPlayerRecasts().hasRecastForSpell(spellData.getSpell());
    }

    /**
     * 两个回魔周期内即可恢复、且二段咏唱允许的法术。选法和实际 onCast 会相差一 tick， 只看一个周期会漏掉“上轮施法后还剩 11 tick”的常见情况。
     */
    public static int getSoonReadyTicks(Mob mob, SpellData spellData) {
        if (!(mob instanceof IMagicEntity magicEntity)
            || hasRecast(mob, spellData) && !MaidSpellCatalog.maidMayRepeatCast(spellData.getSpell())) {
            return 0;
        }
        int ticks;
        if (TlmSpellCompat.usesTlmSpellCooldown(mob)) {
            ticks = TlmSpellCompat.getTlmSpellCooldownRemaining((EntityMaid) mob, spellData.getSpell());
        } else {
            var cooldown = magicEntity.getMagicData().getPlayerCooldowns().getSpellCooldowns()
                .get(spellData.getSpell().getSpellId());
            ticks = cooldown != null ? cooldown.getCooldownRemaining() : 0;
        }
        return ticks > 0 && ticks <= MANA_REGEN_TICKS * 2 ? ticks : 0;
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
