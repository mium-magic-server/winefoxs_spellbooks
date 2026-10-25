package net.magicterra.winefoxsspellbooks.item;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * 品酒排里的四支酒签，下标 0～3 从左到右；空槽为 {@link PotionContents#EMPTY}
 * <p>
 * 装填总是装进最左边的空槽，取用总是取最右边的非空槽。
 *
 * @param vials 固定 {@link #SIZE} 支
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-25
 */
public record VialsContents(List<PotionContents> vials) {
    /**
     * 每排酒签数
     */
    public static final int SIZE = 4;

    public static final VialsContents EMPTY = new VialsContents(Collections.nCopies(SIZE, PotionContents.EMPTY));

    public static final Codec<VialsContents> CODEC = PotionContents.CODEC.listOf().xmap(VialsContents::new, VialsContents::vials);

    public static final StreamCodec<RegistryFriendlyByteBuf, VialsContents> STREAM_CODEC =
        PotionContents.STREAM_CODEC.apply(ByteBufCodecs.list(SIZE)).map(VialsContents::new, VialsContents::vials);

    public VialsContents {
        // 存档或数据包给出的长度不对时补齐或截断，保证下标 0～3 总是可用
        List<PotionContents> fixed = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            fixed.add(i < vials.size() ? vials.get(i) : PotionContents.EMPTY);
        }
        vials = List.copyOf(fixed);
    }

    /**
     * 第 index 支
     */
    public PotionContents get(int index) {
        return vials.get(index);
    }

    /**
     * 第 index 支是否为空
     */
    public boolean isEmpty(int index) {
        return PotionContents.EMPTY.equals(vials.get(index));
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
     * 换掉第 index 支后的新内容
     */
    public VialsContents with(int index, PotionContents contents) {
        List<PotionContents> copy = new ArrayList<>(vials);
        copy.set(index, contents);
        return new VialsContents(copy);
    }

    /**
     * 能否装进酒签：只收能喝的普通药水（含水瓶），不收喷溅型和滞留型
     */
    public static boolean canHold(ItemStack stack) {
        return stack.is(Items.POTION)
            && !PotionContents.EMPTY.equals(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
    }

    /**
     * 把一支酒签倒回一瓶普通药水
     */
    public static ItemStack toPotion(PotionContents contents) {
        ItemStack potion = new ItemStack(Items.POTION);
        potion.set(DataComponents.POTION_CONTENTS, contents);
        return potion;
    }
}
