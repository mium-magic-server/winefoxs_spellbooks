package net.magicterra.winefoxsspellbooks.compat.tlmspell;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yimeng261.maidspell.Config;
import com.github.yimeng261.maidspell.spell.providers.IronsSpellbooksProvider;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.event.MaidEventHandler;
import net.minecraft.world.item.ItemStack;

/**
 * 替换万法皆通的铁魔法 provider：法术书照旧由其识别，选术与施法交给本模组
 * <p>
 * 由 {@code SpellBookManagerMixin} 在万法皆通初始化 provider 表后换入。施法相关的回调都改为空操作，
 * 实际的选术与施法由塞进万法皆通战斗任务的 {@code SpellChooseTask} / {@code SpellCastingTask} 完成，
 * 施法状态以本模组的 {@link IMagicEntity} 为准；法术书增减时顺带重算本模组的法术表，供法术支援任务使用。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public class WsbIronsSpellbooksProvider extends IronsSpellbooksProvider {
    public static final String IRONS_MOD_ID = "irons_spellbooks";

    private static final String REGEX_PREFIX = "regex:";

    /**
     * 按黑名单实例缓存的匹配器；万法皆通重载配置时会换一个新列表，据此失效
     */
    private static List<String> cachedBlacklist;
    private static Predicate<String> cachedMatcher = id -> false;

    /**
     * 万法皆通识别到的全部可用铁魔法法术（法术书 + Curios），剔除其铁魔法黑名单
     */
    public List<SpellSlot> collectAvailableSpells(EntityMaid maid) {
        List<SpellSlot> spells = new ArrayList<>(collectSpellFromAvailableSpellBooks(maid));
        Predicate<String> blacklisted = blacklistMatcher();
        spells.removeIf(slot -> slot == null || slot.getSpell() == null || blacklisted.test(slot.getSpell().getSpellId()));
        return spells;
    }

    /**
     * 黑名单匹配：完整 id，或 {@code regex:} 前缀的正则，与其配置说明一致（其自身 provider 只做了精确匹配）。写错的正则跳过并记警告
     */
    private static synchronized Predicate<String> blacklistMatcher() {
        List<String> blacklist = Config.ironsSpellBlacklist;
        if (blacklist == cachedBlacklist) {
            return cachedMatcher;
        }
        Set<String> ids = new HashSet<>();
        List<Pattern> patterns = new ArrayList<>();
        if (blacklist != null) {
            for (String entry : blacklist) {
                if (entry.startsWith(REGEX_PREFIX)) {
                    try {
                        patterns.add(Pattern.compile(entry.substring(REGEX_PREFIX.length())));
                    } catch (PatternSyntaxException e) {
                        WinefoxsSpellbooks.LOGGER.warn("Ignoring invalid regex {} in the Iron's spell blacklist of Touhou Little Maid: Spell", entry);
                    }
                } else {
                    ids.add(entry);
                }
            }
        }
        cachedBlacklist = blacklist;
        cachedMatcher = id -> ids.contains(id) || patterns.stream().anyMatch(pattern -> pattern.matcher(id).matches());
        return cachedMatcher;
    }

    @Override
    public void onMaidJoin(EntityMaid maid) {
        // 不绑定万法皆通自己的 SyncedSpellData：女仆的施法状态与动画由本模组的适配器同步
    }

    @Override
    public void handleItemStack(EntityMaid maid, ItemStack spellBook, boolean isAddOperation) {
        super.handleItemStack(maid, spellBook, isAddOperation);
        if (!maid.level().isClientSide && isSpellBook(spellBook)) {
            MaidEventHandler.onMaidSpellDataChanged(maid);
        }
    }

    @Override
    public void clearSpellItems(EntityMaid maid) {
        super.clearSpellItems(maid);
        if (!maid.level().isClientSide) {
            MaidEventHandler.onMaidSpellDataChanged(maid);
        }
    }

    @Override
    public void castSpell(EntityMaid maid) {
        // 由本模组的 SpellChooseTask 选术、SpellCastingTask 施放
    }

    @Override
    public void initiateCasting(EntityMaid maid) {
    }

    @Override
    public void processContinuousCasting(EntityMaid maid) {
        // 读条与引导由 MagicMaidAdapter 在女仆自身的 tick 中推进
    }

    @Override
    public boolean isCasting(EntityMaid maid) {
        return maid instanceof IMagicEntity magicEntity && magicEntity.isCasting();
    }

    @Override
    public void stopCasting(EntityMaid maid) {
        // 万法皆通判定目标失效、切换任务等场合会调这里，与原 provider 一样中止当前施法
        if (maid instanceof IMagicEntity magicEntity && magicEntity.isCasting()) {
            magicEntity.cancelCast();
        }
    }

    @Override
    public boolean shouldStopWhenTargetInvalid(EntityMaid maid) {
        return true;
    }
}
