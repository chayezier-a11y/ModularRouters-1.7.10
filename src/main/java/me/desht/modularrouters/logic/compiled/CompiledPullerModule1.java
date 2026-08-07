package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class CompiledPullerModule1 extends CompiledModule {
    public CompiledPullerModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferFull()) return false;

        ForgeDirection facing = getAbsoluteDirection(router);
        int x = router.xCoord + facing.offsetX;
        int y = router.yCoord + facing.offsetY;
        int z = router.zCoord + facing.offsetZ;

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
                            // Return what couldn't fit back to the source inventory
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
