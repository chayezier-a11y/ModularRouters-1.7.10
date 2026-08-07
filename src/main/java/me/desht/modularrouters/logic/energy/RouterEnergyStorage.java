package me.desht.modularrouters.logic.energy;

import cofh.api.energy.IEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;

public class RouterEnergyStorage implements IEnergyStorage {
    private static final String NBT_ENERGY = "Energy";
    private static final String NBT_CAPACITY = "Capacity";
    private static final String NBT_EXCESS = "Excess";

    private int energy;
    private int excess;
    private int capacity;
    private int maxTransfer;

    public RouterEnergyStorage(int capacity, int maxTransfer) {
        this.capacity = nonNegative(capacity);
        this.maxTransfer = nonNegative(maxTransfer);
    }

    public void configure(int capacity, int maxTransfer) {
        long total = (long) energy + excess;
        this.capacity = nonNegative(capacity);
        this.maxTransfer = nonNegative(maxTransfer);
        energy = (int) Math.min(total, this.capacity);
        excess = clampToInt(total - energy);
    }

    public int getTransferRate() {
        return maxTransfer;
    }

    public int getExcessEnergy() {
        return excess;
    }

    public int getTotalEnergyStored() {
        return clampToInt((long) energy + excess);
    }

    public void setTotalEnergyStored(int totalEnergy) {
        int total = nonNegative(totalEnergy);
        energy = Math.min(total, capacity);
        excess = total - energy;
    }

    public boolean consumeEnergy(int amount) {
        int requested = nonNegative(amount);
        if (energy < requested) return false;
        energy -= requested;
        return true;
    }

    public RouterEnergyStorage readFromNBT(NBTTagCompound tag) {
        capacity = nonNegative(tag.getInteger(NBT_CAPACITY));
        long loadedEnergy = nonNegative(tag.getInteger(NBT_ENERGY));
        long loadedExcess = nonNegative(tag.getInteger(NBT_EXCESS));
        energy = (int) Math.min(loadedEnergy, capacity);
        excess = clampToInt(loadedExcess + loadedEnergy - energy);
        return this;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setInteger(NBT_ENERGY, energy);
        tag.setInteger(NBT_CAPACITY, capacity);
        tag.setInteger(NBT_EXCESS, excess);
        return tag;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.min(capacity - energy,
                Math.min(maxTransfer, nonNegative(maxReceive)));
        if (!simulate) energy += received;
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(energy,
                Math.min(maxTransfer, nonNegative(maxExtract)));
        if (!simulate) energy -= extracted;
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    private static int nonNegative(int value) {
        return Math.max(0, value);
    }

    private static int clampToInt(long value) {
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, value));
    }
}
