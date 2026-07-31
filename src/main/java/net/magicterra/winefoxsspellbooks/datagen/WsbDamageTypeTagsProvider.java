package net.magicterra.winefoxsspellbooks.datagen;

import java.util.concurrent.CompletableFuture;
import net.magicterra.winefoxsspellbooks.damage.WsbDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/**
 * 伤害类型标签生成
 * <p>
 * 学派伤害要挂进 {@code neoforge:is_magic}，否则一切「魔法伤害」判定（护符、抗性、附属的免疫）都认不出它。
 * 伤害类型本体是手写数据包条目，不在数据生成的注册表视图里，故用 {@code addOptional} 按 id 挂。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-31
 */
public class WsbDamageTypeTagsProvider extends TagsProvider<DamageType> {

    public WsbDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                     String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Tags.DamageTypes.IS_MAGIC).addOptional(WsbDamageTypes.WINEFOX_HEX_MAGIC.location());
    }
}
