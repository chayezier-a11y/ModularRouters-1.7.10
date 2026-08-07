package me.desht.modularrouters;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;

public class ModularRoutersTags {
    // Tag references for 1.7.10 compatibility
    // In 1.7.10, tags don't exist natively, so we use material/block checks instead

    public static boolean isReplaceableBlock(Block block) {
        return block == null || block.getMaterial() == Material.air
                || block == Blocks.snow_layer || block == Blocks.tallgrass
                || block == Blocks.deadbush || block == Blocks.water
                || block == Blocks.flowing_water || block == Blocks.lava
                || block == Blocks.flowing_lava;
    }
}
