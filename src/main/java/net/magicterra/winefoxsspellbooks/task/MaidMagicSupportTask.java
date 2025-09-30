package net.magicterra.winefoxsspellbooks.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.MaidMagicEntity;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.magicterra.winefoxsspellbooks.registry.MaidSpellRegistry;
import net.magicterra.winefoxsspellbooks.task.brain.MaidMagicSupportOwnerTask;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * 法术支援任务
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-30 02:29
 */
public class MaidMagicSupportTask implements IMaidTask {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "magic_support_task");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return ItemRegistry.VILLAGER_SPELL_BOOK.get().getDefaultInstance();
    }

    @Override
    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.attackSound(maid, InitSounds.MAID_ATTACK.get(), 0.5f);
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        return Lists.newArrayList(Pair.of(5, new MaidMagicSupportOwnerTask(maid, this::hasSpells, 0.6f)));
    }

    @Override
    public float searchRadius(EntityMaid maid) {
        return MaidConfig.DANMAKU_RANGE.get();
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Collections.singletonList(Pair.of("has_spells", this::hasSpells));
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        IMagicEntity magicEntity = (IMagicEntity) maid;
        return !magicEntity.isCasting();
    }

    @Override
    public boolean enablePanic(EntityMaid maid) {
        return false;
    }

    private boolean hasSpells(EntityMaid maid) {
        List<SpellData> defenseSpells = new ArrayList<>();
        List<SpellData> supportSpells = new ArrayList<>();
        List<SpellData> positiveEffectSpells = new ArrayList<>();
        List<SpellData> negativeEffectSpells = new ArrayList<>();
        List<SpellData> supportEffectSpells = new ArrayList<>();
        // 检查主手、盔甲栏、饰品栏法术
        boolean spellBookLoaded = false;
        IItemHandler invWrapper = new CombinedInvWrapper(maid.getArmorInvWrapper(), new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, maid.getMainHandItem())), maid.getMaidBauble());
        for (int i = 0; i < invWrapper.getSlots(); i++) {
            ItemStack stack = invWrapper.getStackInSlot(i);
            Item item = stack.getItem();
            if (item instanceof ISpellbook) {
                // 如果饰品栏放入多个魔法书，只有一本能生效（和玩家保持一致）
                if (spellBookLoaded) {
                    continue;
                } else {
                    spellBookLoaded = true;
                }
            }
            if (!stack.isEmpty() && ISpellContainer.isSpellContainer(stack)) {
                ISpellContainer spellContainer = ISpellContainer.get(stack);
                List<SpellSlot> activeSpells = spellContainer.getActiveSpells();
                for (SpellSlot spellSlot : activeSpells) {
                    AbstractSpell spell = spellSlot.getSpell();
                    int level = MaidMagicManager.getLevelFor(maid, spell, spellSlot.getLevel());
                    SpellData spellData = new SpellData(spell, level);
                    if (MaidSpellRegistry.isDefenseSpell(spell)) {
                        defenseSpells.add(spellData);
                    } else if (MaidSpellRegistry.isSupportSpell(spell)) {
                        supportSpells.add(spellData);
                    } else if (MaidSpellRegistry.isPositiveEffectSpell(spell)) {
                        positiveEffectSpells.add(spellData);
                    } else if (MaidSpellRegistry.isNegativeEffectSpell(spell)) {
                        negativeEffectSpells.add(spellData);
                    } else if (MaidSpellRegistry.isSupportEffectSpell(spell)) {
                        supportEffectSpells.add(spellData);
                    }
                }
            }
        }
        MaidMagicEntity magicEntity = (MaidMagicEntity) maid;
        MaidSpellDataHolder spellDataHolder = magicEntity.winefoxsSpellbooks$getSpellDataHolder();
        spellDataHolder.updateAttackSpells(Collections.emptyList());
        spellDataHolder.updateDefenseSpells(defenseSpells);
        spellDataHolder.updateMovementSpells(Collections.emptyList());
        spellDataHolder.updateSupportSpells(supportSpells);
        spellDataHolder.updatePositiveEffectSpells(positiveEffectSpells);
        spellDataHolder.updateNegativeEffectSpells(negativeEffectSpells);
        spellDataHolder.updateSupportEffectSpells(supportEffectSpells);
        return spellDataHolder.hasAnySpells();
    }
}
