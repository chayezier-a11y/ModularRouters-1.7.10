package me.desht.modularrouters.logic.compiled;

import cofh.api.energy.IEnergyReceiver;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.energy.EnergyTransfer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class CompiledEnergyOutputModule extends CompiledModule {
    public CompiledEnergyOutputModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        ModuleTarget target = getEnergyTarget();
        IEnergyReceiver receiver = resolveReceiver(router, target);
        if (target == null || receiver == null || target.getFacing() == ForgeDirection.UNKNOWN) return false;
        return EnergyTransfer.move(router, target.getFacing().getOpposite(),
                receiver, target.getFacing(), router.getEnergyXferRate()) > 0;
    }

    protected ModuleTarget getEnergyTarget() {
        return getTarget();
    }

    protected IEnergyReceiver resolveReceiver(TileEntityItemRouter router, ModuleTarget target) {
        World world = router.getWorldObj();
        if (target == null || world == null || !target.isSameWorld(world)
                || !world.blockExists(target.getX(), target.getY(), target.getZ())) return null;
        TileEntity tile = world.getTileEntity(target.getX(), target.getY(), target.getZ());
        if (!(tile instanceof IEnergyReceiver)) return null;
        IEnergyReceiver receiver = (IEnergyReceiver) tile;
        return receiver.canConnectEnergy(target.getFacing()) ? receiver : null;
    }
}
