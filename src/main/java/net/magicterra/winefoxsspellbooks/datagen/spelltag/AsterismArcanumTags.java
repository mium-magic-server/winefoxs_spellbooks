package net.magicterra.winefoxsspellbooks.datagen.spelltag;

import com.birdie.asterismarcanum.registries.ASARSpellRegistry;

/**
 * Asterism Arcanum 附属：为其 Astral 学派法术分类。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public final class AsterismArcanumTags {

    private AsterismArcanumTags() {
    }

    public static void contribute(SpellTagContext ctx) {
        // ASTRAL_GATEWAY 的前置条件与传送均要求施法者是 ServerPlayer（传往星海维度），女仆无法施放。
        // audit:ignore ASTRAL_GATEWAY

        // ========== 攻击 ==========
        // BRIGHTBURST: 以施法者为中心的大范围爆发（半径 12 格以上），伤害并击退
        ctx.attack().addOptional(ASARSpellRegistry.BRIGHTBURST.get().getSpellResource());
        ctx.attack().addOptional(ASARSpellRegistry.LUMINOUS_BEAM.get().getSpellResource());
        // PIERCING_LIGHT: 以施法者为中心向四周放出一圈光针
        ctx.attack().addOptional(ASARSpellRegistry.PIERCING_LIGHT.get().getSpellResource());
        ctx.attack().addOptional(ASARSpellRegistry.STARCUTTER.get().getSpellResource());
        ctx.attack().addOptional(ASARSpellRegistry.STARFIRE.get().getSpellResource());
        ctx.attack().addOptional(ASARSpellRegistry.STAR_SWARM.get().getSpellResource());

        // ========== 召唤 ==========
        ctx.attack().addOptional(ASARSpellRegistry.SUMMON_LUNAR_MOTHS.get().getSpellResource());
        ctx.summon().addOptional(ASARSpellRegistry.SUMMON_LUNAR_MOTHS.get().getSpellResource());

        // ========== 自我强化 ==========
        // CELESTIAL_TETHER: 施法者骑乘一个吸收若干次攻击的星轨实体（作者自己的 Astromancer 也把它当防御法术使用）
        ctx.defense().addOptional(ASARSpellRegistry.CELESTIAL_TETHER.get().getSpellResource());

        // ========== 位移 ==========
        // ASTRAL_ECHO: 向视线方向传送，并在原地留下一个无伤害的残影
        ctx.movement().addOptional(ASARSpellRegistry.ASTRAL_ECHO.get().getSpellResource());

        // ========== 增益队友 ==========
        // SILVERY_BARBS: 对准星附近的友军（不含施法者自身）施加 1 秒幸运标记，其间无效化所有来袭伤害
        ctx.positiveEffect().addOptional(ASARSpellRegistry.SILVERY_BARBS.get().getSpellResource());
    }
}
