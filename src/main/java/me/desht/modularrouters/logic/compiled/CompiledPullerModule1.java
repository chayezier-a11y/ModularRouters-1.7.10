package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class CompiledPullerModule1 extends CompiledModule {
    public CompiledPullerModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferFull()) return false;
        ModuleTarget target = getTarget();
        if (!validateTarget(router, target)) return false;

        World world = router.getWorldObj();
        TileEntity tile = world.getTileEntity(target.getX(), target.getY(), target.getZ());
        if (!(tile instanceof IInventory)) return false;

        ItemStack transferred = transferToRouter((IInventory) tile, target.getFacing(), router);
        if (transferred == null) return false;
        playParticles(router, target, transferred);
        return true;
    }

    boolean validateTarget(TileEntityItemRouter router, ModuleTarget target) {
        World world = router.getWorldObj();
        return target != null && world != null && target.isSameWorld(world)
                && world.blockExists(target.getX(), target.getY(), target.getZ());
    }

    void playParticles(TileEntityItemRouter router, ModuleTarget target, ItemStack stack) {
    }
}
