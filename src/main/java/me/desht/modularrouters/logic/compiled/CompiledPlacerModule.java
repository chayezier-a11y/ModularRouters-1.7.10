package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;

public class CompiledPlacerModule extends CompiledModule {
    public CompiledPlacerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        if (!isRegulationOK(router, false)) {
            return false;
        }
        ItemStack toPlace = router.peekBuffer(1);
        if (!getFilter().allowItem(toPlace)) {
            return false;
        }
        World world = router.getWorldObj();
        ForgeDirection facing = getAbsoluteDirection(router);
        int x = router.xCoord + facing.offsetX;
        int y = router.yCoord + facing.offsetY;
        int z = router.zCoord + facing.offsetZ;

        Block existing = world.getBlock(x, y, z);
        if (existing != null && existing != Blocks.air && !existing.isReplaceable(world, x, y, z)) {
            return false;
        }

        if (toPlace == null || !(toPlace.getItem() instanceof ItemBlock)) return false;

        ItemBlock itemBlock = (ItemBlock) toPlace.getItem();
        Block block = itemBlock.blockInstance;
        int meta = itemBlock.getMetadata(toPlace.getMetadata());

        if (!world.checkNoEntityCollision(block.getCollisionBoundingBoxFromPool(world, x, y, z))) {
            return false;
        }

        router.extractBuffer(1);
        world.setBlock(x, y, z, block, meta, 3);
        world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5,
                block.stepSound.getPlaceSound(),
                (block.stepSound.getVolume() + 1.0F) / 2.0F,
                block.stepSound.getFrequency() * 0.8F);

        return true;
    }
}
