package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import me.desht.modularrouters.logic.BlockInteraction;
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
        if (toPlace == null || !(toPlace.getItem() instanceof ItemBlock)) return false;
        if (!performPlacement(router, toPlace)) return false;
        router.extractBuffer(1);
        return true;
    }

    protected boolean performPlacement(TileEntityItemRouter router, ItemStack stack) {
        World world = router.getWorldObj();
        ForgeDirection facing = getAbsoluteDirection(router);
        int x = router.xCoord + facing.offsetX;
        int y = router.yCoord + facing.offsetY;
        int z = router.zCoord + facing.offsetZ;
        Block existing = world.getBlock(x, y, z);
        if (!BlockInteraction.isReplaceable(existing, world, x, y, z)) return false;

        ItemBlock itemBlock = (ItemBlock) stack.getItem();
        Block block = itemBlock.blockInstance;
        if (!world.checkNoEntityCollision(block.getCollisionBoundingBoxFromPool(world, x, y, z))) {
            return false;
        }

        RouterFakePlayer fakePlayer = new RouterFakePlayer(router);
        fakePlayer.setPosition(router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5);
        boolean placed = BlockInteraction.place(itemBlock, stack.copy(), fakePlayer, world, x, y, z,
                facing.getOpposite());
        if (placed) {
            world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5,
                    block.stepSound.getPlaceSound(),
                    (block.stepSound.getVolume() + 1.0F) / 2.0F,
                    block.stepSound.getFrequency() * 0.8F);
        }
        return placed;
    }
}
