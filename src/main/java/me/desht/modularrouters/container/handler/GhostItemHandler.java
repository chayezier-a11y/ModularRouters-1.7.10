package me.desht.modularrouters.container.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class GhostItemHandler implements IInventory {
    protected final ItemStack[] items;

    public GhostItemHandler(int size) {
        this.items = new ItemStack[size];
    }

    @Override
    public int getSizeInventory() { return items.length; }

    @Override
    public ItemStack getStackInSlot(int slot) { return items[slot]; }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        items[slot] = null;
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) { return null; }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (stack != null) {
            items[slot] = stack.copy();
            items[slot].stackSize = Math.min(items[slot].stackSize, getInventoryStackLimit());
        } else {
            items[slot] = null;
        }
    }

    @Override public String getInventoryName() { return "ghost"; }
    @Override public boolean isCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public void markDirty() {}
    @Override public boolean isUseableByPlayer(EntityPlayer player) { return true; }
    @Override public void openChest() {}
    @Override public void closeChest() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }

    public NBTTagList serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                items[i].writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        return list;
    }

    public void deserializeNBT(NBTTagList list) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < items.length) {
                items[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }
}
