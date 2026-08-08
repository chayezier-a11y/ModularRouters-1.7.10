package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.List;

public class CompiledSenderModule2 extends CompiledSenderModule1 {
    public CompiledSenderModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferEmpty()) return false;

        ItemStack toSend = router.peekBuffer(getItemsPerTick(router));
        if (toSend == null || getFilter().rejectItem(toSend)) return false;

        return super.execute(router);
    }

    @Override
    List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        return Collections.singletonList(me.desht.modularrouters.item.module.TargetedModule.getTarget(stack));
    }

    @Override
    PositionedInventory findTargetInventory(TileEntityItemRouter router) {
        ModuleTarget target = getEffectiveTarget(router);
        if (target == null || router.getWorldObj() == null) {
            return PositionedInventory.INVALID;
        }
        boolean loaded = router.getWorldObj().blockExists(target.getX(), target.getY(), target.getZ());
        boolean valid = loaded && target.getDimension() == router.getWorldObj().provider.dimensionId
                && (!isRangeLimited() || isTargetLocationValid(router.getWorldObj().provider.dimensionId,
                router.xCoord, router.yCoord, router.zCoord, getRangeSquared(), target, true));
        if (!valid) {
            return PositionedInventory.INVALID;
        }
        if (router.getWorldObj().getTileEntity(target.getX(), target.getY(), target.getZ()) instanceof IInventory) {
            return new PositionedInventory((IInventory) router.getWorldObj().getTileEntity(
                    target.getX(), target.getY(), target.getZ()), target);
        }
        return PositionedInventory.INVALID;
    }

    boolean isRangeLimited() { return true; }

    static boolean isTargetLocationValid(int routerDimension, int routerX, int routerY, int routerZ,
                                         int rangeSquared, ModuleTarget target, boolean loaded) {
        if (target == null || !loaded || target.getDimension() != routerDimension) return false;
        long dx = (long) target.getX() - routerX;
        long dy = (long) target.getY() - routerY;
        long dz = (long) target.getZ() - routerZ;
        return dx * dx + dy * dy + dz * dz <= rangeSquared;
    }
}
