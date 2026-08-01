package net.magicterra.winefoxsspellbooks.magic.recast;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.fire.FlamingBarrageSpell;
import io.redspace.ironsspellbooks.spells.fire.WallOfFireSpell;
import io.redspace.ironsspellbooks.spells.lightning.ThunderStepSpell;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.magicterra.winefoxsspellbooks.magic.recast.addon.AeromancyAdditionsRecasts;
import net.magicterra.winefoxsspellbooks.magic.recast.addon.CataclysmRecasts;
import net.magicterra.winefoxsspellbooks.magic.recast.addon.DiscerningTheEldritchRecasts;
import net.magicterra.winefoxsspellbooks.magic.recast.addon.EndersSpellsRequiemRecasts;
import net.magicterra.winefoxsspellbooks.magic.recast.addon.FiresEnderExpansionRecasts;
import net.magicterra.winefoxsspellbooks.magic.recast.addon.GtbcsSpellbooksRecasts;

/**
 * 法术 id → recast 收尾动作的全局注册表，外加派发逻辑。
 * <p>
 * 整张表静态、全进程一份；施法者由 {@link RecastContext} 传入，故处理器一律是静态方法引用。
 * 附属模组的处理器按模组分文件放在 {@code addon/} 下。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public final class RecastHandlers {
    private RecastHandlers() {
    }

    /**
     * 按法术 id 精确匹配的处理器。附属模组的法术走这里（不能直接 instanceof，会硬依赖）。
     */
    private static final Map<String, RecastHandler> BY_SPELL_ID = new HashMap<>();

    static {
        // 每个附属一行，法术路径与反射登记都写在各自的 contribute 里。加新附属只动它自己的文件
        // 加这里一行，互不打架。反射目标必须由 contribute 顺带登记：注册表存的是方法引用，
        // 而方法引用不触发目标类的静态初始化，写在各 handler 的 static 块里会赶不上启动自检
        contribute(FiresEnderExpansionRecasts.MOD_ID, FiresEnderExpansionRecasts::contribute);
        contribute(CataclysmRecasts.MOD_ID, CataclysmRecasts::contribute);
        contribute(EndersSpellsRequiemRecasts.MOD_ID, EndersSpellsRequiemRecasts::contribute);
        contribute(AeromancyAdditionsRecasts.MOD_ID, AeromancyAdditionsRecasts::contribute);
        contribute(DiscerningTheEldritchRecasts.MOD_ID, DiscerningTheEldritchRecasts::contribute);
        contribute(GtbcsSpellbooksRecasts.MOD_ID, GtbcsSpellbooksRecasts::contribute);

        // ========== 已确认无法兼容 ==========
        // Apprentice's Codex 的 BOUND_BOW、BOUND_SWORD、TOTEM_OF_PERMAFROST、FIELD_OVERSEER：
        // 收尾逻辑封装在各自的 XxxManager 静态方法里，形参类型就是 ServerPlayer，不是能反射构造的
        // 实体；要兼容得把对方整个 Manager 连同其玩家态一起搬过来，收益远不抵维护成本。
        // 女仆施放这几个法术时收尾只会挂冷却（defaultFinish），特效静默丢失，这是已知且接受的。
        // FIELD_OVERSEER 因为打了 summon 标签，会走 IronsCoreRecasts::summon 拿到一个近似的召唤物回收。
    }

    private static void contribute(String modId, Consumer<RecastRegistrar> addon) {
        addon.accept(new RecastRegistrar(modId, BY_SPELL_ID));
    }

    /**
     * 服务器启动时校验所有附属模组反射目标是否还在。
     * <p>
     * 入口放在这里而不是直接暴露 {@link AddonReflection#selfCheck()}：调用本方法会先触发本类的 静态初始化，登记动作才跑得到。直接调 {@code AddonReflection.selfCheck()} 有可能查一张空表。
     */
    public static void selfCheck() {
        AddonReflection.selfCheck();
    }

    /**
     * 找出某个法术该用哪个收尾动作：Iron's 本体特例 → 召唤类 → 按法术 id 匹配的附属模组 → 只挂冷却。
     */
    public static RecastHandler resolve(AbstractSpell spell) {
        if (spell instanceof FlamingBarrageSpell) {
            return IronsCoreRecasts::flamingBarrage;
        }
        if (spell instanceof ThunderStepSpell) {
            return IronsCoreRecasts::thunderStep;
        }
        if (spell instanceof WallOfFireSpell) {
            return IronsCoreRecasts::wallOfFire;
        }
        if (MaidSpellCatalog.isSummonSpell(spell)) {
            return IronsCoreRecasts::summon;
        }
        RecastHandler addon = BY_SPELL_ID.get(spell.getSpellId());
        return addon != null ? addon : IronsCoreRecasts::defaultFinish;
    }

    /**
     * 执行一次 recast 收尾。附属模组处理器抛异常时退回默认收尾，保证冷却一定挂上—— 否则该法术会变成无冷却。
     */
    public static void dispatch(RecastContext ctx) {
        RecastHandler handler = resolve(ctx.spell());
        try {
            handler.onRecastFinished(ctx);
        } catch (NoClassDefFoundError | Exception e) {
            WinefoxsSpellbooks.LOGGER.warn("Recast handler for '{}' failed, falling back to cooldown-only: {}",
                ctx.spell().getSpellId(), e.toString());
            IronsCoreRecasts.defaultFinish(ctx);
        }
    }
}
