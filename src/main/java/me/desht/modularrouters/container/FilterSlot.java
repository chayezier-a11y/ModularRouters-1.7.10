package me.desht.modularrouters.container;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class FilterSlot extends Slot {
    private final ItemStack moduleStack;

    public FilterSlot(IInventory inventory, int index, int x, int y, ItemStack moduleStack) {
        super(inventory, index, x, y);
        this.moduleStack = moduleStack;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (stack == null) return true;
        Module module = ItemModule.getModule(moduleStack);
        return module != null && module.isItemValidForFilter(stack);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
