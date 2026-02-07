package net.magicterra.winefoxsspellbooks.registry;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedMaidBroom;
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
public class InitEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, WinefoxsSpellbooks.MODID);

    public static DeferredHolder<EntityType<?>, EntityType<SummonedEntityMaid>> SUMMONED_MAID = ENTITY_TYPES.register("summoned_maid", () -> SummonedEntityMaid.TYPE);

    public static final DeferredHolder<EntityType<?>, EntityType<SummonedMaidBroom>> SUMMONED_MAID_BROOM = ENTITY_TYPES.register("summoned_maid_broom", () -> SummonedMaidBroom.TYPE);

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
        modBus.addListener(InitEntities::addEntityAttributeEvent);
    }
}
