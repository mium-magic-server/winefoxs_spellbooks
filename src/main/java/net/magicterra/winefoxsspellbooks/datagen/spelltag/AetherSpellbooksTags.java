package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.aetherspellbooks.registry.ASSpells;

/**
 * Aether Spellbooks（以太法术书，Aether 联动）的法术分类贡献者。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class AetherSpellbooksTags {

    private AetherSpellbooksTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // 原因：AERCLOUD_STEP 只在脚下铺一块限时云平台并给慢落，属于搭路 / 移动辅助，没有战斗价值。
        // 原因：CLOUD_SENTINELS 的 onCast 整体被 caster instanceof Player 限制，女仆施放时什么都不会生成。
        // 原因：SUMMON_MOA 召唤的是只能被玩家骑乘的坐骑，没有任何攻击目标选择，空骑 SUMMON 时长后自行消失。
        // audit:ignore AERCLOUD_STEP, CLOUD_SENTINELS, SUMMON_MOA
        // 原因：FROSTBOUND_CRYSTAL 发射的 Aether IceCrystal 结算的是固定伤害，没有任何敌我判定，会穿透命中并在场内弹跳约 25 秒，
        // 在狭小空间里会反弹回来误伤主人。
        // audit:ignore FROSTBOUND_CRYSTAL

        // ========== 攻击：弹射 / 射线 ==========
        ctx.attack().addOptional(ASSpells.RADIANT_JAVELIN.get().getSpellResource());
        ctx.attack().addOptional(ASSpells.STONEBREAKER_SHARD.get().getSpellResource());
        ctx.attack().addOptional(ASSpells.THUNDER_CRYSTAL.get().getSpellResource());
        ctx.attack().addOptional(ASSpells.ZEPHYR_BLAST.get().getSpellResource());

        // ========== 攻击：范围 / 持续 ==========
        // AERWHALE_SONG: 幽灵鲸鱼沿视线方向飞行并周期性鸣唱，对敌人造成伤害，对盟友只给缓降与跳跃提升
        ctx.attack().addOptional(ASSpells.AERWHALE_SONG.get().getSpellResource());
        ctx.attack().addOptional(ASSpells.AETHER_WHIRLWIND.get().getSpellResource());
        // GRAVITITE_SURGE: 以施法者为中心把范围内敌人抬起后砸下，需要敌人贴近
        ctx.attack().addOptional(ASSpells.GRAVITITE_SURGE.get().getSpellResource());
        ctx.attack().addOptional(ASSpells.ICESTONE_METEOR.get().getSpellResource());
        ctx.attack().addOptional(ASSpells.SOLAR_FLARE.get().getSpellResource());
        ctx.attack().addOptional(ASSpells.THUNDERHEAD.get().getSpellResource());

        // ========== 攻击：冲刺 ==========
        // VALKYRIE_LUNGE: 向前冲刺并伤害路径上的敌人；追加冲刺与审判只在 ServerPlayer 分支内，女仆只有单次冲刺，与 BURNING_DASH 同按攻击处理
        ctx.attack().addOptional(ASSpells.VALKYRIE_LUNGE.get().getSpellResource());

        // ========== 召唤 ==========
        // SUMMON_FIRE_MINION: 召唤物实现 IMagicSummon，目标选择只跟随召唤者的仇恨，死亡或到期时爆裂
        ctx.attack().addOptional(ASSpells.SUMMON_FIRE_MINION.get().getSpellResource());
        ctx.summon().addOptional(ASSpells.SUMMON_FIRE_MINION.get().getSpellResource());
    }
}
