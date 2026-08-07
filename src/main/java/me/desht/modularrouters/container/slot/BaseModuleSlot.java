package me.desht.modularrouters.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BaseModuleSlot extends Slot {

    public BaseModuleSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return inventory.isItemValidForSlot(getSlotIndex(), stack);
    }
}
