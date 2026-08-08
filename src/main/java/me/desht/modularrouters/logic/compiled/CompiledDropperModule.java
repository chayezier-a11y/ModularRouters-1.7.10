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
        pickupDelay = pickupDelayForCount(getAugmentCount(ItemAugment.getAugment("pickupDelayAugment")));
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        ItemStack stack = router.getBufferItemStack();
        if (stack != null && getFilter().allowItem(stack) && isRegulationOK(router, false)) {
            int nItems = Math.min(getItemsPerTick(router), stack.stackSize - getRegulationAmount());
            if (nItems <= 0) {
                return false;
            }
            ItemStack toDrop = router.peekBuffer(nItems);
            ForgeDirection facing = getAbsoluteDirection(router);
            double x = router.xCoord + 0.5 + facing.offsetX * 0.8;
            double y = router.yCoord + 0.5 + facing.offsetY * 0.8;
            double z = router.zCoord + 0.5 + facing.offsetZ * 0.8;

            EntityItem item = new EntityItem(router.getWorldObj(), x, y, z, toDrop);
            setupItemVelocity(router, item);
            item.delayBeforeCanPickup = pickupDelay;
            if (!spawnItem(router, item)) return false;
            router.extractBuffer(toDrop.stackSize);
            return true;
        }
        return false;
    }

    protected boolean spawnItem(TileEntityItemRouter router, EntityItem item) {
        return router.getWorldObj() != null && router.getWorldObj().spawnEntityInWorld(item);
    }

    static int pickupDelayForCount(int count) {
        return count * PickupDelayAugment.TICKS_PER_AUGMENT;
    }

    void setupItemVelocity(TileEntityItemRouter router, EntityItem item) {
        item.motionX = 0;
        item.motionY = 0;
        item.motionZ = 0;
    }
}
