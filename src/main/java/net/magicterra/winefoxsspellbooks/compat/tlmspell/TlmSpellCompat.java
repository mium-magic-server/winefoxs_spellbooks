package net.magicterra.winefoxsspellbooks.compat.tlmspell;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import java.util.List;
import java.util.Set;
import net.magicterra.winefoxsspellbooks.Config;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.entity.ai.behavior.common.EngageBand;
import net.magicterra.winefoxsspellbooks.entity.ai.memory.MaidCastingMemoryModuleTypes;
import net.magicterra.winefoxsspellbooks.task.MaidCastingTask;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.loading.LoadingModList;

/**
 * 车万女仆：万法皆通（{@code touhou_little_maid_spell}）联动的统一入口
 * <p>
 * 装了万法皆通时，女仆的铁魔法改走其通用逻辑：法术攻击任务与法术书饰品绑定都不注册，法术书由万法皆通识别（手持、背包、Curios），
 * 索敌与走位用其战斗任务；本模组通过替换其铁魔法 provider、并向其战斗任务塞入选术与施法行为接管选术和施法。
 * 玩家女仆的铁魔法冷却一律记在万法皆通的女仆数据里，以便其冷却倍率与饰品生效；是否消耗法力见 {@link Config#isTlmSpellMana()}。
 * <p>
 * 本类不引用万法皆通的任何类，未安装时也可放心调用；真正碰到万法皆通的逻辑都在 {@link TlmSpellBridge} 里，只在已安装时才会被加载。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class TlmSpellCompat {
    public static final String MOD_ID = "touhou_little_maid_spell";
    /**
     * 万法皆通战斗任务 UID 的命名空间（与 mod id 不同）
     */
    private static final String TASK_NAMESPACE = "maidspell";

    private static final boolean LOADED;

    static {
        LoadingModList list = LoadingModList.get();
        LOADED = list != null && list.getModFileById(MOD_ID) != null;
    }

    private TlmSpellCompat() {
    }

    /**
     * 是否安装了万法皆通
     */
    public static boolean isLoaded() {
        return LOADED;
    }

    /**
     * 该实体的铁魔法冷却是否记在万法皆通的女仆数据里：已安装，且是玩家的女仆（召唤女仆不受影响）
     */
    public static boolean usesTlmSpellCooldown(LivingEntity entity) {
        return LOADED && entity instanceof EntityMaid;
    }

    /**
     * 该实体施放铁魔法是否免法力：已安装、配置关闭了法力，且是玩家的女仆
     */
    public static boolean isManaFree(LivingEntity entity) {
        return usesTlmSpellCooldown(entity) && !Config.isTlmSpellMana();
    }

    /**
     * 女仆当前是否在用法力：执行本模组的施法任务，或在启用法力时执行万法皆通的战斗任务。浮动法力条、末影腰包 HUD、Jade 都按此显示
     */
    public static boolean usesMana(EntityMaid maid) {
        if (isManaFree(maid)) {
            return false;
        }
        IMaidTask task = maid.getTask();
        return task instanceof MaidCastingTask || LOADED && isTlmSpellCombatTask(task);
    }

    /**
     * 是否为万法皆通的近战 / 远程法术战斗任务。按 UID 判断，不加载其任务类
     */
    public static boolean isTlmSpellCombatTask(IMaidTask task) {
        return task != null && TASK_NAMESPACE.equals(task.getUid().getNamespace());
    }

    /**
     * 万法皆通识别到的、该女仆可用的铁魔法法术（已剔除其黑名单）
     */
    public static List<SpellSlot> collectSpells(EntityMaid maid) {
        return LOADED ? TlmSpellBridge.collectSpells(maid) : List.of();
    }

    /**
     * 法术是否在万法皆通的冷却中
     */
    public static boolean isOnTlmSpellCooldown(EntityMaid maid, AbstractSpell spell) {
        return LOADED && TlmSpellBridge.isOnCooldown(maid, spell);
    }

    /**
     * 按万法皆通的规则给法术挂冷却
     */
    public static void addTlmSpellCooldown(EntityMaid maid, AbstractSpell spell) {
        if (LOADED) {
            TlmSpellBridge.addCooldown(maid, spell);
        }
    }

    /**
     * 万法皆通冷却的剩余 tick，不在冷却中为 0
     */
    public static int getTlmSpellCooldownRemaining(EntityMaid maid, AbstractSpell spell) {
        return LOADED ? TlmSpellBridge.getCooldownRemaining(maid, spell) : 0;
    }

    /**
     * 铁魔法每次 {@code onCast} 后通知万法皆通（春花-返计层等），与其铁魔法 provider 的调用时机一致
     */
    public static void onSpellCast(EntityMaid maid, AbstractSpell spell, LivingEntity target) {
        if (LOADED) {
            TlmSpellBridge.onSpellCast(maid, spell, target);
        }
    }

    /**
     * 按比例缩短万法皆通记录的全部法术冷却
     */
    public static void refundCooldowns(EntityMaid maid, double ratio) {
        if (LOADED) {
            TlmSpellBridge.refundCooldowns(maid, ratio);
        }
    }

    /**
     * 女仆佩戴的法术白名单（{@code blue_note}）记录的法术 id，未安装万法皆通或未佩戴时为空集。
     * <p>
     * 白名单在万法皆通里的含义是「允许对主人施放」。本模组本就按法术数据把治疗、增益队友的法术用在主人身上，
     * 所以名单只对其余类别的法术起作用：把它们额外放行为对主人施放的增益法术，用于纠正附属法术分类不准的情况。
     */
    public static Set<String> getOwnerCastableSpellIds(EntityMaid maid) {
        return LOADED ? TlmSpellBridge.getOwnerCastableSpellIds(maid) : Set.of();
    }

    /**
     * 万法皆通近战法术任务的距离带，取自其走位：[{@code meleeRange - 1}, {@code meleeRange + 0.5}]
     */
    public static EngageBand meleeBand() {
        return LOADED ? TlmSpellBridge.meleeBand() : EngageBand.NONE;
    }

    /**
     * 万法皆通远程法术任务的距离带，取自其走位：[{@code farRange}, {@code farRange + 5}]，上限不超过其施法距离
     */
    public static EngageBand farBand() {
        return LOADED ? TlmSpellBridge.farBand() : EngageBand.NONE;
    }

    /**
     * 万法皆通里除铁魔法以外、占用施法的 provider 是否正在施法（拔刀剑与普攻并行，不计入）
     */
    public static boolean isOtherProviderCasting(EntityMaid maid) {
        return LOADED && TlmSpellBridge.isOtherProviderCasting(maid);
    }

    /**
     * 铁魔法是否占着施法优先权：正在施法、已选中待施放的法术，或刚放完、仍在优先期内。期间万法皆通的其他 provider 让出施法
     */
    public static boolean hasIronsSpellPriority(EntityMaid maid) {
        if (maid instanceof IMagicEntity magicEntity && magicEntity.isCasting()
            || maid.getBrain().hasMemoryValue(MaidCastingMemoryModuleTypes.CURRENT_SPELL.get())) {
            return true;
        }
        return maid.level().getGameTime() < ((MaidMagicEntity) maid).winefoxsSpellbooks$getMagicMaidAdapter().getTlmSpellPriorityUntil();
    }

    /**
     * 坐下或骑乘的女仆能否施法：同万法皆通，须佩戴法术射表；未安装时不限制
     */
    public static boolean canCastInRidePose(EntityMaid maid) {
        return !LOADED || TlmSpellBridge.canCastInRidePose(maid);
    }

    /**
     * 按万法皆通的敌我判定两者是否友好，友好的不作为攻击目标；未安装时恒为 false
     */
    public static boolean isFriendly(EntityMaid maid, Entity target) {
        return LOADED && TlmSpellBridge.isFriendly(maid, target);
    }
}
