package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.augment.PickupDelayAugment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;

public class CompiledDropperModule extends CompiledModule {
    private final int pickupDelay;

    public CompiledDropperModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        pickupDelay = getAugmentCount(ItemAugment.getAugment("pickupDelayAugment"))
                * PickupDelayAugment.TICKS_PER_AUGMENT;
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        ItemStack stack = router.getBufferItemStack();
        if (getFilter().allowItem(stack) && isRegulationOK(router, false)) {
            int nItems = Math.min(getItemsPerTick(router), stack.stackSize - getRegulationAmount());
            if (nItems <= 0) {
                return false;
            }
            ItemStack toDrop = router.peekBuffer(nItems);
            ForgeDirection facing = getAbsoluteDirection(router);
            double x = router.xCoord + 0.5 + facing.offsetX * 0.7;
            double y = router.yCoord + 0.3 + facing.offsetY * 0.7;
            double z = router.zCoord + 0.5 + facing.offsetZ * 0.7;

            EntityItem item = new EntityItem(router.getWorldObj(), x, y, z, toDrop);
            setupItemVelocity(router, item);
            item.delayBeforeCanPickup = pickupDelay;
            router.getWorldObj().spawnEntityInWorld(item);
            router.extractBuffer(toDrop.stackSize);
            return true;
        }
        return false;
    }

    void setupItemVelocity(TileEntityItemRouter router, EntityItem item) {
        item.motionX = 0;
        item.motionY = 0;
        item.motionZ = 0;
    }
}
