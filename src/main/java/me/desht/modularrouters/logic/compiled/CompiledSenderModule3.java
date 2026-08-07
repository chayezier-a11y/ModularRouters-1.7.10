package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class CompiledSenderModule3 extends CompiledModule {
    public CompiledSenderModule3(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean hasTarget() {
        return hasExplicitTarget();
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferEmpty()) return false;

        ItemStack toSend = router.peekBuffer(getItemsPerTick(router));
        if (toSend == null || getFilter().rejectItem(toSend)) return false;

        ModuleTarget target = getExplicitTarget();
        if (target == null) return false;

        int x = target.getX();
        int y = target.getY();
        int z = target.getZ();

        if (router.getWorldObj().getTileEntity(x, y, z) instanceof IInventory) {
            IInventory inv = (IInventory) router.getWorldObj().getTileEntity(x, y, z);
            return CompiledSenderModule1.insertIntoInventory(router, inv, getItemsPerTick(router));
        }
        return false;
    }
}
