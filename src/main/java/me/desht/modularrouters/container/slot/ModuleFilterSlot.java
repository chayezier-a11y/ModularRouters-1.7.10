package me.desht.modularrouters.container.slot;

import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ModuleFilterSlot extends Slot {
    private final ModuleFilterHandler handler;

    public ModuleFilterSlot(ModuleFilterHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.handler = handler;
    }

    @Override
    public void putStack(net.minecraft.item.ItemStack stack) {
        if (stack != null) {
            stack = stack.copy();
            stack.stackSize = 1;
        }
        handler.setInventorySlotContents(getSlotIndex(), stack);
        handler.save();
        onSlotChanged();
    }

    @Override
    public boolean isItemValid(net.minecraft.item.ItemStack stack) {
        return true;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
