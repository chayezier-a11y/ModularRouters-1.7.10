package me.desht.modularrouters.container;

import me.desht.modularrouters.container.handler.GhostItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBulkItemFilter extends Container {
    private final GhostItemHandler ghostHandler;
    private final ItemStack filterStack;

    public ContainerBulkItemFilter(EntityPlayer player, ItemStack filterStack) {
        this.filterStack = filterStack;
        this.ghostHandler = new GhostItemHandler(18);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 6; j++) {
                addSlotToContainer(new Slot(ghostHandler, i * 6 + j, 8 + j * 18, 18 + i * 18));
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int j = 0; j < 9; j++) {
            addSlotToContainer(new Slot(player.inventory, j, 8 + j * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) { return true; }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) { return null; }
}
