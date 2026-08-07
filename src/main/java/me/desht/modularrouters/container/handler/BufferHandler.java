package me.desht.modularrouters.container.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BufferHandler implements IInventory {
    private ItemStack buffer = null;
    private int stackLimit;
    private Runnable dirtyCallback;

    public BufferHandler() {
        this(64);
    }

    public BufferHandler(int stackLimit) {
        this.stackLimit = stackLimit;
    }

    public void setDirtyCallback(Runnable callback) {
        this.dirtyCallback = callback;
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return buffer;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (buffer != null) {
            if (buffer.stackSize <= amount) {
                ItemStack result = buffer;
                buffer = null;
                markDirty();
                return result;
            }
            ItemStack result = buffer.splitStack(amount);
            if (buffer.stackSize == 0) {
                buffer = null;
            }
            markDirty();
            return result;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (buffer != null) {
            ItemStack result = buffer;
            buffer = null;
            return result;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        buffer = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "buffer";
    }

    @Override
    public boolean isCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return stackLimit;
    }

    public void setStackLimit(int limit) {
        this.stackLimit = limit;
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

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (stack == null) return null;
        if (buffer == null) {
            if (!simulate) {
                buffer = stack.copy();
                markDirty();
            }
            return null;
        }
        if (buffer.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(buffer, stack)) {
            int space = Math.min(getInventoryStackLimit(), buffer.getMaxStackSize()) - buffer.stackSize;
            if (space <= 0) return stack;
            int toInsert = Math.min(space, stack.stackSize);
            if (!simulate) {
                buffer.stackSize += toInsert;
                markDirty();
            }
            if (toInsert >= stack.stackSize) return null;
            ItemStack result = stack.copy();
            result.stackSize -= toInsert;
            return result;
        }
        return stack;
    }

    public ItemStack extractItem(int amount, boolean simulate) {
        if (buffer == null) return null;
        int toExtract = Math.min(amount, buffer.stackSize);
        ItemStack result = buffer.copy();
        result.stackSize = toExtract;
        if (!simulate) {
            buffer.stackSize -= toExtract;
            if (buffer.stackSize <= 0) buffer = null;
            markDirty();
        }
        return result;
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("Buffer")) {
            buffer = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Buffer"));
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        if (buffer != null) {
            NBTTagCompound itemTag = new NBTTagCompound();
            buffer.writeToNBT(itemTag);
            tag.setTag("Buffer", itemTag);
        }
        return tag;
    }

    @Override
    public void markDirty() {
        if (dirtyCallback != null) {
            dirtyCallback.run();
        }
    }
}
