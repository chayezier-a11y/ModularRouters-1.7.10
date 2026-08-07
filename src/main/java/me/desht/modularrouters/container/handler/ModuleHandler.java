package me.desht.modularrouters.container.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ModuleHandler implements IInventory {
    private final ItemStack[] slots;
    private final int size;
    private final int stackLimit;
    private Runnable dirtyCallback;

    public ModuleHandler(int size) {
        this(size, 1);
    }

    public ModuleHandler(int size, int stackLimit) {
        this.size = size;
        this.stackLimit = stackLimit;
        this.slots = new ItemStack[size];
    }

    public void setDirtyCallback(Runnable callback) {
        this.dirtyCallback = callback;
    }

    @Override
    public int getSizeInventory() {
        return size;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slots[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slots[slot] != null) {
            if (slots[slot].stackSize <= amount) {
                ItemStack result = slots[slot];
                slots[slot] = null;
                markDirty();
                return result;
            }
            ItemStack result = slots[slot].splitStack(amount);
            if (slots[slot].stackSize == 0) slots[slot] = null;
            markDirty();
            return result;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (slots[slot] != null) {
            ItemStack result = slots[slot];
            slots[slot] = null;
            return result;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        slots[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "modules";
    }

    @Override
    public boolean isCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return stackLimit;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openChest() {}

    @Override
    public void closeChest() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    public void readFromNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount() && i < size; i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < size) {
                slots[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < size; i++) {
            if (slots[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                slots[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("Items", list);
        tag.setInteger("Size", size);
        return tag;
    }

    @Override
    public void markDirty() {
        if (dirtyCallback != null) {
            dirtyCallback.run();
        }
    }
}
