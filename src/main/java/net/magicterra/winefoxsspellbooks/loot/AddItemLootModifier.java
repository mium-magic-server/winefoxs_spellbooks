package net.magicterra.winefoxsspellbooks.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * 向战利品池追加一个固定物品的 GlobalLootModifier
 * <p>
 * 触发与否完全交给 {@link LootModifier#conditions}（vanilla {@code entity_properties} /
 * {@code random_chance_with_enchanted_bonus} / NeoForge {@code loot_table_id}），
 * 本类只在条件全部通过后把 ItemStack 加进去。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public class AddItemLootModifier extends LootModifier {
    public static final MapCodec<AddItemLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
        .and(inst.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item),
            IntProvider.CODEC.optionalFieldOf("count", ConstantInt.of(1)).forGetter(m -> m.count)))
        .apply(inst, AddItemLootModifier::new));

    private final Item item;
    private final IntProvider count;

    public AddItemLootModifier(LootItemCondition[] conditions, Item item, IntProvider count) {
        super(conditions);
        this.item = item;
        this.count = count;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        int n = count.sample(ctx.getRandom());
        if (n > 0) {
            loot.add(new ItemStack(item, n));
        }
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
