package net.magicterra.winefoxsspellbooks.datagen;

import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/**
 * 女仆实体标签生成
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-03-17
 */
public class MaidEntityTagsProvider extends net.minecraft.data.tags.EntityTypeTagsProvider {
    public static final TagKey<EntityType<?>> MAID_TAG = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "maid"));

    public MaidEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MAID_TAG)
            .add(InitEntities.MAID.get())
            .add(net.magicterra.winefoxsspellbooks.registry.WsbEntities.SUMMONED_MAID.get());
    }
}
