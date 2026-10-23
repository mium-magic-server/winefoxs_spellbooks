package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import net.ender.ess_requiem.registries.GGSpellRegistry;

public final class EndersSpellsRequiemTags {

    private EndersSpellsRequiemTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        ctx.attack().addOptional(GGSpellRegistry.CLAW.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.WRETCH.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.BOILING_BLOOD.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.NECROTIC_BURST.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.DECAYING_WILL.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SPIKES_OF_AGONY.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.TENTACLE_WHIP.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.PALE_FLAME.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.TWILIGHT_ASSAULT.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.DAMNATION.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.LORD_OF_FROST.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.UNDEAD_PACT.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.STRAIN.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.REAPER.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.EBONY_ARMOR.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.PROTECTION_OF_THE_FALLEN.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.CURSED_IMMORTALITY.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.EBONY_CATAPHRACT.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.BASTION_OF_LIGHT.get().getSpellResource());
        // 黑铁重甲套装的三个内置技能：不可合成/掉落，只由 EBONY_CATAPHRACT 效果驱动，
        // 但仍按行为归类，保持与 ARM_OF_DECAY_PASSIVE 一致。
        // CATAPHRACT_HEAL 只作用于自己，归 defense（该档查 mob 自身有无该效果）；
        // support/positiveEffect 查的是队友身上的效果，自我增益放进去会让判定恒真。
        ctx.defense().addOptional(GGSpellRegistry.CATAPHRACT_HEAL.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.CATAPHRACT_SLAM.get().getSpellResource());
        // CATAPHRACT_TACKLE: 冲锋位移，伤害由施加在自己身上的同名效果在接触时结算
        ctx.attack().addOptional(GGSpellRegistry.CATAPHRACT_TACKLE.get().getSpellResource());
        ctx.negativeEffect().addOptional(GGSpellRegistry.FINALITY_OF_DECAY.get().getSpellResource());
        ctx.negativeEffect().addOptional(GGSpellRegistry.ETERNAL_BATTLEFIELD.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SKULLS.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.SKULLS.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SLASH.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SLAM.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.QUICK_SLICE.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.UPPERCUT.get().getSpellResource());
        ctx.defense().addOptional(GGSpellRegistry.PARRY.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SOULMASTER.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.SOULMASTER.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.OVERWHELMING.get().getSpellResource());
        ctx.positiveEffect().addOptional(GGSpellRegistry.HONE_EDGE.get().getSpellResource());
        ctx.positiveEffect().addOptional(GGSpellRegistry.UNDYING_DREAD.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.DISMANTLE.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.CLEAVE.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.MALEVOLENT_SLASHING.get().getSpellResource());
        ctx.movement().addOptional(GGSpellRegistry.DISAPPEARING_ACT.get().getSpellResource());
        ctx.positiveEffect().addOptional(GGSpellRegistry.ADRENALINE_RUSH.get().getSpellResource());
        // SWITCHAROO: 用 setHealth 交换双方当前生命，女仆比目标健康时等于给敌人回血、自己濒死。
        // audit:ignore SWITCHAROO
        // ctx.attack().addOptional(GGSpellRegistry.SWITCHAROO.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.GILDED_SWORD_SUMMON.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.GILDED_SWORD_SUMMON.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.SLASHING_ABILITY.get().getSpellResource());

        // ARM_OF_DECAY_PASSIVE / FIELD_OF_MOURNING / MAGGOT_BURST: 召唤类
        ctx.summon().addOptional(GGSpellRegistry.ARM_OF_DECAY_PASSIVE.get().getSpellResource());
        // 0.1.6 把腐朽之臂拆成强/弱两档，弱档同样是纯召唤（不可合成，只由武器被动触发）
        ctx.summon().addOptional(GGSpellRegistry.ARM_OF_DECAY_WEAK_PASSIVE.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.FIELD_OF_MOURNING.get().getSpellResource());
        ctx.summon().addOptional(GGSpellRegistry.MAGGOT_BURST.get().getSpellResource());
        // CORPSE_EXPLOSION / GLACIAL_SCULPTING: 引爆己方召唤物造成 AOE 伤害
        ctx.attack().addOptional(GGSpellRegistry.CORPSE_EXPLOSION.get().getSpellResource());
        ctx.attack().addOptional(GGSpellRegistry.GLACIAL_SCULPTING.get().getSpellResource());
        // NIGHT_VEIL / OVERWHELMING_LIGHT: 多次施放的攻击 + 同时给自己加增益。
        // 两者的 onCast 都只在施法者为 ServerPlayer 时生效，女仆放了只是白耗蓝。
        // audit:ignore NIGHT_VEIL, OVERWHELMING_LIGHT
        // ctx.attack().addOptional(GGSpellRegistry.NIGHT_VEIL.get().getSpellResource());
        // ctx.attack().addOptional(GGSpellRegistry.OVERWHELMING_LIGHT.get().getSpellResource());

        // 需要多次施放（recast）的法术
        ctx.maidShouldRecast().addOptional(GGSpellRegistry.CLAW.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GGSpellRegistry.BOILING_BLOOD.get().getSpellResource());
        ctx.maidShouldRecast().addOptional(GGSpellRegistry.TWILIGHT_ASSAULT.get().getSpellResource());
        // ctx.maidShouldRecast().addOptional(GGSpellRegistry.NIGHT_VEIL.get().getSpellResource());
        // ctx.maidShouldRecast().addOptional(GGSpellRegistry.OVERWHELMING_LIGHT.get().getSpellResource());
    }
}
