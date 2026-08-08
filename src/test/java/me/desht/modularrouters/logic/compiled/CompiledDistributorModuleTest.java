package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

public class CompiledDistributorModuleTest {
    @Test
    public void invalidStrategyFallsBackToRoundRobin() {
        ItemStack stack = distributorStack();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(CompiledDistributorModule.NBT_STRATEGY, 99);
        stack.setTagCompound(tag);

        assertEquals(CompiledDistributorModule.DistributionStrategy.ROUND_ROBIN,
                new CompiledDistributorModule(null, stack).getDistributionStrategy());
    }

    @Test
    public void choosesEligibleTargetsAccordingToEachStrategy() {
        boolean[] eligible = new boolean[] { true, false, true, false };

        assertEquals(2, CompiledDistributorModule.chooseTargetIndex(
                CompiledDistributorModule.DistributionStrategy.ROUND_ROBIN, 0, eligible, 1));
        assertEquals(0, CompiledDistributorModule.chooseTargetIndex(
                CompiledDistributorModule.DistributionStrategy.NEAREST_FIRST, 0, eligible, 1));
        assertEquals(2, CompiledDistributorModule.chooseTargetIndex(
                CompiledDistributorModule.DistributionStrategy.FURTHEST_FIRST, 0, eligible, 1));
    }

    @Test
    public void loadsAllOrderedMultiTargets() {
        ItemStack stack = distributorStack();
        LinkedHashSet<ModuleTarget> targets = new LinkedHashSet<ModuleTarget>(Arrays.asList(
                new ModuleTarget(0, 1, 2, 3, ForgeDirection.NORTH, "one"),
                new ModuleTarget(0, 4, 5, 6, ForgeDirection.SOUTH, "two"),
                new ModuleTarget(0, 7, 8, 9, ForgeDirection.UP, "three")));
        TargetedModule.setTargets(stack, targets);

        assertEquals(3, new CompiledDistributorModule(null, stack).getTargets().size());
        assertEquals(4, new CompiledDistributorModule(null, stack).getTargets().get(1).getX());
    }

    @Test
    public void usesOrangePushAndBluePullBeamColors() {
        CompiledDistributorModule pushing = new CompiledDistributorModule(null, distributorStack());
        ItemStack pullingStack = distributorStack();
        pullingStack.setTagCompound(new NBTTagCompound());
        pullingStack.getTagCompound().setBoolean(CompiledDistributorModule.NBT_PULLING, true);
        CompiledDistributorModule pulling = new CompiledDistributorModule(null, pullingStack);

        assertEquals(0xFF8000, pushing.getBeamColor());
        assertEquals(0x6080FF, pulling.getBeamColor());
    }

    private static ItemStack distributorStack() {
        return new ItemStack(new ItemModule(), 1,
                ItemModule.ModuleType.DISTRIBUTOR.ordinal());
    }
}
