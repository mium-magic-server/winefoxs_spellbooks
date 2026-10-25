package net.magicterra.winefoxsspellbooks.item;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.item.consumables.OakskinElixir;
import io.redspace.ironsspellbooks.item.consumables.SimpleElixir;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/**
 * 品酒排里的四支酒签，下标 0～3 从左到右；空槽为 {@link ItemStack#EMPTY}
 * <p>
 * 每支存一件物品：可饮用的普通药水，或铁魔法（及附属）的药剂。装填总是装进最左边的空槽，取用总是取最右边的非空槽。
 * 存进来的物品不再改动，读出后也不要改。
 *
 * @param vials 固定 {@link #SIZE} 支
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public record VialsContents(List<ItemStack> vials) {
    /**
     * 每排酒签数
     */
    public static final int SIZE = 4;

    public static final VialsContents EMPTY = new VialsContents(Collections.nCopies(SIZE, ItemStack.EMPTY));

    /**
     * 1.0.2-alpha.3 每支只存药水的 {@link PotionContents}，读旧档时换成药水瓶；写出时不会用到
     */
    private static final Codec<List<ItemStack>> LEGACY_CODEC = PotionContents.CODEC.listOf().xmap(
        list -> list.stream().map(contents -> PotionContents.EMPTY.equals(contents) ? ItemStack.EMPTY : toPotion(contents)).toList(),
        list -> list.stream().map(stack -> stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)).toList());

    public static final Codec<VialsContents> CODEC = Codec.withAlternative(ItemStack.OPTIONAL_CODEC.listOf(), LEGACY_CODEC)
        .xmap(VialsContents::new, VialsContents::vials);

    public static final StreamCodec<RegistryFriendlyByteBuf, VialsContents> STREAM_CODEC =
        ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(SIZE)).map(VialsContents::new, VialsContents::vials);

    public VialsContents {
        // 存档或数据包给出的长度不对时补齐或截断，保证下标 0～3 总是可用
        List<ItemStack> fixed = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            fixed.add(i < vials.size() && !vials.get(i).isEmpty() ? vials.get(i).copyWithCount(1) : ItemStack.EMPTY);
        }
        vials = Collections.unmodifiableList(fixed);
    }

    /**
     * 第 index 支，只读
     */
    public ItemStack get(int index) {
        return vials.get(index);
    }

    /**
     * 第 index 支是否为空
     */
    public boolean isEmpty(int index) {
        return vials.get(index).isEmpty();
    }

    /**
     * 最左边的空槽，装填用；装满时返回 -1
     */
    public int firstEmpty() {
        for (int i = 0; i < SIZE; i++) {
            if (isEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 最右边的非空槽，取用用；全空时返回 -1
     */
    public int lastFilled() {
        for (int i = SIZE - 1; i >= 0; i--) {
            if (!isEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 换掉第 index 支后的新内容；vial 只取一件
     */
    public VialsContents with(int index, ItemStack vial) {
        List<ItemStack> copy = new ArrayList<>(vials);
        copy.set(index, vial);
        return new VialsContents(copy);
    }

    // ItemStack 没有按内容比较的 equals，数据组件判断改动与缓存编码都要按内容比

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof VialsContents other && ItemStack.listMatches(vials, other.vials);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(vials);
    }

    // ==================== 单支 ====================

    /**
     * 能否装进酒签：能喝的普通药水（含水瓶，不收喷溅型和滞留型），或铁魔法（及附属）的药剂
     */
    public static boolean canHold(ItemStack stack) {
        return isPotion(stack) || isElixir(stack);
    }

    /**
     * 是否普通药水
     */
    public static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION)
            && !PotionContents.EMPTY.equals(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
    }

    /**
     * 是否铁魔法的药剂：只认能读出效果的两类，女仆挑酒签时要看效果；茶、麦酒等其他饮品效果读不出来，不收
     */
    public static boolean isElixir(ItemStack stack) {
        return stack.getItem() instanceof SimpleElixir || stack.getItem() instanceof OakskinElixir;
    }

    /**
     * 是否一瓶水，只有水能倒回原版炼药锅
     */
    public static boolean isWater(ItemStack vial) {
        return vial.is(Items.POTION) && vial.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
    }

    /**
     * 喝下这一支会得到的效果
     */
    public static List<MobEffectInstance> effects(ItemStack vial) {
        if (vial.getItem() instanceof SimpleElixir elixir) {
            return List.of(elixir.getMobEffect());
        }
        if (vial.getItem() instanceof OakskinElixir elixir) {
            return List.of(elixir.getMobEffect());
        }
        List<MobEffectInstance> effects = new ArrayList<>();
        vial.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects().forEach(effects::add);
        return effects;
    }

    /**
     * 液体颜色：药水取药水颜色，药剂取其效果的颜色
     */
    public static int color(ItemStack vial) {
        if (isElixir(vial)) {
            return effects(vial).getFirst().getEffect().value().getColor();
        }
        return vial.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();
    }

    /**
     * 一瓶普通药水
     */
    public static ItemStack toPotion(PotionContents contents) {
        ItemStack potion = new ItemStack(Items.POTION);
        potion.set(DataComponents.POTION_CONTENTS, contents);
        return potion;
    }
}
