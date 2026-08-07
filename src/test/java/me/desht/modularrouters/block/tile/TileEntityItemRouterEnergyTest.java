package me.desht.modularrouters.block.tile;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.registry.GameRegistry;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TileEntityItemRouterEnergyTest {
    private int oldCapacity;
    private int oldTransfer;

    @BeforeClass
    public static void registerTileEntityForNbtTests() {
        GameRegistry.registerTileEntity(TileEntityItemRouter.class, "modularrouters_test_item_router");
    }

    @Before
    public void setConfig() {
        oldCapacity = Config.fePerEnergyUpgrade;
        oldTransfer = Config.feXferPerEnergyUpgrade;
        Config.fePerEnergyUpgrade = 500;
        Config.feXferPerEnergyUpgrade = 100;
    }

    @After
    public void restoreConfig() {
        Config.fePerEnergyUpgrade = oldCapacity;
        Config.feXferPerEnergyUpgrade = oldTransfer;
    }

    @Test
    public void compilesEnergyUpgradeCapacityAndTransferRate() {
        TileEntityItemRouter router = routerWithEnergyUpgrades(2);

        assertEquals(1000, router.getMaxEnergyStored(ForgeDirection.NORTH));
        assertEquals(200, router.getEnergyXferRate());
        assertTrue(router.canConnectEnergy(ForgeDirection.NORTH));
    }

    @Test
    public void externalRfRemainsBidirectionalForEveryBufferDirection() {
        TileEntityItemRouter router = routerWithEnergyUpgrades(1);

        for (TileEntityItemRouter.EnergyDirection direction : TileEntityItemRouter.EnergyDirection.values()) {
            router.setEnergyDirection(direction);
            assertEquals(100, router.receiveEnergy(ForgeDirection.NORTH, 100, false));
            assertEquals(100, router.extractEnergy(ForgeDirection.NORTH, 100, false));
        }
    }

    @Test
    public void roundTripsEnergyCapacityExcessAndDirection() {
        TileEntityItemRouter router = routerWithEnergyUpgrades(2);
        router.receiveEnergy(ForgeDirection.NORTH, 200, false);
        router.setEnergyDirection(TileEntityItemRouter.EnergyDirection.TO_ROUTER);
        NBTTagCompound tag = new NBTTagCompound();
        router.writeToNBT(tag);

        TileEntityItemRouter restored = new TileEntityItemRouter();
        restored.readFromNBT(tag);

        assertEquals(200, restored.getEnergyStored(ForgeDirection.NORTH));
        assertEquals(1000, restored.getMaxEnergyStored(ForgeDirection.NORTH));
        assertEquals(TileEntityItemRouter.EnergyDirection.TO_ROUTER, restored.getEnergyDirection());
    }

    @Test
    public void directionControlsBufferEnergyItem() {
        TileEntityItemRouter router = routerWithEnergyUpgrades(1);
        FakeEnergyItem item = new FakeEnergyItem(250);
        ItemStack stack = new ItemStack(item);
        router.getBuffer().setInventorySlotContents(0, stack);

        router.setEnergyDirection(TileEntityItemRouter.EnergyDirection.TO_ROUTER);
        router.transferBufferEnergy();
        assertEquals(100, router.getEnergyStored(ForgeDirection.UNKNOWN));
        assertEquals(150, item.energy);

        router.setEnergyDirection(TileEntityItemRouter.EnergyDirection.FROM_ROUTER);
        router.transferBufferEnergy();
        assertEquals(0, router.getEnergyStored(ForgeDirection.UNKNOWN));
        assertEquals(250, item.energy);

        router.setEnergyDirection(TileEntityItemRouter.EnergyDirection.NONE);
        router.transferBufferEnergy();
        assertEquals(0, router.getEnergyStored(ForgeDirection.UNKNOWN));
        assertEquals(250, item.energy);
    }

    @Test
    public void moduleExecutesAndPaysOnlyWhenFullCostIsAvailable() {
        TileEntityItemRouter router = routerWithEnergyUpgrades(1);
        ItemStack moduleStack = new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.CREATIVE.ordinal());
        Module original = ItemModule.getModule(moduleStack);
        ItemModule.registerSubItem(ItemModule.ModuleType.CREATIVE, new CostModule());
        try {
            FakeCompiledModule module = new FakeCompiledModule(moduleStack);

            assertFalse(router.tryExecuteEnergyModule(module));
            assertEquals(0, module.executions);

            router.receiveEnergy(ForgeDirection.NORTH, 100, false);
            router.receiveEnergy(ForgeDirection.NORTH, 100, false);
            assertTrue(router.tryExecuteEnergyModule(module));
            assertEquals(1, module.executions);
            assertEquals(50, router.getEnergyStored(ForgeDirection.NORTH));
        } finally {
            ItemModule.registerSubItem(ItemModule.ModuleType.CREATIVE, original);
        }
    }

    private static TileEntityItemRouter routerWithEnergyUpgrades(int count) {
        TileEntityItemRouter router = new TileEntityItemRouter();
        router.getUpgrades().setInventorySlotContents(0,
                new ItemStack(new ItemUpgrade(), count, ItemUpgrade.UpgradeType.ENERGY.ordinal()));
        router.compileUpgrades();
        return router;
    }

    private static class CostModule extends Module {
        @Override public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) { return new FakeCompiledModule(stack); }
        @Override public IRecipe getRecipe() { return null; }
        @Override public int getEnergyCost(ItemStack stack) { return 150; }
    }

    private static class FakeCompiledModule extends CompiledModule {
        int executions;

        FakeCompiledModule(ItemStack stack) { super(null, stack); }
        @Override public boolean hasTarget() { return true; }
        @Override public boolean execute(TileEntityItemRouter router) { executions++; return true; }
    }

    private static class FakeEnergyItem extends Item implements IEnergyContainerItem {
        int energy;

        FakeEnergyItem(int energy) { this.energy = energy; }
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
