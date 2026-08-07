package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.FluidModule1;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public class CompiledFluidModule1 extends CompiledModule {
    public static final String NBT_FORCE_EMPTY = "ForceEmpty";
    public static final String NBT_MAX_TRANSFER = "MaxTransfer";
    public static final String NBT_FLUID_DIRECTION = "FluidDir";
    public static final String NBT_REGULATE_ABSOLUTE = "RegulateAbsolute";

    private final int maxTransfer;
    private final FluidModule1.FluidDirection fluidDir;
    private final boolean forceEmpty;
    private final boolean regulateAbsolute;

    public CompiledFluidModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        NBTTagCompound compound = ModuleHelper.validateNBT(stack);
        if (!compound.hasKey(NBT_MAX_TRANSFER)) compound.setInteger(NBT_MAX_TRANSFER, 1000);
        if (!compound.hasKey(NBT_FLUID_DIRECTION)) compound.setByte(NBT_FLUID_DIRECTION,
                (byte) FluidModule1.FluidDirection.IN.ordinal());
        if (!compound.hasKey(NBT_FORCE_EMPTY)) compound.setBoolean(NBT_FORCE_EMPTY, false);
        if (!compound.hasKey(NBT_REGULATE_ABSOLUTE)) compound.setBoolean(NBT_REGULATE_ABSOLUTE, false);
        maxTransfer = Math.max(0, compound.getInteger(NBT_MAX_TRANSFER));
        int direction = compound.getByte(NBT_FLUID_DIRECTION);
        fluidDir = direction >= 0 && direction < FluidModule1.FluidDirection.values().length
                ? FluidModule1.FluidDirection.values()[direction] : FluidModule1.FluidDirection.IN;
        forceEmpty = compound.getBoolean(NBT_FORCE_EMPTY);
        regulateAbsolute = compound.getBoolean(NBT_REGULATE_ABSOLUTE);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        ItemStack bufferStack = router.getBufferItemStack();
        if (bufferStack == null) return false;

        ForgeDirection facing = getAbsoluteDirection(router);
        int x = router.xCoord + facing.offsetX;
        int y = router.yCoord + facing.offsetY;
        int z = router.zCoord + facing.offsetZ;

        IFluidHandler tank = getFluidHandlerAt(router, x, y, z);
        if (tank == null) return false;

        if (fluidDir == FluidModule1.FluidDirection.IN) {
            return transferFluidIn(router, bufferStack, tank);
        } else {
            return transferFluidOut(router, bufferStack, tank);
        }
    }

    private boolean transferFluidIn(TileEntityItemRouter router, ItemStack bufferStack, IFluidHandler tank) {
        if (getRegulationAmount() > 0
                && getFluidAmount(tank) <= regulationThreshold(tank)) return false;
        FluidStack drained = tank.drain(ForgeDirection.UNKNOWN, transferLimit(router), false);
        if (drained == null || drained.amount == 0) return false;

        if (getFilter().rejectItem(new ItemStack(drained.getFluid().getBlock()))) return false;

        int maxTransfer = router.getCurrentFluidTransferAllowance(FluidModule1.FluidDirection.IN);
        int toTransfer = Math.min(drained.amount, maxTransfer);

        FluidStack toDrain = tank.drain(ForgeDirection.UNKNOWN, toTransfer, true);
        if (toDrain != null && toDrain.amount > 0) {
            int filled = fillItem(bufferStack, toDrain, true);
            if (filled > 0) {
                FluidStack finalDrain = tank.drain(ForgeDirection.UNKNOWN, filled, true);
                fillItem(bufferStack, finalDrain, false);
                router.transferredFluid(filled, FluidModule1.FluidDirection.IN);
                return true;
            }
        }
        return false;
    }

    private boolean transferFluidOut(TileEntityItemRouter router, ItemStack bufferStack, IFluidHandler tank) {
        if (getRegulationAmount() > 0
                && getFluidAmount(tank) >= regulationThreshold(tank)) return false;
        FluidStack drainedFromItem = drainItem(bufferStack, transferLimit(router), true);
        if (drainedFromItem == null || drainedFromItem.amount == 0) return false;

        int maxTransfer = router.getCurrentFluidTransferAllowance(FluidModule1.FluidDirection.OUT);
        int toTransfer = Math.min(drainedFromItem.amount, maxTransfer);

        int filled = tank.fill(ForgeDirection.UNKNOWN, new FluidStack(drainedFromItem.getFluid(), toTransfer), false);
        if (filled > 0) {
            FluidStack actualDrain = drainItem(bufferStack, filled, false);
            if (actualDrain != null) {
                tank.fill(ForgeDirection.UNKNOWN, actualDrain, true);
                router.transferredFluid(filled, FluidModule1.FluidDirection.OUT);
                return true;
            }
        }
        return false;
    }

    private IFluidHandler getFluidHandlerAt(TileEntityItemRouter router, int x, int y, int z) {
        if (router.getWorldObj().getTileEntity(x, y, z) instanceof IFluidHandler) {
            return (IFluidHandler) router.getWorldObj().getTileEntity(x, y, z);
        }
        return null;
    }

    private int fillItem(ItemStack stack, FluidStack fluid, boolean simulate) {
        if (stack.getItem() instanceof IFluidContainerItem) {
            return ((IFluidContainerItem) stack.getItem()).fill(stack, fluid, simulate);
        }
        return 0;
    }

    private FluidStack drainItem(ItemStack stack, int maxDrain, boolean simulate) {
        if (stack.getItem() instanceof IFluidContainerItem) {
            return ((IFluidContainerItem) stack.getItem()).drain(stack, maxDrain, simulate);
        }
        return null;
    }

    private int transferLimit(TileEntityItemRouter router) {
        return Math.min(router.getFluidTransferRate(), maxTransfer);
    }

    private int getFluidAmount(IFluidHandler tank) {
        int amount = 0;
        FluidTankInfo[] info = tank.getTankInfo(ForgeDirection.UNKNOWN);
        if (info != null) {
            for (FluidTankInfo tankInfo : info) {
                if (tankInfo != null && tankInfo.fluid != null) amount += tankInfo.fluid.amount;
            }
        }
        return amount;
    }

    private int regulationThreshold(IFluidHandler tank) {
        if (regulateAbsolute) return getRegulationAmount();
        int capacity = 0;
        FluidTankInfo[] info = tank.getTankInfo(ForgeDirection.UNKNOWN);
        if (info != null) {
            for (FluidTankInfo tankInfo : info) {
                if (tankInfo != null) capacity += tankInfo.capacity;
            }
        }
        return capacity * getRegulationAmount() / 100;
    }

    public int getMaxTransfer() { return maxTransfer; }
    public FluidModule1.FluidDirection getFluidDirection() { return fluidDir; }
    public boolean isForceEmpty() { return forceEmpty; }
    public boolean isRegulateAbsolute() { return regulateAbsolute; }
}
