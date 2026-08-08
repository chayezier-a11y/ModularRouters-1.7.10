package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TargetedModuleTest {
    @Test
    public void energyDistributorAcceptsEightTargets() {
        assertEquals(8, new EnergyDistributorModule().getMaxTargets());
    }

    @Test
    public void readsLegacySingleTargetIncludingDimension() {
        ItemStack stack = distributor();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("HasTarget", true);
        tag.setInteger("TargetX", 4);
        tag.setInteger("TargetY", 5);
        tag.setInteger("TargetZ", 6);
        tag.setInteger("TargetDim", -1);
        stack.setTagCompound(tag);

        ModuleTarget target = TargetedModule.getTargets(stack).iterator().next();

        assertEquals(-1, target.getDimension());
        assertEquals(4, target.getX());
        assertEquals(5, target.getY());
        assertEquals(6, target.getZ());
    }

    @Test
    public void storesAtMostEightDistinctTargetsInBindingOrder() {
        ItemStack stack = distributor();
        for (int i = 0; i < 9; i++) {
            TargetedModule.addTarget(stack,
                    new ModuleTarget(0, i, 64, 0, ForgeDirection.UP, "target"), 8);
        }

        Set<ModuleTarget> targets = TargetedModule.getTargets(stack);
        assertEquals(8, targets.size());
        Iterator<ModuleTarget> iterator = targets.iterator();
        for (int i = 0; i < 8; i++) assertEquals(i, iterator.next().getX());
    }

    @Test
    public void duplicateDoesNotConsumeCapacityAndToggleRemovesExistingTarget() {
        ItemStack stack = distributor();
        ModuleTarget target = new ModuleTarget(0, 1, 2, 3, ForgeDirection.NORTH, "target");

        assertTrue(TargetedModule.addTarget(stack, target, 8));
        assertFalse(TargetedModule.addTarget(stack,
                new ModuleTarget(0, 1, 2, 3, ForgeDirection.NORTH, "renamed"), 8));
        assertFalse(TargetedModule.toggleTarget(stack, target, 8));
        assertTrue(TargetedModule.getTargets(stack).isEmpty());
    }

    @Test
    public void multiTargetChangesDistinguishAddRemoveAndFull() {
        ItemStack stack = distributor();
        ModuleTarget first = new ModuleTarget(0, 1, 2, 3, ForgeDirection.NORTH, "first");

        assertEquals(TargetedModule.TargetChange.ADDED,
                TargetedModule.changeTarget(stack, first, 2));
        assertEquals(TargetedModule.TargetChange.REMOVED,
                TargetedModule.changeTarget(stack, first, 2));
        assertEquals(TargetedModule.TargetChange.ADDED,
                TargetedModule.changeTarget(stack, first, 2));
        assertEquals(TargetedModule.TargetChange.ADDED,
                TargetedModule.changeTarget(stack,
                        new ModuleTarget(0, 4, 5, 6, ForgeDirection.SOUTH, "second"), 2));
        assertEquals(TargetedModule.TargetChange.FULL,
                TargetedModule.changeTarget(stack,
                        new ModuleTarget(0, 7, 8, 9, ForgeDirection.UP, "third"), 2));
        assertEquals(2, TargetedModule.getTargets(stack).size());
    }

    private static ItemStack distributor() {
        return new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.ENERGY_DISTRIBUTOR.ordinal());
    }
}
