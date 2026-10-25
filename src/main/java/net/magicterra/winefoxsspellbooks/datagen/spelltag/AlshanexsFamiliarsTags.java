package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.alshanex.alshanex_familiars.registry.PetSpellRegistry;

/**
 * Alshanex's Familiars（alshanex_familiars）的法术分类贡献者：原本供使魔施放的七个法术。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class AlshanexsFamiliarsTags {

    private AlshanexsFamiliarsTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：FAMILIAR_SWAP 的前置检查要求准星目标是 AbstractSpellCastingPet 且其召唤者就是施法者本人，
        // 女仆没有这类使魔，检查恒为 false，无法施放。
        // audit:ignore FAMILIAR_SWAP

        // ========== 攻击 ==========
        // HIKEN: 向前连发三枚穿透火球，结算范围内用 owner.isAlliedTo(目标) 排除盟友；撞方块时还会产生真实爆炸（受法术破坏配置控制，
        // 不区分敌我），因此只有在敌人背后紧贴墙面且主人也站在爆心附近时才有误伤风险
        ctx.attack().addOptional(PetSpellRegistry.HIKEN.get().getSpellResource());
        // ICE_AGE: 持续施法，以施法者为中心逐步扩大冰冻冲击波，伤害经 isFriendlyFireBetween 排除盟友，同时冻结周围方块
        ctx.attack().addOptional(PetSpellRegistry.ICE_AGE.get().getSpellResource());
        // ICE_CHAMBER: 30 格射线命中单体造成冰冻伤害，血量过低的白名单生物会被冰封击杀
        ctx.attack().addOptional(PetSpellRegistry.ICE_CHAMBER.get().getSpellResource());
        // MEGIDO: 对准星目标释放连锁闪电，并在其周围生成伤害为 0 的球状闪电作为跳点；伤害经 applyDamage 的友军检查，不会伤到主人
        ctx.attack().addOptional(PetSpellRegistry.MEGIDO.get().getSpellResource());

        // ========== 召唤 ==========
        // SUMMON_SHADOW: 召唤影子仆从，使用 Iron's 的 SummonManager 登记主人，目标选择为通用的主人受击 / 主人攻击目标，
        // 一次施放即可，不属于需要再次施放的类型（再次施放只会收回召唤物）
        ctx.attack().addOptional(PetSpellRegistry.SUMMON_SHADOW.get().getSpellResource());
        ctx.summon().addOptional(PetSpellRegistry.SUMMON_SHADOW.get().getSpellResource());

        // ========== 自我强化 ==========
        // MAYHEM: 给施法者挂 MAYHEM 效果，其间每 1.5 秒向 10 格内非友军目标发射传送门弹体，本身不直接伤害任何人
        ctx.defense().addOptional(PetSpellRegistry.MAYHEM.get().getSpellResource());
    }
}
