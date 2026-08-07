package me.desht.modularrouters.logic.energy;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RouterEnergyStorageTest {
    @Test
    public void preservesEnergyAboveReducedCapacity() {
        RouterEnergyStorage storage = new RouterEnergyStorage(1000, 200);
        storage.setTotalEnergyStored(900);

        storage.configure(400, 100);

        assertEquals(400, storage.getEnergyStored());
        assertEquals(500, storage.getExcessEnergy());

        storage.configure(1000, 200);

        assertEquals(900, storage.getEnergyStored());
        assertEquals(0, storage.getExcessEnergy());
    }

    @Test
    public void receiveExtractAndSimulationRespectTransferLimit() {
        RouterEnergyStorage storage = new RouterEnergyStorage(1000, 100);

        assertEquals(100, storage.receiveEnergy(500, true));
        assertEquals(0, storage.getEnergyStored());
        assertEquals(100, storage.receiveEnergy(500, false));
        assertEquals(100, storage.extractEnergy(500, true));
        assertEquals(100, storage.getEnergyStored());
        assertEquals(100, storage.extractEnergy(500, false));
        assertEquals(0, storage.getEnergyStored());
    }

    @Test
    public void roundTripsVisibleExcessAndCapacity() {
        RouterEnergyStorage source = new RouterEnergyStorage(1000, 250);
        source.setTotalEnergyStored(900);
        source.configure(400, 125);
        NBTTagCompound tag = source.writeToNBT(new NBTTagCompound());

        RouterEnergyStorage restored = new RouterEnergyStorage(0, 0);
        restored.readFromNBT(tag);

        assertEquals(400, restored.getEnergyStored());
        assertEquals(500, restored.getExcessEnergy());
        assertEquals(400, restored.getMaxEnergyStored());
        assertEquals(0, restored.getTransferRate());
    }

    @Test
    public void negativeConfigurationAndRequestsAreClampedToZero() {
        RouterEnergyStorage storage = new RouterEnergyStorage(-1, -1);

        assertEquals(0, storage.getMaxEnergyStored());
        assertEquals(0, storage.receiveEnergy(-100, false));
        assertEquals(0, storage.extractEnergy(-100, false));
    }
}
