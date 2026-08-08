package me.desht.modularrouters.logic;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class BlockInteraction {
    private BlockInteraction() {
    }

    public static int[] offset(int x, int y, int z, ForgeDirection face) {
        return new int[] { x + face.offsetX, y + face.offsetY, z + face.offsetZ };
    }

    public static boolean isReplaceable(Block block, World world, int x, int y, int z) {
        return block == null || block.isReplaceable(world, x, y, z);
    }

    public static boolean canBreak(Block block, World world, int x, int y, int z) {
        return block != null && block.getBlockHardness(world, x, y, z) >= 0.0f;
    }

    public static boolean place(ItemBlock itemBlock, ItemStack stack, EntityPlayer player,
                                World world, int x, int y, int z, ForgeDirection face) {
        return itemBlock != null && stack != null && player != null && world != null
                && itemBlock.placeBlockAt(stack, player, world, x, y, z, face.ordinal(),
                0.5f, 0.5f, 0.5f, itemBlock.getMetadata(stack.getMetadata()));
    }
}
