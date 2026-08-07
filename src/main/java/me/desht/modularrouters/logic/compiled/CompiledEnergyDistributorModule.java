package me.desht.modularrouters.logic.compiled;

import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.common.network.NetworkRegistry;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.energy.EnergyTransfer;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class CompiledEnergyDistributorModule extends CompiledModule {
    private static final int BEAM_COLOR = 0xE04040;

    public CompiledEnergyDistributorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        return new ArrayList<ModuleTarget>(TargetedModule.getTargets(stack));
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        List<TargetReceiver> receivers = new ArrayList<TargetReceiver>();
        for (ModuleTarget target : getTargets()) {
            if (!isTargetValid(router, target)) continue;
            IEnergyReceiver receiver = resolveReceiver(router, target);
            if (receiver != null) receivers.add(new TargetReceiver(target, receiver));
        }
        if (receivers.isEmpty()) return false;

        int share = router.getEnergyStored(ForgeDirection.UNKNOWN) / receivers.size();
        if (share <= 0) return false;

        int total = 0;
        boolean showBeams = router.getUpgradeCount(UpgradeType.MUFFLER) < 2;
        for (TargetReceiver entry : receivers) {
            ModuleTarget target = entry.target;
            if (target.getFacing() == ForgeDirection.UNKNOWN) continue;
            int sent = EnergyTransfer.move(router, target.getFacing().getOpposite(),
                    entry.receiver, target.getFacing(), Math.min(share, router.getEnergyXferRate()));
            total += sent;
            if (sent > 0 && showBeams) addEnergyBeam(router, target);
        }
        return total > 0;
    }

    protected boolean isTargetValid(TileEntityItemRouter router, ModuleTarget target) {
        World world = router.getWorldObj();
        return world != null && isTargetLocationValid(world.provider.dimensionId,
                router.xCoord, router.yCoord, router.zCoord, getRangeSquared(), target,
                world.blockExists(target.getX(), target.getY(), target.getZ()));
    }

    protected IEnergyReceiver resolveReceiver(TileEntityItemRouter router, ModuleTarget target) {
        World world = router.getWorldObj();
        if (world == null) return null;
        TileEntity tile = world.getTileEntity(target.getX(), target.getY(), target.getZ());
        if (!(tile instanceof IEnergyReceiver)) return null;
        IEnergyReceiver receiver = (IEnergyReceiver) tile;
        return receiver.canConnectEnergy(target.getFacing()) ? receiver : null;
    }

    protected void addEnergyBeam(TileEntityItemRouter router, ModuleTarget target) {
        if (ModularRouters.network == null || router.getWorldObj() == null) return;
        ModularRouters.network.sendToAllAround(
                new ParticleBeamMessage(router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5,
                        target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                        BEAM_COLOR, 0.08f),
                new NetworkRegistry.TargetPoint(router.getWorldObj().provider.dimensionId,
                        router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5, 64.0));
    }

    static boolean isTargetLocationValid(int routerDimension, int routerX, int routerY, int routerZ,
                                         int rangeSquared, ModuleTarget target, boolean loaded) {
        if (target == null || !loaded || target.getDimension() != routerDimension) return false;
        long dx = (long) target.getX() - routerX;
        long dy = (long) target.getY() - routerY;
        long dz = (long) target.getZ() - routerZ;
        return dx * dx + dy * dy + dz * dz <= rangeSquared;
    }

    private static class TargetReceiver {
        final ModuleTarget target;
        final IEnergyReceiver receiver;

        TargetReceiver(ModuleTarget target, IEnergyReceiver receiver) {
            this.target = target;
            this.receiver = receiver;
        }
    }
}
