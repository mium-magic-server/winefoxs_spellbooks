package net.magicterra.winefoxsspellbooks.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.BaubleContainer;
import net.magicterra.winefoxsspellbooks.bauble.PlaceableInterceptBaubleSlotHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 添加饰品切换监听功能
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-07-27 23:16
 */
@Mixin(BaubleContainer.class)
public abstract class BaubleContainerMixin extends MaidMainContainer {
    public BaubleContainerMixin(int id, Inventory inventory, int entityId) {
        super(BaubleContainer.TYPE, id, inventory, entityId);
    }

    @Redirect(method = "addBackpackInv", at = @At(value = "NEW", target = "(Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;III)Lcom/github/tartaricacid/touhoulittlemaid/inventory/container/backpack/BaubleContainer$BaubleSlot;"))
    private BaubleContainer.BaubleSlot addMaidBauble(EntityMaid maid, int index, int xPosition, int yPosition) {
        return new PlaceableInterceptBaubleSlotHandler(maid, index, xPosition, yPosition);
    }
}
