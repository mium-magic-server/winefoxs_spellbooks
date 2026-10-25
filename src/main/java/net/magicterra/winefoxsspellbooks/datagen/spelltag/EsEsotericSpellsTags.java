package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.inolia_zaicek.barrier_spell.registries.BSSpellRegistry;

/**
 * Esoteric Spells（秘法法术，命名空间 esoteric_spell）的法术分类贡献者。
 * 该模组的结界 / 光环由 BarrierEntity、AuraEntity 在服务端每刻结算，所有者按 LivingEntity 处理，女仆可以正常施放；
 * 依赖玩家专属事件（SpellOnCastEvent、ItemStack.hurtEnemy(Player)）或只对 Player 结算的法术在此一律排除。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class EsEsotericSpellsTags {

    private EsEsotericSpellsTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：以下法术对非玩家施法者无效、依赖玩家专属事件、需要预先状态或会让施法者自损，女仆施放只会白耗蓝。
        // CANNON_MAGIC: onCast 对非 Player 直接返回。
        // CHRONOS_SHIFT_MAGIC: checkPreCastConditions 对非 Player 恒为 false，且依赖玩家侧记录的上一次施法。
        // QADAL_MAGIC: 增益只会被 SpellOnCastEvent（仅玩家施法触发）消耗，女仆施放后不会生效。
        // MAGIC_SKIN: 减伤结算要求受击者为 Player，女仆获得效果后不会减伤。
        // MAGIC_BLADE_CONVERSION / MAGIC_BLADE_ENCHANTING / MAGIC_BLADE_ABSORB / ECHANTED_SPELL:
        //   触发点是仅对 Player 生效的 ItemStack.hurtEnemy 注入，女仆挥剑不会触发，施放后无实际收益。
        // UNION_SWORD: 必须先持有 ECHANTED_BURST 给予的魔剑强化效果，缺少时 onCast 直接返回，无前置检查。
        // MAXIMIZER_CHANGE_BARRIER: 必须先有自己放置的魔力结界（MANA_CHANGE_BARRIER），且只给玩家回蓝。
        // RAPID_CHANGE_BARRIER: 本身无效果，仅放大魔力结界的回蓝量。
        // MANA_AURA: 施法者会获得 ENERGY_AURA（受到的伤害三倍、造成的伤害减半），战斗中等同自残，且只给玩家回蓝。
        // GUANDIAN_BEAM_MAGIC: 光束仅在施法者施放其他秘法法术（玩家专属事件）或魔法阵开火时触发，单独施放没有效果。
        // FAMILIA_SYMPHONY: 复制施法的结算全部要求施法者为 Player。
        // FINALE_MANA_REGAN: 回蓝仅对 Player 结算，对女仆没有效果。
        // ARRAY_DEVICE / MISSILE_DEVICE: 装置开火前的耗蓝检查对非 Player 恒为失败，装置不会开火。
        // SUPPRESS_DEVICE / VULCAN_DEVICE: onCast 对非 Player 不执行任何结算。
        // audit:ignore ARRAY_DEVICE, CANNON_MAGIC, CHRONOS_SHIFT_MAGIC, ECHANTED_SPELL, FAMILIA_SYMPHONY
        // audit:ignore FINALE_MANA_REGAN, GUANDIAN_BEAM_MAGIC, MAGIC_BLADE_ABSORB
        // audit:ignore MAGIC_BLADE_CONVERSION, MAGIC_BLADE_ENCHANTING, MAGIC_SKIN, MANA_AURA
        // audit:ignore MAXIMIZER_CHANGE_BARRIER, MISSILE_DEVICE, QADAL_MAGIC, RAPID_CHANGE_BARRIER
        // audit:ignore SUPPRESS_DEVICE, UNION_SWORD, VULCAN_DEVICE

        // ========== 结界 ==========
        // 结界以施法者为中心，范围内的友军（含施法者与其主人）都会受益
        ctx.defense().addOptional(BSSpellRegistry.AEGIS_BARRIER.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.ASPIS_SOUL_BARRIER.get().getSpellResource());
        // BLESS_BARRIER: 周期性治疗范围内友军，施法者始终在范围内
        ctx.support().addOptional(BSSpellRegistry.BLESS_BARRIER.get().getSpellResource());
        // CAST_BARRIER: 给范围内友军持续施加 Iron's 的 HASTENED
        ctx.defense().addOptional(BSSpellRegistry.CAST_BARRIER.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.ETHER_BARRIER.get().getSpellResource());
        // FLOWING_BARRIER: 自身减伤并按承受伤害回蓝
        ctx.defense().addOptional(BSSpellRegistry.FLOWING_BARRIER.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.GLORIA_BARRIER.get().getSpellResource());
        // GUARD_BARRIER: 范围内友军受击时由结界吸收一部分伤害
        ctx.defense().addOptional(BSSpellRegistry.GUARD_BARRIER.get().getSpellResource());
        // GUARDIAN_BARRIER: 施法者获得 GUARDING，并把盯着范围内友军的怪物仇恨拉到自己身上
        ctx.defense().addOptional(BSSpellRegistry.GUARDIAN_BARRIER.get().getSpellResource());
        // MANA_CHANGE_BARRIER: 只给范围内的玩家友军（即主人）回蓝，女仆自身不受益
        ctx.positiveEffect().addOptional(BSSpellRegistry.MANA_CHANGE_BARRIER.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.PRAYER_BARRIER.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.PROTECTION_BARRIER.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.SANCTUARY_BARRIER.get().getSpellResource());
        // WALL_BARRIER: 击退并伤害盯着友军的怪物，同时让外来弹射物坠地，主用途仍是防护
        ctx.defense().addOptional(BSSpellRegistry.WALL_BARRIER.get().getSpellResource());

        // ========== 光环 ==========
        // 光环在施法者受到任何伤害时会整体消散，价值有限，但并无副作用
        ctx.defense().addOptional(BSSpellRegistry.BARRIER_AURA.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.BRAVE_AURA.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.CYCLE_AURA.get().getSpellResource());
        // FAST_REACTION_AURA: 范围内友军受击时按概率完全闪避
        ctx.defense().addOptional(BSSpellRegistry.FAST_REACTION_AURA.get().getSpellResource());
        // HEALTH_AURA: 每秒治疗半径 7 格内的友军（含施法者，治疗量 1 点起随等级递增），归入治疗友军的 supportEffect；
        // 代价：施法者获得 ENERGY_AURA，直到第一次受击前受到的伤害 ×3、造成的伤害 ×0.5，受击后光环与该效果一并消散，
        // 因此只在友军确实需要治疗时才值得放，不宜归为每个战斗模式都会自施的 support
        ctx.supportEffect().addOptional(BSSpellRegistry.HEALTH_AURA.get().getSpellResource());
        // IMMUNITY_AURA: 按概率清除范围内友军的一个非增益效果
        ctx.defense().addOptional(BSSpellRegistry.IMMUNITY_AURA.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.QUICK_MOTION_AURA.get().getSpellResource());

        // ========== 秘法攻击 ==========
        ctx.attack().addOptional(BSSpellRegistry.AUTO_ARROW_MAGIC.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.AUTO_JAVELIN_MAGIC.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.AUTO_LANCES_MAGIC.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.BLAST_MAGIC.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.CRASH_MAGIC.get().getSpellResource());
        // ECHANTED_BURST: 对视线目标直接造成伤害，并给自己附加魔剑强化
        ctx.attack().addOptional(BSSpellRegistry.ECHANTED_BURST.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.ELEMENT_SLASH.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.ESOTERIC_MISSILE.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.FINALE_MAGIC.get().getSpellResource());
        // IMPACT_MAGIC: 以施法者为中心的冲击波，会伤害并击退范围内除施法者外的所有生物
        ctx.attack().addOptional(BSSpellRegistry.IMPACT_MAGIC.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.LASER_MAGIC.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.MAGIC_BLADE_ENCHANT_SLASH.get().getSpellResource());
        // NAMESIS_MAGIC: 首次施放只进入蓄势状态，再次施放时在目标处生成伤害结界
        ctx.attack().addOptional(BSSpellRegistry.NAMESIS_MAGIC.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.STORM_MAGIC.get().getSpellResource());

        // ========== 控制 / 位移 ==========
        // ESOTERIC_SHACKLE: 在范围内的敌人身上生成锁链，将其定在原地并吸引怪物仇恨
        ctx.negativeEffect().addOptional(BSSpellRegistry.ESOTERIC_SHACKLE.get().getSpellResource());
        ctx.movement().addOptional(BSSpellRegistry.ESOTERIC_STEP.get().getSpellResource());

        // ========== 自身强化 ==========
        ctx.defense().addOptional(BSSpellRegistry.DUAL_BRINGER.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.LIMIT_BREAK.get().getSpellResource());
        // MANA_RESONANCE: 玩家专属的血魔平均化在女仆身上被跳过，只保留属性增益
        ctx.defense().addOptional(BSSpellRegistry.MANA_RESONANCE.get().getSpellResource());
        // RESURRECTION: 给范围内友军挂上死亡保护，通过通用的 LivingDeathEvent 结算
        ctx.defense().addOptional(BSSpellRegistry.RESURRECTION.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.SPELL_CALIBRATION.get().getSpellResource());

        // ========== 使魔 ==========
        // 以下除 FAMILIA_CAT_SUMMON 外的使魔法术都要求施法者已有使魔，否则施放前检查失败（不会耗蓝）
        ctx.attack().addOptional(BSSpellRegistry.FAMILIA_BLIZZARD.get().getSpellResource());
        // FAMILIA_CAT_SUMMON: 召唤黑猫使魔并给施法者增加魔力上限与法术强度
        ctx.defense().addOptional(BSSpellRegistry.FAMILIA_CAT_SUMMON.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.FAMILIA_IMPERIAL_RAY.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.FAMILIA_LIGHTNING.get().getSpellResource());
        ctx.attack().addOptional(BSSpellRegistry.FAMILIA_METEOR.get().getSpellResource());
        // FAMILIA_RESEARCH: 给自身 MAGIC_CIRCUIT，强化使魔相关法术
        ctx.defense().addOptional(BSSpellRegistry.FAMILIA_RESEARCH.get().getSpellResource());
        // FINALE_GUIDE: 使魔会自动对附近敌人施放法术
        ctx.defense().addOptional(BSSpellRegistry.FINALE_GUIDE.get().getSpellResource());
        ctx.defense().addOptional(BSSpellRegistry.FINALE_STONE_BARRIER.get().getSpellResource());

        // ========== 自动装置 ==========
        // AUTO_DEVICE: 召唤环绕施法者的自动装置，自身不攻击，需要 CHASE_DEVICE / COUNTER_DEVICE 下达指令
        ctx.defense().addOptional(BSSpellRegistry.AUTO_DEVICE.get().getSpellResource());
        // CHASE_DEVICE: 施法者以秘法伤害命中敌人时，装置追加射击；指令效果无限时长且不随装置消失而清除，不登记已造成效果
        ctx.defense().addOptional(BSSpellRegistry.CHASE_DEVICE.get().getSpellResource());
        // COUNTER_DEVICE: 友军受到生物攻击时，装置向攻击者反击；同上不登记已造成效果
        ctx.defense().addOptional(BSSpellRegistry.COUNTER_DEVICE.get().getSpellResource());

        // ========== 召唤标记 ==========
        ctx.summon().addOptional(BSSpellRegistry.AUTO_DEVICE.get().getSpellResource());
        ctx.summon().addOptional(BSSpellRegistry.FAMILIA_CAT_SUMMON.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(BSSpellRegistry.NAMESIS_MAGIC.get().getSpellResource());
    }
}
