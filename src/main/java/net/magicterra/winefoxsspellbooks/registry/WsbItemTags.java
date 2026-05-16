package net.magicterra.winefoxsspellbooks.registry;

import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * 模组自有物品标签常量
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-05-11
 */
public final class WsbItemTags {
    private WsbItemTags() {
    }

    /** 酒狐巫法学派 Focus 物品集合 */
    public static final TagKey<Item> WINEFOX_HEX_FOCUS = create("winefox_hex_focus");

    /** Iron's Spellbooks 总 Focus tag，本模组通过 additive datagen 把自家 focus 并进来 */
    public static final TagKey<Item> IRONS_SCHOOL_FOCUS = TagKey.create(Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "school_focus"));

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, path));
    }
}
