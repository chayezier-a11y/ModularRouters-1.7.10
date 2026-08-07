package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class CompiledExtruderModule1 extends CompiledModule {
    public static final String NBT_EXTRUDER_DIST = "ExtruderDist";
    protected int distance;

    public CompiledExtruderModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        distance = router == null ? 0 : router.getExtData().getInteger(NBT_EXTRUDER_DIST + getFacing());
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        boolean extend = shouldExtend(router);
        ForgeDirection facing = getAbsoluteDirection(router);

        if (extend && !router.isBufferEmpty() && distance < getRange() && isRegulationOK(router, false)) {
            int x = router.xCoord + facing.offsetX * (distance + 1);
            int y = router.yCoord + facing.offsetY * (distance + 1);
            int z = router.zCoord + facing.offsetZ * (distance + 1);
            ItemStack toPlace = router.peekBuffer(1);
            if (toPlace == null || !(toPlace.getItem() instanceof ItemBlock) || getFilter().rejectItem(toPlace)
                    || !router.getWorldObj().isAirBlock(x, y, z)) return false;

            router.extractBuffer(1);
            ItemBlock itemBlock = (ItemBlock) toPlace.getItem();
            Block block = itemBlock.blockInstance;
            int meta = itemBlock.getMetadata(toPlace.getMetadata());
            router.getWorldObj().setBlock(x, y, z, block, meta, 3);
            router.getExtData().setInteger(NBT_EXTRUDER_DIST + facing, ++distance);
            return true;
        } else if (!extend && distance > 0 && isRegulationOK(router, true)) {
            int x = router.xCoord + facing.offsetX * distance;
            int y = router.yCoord + facing.offsetY * distance;
            int z = router.zCoord + facing.offsetZ * distance;
            if (router.getWorldObj().isAirBlock(x, y, z)) {
                router.getExtData().setInteger(NBT_EXTRUDER_DIST + facing, --distance);
                return false;
            }
            router.getWorldObj().setBlockToAir(x, y, z);
            router.getExtData().setInteger(NBT_EXTRUDER_DIST + facing, --distance);
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRun(boolean powered, boolean pulsed) {
        return true;
    }

    protected boolean shouldExtend(TileEntityItemRouter router) {
        switch (getRedstoneBehaviour()) {
            case ALWAYS: return router.getRedstonePower() > 0;
            case HIGH: return router.getRedstonePower() == 15;
            case LOW: return router.getRedstonePower() == 0;
            default: return false;
        }
    }
}
