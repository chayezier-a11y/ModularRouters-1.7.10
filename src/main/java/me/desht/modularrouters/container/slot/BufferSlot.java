package me.desht.modularrouters.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class BufferSlot extends Slot {

    public BufferSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
}
