package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.SenderModule1;
import me.desht.modularrouters.item.module.SenderModule2;
import me.desht.modularrouters.item.module.SenderModule3;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompiledSenderModuleTest {
    private static final ItemBase APPLE = new ItemBase("senderTestApple");

    @Test
    public void senderMk1IsDirectionalButNotExplicitlyTargeted() {
        SenderModule1 module = new SenderModule1();

        assertFalse(TargetedModule.class.isAssignableFrom(module.getClass()));
        assertTrue(module.isDirectional());
    }

    @Test
    public void senderMk2AndMk3UseOnlyExplicitTargets() {
        assertFalse(new SenderModule2().isDirectional());
        assertFalse(new SenderModule3().isDirectional());
    }

    @Test
    public void senderHonorsSidedInsertionAndPartialCapacity() {
        TileEntityItemRouter router = new TileEntityItemRouter();
        router.getBuffer().setInventorySlotContents(0, new ItemStack(APPLE, 5));
        DeniedSidedInventory denied = new DeniedSidedInventory();

        assertFalse(CompiledSenderModule1.insertIntoInventory(
                router, denied, ForgeDirection.NORTH, 5));
        assertEquals(5, router.getBufferItemStack().stackSize);

        InventoryBasic partial = new InventoryBasic("partial", false, 1);
        partial.setInventorySlotContents(0, new ItemStack(APPLE, 63));
        assertTrue(CompiledSenderModule1.insertIntoInventory(
                router, partial, ForgeDirection.UNKNOWN, 5));
        assertEquals(64, partial.getStackInSlot(0).stackSize);
        assertEquals(4, router.getBufferItemStack().stackSize);
    }

    @Test
    public void senderMk2IsBoundedWhileMk3IsUnlimited() {
        ModuleTarget target = new ModuleTarget(2, 13, 64, 10, ForgeDirection.WEST, "target");

        assertTrue(CompiledSenderModule2.isTargetLocationValid(
                2, 10, 64, 10, 16, target, true));
        assertFalse(CompiledSenderModule2.isTargetLocationValid(
                3, 10, 64, 10, 16, target, true));
        assertFalse(CompiledSenderModule2.isTargetLocationValid(
                2, 10, 64, 10, 4, target, true));
        assertFalse(CompiledSenderModule2.isTargetLocationValid(
                2, 10, 64, 10, 16, target, false));
        assertFalse(new CompiledSenderModule3(null, sender3Stack()).isRangeLimited());
        assertEquals(0xFF8000, new CompiledSenderModule2(null, sender2Stack()).getBeamColor());
    }

    private static ItemStack sender2Stack() {
        return new ItemStack(new me.desht.modularrouters.item.module.ItemModule(), 1,
                me.desht.modularrouters.item.module.ItemModule.ModuleType.SENDER2.ordinal());
    }

    private static ItemStack sender3Stack() {
        return new ItemStack(new me.desht.modularrouters.item.module.ItemModule(), 1,
                me.desht.modularrouters.item.module.ItemModule.ModuleType.SENDER3.ordinal());
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
            return false;
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            return true;
        }
    }
}
