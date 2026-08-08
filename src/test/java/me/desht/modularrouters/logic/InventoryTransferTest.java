package me.desht.modularrouters.logic;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import me.desht.modularrouters.item.ItemBase;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.inventory.ISidedInventory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class InventoryTransferTest {
    private static final ItemBase APPLE = new ItemBase("testApple");
    private static final ItemBase BREAD = new ItemBase("testBread");

    @Test
    public void sidedSlotsControlInsertAndExtract() {
        SidedInventory inventory = new SidedInventory(2, new int[] { 0 });
        ItemStack stack = new ItemStack(APPLE, 3);

        assertEquals(3, InventoryTransfer.insert(inventory, ForgeDirection.NORTH, stack, false));
        assertEquals(0, InventoryTransfer.insert(inventory, ForgeDirection.NORTH,
                new ItemStack(BREAD, 1), false));
        assertNull(InventoryTransfer.extract(inventory, ForgeDirection.NORTH,
                new ItemStack(APPLE), 1, true));
        assertEquals(3, inventory.getStackInSlot(0).stackSize);
    }

    @Test
    public void simulationAndMatchingRespectMetadataAndNbt() {
        InventoryBasic inventory = new InventoryBasic("test", false, 1);
        ItemStack stored = new ItemStack(APPLE, 4, 0);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("flavor", "golden");
        stored.setTagCompound(tag);
        inventory.setInventorySlotContents(0, stored);

        ItemStack differentTag = new ItemStack(APPLE, 2, 0);
        NBTTagCompound otherTag = new NBTTagCompound();
        otherTag.setString("flavor", "red");
        differentTag.setTagCompound(otherTag);

        assertEquals(0, InventoryTransfer.count(inventory, ForgeDirection.UNKNOWN,
                differentTag, false, false));
        assertEquals(4, InventoryTransfer.count(inventory, ForgeDirection.UNKNOWN,
                differentTag, false, true));
        ItemStack simulatedInsert = stored.copy();
        simulatedInsert.stackSize = 2;
        assertEquals(2, InventoryTransfer.insert(inventory, ForgeDirection.UNKNOWN,
                simulatedInsert, true));
        assertEquals(4, inventory.getStackInSlot(0).stackSize);
    }

    @Test
    public void transferCommitsOnlyAcceptedItemsWithoutLoss() {
        InventoryBasic source = new InventoryBasic("source", false, 1);
        InventoryBasic destination = new InventoryBasic("destination", false, 1);
        source.setInventorySlotContents(0, new ItemStack(APPLE, 5));
        destination.setInventorySlotContents(0, new ItemStack(APPLE, 62));

        assertEquals(2, InventoryTransfer.transfer(source, ForgeDirection.UNKNOWN,
                destination, ForgeDirection.UNKNOWN, new ItemStack(APPLE), 5));
        assertEquals(3, source.getStackInSlot(0).stackSize);
        assertEquals(64, destination.getStackInSlot(0).stackSize);
    }

    private static class SidedInventory extends InventoryBasic implements ISidedInventory {
        private final int[] slots;

        private SidedInventory(int size, int[] slots) {
            super("sided", false, size);
            this.slots = slots;
        }

        @Override
        public int[] getSlotsForFace(int side) {
            return slots;
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            return slot == 0 && stack.getItem() == APPLE;
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            return false;
        }
    }
}
