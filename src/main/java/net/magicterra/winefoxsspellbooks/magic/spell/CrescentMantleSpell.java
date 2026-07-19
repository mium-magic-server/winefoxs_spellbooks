package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import java.util.List;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 残月披风:给自己和附近自家女仆同时套上吸收护盾与短暂隐身,用于战斗中紧急脱险。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-05
 */
public class CrescentMantleSpell extends AbstractSpell {

    /** 女仆搜索半径(格);数据生成的女仆 AI 施法距离引用同一常量。 */
    public static final double MAID_RADIUS = 6.0;
    /** 吸收持续 4s。 */
    private static final int ABSORPTION_TICKS = 80;
    /** 隐身持续 3s。 */
    private static final int INVISIBILITY_TICKS = 60;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "crescent_mantle");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.UNCOMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
        .setMaxLevel(8)
        .setCooldownSeconds(30.0)
        .build();

    public CrescentMantleSpell() {
        this.baseManaCost = 40;
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    @Override
    public int getMinLevel() {
        return 3;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
            Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(ABSORPTION_TICKS, 1)),
            Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(MAID_RADIUS, 1)));
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        // 吸收等级随法术等级缓慢成长:Lv3-5 → II,Lv6-8 → III
        int absorptionAmp = spellLevel >= 6 ? 2 : 1;
        applyMantle(entity, absorptionAmp);
        // 附近自家女仆一并披上
        level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(MAID_RADIUS),
                e -> e != entity && WinefoxHexHelper.isMaidOwnedBy(e, entity))
            .forEach(maid -> applyMantle(maid, absorptionAmp));
        WinefoxHexHelper.spawnCastBurst(serverLevel, entity);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private static void applyMantle(LivingEntity target, int absorptionAmp) {
        target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_TICKS, absorptionAmp));
        target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBILITY_TICKS, 0));
    }
}
