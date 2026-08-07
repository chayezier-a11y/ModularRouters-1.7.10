package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class CompiledSenderModule1 extends CompiledModule {
    public CompiledSenderModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferEmpty()) return false;

        int range = getRange();
        ForgeDirection facing = getAbsoluteDirection(router);
        ItemStack toSend = router.peekBuffer(getItemsPerTick(router));
        if (toSend == null || getFilter().rejectItem(toSend)) return false;

        for (int d = 1; d <= range; d++) {
            int x = router.xCoord + facing.offsetX * d;
            int y = router.yCoord + facing.offsetY * d;
            int z = router.zCoord + facing.offsetZ * d;

            if (router.getWorldObj().getTileEntity(x, y, z) instanceof IInventory) {
                IInventory inv = (IInventory) router.getWorldObj().getTileEntity(x, y, z);
                return insertIntoInventory(router, inv, getItemsPerTick(router));
            }
        }
        return false;
    }

    /** Moves only what the destination accepts and returns the rest to the router buffer. */
    static boolean insertIntoInventory(TileEntityItemRouter router, IInventory inv, int amount) {
        ItemStack extracted = router.extractBuffer(amount);
        if (extracted == null) return false;
        int originalSize = extracted.stackSize;

        for (int i = 0; i < inv.getSizeInventory() && extracted.stackSize > 0; i++) {
            if (!inv.isItemValidForSlot(i, extracted)) continue;
            ItemStack existing = inv.getStackInSlot(i);
            int max = Math.min(inv.getInventoryStackLimit(), extracted.getMaxStackSize());
            if (existing == null) {
                ItemStack inserted = extracted.copy();
                inserted.stackSize = Math.min(max, extracted.stackSize);
                inv.setInventorySlotContents(i, inserted);
                extracted.stackSize -= inserted.stackSize;
            } else if (existing.isItemEqual(extracted) && ItemStack.areItemStackTagsEqual(existing, extracted)) {
                int space = Math.min(inv.getInventoryStackLimit(), existing.getMaxStackSize()) - existing.stackSize;
                if (space > 0) {
                    int inserted = Math.min(space, extracted.stackSize);
                    existing.stackSize += inserted;
                    extracted.stackSize -= inserted;
                }
            }
        }

        if (extracted.stackSize > 0) router.insertBuffer(extracted);
        return extracted.stackSize < originalSize;
    }
}
