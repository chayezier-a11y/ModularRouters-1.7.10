package me.desht.modularrouters.logic.energy;

import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnergyTransferTest {
    @Test
    public void commitsOnlyWhatReceiverAccepts() {
        FakeEndpoint source = new FakeEndpoint(500, 1000);
        FakeEndpoint target = new FakeEndpoint(0, 120);

        int moved = EnergyTransfer.move(source, ForgeDirection.EAST,
                target, ForgeDirection.WEST, 300);

        assertEquals(120, moved);
        assertEquals(380, source.energy);
        assertEquals(120, target.energy);
    }

    @Test
    public void rejectedAndInvalidTransfersDoNotDrainSource() {
        FakeEndpoint source = new FakeEndpoint(500, 1000);
        FakeEndpoint target = new FakeEndpoint(0, 0);

        assertEquals(0, EnergyTransfer.move(source, ForgeDirection.EAST,
                target, ForgeDirection.WEST, 300));
        assertEquals(0, EnergyTransfer.move(source, ForgeDirection.UNKNOWN,
                target, ForgeDirection.WEST, 300));
        assertEquals(500, source.energy);
    }

    @Test
    public void restoresCommitShortfallToBidirectionalSource() {
        FakeEndpoint source = new FakeEndpoint(500, 1000);
        FakeEndpoint target = new ShortCommitEndpoint(0, 1000, 60);

        assertEquals(60, EnergyTransfer.move(source, ForgeDirection.EAST,
                target, ForgeDirection.WEST, 100));
        assertEquals(440, source.energy);
        assertEquals(60, target.energy);
    }

    @Test
    public void transfersBothDirectionsBetweenEndpointAndContainerItem() {
        FakeEndpoint endpoint = new FakeEndpoint(200, 1000);
        FakeEnergyItem item = new FakeEnergyItem();
        ItemStack stack = new ItemStack(item);

        assertEquals(80, EnergyTransfer.moveToItem(endpoint, ForgeDirection.UP,
                item, stack, 80));
        assertEquals(120, endpoint.energy);
        assertEquals(80, item.energy);

        assertEquals(50, EnergyTransfer.moveFromItem(item, stack,
                endpoint, ForgeDirection.UP, 50));
        assertEquals(170, endpoint.energy);
        assertEquals(30, item.energy);
    }

    private static class FakeEndpoint implements IEnergyHandler {
        int energy;
        final int capacity;

        FakeEndpoint(int energy, int capacity) {
            this.energy = energy;
            this.capacity = capacity;
        }

        @Override public boolean canConnectEnergy(ForgeDirection from) { return from != ForgeDirection.UNKNOWN; }
        @Override public int receiveEnergy(ForgeDirection from, int amount, boolean simulate) {
            int accepted = Math.min(Math.max(0, amount), capacity - energy);
            if (!simulate) energy += accepted;
            return accepted;
        }
        @Override public int extractEnergy(ForgeDirection from, int amount, boolean simulate) {
            int extracted = Math.min(Math.max(0, amount), energy);
            if (!simulate) energy -= extracted;
            return extracted;
        }
        @Override public int getEnergyStored(ForgeDirection from) { return energy; }
        @Override public int getMaxEnergyStored(ForgeDirection from) { return capacity; }
    }

    private static class ShortCommitEndpoint extends FakeEndpoint {
        private final int commitLimit;

        ShortCommitEndpoint(int energy, int capacity, int commitLimit) {
            super(energy, capacity);
            this.commitLimit = commitLimit;
        }

        @Override public int receiveEnergy(ForgeDirection from, int amount, boolean simulate) {
            return super.receiveEnergy(from, simulate ? amount : Math.min(amount, commitLimit), simulate);
        }
    }

    private static class FakeEnergyItem extends Item implements IEnergyContainerItem {
        int energy;

        @Override public int receiveEnergy(ItemStack container, int amount, boolean simulate) {
            int accepted = Math.min(Math.max(0, amount), 1000 - energy);
            if (!simulate) energy += accepted;
            return accepted;
        }
        @Override public int extractEnergy(ItemStack container, int amount, boolean simulate) {
            int extracted = Math.min(Math.max(0, amount), energy);
            if (!simulate) energy -= extracted;
            return extracted;
        }
        @Override public int getEnergyStored(ItemStack container) { return energy; }
        @Override public int getMaxEnergyStored(ItemStack container) { return 1000; }
    }
}
