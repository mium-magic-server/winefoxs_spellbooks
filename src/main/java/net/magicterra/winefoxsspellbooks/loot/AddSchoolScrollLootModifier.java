package net.magicterra.winefoxsspellbooks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * 往命中的战利品表追加一卷指定学派（{@code school} 字段，学派注册 id）的卷轴。
 * <p>
 * 法术按稀有度加权随机（权重表照抄 Iron's {@code RandomizeSpellFunction}），等级由
 * {@code minQuality}~{@code maxQuality} 区间内的随机 quality 映射：
 * {@code level = 1 + round(quality * (maxLevel - 1))}。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-06
 */
public class AddSchoolScrollLootModifier extends LootModifier {
    public static final MapCodec<AddSchoolScrollLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
        codecStart(inst).and(inst.group(
            ResourceLocation.CODEC.fieldOf("school").forGetter(m -> m.school),
            Codec.floatRange(0f, 1f).fieldOf("min_quality").forGetter(m -> m.minQuality),
            Codec.floatRange(0f, 1f).fieldOf("max_quality").forGetter(m -> m.maxQuality)
        )).apply(inst, AddSchoolScrollLootModifier::new));

    private final ResourceLocation school;
    private final float minQuality;
    private final float maxQuality;

    public AddSchoolScrollLootModifier(LootItemCondition[] conditions, ResourceLocation school,
                                       float minQuality, float maxQuality) {
        super(conditions);
        this.school = school;
        this.minQuality = minQuality;
        this.maxQuality = maxQuality;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        // 不走 Iron's SpellFilter:其学派→法术映射是永不失效的静态缓存,若在法术配置
        // 加载完成前被任何 mod 触发,会以"空列表"定格,本学派卷轴从此永远不掉。
        // 这里每次即时枚举注册表,并按学派注册 id 比较(getSchoolType 的引用相等在
        // 配置未就绪时会失配),开销可忽略。
        List<AbstractSpell> spells = SpellRegistry.REGISTRY.stream()
            .filter(spell -> spell.isEnabled() && spell.allowLooting()
                && spell.getSchoolType() != null && school.equals(spell.getSchoolType().getId()))
            .toList();
        if (spells.isEmpty()) {
            return loot;
        }

        RandomSource random = ctx.getRandom();
        AbstractSpell spell = pickWeightedByRarity(spells, random);
        float quality = minQuality + random.nextFloat() * (maxQuality - minQuality);
        // Iron's 同款 quality→等级公式,再夹到法术自身等级下限(如 Decant 最低 2 级),避免掉出低于下限的卷轴
        int level = Math.max(spell.getMinLevel(), 1 + Math.round(quality * (spell.getMaxLevel() - 1)));

        ItemStack scroll = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, level, scroll);
        loot.add(scroll);
        return loot;
    }

    private static AbstractSpell pickWeightedByRarity(List<AbstractSpell> spells, RandomSource random) {
        SpellRarity[] rarities = SpellRarity.values();
        int total = 0;
        for (AbstractSpell spell : spells) {
            total += weightFromRarity(rarities[spell.getMinRarity()]);
        }
        int roll = random.nextInt(total);
        for (AbstractSpell spell : spells) {
            roll -= weightFromRarity(rarities[spell.getMinRarity()]);
            if (roll < 0) {
                return spell;
            }
        }
        return spells.getLast(); // roll < total 保证循环内必命中,此处仅作保底
    }

    /** 权重表与 Iron's {@code RandomizeSpellFunction#getWeightFromRarity} 保持一致。 */
    private static int weightFromRarity(SpellRarity rarity) {
        return switch (rarity) {
            case COMMON -> 40;
            case UNCOMMON -> 30;
            case RARE -> 15;
            case EPIC -> 8;
            case LEGENDARY -> 4;
        };
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
