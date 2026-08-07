package me.desht.modularrouters.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class ContainerSmartFilter extends Container {
    private final ItemStack filterStack;

    public ContainerSmartFilter(EntityPlayer player, ItemStack filterStack) {
        this.filterStack = filterStack;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new net.minecraft.inventory.Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int j = 0; j < 9; j++) {
            addSlotToContainer(new net.minecraft.inventory.Slot(player.inventory, j, 8 + j * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) { return true; }
}
