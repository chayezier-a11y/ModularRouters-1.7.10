package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class CompiledPullerModule2 extends CompiledModule {
    public CompiledPullerModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferFull()) return false;

        // Try bound target first
        ModuleTarget target = getExplicitTarget();
        if (target != null) {
            return pullFrom(router, target.getX(), target.getY(), target.getZ());
        }

        // Fall back to scanning backward
        int range = getRange();
        ForgeDirection facing = getAbsoluteDirection(router);
        for (int d = 1; d <= range; d++) {
            int x = router.xCoord + facing.offsetX * d;
            int y = router.yCoord + facing.offsetY * d;
            int z = router.zCoord + facing.offsetZ * d;
            if (pullFrom(router, x, y, z)) return true;
        }
        return false;
    }

    private boolean pullFrom(TileEntityItemRouter router, int x, int y, int z) {
        if (router.getWorldObj().getTileEntity(x, y, z) instanceof IInventory) {
            IInventory inv = (IInventory) router.getWorldObj().getTileEntity(x, y, z);
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null && !getFilter().rejectItem(stack)) {
                    int toPull = Math.min(stack.stackSize, getItemsPerTick(router));
                    ItemStack pulled = inv.decrStackSize(i, toPull);
                    if (pulled != null) {
                        ItemStack remaining = router.insertBuffer(pulled);
                        if (remaining != null) {
                            ItemStack slotStack = inv.getStackInSlot(i);
                            if (slotStack == null) {
                                inv.setInventorySlotContents(i, remaining);
                            } else if (slotStack.isItemEqual(remaining) && ItemStack.areItemStackTagsEqual(slotStack, remaining)) {
                                slotStack.stackSize += remaining.stackSize;
                            } else {
                                inv.setInventorySlotContents(i, remaining);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
