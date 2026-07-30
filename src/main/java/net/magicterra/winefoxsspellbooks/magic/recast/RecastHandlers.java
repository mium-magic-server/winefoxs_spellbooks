package net.magicterra.winefoxsspellbooks.magic.recast;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.fire.FlamingBarrageSpell;
import io.redspace.ironsspellbooks.spells.fire.WallOfFireSpell;
import io.redspace.ironsspellbooks.spells.lightning.ThunderStepSpell;
import java.util.HashMap;
import java.util.Map;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.catalog.MaidSpellCatalog;
import net.magicterra.winefoxsspellbooks.magic.recast.addon.CataclysmRecasts;
import net.magicterra.winefoxsspellbooks.magic.recast.addon.EndersSpellsRequiemRecasts;
import net.magicterra.winefoxsspellbooks.magic.recast.addon.FiresEnderExpansionRecasts;

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
        // 反射目标必须在这里显式登记：注册表存的是方法引用，而方法引用不触发目标类的
        // 静态初始化，写在各 handler 的 static 块里会赶不上启动自检
        FiresEnderExpansionRecasts.declareReflection();
        CataclysmRecasts.declareReflection();
        EndersSpellsRequiemRecasts.declareReflection();

        // ========== Fires Ender Expansion ==========
        register(FiresEnderExpansionRecasts.MOD_ID, "scintillating_stride",
            FiresEnderExpansionRecasts::scintillatingStride);
        register(FiresEnderExpansionRecasts.MOD_ID, "binary_stars",
            FiresEnderExpansionRecasts::binaryStars);
        register(FiresEnderExpansionRecasts.MOD_ID, "hollow_crystal",
            FiresEnderExpansionRecasts::hollowCrystal);

        // ========== Cataclysm Spellbooks ==========
        register(CataclysmRecasts.MOD_ID, "piercing_bone", CataclysmRecasts::bonePierce);
        register(CataclysmRecasts.MOD_ID, "abyss_fireball", CataclysmRecasts::abyssFireball);

        // ========== Enders Spells Requiem ==========
        register(EndersSpellsRequiemRecasts.MOD_ID, "twilight_assault",
            EndersSpellsRequiemRecasts::twilightAssault);
    }

    private static void register(String modId, String spellPath, RecastHandler handler) {
        BY_SPELL_ID.put(modId + ":" + spellPath, handler);
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
