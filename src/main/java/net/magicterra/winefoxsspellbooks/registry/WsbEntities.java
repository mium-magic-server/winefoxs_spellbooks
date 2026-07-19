package net.magicterra.winefoxsspellbooks.registry;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.spells.FoxshadeWispProjectile;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
import net.magicterra.winefoxsspellbooks.entity.spells.TipsyMistCloud;
import net.magicterra.winefoxsspellbooks.entity.spells.WineBombProjectile;
import net.magicterra.winefoxsspellbooks.entity.spells.WinePoolCloud;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 注册实体
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-11-20 01:44
 */
public class WsbEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, WinefoxsSpellbooks.MODID);

    public static DeferredHolder<EntityType<?>, EntityType<SummonedEntityMaid>> SUMMONED_MAID = ENTITY_TYPES.register("summoned_maid", () -> SummonedEntityMaid.TYPE);

    public static final DeferredHolder<EntityType<?>, EntityType<SummonedMaidBroom>> SUMMONED_MAID_BROOM = ENTITY_TYPES.register("summoned_maid_broom", () -> SummonedMaidBroom.TYPE);

    /** 酒杯狐影弹。投射物实体，无 LivingEntity 属性，故不进 addEntityAttributeEvent。 */
    public static final DeferredHolder<EntityType<?>, EntityType<FoxshadeWispProjectile>> FOXSHADE_WISP = ENTITY_TYPES.register("foxshade_wisp", () -> FoxshadeWispProjectile.TYPE);

    /** 醉狐之雾的紫粉雾团。AoeEntity 子类,无 LivingEntity 属性,不进 addEntityAttributeEvent。 */
    public static final DeferredHolder<EntityType<?>, EntityType<TipsyMistCloud>> TIPSY_MIST_CLOUD = ENTITY_TYPES.register("tipsy_mist_cloud", () -> TipsyMistCloud.TYPE);

    /** 狐酿炸弹的投掷弹体。AbstractMagicProjectile 子类,无 LivingEntity 属性,不进 addEntityAttributeEvent。 */
    public static final DeferredHolder<EntityType<?>, EntityType<WineBombProjectile>> WINE_BOMB = ENTITY_TYPES.register("wine_bomb", () -> WineBombProjectile.TYPE);

    /** 狐酿炸弹落点铺开的酒池。AoeEntity 子类,无 LivingEntity 属性,不进 addEntityAttributeEvent。 */
    public static final DeferredHolder<EntityType<?>, EntityType<WinePoolCloud>> WINE_POOL = ENTITY_TYPES.register("wine_pool", () -> WinePoolCloud.TYPE);

    /**
     * 注册实体属性
     * <p>
     * 使用 SummonedEntityMaid.createAttributes() 注册召唤女仆的属性，
     * 包含魔法属性 (MAX_MANA, MANA_REGEN, COOLDOWN_REDUCTION)
     *
     * @param event 属性创建事件
     */
    public static void addEntityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(SummonedEntityMaid.TYPE, SummonedEntityMaid.createAttributes().build());
        // SummonedMaidBroom 继承自 EntityBroom (LivingEntity)，需要注册基础属性
        event.put(SummonedMaidBroom.TYPE, LivingEntity.createLivingAttributes().build());
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(WsbEntities::addEntityAttributeEvent);
    }
}
