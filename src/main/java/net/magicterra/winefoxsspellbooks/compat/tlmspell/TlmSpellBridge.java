package net.magicterra.winefoxsspellbooks.compat.tlmspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.github.yimeng261.maidspell.Config;
import com.github.yimeng261.maidspell.api.ISpellBookProvider;
import com.github.yimeng261.maidspell.compat.MaidSpellAllyResolver;
import com.github.yimeng261.maidspell.item.MaidSpellItems;
import com.github.yimeng261.maidspell.item.bauble.spellWhiteList.SpellWhiteList;
import com.github.yimeng261.maidspell.item.bauble.spellWhiteList.contianer.SpellWhiteListSpellManager;
import com.github.yimeng261.maidspell.item.bauble.springBloomReturn.SpringBloomReturnBauble;
import com.github.yimeng261.maidspell.spell.data.MaidIronsSpellData;
import com.github.yimeng261.maidspell.spell.manager.BaubleStateManager;
import com.github.yimeng261.maidspell.spell.manager.SpellBookManager;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.api.util.Utils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.EngageBand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 直接调用万法皆通 API 的部分，只经由 {@link TlmSpellCompat} 在已安装时访问
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
final class TlmSpellBridge {
    private TlmSpellBridge() {
    }

    static List<SpellSlot> collectSpells(EntityMaid maid) {
        if (SpellBookManager.getProvider(WsbIronsSpellbooksProvider.IRONS_MOD_ID) instanceof WsbIronsSpellbooksProvider provider) {
            return provider.collectAvailableSpells(maid);
        }
        return List.of();
    }

    static boolean isOnCooldown(EntityMaid maid, AbstractSpell spell) {
        MaidIronsSpellData data = MaidIronsSpellData.get(maid.getUUID());
        return data != null && data.isSpellOnCooldown(spell.getSpellId());
    }

    static int getCooldownRemaining(EntityMaid maid, AbstractSpell spell) {
        MaidIronsSpellData data = MaidIronsSpellData.get(maid.getUUID());
        return data == null ? 0 : Math.max(0, data.getSpellCooldown(spell.getSpellId()));
    }

    /**
     * 冷却时长公式同万法皆通 {@code IronsSpellbooksProvider#setCooldown}；写入时会经过其通用冷却修正与饰品冷却处理
     */
    static void addCooldown(EntityMaid maid, AbstractSpell spell) {
        double cooldownModifier = maid.getAttributeValue(AttributeRegistry.COOLDOWN_REDUCTION);
        int ticks = (int) (spell.getSpellCooldown() * (2.0 - Utils.softCapFormula(cooldownModifier)));
        MaidIronsSpellData.getOrCreate(maid).setSpellCooldown(spell.getSpellId(), ticks, maid);
    }

    static void onSpellCast(EntityMaid maid, AbstractSpell spell, LivingEntity target) {
        SpringBloomReturnBauble.onSpellCast(maid, WsbIronsSpellbooksProvider.IRONS_MOD_ID, spell.getSpellId(), target);
    }

    static void refundCooldowns(EntityMaid maid, double ratio) {
        MaidIronsSpellData data = MaidIronsSpellData.get(maid.getUUID());
        if (data != null) {
            data.refundCooldowns(ratio);
        }
    }

    /**
     * 取饰品栏里第一个法术白名单（{@code blue_note}）记录的法术 id，找法同万法皆通 provider
     */
    static Set<String> getOwnerCastableSpellIds(EntityMaid maid) {
        if (!BaubleStateManager.hasBauble(maid, MaidSpellItems.SPELL_WHITE_LIST)) {
            return Set.of();
        }
        BaubleItemHandler baubles = maid.getMaidBauble();
        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SpellWhiteList) {
                return new HashSet<>(SpellWhiteListSpellManager.getStoredSpellIds(stack, maid.level().registryAccess()));
            }
        }
        return Set.of();
    }

    /**
     * 与普攻并行、不占用施法的 provider，互斥时不拦它们
     */
    private static final String SLASHBLADE_MOD_ID = "slashblade";

    static EngageBand meleeBand() {
        return new EngageBand(Math.max(0, Config.meleeRange - 1), Math.min(Config.meleeRange + 0.5, Config.maxSpellRange));
    }

    static EngageBand farBand() {
        return new EngageBand(Math.min(Config.farRange, Config.maxSpellRange), Math.min(Config.farRange + 5, Config.maxSpellRange));
    }

    static boolean isOtherProviderCasting(EntityMaid maid) {
        for (String modId : SpellBookManager.getLoadedMods()) {
            if (!occupiesCasting(modId)) {
                continue;
            }
            ISpellBookProvider<?, ?> provider = SpellBookManager.getProvider(modId);
            if (provider != null && provider.isCasting(maid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 该 provider 的施法是否与铁魔法互斥：铁魔法自己与拔刀剑除外
     */
    static boolean occupiesCasting(String modId) {
        return !WsbIronsSpellbooksProvider.IRONS_MOD_ID.equals(modId) && !SLASHBLADE_MOD_ID.equals(modId);
    }

    static boolean canCastInRidePose(EntityMaid maid) {
        return BaubleStateManager.hasBauble(maid, MaidSpellItems.SPELL_RANGE_TABLE);
    }

    static boolean isFriendly(EntityMaid maid, Entity target) {
        return MaidSpellAllyResolver.areFriendly(maid, target);
    }
}
