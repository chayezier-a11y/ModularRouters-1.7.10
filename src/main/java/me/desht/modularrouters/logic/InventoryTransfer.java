package me.desht.modularrouters.logic;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public final class InventoryTransfer {
    private InventoryTransfer() {
    }

    public static int insert(IInventory inventory, ForgeDirection side, ItemStack input, boolean simulate) {
        if (inventory == null || input == null || input.stackSize <= 0) return 0;

        int remaining = input.stackSize;
        for (int slot : slots(inventory, side)) {
            if (remaining <= 0) break;
            if (!canInsert(inventory, slot, input, side)) continue;

            ItemStack existing = inventory.getStackInSlot(slot);
            int limit = Math.min(inventory.getInventoryStackLimit(), input.getMaxStackSize());
            if (existing == null) {
                int moved = Math.min(limit, remaining);
                if (!simulate) {
                    ItemStack inserted = input.copy();
                    inserted.stackSize = moved;
                    inventory.setInventorySlotContents(slot, inserted);
                }
                remaining -= moved;
            } else if (matches(existing, input, false, false)) {
                int moved = Math.min(Math.max(0, limit - existing.stackSize), remaining);
                if (!simulate && moved > 0) existing.stackSize += moved;
                remaining -= moved;
            }
        }
        if (!simulate && remaining != input.stackSize) inventory.markDirty();
        return input.stackSize - remaining;
    }

    public static ItemStack extract(IInventory inventory, ForgeDirection side, ItemStack prototype,
                                    int amount, boolean simulate) {
        if (inventory == null || amount <= 0) return null;

        ItemStack result = null;
        int remaining = amount;
        for (int slot : slots(inventory, side)) {
            if (remaining <= 0) break;
            ItemStack existing = inventory.getStackInSlot(slot);
            ItemStack match = prototype == null ? result : prototype;
            if (existing == null || match != null && !matches(existing, match, false, false)
                    || !canExtract(inventory, slot, existing, side)) continue;

            int moved = Math.min(existing.stackSize, remaining);
            if (result == null) {
                result = existing.copy();
                result.stackSize = 0;
            }
            result.stackSize += moved;
            remaining -= moved;
            if (!simulate) inventory.decrStackSize(slot, moved);
        }
        if (!simulate && result != null) inventory.markDirty();
        return result;
    }

    public static int transfer(IInventory source, ForgeDirection sourceSide,
                               IInventory destination, ForgeDirection destinationSide,
                               ItemStack prototype, int amount) {
        ItemStack available = extract(source, sourceSide, prototype, amount, true);
        if (available == null) return 0;

        int accepted = insert(destination, destinationSide, available, true);
        if (accepted <= 0) return 0;

        ItemStack[] sourceSnapshot = snapshot(source);
        ItemStack[] destinationSnapshot = snapshot(destination);
        ItemStack extracted = extract(source, sourceSide, prototype, accepted, false);
        if (extracted == null || extracted.stackSize != accepted
                || insert(destination, destinationSide, extracted, false) != accepted) {
            restore(source, sourceSnapshot);
            restore(destination, destinationSnapshot);
            return 0;
        }
        return accepted;
    }

    public static int count(IInventory inventory, ForgeDirection side, ItemStack prototype,
                            boolean ignoreMetadata, boolean ignoreNbt) {
        if (inventory == null || prototype == null) return 0;
        int count = 0;
        for (int slot : slots(inventory, side)) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null && matches(stack, prototype, ignoreMetadata, ignoreNbt)) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    private static boolean matches(ItemStack first, ItemStack second,
                                   boolean ignoreMetadata, boolean ignoreNbt) {
        return first.getItem() == second.getItem()
                && (ignoreMetadata || first.getMetadata() == second.getMetadata())
                && (ignoreNbt || ItemStack.areItemStackTagsEqual(first, second));
    }

    private static int[] slots(IInventory inventory, ForgeDirection side) {
        if (inventory instanceof ISidedInventory && side != null && side != ForgeDirection.UNKNOWN) {
            return ((ISidedInventory) inventory).getSlotsForFace(side.ordinal());
        }
        int[] slots = new int[inventory.getSizeInventory()];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        return slots;
    }

    private static boolean canInsert(IInventory inventory, int slot, ItemStack stack, ForgeDirection side) {
        if (slot < 0 || slot >= inventory.getSizeInventory() || !inventory.isItemValidForSlot(slot, stack)) {
            return false;
        }
        return !(inventory instanceof ISidedInventory) || side == null || side == ForgeDirection.UNKNOWN
                || ((ISidedInventory) inventory).canInsertItem(slot, stack, side.ordinal());
    }

    private static boolean canExtract(IInventory inventory, int slot, ItemStack stack, ForgeDirection side) {
        if (slot < 0 || slot >= inventory.getSizeInventory()) return false;
        return !(inventory instanceof ISidedInventory) || side == null || side == ForgeDirection.UNKNOWN
                || ((ISidedInventory) inventory).canExtractItem(slot, stack, side.ordinal());
    }

    private static ItemStack[] snapshot(IInventory inventory) {
        ItemStack[] snapshot = new ItemStack[inventory.getSizeInventory()];
        for (int i = 0; i < snapshot.length; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            snapshot[i] = stack == null ? null : stack.copy();
        }
        return snapshot;
    }

    private static void restore(IInventory inventory, ItemStack[] snapshot) {
        for (int i = 0; i < snapshot.length; i++) {
            inventory.setInventorySlotContents(i, snapshot[i] == null ? null : snapshot[i].copy());
        }
        inventory.markDirty();
    }
}
