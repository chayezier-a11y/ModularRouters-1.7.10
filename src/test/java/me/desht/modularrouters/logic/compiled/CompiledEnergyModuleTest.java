package me.desht.modularrouters.logic.compiled;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyReceiver;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompiledEnergyModuleTest {
    @Test
    public void energyOutputTransfersUpToReceiverAcceptance() {
        TestRouter router = routerWithEnergy(200, 100, 0);
        FakeEndpoint receiver = new FakeEndpoint(0, 60);
        ModuleTarget target = target(0, 1, 64, 0);
        TestOutput module = new TestOutput(outputStack(), target, receiver);

        assertTrue(module.execute(router));
        assertEquals(140, router.getEnergyStored(ForgeDirection.UNKNOWN));
        assertEquals(60, receiver.energy);
    }

    @Test
    public void distributorSplitsStoredEnergyAcrossValidTargetsAndAddsBeams() {
        TestRouter router = routerWithEnergy(600, 300, 0);
        ModuleTarget first = target(0, 2, 64, 0);
        ModuleTarget second = target(0, 3, 64, 0);
        ItemStack stack = distributorStack(first, second);
        TestDistributor module = new TestDistributor(stack);

        assertTrue(module.execute(router));
        assertEquals(0, router.getEnergyStored(ForgeDirection.UNKNOWN));
        assertEquals(300, module.endpoint(first).energy);
        assertEquals(300, module.endpoint(second).energy);
        assertEquals(2, module.beams);
    }

    @Test
    public void distributorIgnoresInvalidAndNonEnergyTargets() {
        TestRouter router = routerWithEnergy(200, 200, 0);
        ModuleTarget valid = target(0, 2, 64, 0);
        ModuleTarget invalid = target(0, 99, 64, 0);
        ModuleTarget nonEnergy = target(0, 4, 64, 0);
        TestDistributor module = new TestDistributor(distributorStack(valid, invalid, nonEnergy));
        module.nonEnergyX = 4;

        assertTrue(module.execute(router));
        assertEquals(200, module.endpoint(valid).energy);
        assertEquals(0, module.endpoint(invalid).energy);
        assertEquals(0, module.endpoint(nonEnergy).energy);
    }

    @Test
    public void distributorReturnsFalseWhenNothingAcceptsEnergy() {
        TestRouter router = routerWithEnergy(100, 100, 0);
        ModuleTarget target = target(0, 2, 64, 0);
        TestDistributor module = new TestDistributor(distributorStack(target));
        module.endpoint(target).capacity = 0;

        assertFalse(module.execute(router));
        assertEquals(100, router.getEnergyStored(ForgeDirection.UNKNOWN));
    }

    @Test
    public void twoMufflersSuppressDistributorBeams() {
        TestRouter router = routerWithEnergy(100, 100, 2);
        ModuleTarget target = target(0, 2, 64, 0);
        TestDistributor module = new TestDistributor(distributorStack(target));

        assertTrue(module.execute(router));
        assertEquals(0, module.beams);
    }

    @Test
    public void locationValidationRejectsDimensionRangeAndUnloadedChunk() {
        ModuleTarget wrongDimension = target(1, 1, 64, 0);
        ModuleTarget outOfRange = target(0, 9, 64, 0);
        ModuleTarget valid = target(0, 8, 64, 0);

        assertFalse(CompiledEnergyDistributorModule.isTargetLocationValid(
                0, 0, 64, 0, 64, wrongDimension, true));
        assertFalse(CompiledEnergyDistributorModule.isTargetLocationValid(
                0, 0, 64, 0, 64, outOfRange, true));
        assertFalse(CompiledEnergyDistributorModule.isTargetLocationValid(
                0, 0, 64, 0, 64, valid, false));
        assertTrue(CompiledEnergyDistributorModule.isTargetLocationValid(
                0, 0, 64, 0, 64, valid, true));
    }

    private static TestRouter routerWithEnergy(int energy, int transfer, int mufflers) {
        TestRouter router = new TestRouter(mufflers);
        router.getEnergyStorage().configure(1000, transfer);
        router.getEnergyStorage().setTotalEnergyStored(energy);
        return router;
    }

    private static ItemStack outputStack() {
        return new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.ENERGY_OUTPUT.ordinal());
    }

    private static ItemStack distributorStack(ModuleTarget... targets) {
        ItemStack stack = new ItemStack(new ItemModule(), 1,
                ItemModule.ModuleType.ENERGY_DISTRIBUTOR.ordinal());
        TargetedModule.setTargets(stack, Arrays.asList(targets));
        return stack;
    }

    private static ModuleTarget target(int dimension, int x, int y, int z) {
        return new ModuleTarget(dimension, x, y, z, ForgeDirection.WEST, "target");
    }

    private static class TestOutput extends CompiledEnergyOutputModule {
        private final ModuleTarget target;
        private final IEnergyReceiver receiver;

        TestOutput(ItemStack stack, ModuleTarget target, IEnergyReceiver receiver) {
            super(null, stack);
            this.target = target;
            this.receiver = receiver;
        }

        @Override protected ModuleTarget getEnergyTarget() { return target; }
        @Override protected IEnergyReceiver resolveReceiver(TileEntityItemRouter router, ModuleTarget target) {
            return receiver;
        }
    }

    private static class TestDistributor extends CompiledEnergyDistributorModule {
        private final Map<ModuleTarget, FakeEndpoint> endpoints = new HashMap<ModuleTarget, FakeEndpoint>();
        int beams;
        int nonEnergyX = Integer.MIN_VALUE;

        TestDistributor(ItemStack stack) {
            super(null, stack);
            for (ModuleTarget target : TargetedModule.getTargets(stack)) {
                endpoints.put(target, new FakeEndpoint(0, 1000));
            }
        }

        FakeEndpoint endpoint(ModuleTarget target) { return endpoints.get(target); }
        @Override protected boolean isTargetValid(TileEntityItemRouter router, ModuleTarget target) {
            return target.getX() != 99;
        }
        @Override protected IEnergyReceiver resolveReceiver(TileEntityItemRouter router, ModuleTarget target) {
            return target.getX() == nonEnergyX ? null : endpoint(target);
        }
        @Override protected void addEnergyBeam(TileEntityItemRouter router, ModuleTarget target) { beams++; }
    }

    private static class TestRouter extends TileEntityItemRouter {
        private final int mufflers;

        TestRouter(int mufflers) { this.mufflers = mufflers; }
        @Override public int getUpgradeCount(UpgradeType type) {
            return type == UpgradeType.MUFFLER ? mufflers : super.getUpgradeCount(type);
        }
    }

    private static class FakeEndpoint implements IEnergyHandler {
        int energy;
        int capacity;

        FakeEndpoint(int energy, int capacity) { this.energy = energy; this.capacity = capacity; }
        @Override public boolean canConnectEnergy(ForgeDirection from) { return true; }
        @Override public int receiveEnergy(ForgeDirection from, int amount, boolean simulate) {
            int accepted = Math.min(Math.max(0, amount), capacity - energy);
            if (!simulate) energy += accepted;
            return accepted;
        }
        @Override public int extractEnergy(ForgeDirection from, int amount, boolean simulate) { return 0; }
        @Override public int getEnergyStored(ForgeDirection from) { return energy; }
        @Override public int getMaxEnergyStored(ForgeDirection from) { return capacity; }
    }
}
