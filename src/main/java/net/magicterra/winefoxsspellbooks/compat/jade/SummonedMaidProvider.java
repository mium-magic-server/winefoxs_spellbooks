package net.magicterra.winefoxsspellbooks.compat.jade;

import io.redspace.ironsspellbooks.api.spells.SpellData;
import java.util.Collection;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.magicterra.winefoxsspellbooks.entity.spells.SummonedEntityMaid;
import net.magicterra.winefoxsspellbooks.magic.MaidMagicManager;
import net.magicterra.winefoxsspellbooks.magic.MaidSpellDataHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade 显示法力值
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-23 01:18
 */
public enum SummonedMaidProvider implements IEntityComponentProvider {

    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(WinefoxsSpellbooks.MODID, "summoned_maid");

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if (entityAccessor.getEntity() instanceof SummonedEntityMaid maid) {
            int maxMana = (int) MaidMagicManager.getMaxMana(maid);
            int mana = (int) maid.winefoxsSpellbooks$getMana();
            iTooltip.add(Component.translatable("top.winefoxs_spellbooks.entity_maid.mana", mana, maxMana));
            MaidSpellDataHolder maidSpellDataHolder = maid.getMagicAdapter().winefoxsSpellbooks$getSpellDataHolder();


            Collection<SpellData> spells = maidSpellDataHolder.getAllSpells();
            if (!spells.isEmpty()) {
                if (spells.size() > 4) {
                    iTooltip.add(Component.translatable("top.winefoxs_spellbooks.summoned_maid.spells_short", spells.size()));
                } else {
                    for (SpellData spell : spells) {
                        iTooltip.add(Component.translatable("top.winefoxs_spellbooks.summoned_maid.spells", Component.translatable(spell.getSpell().getComponentId())));
                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
