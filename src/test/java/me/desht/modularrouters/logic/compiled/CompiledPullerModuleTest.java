package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CompiledPullerModuleTest {
    private static final ItemBase APPLE = new ItemBase("pullerTestApple");
    private static final ItemBase BREAD = new ItemBase("pullerTestBread");

    @Test
    public void pullerCompletesExistingBufferStackBeforeOtherItems() {
        TileEntityItemRouter router = new TileEntityItemRouter();
        router.getBuffer().setInventorySlotContents(0, new ItemStack(APPLE, 60));
        InventoryBasic source = new InventoryBasic("source", false, 2);
        source.setInventorySlotContents(0, new ItemStack(BREAD, 4));
        source.setInventorySlotContents(1, new ItemStack(APPLE, 4));

        ItemStack moved = puller().pull(source, ForgeDirection.UNKNOWN, router);

        assertEquals(APPLE, moved.getItem());
        assertEquals(1, moved.stackSize);
        assertEquals(61, router.getBufferItemStack().stackSize);
        assertEquals(4, source.getStackInSlot(0).stackSize);
        assertEquals(3, source.getStackInSlot(1).stackSize);
    }

    @Test
    public void pullerHonorsSidedExtractionDenial() {
        TileEntityItemRouter router = new TileEntityItemRouter();
        DeniedSidedInventory source = new DeniedSidedInventory();
        source.setInventorySlotContents(0, new ItemStack(APPLE, 4));

        assertNull(puller().pull(source, ForgeDirection.NORTH, router));
        assertTrue(router.isBufferEmpty());
        assertEquals(4, source.getStackInSlot(0).stackSize);
    }

    @Test
    public void mk2TargetRequiresSameLoadedWorldAndSquaredRange() {
        ModuleTarget target = new ModuleTarget(2, 13, 64, 10, ForgeDirection.WEST, "target");

        assertTrue(CompiledPullerModule2.isTargetLocationValid(
                2, 10, 64, 10, 16, target, true));
        assertFalse(CompiledPullerModule2.isTargetLocationValid(
                3, 10, 64, 10, 16, target, true));
        assertFalse(CompiledPullerModule2.isTargetLocationValid(
                2, 10, 64, 10, 4, target, true));
        assertFalse(CompiledPullerModule2.isTargetLocationValid(
                2, 10, 64, 10, 16, target, false));
    }

    @Test
    public void unboundMk2DoesNotFallBackToAdjacentInventory() {
        TileEntityItemRouter router = new TileEntityItemRouter() {
            @Override
            public ForgeDirection getAbsoluteFacing(Module.RelativeDirection direction) {
                return ForgeDirection.NORTH;
            }
        };
        ItemStack stack = new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.PULLER2.ordinal());

        assertFalse(new CompiledPullerModule2(router, stack).hasTarget());
    }

    private static TestPuller puller() {
        return new TestPuller(moduleStack());
    }

    private static ItemStack moduleStack() {
        return new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.PULLER1.ordinal());
    }

    private static class TestPuller extends CompiledPullerModule1 {
        private TestPuller(ItemStack stack) {
            super(null, stack);
        }

        private ItemStack pull(IInventory source, ForgeDirection side, TileEntityItemRouter router) {
            return transferToRouter(source, side, router);
        }
    }

    private static class DeniedSidedInventory extends InventoryBasic implements ISidedInventory {
        private DeniedSidedInventory() {
            super("denied", false, 1);
        }

        @Override
        public int[] getSlotsForFace(int side) {
            return new int[] { 0 };
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            return true;
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            return false;
        }
    }
}
