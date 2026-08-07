package me.desht.modularrouters.block.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTemplateFrame extends TileEntity implements IInventory {
    private ItemStack[] inventory = new ItemStack[1];
    private String customName;

    @Override
    public int getSizeInventory() { return 1; }

    @Override
    public ItemStack getStackInSlot(int slot) { return inventory[slot]; }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] != null) {
            if (inventory[slot].stackSize <= amount) {
                ItemStack result = inventory[slot];
                inventory[slot] = null;
                markDirty();
                return result;
            }
            ItemStack result = inventory[slot].splitStack(amount);
            if (inventory[slot].stackSize == 0) inventory[slot] = null;
            markDirty();
            return result;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (inventory[slot] != null) {
            ItemStack result = inventory[slot];
            inventory[slot] = null;
            return result;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return customName != null ? customName : "container.templateFrame";
    }

    @Override
    public boolean isCustomInventoryName() { return customName != null; }

    public void setInventoryName(String name) { this.customName = name; }

    @Override
    public int getInventoryStackLimit() { return 1; }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) { return true; }

    @Override
    public void openChest() {}

    @Override
    public void closeChest() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList list = tag.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
        if (tag.hasKey("CustomName")) {
            customName = tag.getString("CustomName");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("Items", list);
        if (customName != null) {
            tag.setString("CustomName", customName);
        }
    }

    public static TileEntityTemplateFrame getFrameAt(net.minecraft.world.IBlockAccess world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        return te instanceof TileEntityTemplateFrame ? (TileEntityTemplateFrame) te : null;
    }
}
