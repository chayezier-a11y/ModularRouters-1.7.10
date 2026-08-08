package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;
import net.minecraft.init.Blocks;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompiledPlacerModuleTest {
    private static final ItemBlock PLACE_ITEM = new ItemBlock(Blocks.stone);

    @Test
    public void successfulPlacementConsumesOneBufferItem() {
        TestPlacer placer = new TestPlacer(true);
        TileEntityItemRouter router = routerWithBuffer(2);

        assertTrue(placer.execute(router));
        assertEquals(1, router.getBufferItemStack().stackSize);
    }

    @Test
    public void failedPlacementPreservesBufferItem() {
        TestPlacer placer = new TestPlacer(false);
        TileEntityItemRouter router = routerWithBuffer(2);

        assertFalse(placer.execute(router));
        assertEquals(2, router.getBufferItemStack().stackSize);
    }

    private static TileEntityItemRouter routerWithBuffer(int count) {
        TileEntityItemRouter router = new TileEntityItemRouter();
        router.getBuffer().setInventorySlotContents(0, new ItemStack(PLACE_ITEM, count));
        return router;
    }

    private static class TestPlacer extends CompiledPlacerModule {
        private final boolean result;

        private TestPlacer(boolean result) {
            super(null, new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.PLACER.ordinal()));
            this.result = result;
        }

        @Override
        protected boolean performPlacement(TileEntityItemRouter router, ItemStack stack) {
            return result;
        }
    }
}
