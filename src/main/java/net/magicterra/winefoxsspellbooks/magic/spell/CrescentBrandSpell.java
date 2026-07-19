package net.magicterra.winefoxsspellbooks.magic.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
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
 * 单目标烙印法术：对准星前方的生物施加 {@link WsbEffects#CRESCENT_BRAND} 效果。
 * 烙印本身是纯标记效果，由引爆系统
 * （{@link net.magicterra.winefoxsspellbooks.event.WinefoxDetonationHandler}）识别并触发爆炸/联动。
 * 使用射线检测（{@link Utils#raycastForEntity}）获取单个目标，射程约 16 格。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-06-04
 */
public class CrescentBrandSpell extends AbstractSpell {

    /** 射线施法射程(格);数据生成的女仆 AI 施法距离引用同一常量。 */
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
        var raycast = Utils.raycastForEntity(level, entity, CAST_RANGE, true, 0.35F);
        if (raycast.getType() == EntityHitResult.Type.ENTITY
                && ((EntityHitResult) raycast).getEntity() instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(
                WsbEffects.CRESCENT_BRAND,
                WinefoxHexHelper.BRAND_DURATION,
                0
            ));
        }
        // onCast 只在服务端被调用（Iron's castSpell/女仆 AI 均为服务端），super 负责播放施法完成音效；
        // 漏调会让女仆/生物施放本法术完全无声
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
