package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.magic.WinefoxHexHelper;
import net.magicterra.winefoxsspellbooks.registry.WsbEffects;
import net.magicterra.winefoxsspellbooks.registry.WsbSchools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 残月烙印法术
 * <p>
 * 单目标烙印法术：对准星前方的生物施加 {@link WsbEffects#CRESCENT_BRAND} 效果。 烙印本身是纯标记效果，由引爆系统 （{@link net.magicterra.winefoxsspellbooks.event.WinefoxDetonationHandler}）识别并触发爆炸/联动。 使用{@link RaycastBuilder 射线检测}获取单个目标，射程约 16 格。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
public class CrescentBrandSpell extends AbstractSpell {

    /**
     * 射线施法射程(格);数据生成的女仆 AI 施法距离引用同一常量。
     */
    public static final float CAST_RANGE = 16.0f;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "crescent_brand");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(WsbSchools.WINEFOX_HEX_RESOURCE)
        .setMaxLevel(8)
        .setCooldownSeconds(10.0)
        .build();

    public CrescentBrandSpell() {
        this.baseManaCost = 15;
        this.manaCostPerLevel = 5;
        // 法术强度即引爆加成：引爆伤害 = 基础值 + 挂印时的法术强度（经 amplifier 传递）
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, playerMagicData);
            return;
        }
        WinefoxHexHelper.spawnCastBurst(serverLevel, entity);
        // 玩家准星套住谁就烙谁：判据用「指向目标」而非「学派敌我」，否则猪、铁傀儡这类非敌对生物
        // 永远挂不上印，连带引爆整套连招对它们失效。自家女仆、召唤物与队友仍由阵营判定挡住。
        // 女仆施法走同一入口但没有准星，只认 AI 指派的交战对象（见 castRaycastFilter）
        var raycast = RaycastBuilder.begin(level, entity)
            .range(CAST_RANGE)
            .checkForBlocks(true)
            .bbInflation(0.35f)
            .filter(WinefoxHexHelper.castRaycastFilter(entity))
            .build();
        if (raycast.getType() == EntityHitResult.Type.ENTITY
            && ((EntityHitResult) raycast).getEntity() instanceof LivingEntity target) {
            // 时长每级 +1s，高等级烙印留出更长的引爆窗口
            target.addEffect(new MobEffectInstance(
                WsbEffects.CRESCENT_BRAND,
                WinefoxHexHelper.BRAND_DURATION + 20 * (spellLevel - 1),
                Math.max(0, Math.round(getSpellPower(spellLevel, entity)))
            ));
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
