package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.inventory.container.AbstractMaidContainer;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer;
import net.magicterra.winefoxsspellbooks.bauble.SpellBookAwareSlotItemHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 添加饰品切换监听功能
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 23:16
 */
@Mixin(MaidMainContainer.class)
public abstract class MaidMainContainerMixin extends AbstractMaidContainer {
    public MaidMainContainerMixin(@Nullable MenuType<?> type, int id, Inventory inventory, int entityId) {
        super(type, id, inventory, entityId);
    }

    @Redirect(method = "addMaidBauble", at = @At(value = "NEW", target = "(Lnet/neoforged/neoforge/items/IItemHandler;III)Lnet/neoforged/neoforge/items/SlotItemHandler;"))
    private SlotItemHandler addMaidBauble(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        return new SpellBookAwareSlotItemHandler(itemHandler, index, xPosition, yPosition, maid);
    }
}
