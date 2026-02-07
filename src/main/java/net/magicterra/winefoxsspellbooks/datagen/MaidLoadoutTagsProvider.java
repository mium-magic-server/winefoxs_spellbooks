package net.magicterra.winefoxsspellbooks.datagen;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/**
 * 女仆装备标签生成器
 * 生成按等级分层的武器和护甲物品标签
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public class MaidLoadoutTagsProvider extends ItemTagsProvider {

    // 武器标签
    public static final TagKey<Item> MAID_WEAPONS_TIER1 = createTag("maid_loadout/weapons/tier1");
    public static final TagKey<Item> MAID_WEAPONS_TIER2 = createTag("maid_loadout/weapons/tier2");
    public static final TagKey<Item> MAID_WEAPONS_TIER3 = createTag("maid_loadout/weapons/tier3");

    // 头盔标签
    public static final TagKey<Item> MAID_HELMETS_TIER1 = createTag("maid_loadout/armor/helmets/tier1");
    public static final TagKey<Item> MAID_HELMETS_TIER2 = createTag("maid_loadout/armor/helmets/tier2");
    public static final TagKey<Item> MAID_HELMETS_TIER3 = createTag("maid_loadout/armor/helmets/tier3");

    // 胸甲标签
    public static final TagKey<Item> MAID_CHESTPLATES_TIER1 = createTag("maid_loadout/armor/chestplates/tier1");
    public static final TagKey<Item> MAID_CHESTPLATES_TIER2 = createTag("maid_loadout/armor/chestplates/tier2");
    public static final TagKey<Item> MAID_CHESTPLATES_TIER3 = createTag("maid_loadout/armor/chestplates/tier3");

    // 护腿标签
    public static final TagKey<Item> MAID_LEGGINGS_TIER1 = createTag("maid_loadout/armor/leggings/tier1");
    public static final TagKey<Item> MAID_LEGGINGS_TIER2 = createTag("maid_loadout/armor/leggings/tier2");
    public static final TagKey<Item> MAID_LEGGINGS_TIER3 = createTag("maid_loadout/armor/leggings/tier3");

    // 靴子标签
    public static final TagKey<Item> MAID_BOOTS_TIER1 = createTag("maid_loadout/armor/boots/tier1");
    public static final TagKey<Item> MAID_BOOTS_TIER2 = createTag("maid_loadout/armor/boots/tier2");
    public static final TagKey<Item> MAID_BOOTS_TIER3 = createTag("maid_loadout/armor/boots/tier3");

    private static TagKey<Item> createTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, path));
    }

    public MaidLoadoutTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   CompletableFuture<TagLookup<Block>> blockTags, String modId,
                                   @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addWeaponTags();
        addHelmetTags();
        addChestplateTags();
        addLeggingsTags();
        addBootsTags();
    }

    private void addWeaponTags() {
        // Tier 1: 等级 1-3 的武器
        tag(MAID_WEAPONS_TIER1)
            .add(ItemRegistry.GRAYBEARD_STAFF.get())
            .add(ItemRegistry.ARTIFICER_STAFF.get())
            .add(Items.IRON_SWORD)
            .add(Items.STONE_SWORD);

        // Tier 2: 等级 4-6 的武器
        tag(MAID_WEAPONS_TIER2)
            .add(ItemRegistry.ICE_STAFF.get())
            .add(ItemRegistry.BLOOD_STAFF.get())
            .add(ItemRegistry.LIGHTNING_ROD_STAFF.get())
            .add(ItemRegistry.MAGEHUNTER.get())
            .add(Items.DIAMOND_SWORD)
            .add(Items.IRON_SWORD);

        // Tier 3: 等级 7-10 的武器
        tag(MAID_WEAPONS_TIER3)
            .add(ItemRegistry.PYRIUM_STAFF.get())
            .add(ItemRegistry.HITHER_THITHER_WAND.get())
            .add(ItemRegistry.HELLRAZOR.get())
            .add(ItemRegistry.LEGIONNAIRE_FLAMBERGE.get())
            .add(ItemRegistry.TWILIGHT_GALE.get())
            .add(ItemRegistry.SPELLBREAKER.get())
            .add(ItemRegistry.AMETHYST_RAPIER.get())
            .add(ItemRegistry.KEEPER_FLAMBERGE.get())
            .add(ItemRegistry.ICE_GREATSWORD.get())
            .add(Items.NETHERITE_SWORD);
    }

    private void addHelmetTags() {
        // Tier 1: 等级 1-3 的头盔
        tag(MAID_HELMETS_TIER1)
            .add(ItemRegistry.WIZARD_HELMET.get())
            .add(ItemRegistry.WANDERING_MAGICIAN_HELMET.get())
            .add(Items.LEATHER_HELMET)
            .add(Items.CHAINMAIL_HELMET);

        // Tier 2: 等级 4-6 的头盔
        tag(MAID_HELMETS_TIER2)
            .add(ItemRegistry.WIZARD_HELMET.get())
            .add(ItemRegistry.PYROMANCER_HELMET.get())
            .add(ItemRegistry.CRYOMANCER_HELMET.get())
            .add(ItemRegistry.ELECTROMANCER_HELMET.get())
            .add(Items.IRON_HELMET)
            .add(Items.DIAMOND_HELMET);

        // Tier 3: 等级 7-10 的头盔
        tag(MAID_HELMETS_TIER3)
            .add(ItemRegistry.NETHERITE_MAGE_HELMET.get())
            .add(ItemRegistry.ARCHEVOKER_HELMET.get())
            .add(ItemRegistry.SHADOWWALKER_HELMET.get())
            .add(ItemRegistry.CULTIST_HELMET.get())
            .add(ItemRegistry.TARNISHED_CROWN.get())
            .add(ItemRegistry.PRIEST_HELMET.get())
            .add(Items.NETHERITE_HELMET);
    }

    private void addChestplateTags() {
        // Tier 1: 等级 1-3 的胸甲
        tag(MAID_CHESTPLATES_TIER1)
            .add(ItemRegistry.WIZARD_CHESTPLATE.get())
            .add(ItemRegistry.WANDERING_MAGICIAN_CHESTPLATE.get())
            .add(Items.LEATHER_CHESTPLATE)
            .add(Items.CHAINMAIL_CHESTPLATE);

        // Tier 2: 等级 4-6 的胸甲
        tag(MAID_CHESTPLATES_TIER2)
            .add(ItemRegistry.WIZARD_CHESTPLATE.get())
            .add(ItemRegistry.PYROMANCER_CHESTPLATE.get())
            .add(ItemRegistry.CRYOMANCER_CHESTPLATE.get())
            .add(ItemRegistry.ELECTROMANCER_CHESTPLATE.get())
            .add(Items.IRON_CHESTPLATE)
            .add(Items.DIAMOND_CHESTPLATE);

        // Tier 3: 等级 7-10 的胸甲
        tag(MAID_CHESTPLATES_TIER3)
            .add(ItemRegistry.NETHERITE_MAGE_CHESTPLATE.get())
            .add(ItemRegistry.ARCHEVOKER_CHESTPLATE.get())
            .add(ItemRegistry.SHADOWWALKER_CHESTPLATE.get())
            .add(ItemRegistry.CULTIST_CHESTPLATE.get())
            .add(ItemRegistry.PRIEST_CHESTPLATE.get())
            .add(ItemRegistry.PALADIN_CHESTPLATE.get())
            .add(ItemRegistry.INFERNAL_SORCERER_CHESTPLATE.get())
            .add(Items.NETHERITE_CHESTPLATE);
    }

    private void addLeggingsTags() {
        // Tier 1: 等级 1-3 的护腿
        tag(MAID_LEGGINGS_TIER1)
            .add(ItemRegistry.WIZARD_LEGGINGS.get())
            .add(ItemRegistry.WANDERING_MAGICIAN_LEGGINGS.get())
            .add(Items.LEATHER_LEGGINGS)
            .add(Items.CHAINMAIL_LEGGINGS);

        // Tier 2: 等级 4-6 的护腿
        tag(MAID_LEGGINGS_TIER2)
            .add(ItemRegistry.WIZARD_LEGGINGS.get())
            .add(ItemRegistry.PYROMANCER_LEGGINGS.get())
            .add(ItemRegistry.CRYOMANCER_LEGGINGS.get())
            .add(ItemRegistry.ELECTROMANCER_LEGGINGS.get())
            .add(Items.IRON_LEGGINGS)
            .add(Items.DIAMOND_LEGGINGS);

        // Tier 3: 等级 7-10 的护腿
        tag(MAID_LEGGINGS_TIER3)
            .add(ItemRegistry.NETHERITE_MAGE_LEGGINGS.get())
            .add(ItemRegistry.ARCHEVOKER_LEGGINGS.get())
            .add(ItemRegistry.SHADOWWALKER_LEGGINGS.get())
            .add(ItemRegistry.CULTIST_LEGGINGS.get())
            .add(ItemRegistry.PRIEST_LEGGINGS.get())
            .add(Items.NETHERITE_LEGGINGS);
    }

    private void addBootsTags() {
        // Tier 1: 等级 1-3 的靴子
        tag(MAID_BOOTS_TIER1)
            .add(ItemRegistry.WIZARD_BOOTS.get())
            .add(ItemRegistry.WANDERING_MAGICIAN_BOOTS.get())
            .add(Items.LEATHER_BOOTS)
            .add(Items.CHAINMAIL_BOOTS);

        // Tier 2: 等级 4-6 的靴子
        tag(MAID_BOOTS_TIER2)
            .add(ItemRegistry.WIZARD_BOOTS.get())
            .add(ItemRegistry.PYROMANCER_BOOTS.get())
            .add(ItemRegistry.CRYOMANCER_BOOTS.get())
            .add(ItemRegistry.ELECTROMANCER_BOOTS.get())
            .add(Items.IRON_BOOTS)
            .add(Items.DIAMOND_BOOTS);

        // Tier 3: 等级 7-10 的靴子
        tag(MAID_BOOTS_TIER3)
            .add(ItemRegistry.NETHERITE_MAGE_BOOTS.get())
            .add(ItemRegistry.ARCHEVOKER_BOOTS.get())
            .add(ItemRegistry.SHADOWWALKER_BOOTS.get())
            .add(ItemRegistry.CULTIST_BOOTS.get())
            .add(ItemRegistry.PRIEST_BOOTS.get())
            .add(ItemRegistry.BOOTS_OF_SPEED.get())
            .add(Items.NETHERITE_BOOTS);
    }
}
