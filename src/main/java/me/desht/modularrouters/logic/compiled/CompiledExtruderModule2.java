package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerExtruder2Module.TemplateHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class CompiledExtruderModule2 extends CompiledExtruderModule1 {
    private final List<ItemStack> template;

    public CompiledExtruderModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        template = new ArrayList<ItemStack>();
        TemplateHandler handler = new TemplateHandler(stack, router);
        for (int i = 0; i < handler.getSizeInventory() && template.size() < getRange(); i++) {
            ItemStack stackInSlot = handler.getStackInSlot(i);
            if (stackInSlot == null) break;
            for (int j = 0; j < stackInSlot.stackSize && template.size() < getRange(); j++) {
                ItemStack one = stackInSlot.copy();
                one.stackSize = 1;
                template.add(one);
            }
        }
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        boolean extend = shouldExtend(router);
        ForgeDirection facing = getAbsoluteDirection(router);

        if (extend && distance < template.size()) {
            ItemStack toPlace = template.get(distance);
            distance++;
            if (!(toPlace.getItem() instanceof ItemBlock)) {
                router.getExtData().setInteger(NBT_EXTRUDER_DIST + facing, distance);
                return false;
            }
            int x = router.xCoord + facing.offsetX * distance;
            int y = router.yCoord + facing.offsetY * distance;
            int z = router.zCoord + facing.offsetZ * distance;
            if (!router.getWorldObj().isAirBlock(x, y, z)) {
                distance--;
                return false;
            }
            ItemBlock itemBlock = (ItemBlock) toPlace.getItem();
            Block block = itemBlock.blockInstance;
            int meta = itemBlock.getMetadata(toPlace.getMetadata());
            router.getWorldObj().setBlock(x, y, z, block, meta, 3);
            router.getExtData().setInteger(NBT_EXTRUDER_DIST + facing, distance);
            return true;
        } else if (!extend && distance > 0) {
            int x = router.xCoord + facing.offsetX * distance;
            int y = router.yCoord + facing.offsetY * distance;
            int z = router.zCoord + facing.offsetZ * distance;
            if (!router.getWorldObj().isAirBlock(x, y, z)) {
                router.getWorldObj().setBlockToAir(x, y, z);
            }
            router.getExtData().setInteger(NBT_EXTRUDER_DIST + facing, --distance);
            return true;
        }
        return false;
    }
}
