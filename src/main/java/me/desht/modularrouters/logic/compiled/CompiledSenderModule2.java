package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class CompiledSenderModule2 extends CompiledModule {
    public CompiledSenderModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferEmpty()) return false;

        ItemStack toSend = router.peekBuffer(getItemsPerTick(router));
        if (toSend == null || getFilter().rejectItem(toSend)) return false;

        // Try bound target first
        ModuleTarget target = getExplicitTarget();
        if (target != null) {
            return tryInsertAt(router, target.getX(), target.getY(), target.getZ());
        }

        // Fall back to scanning forward
        int range = getRange();
        ForgeDirection facing = getAbsoluteDirection(router);
        for (int d = 1; d <= range; d++) {
            int x = router.xCoord + facing.offsetX * d;
            int y = router.yCoord + facing.offsetY * d;
            int z = router.zCoord + facing.offsetZ * d;
            if (tryInsertAt(router, x, y, z)) return true;
        }
        return false;
    }

    private boolean tryInsertAt(TileEntityItemRouter router, int x, int y, int z) {
        if (router.getWorldObj().getTileEntity(x, y, z) instanceof IInventory) {
            IInventory inv = (IInventory) router.getWorldObj().getTileEntity(x, y, z);
            return CompiledSenderModule1.insertIntoInventory(router, inv, getItemsPerTick(router));
        }
        return false;
    }
}
