package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;

public class MufflerUpgrade extends Upgrade {
    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        // Effect: reduces/eliminates sound; 3 mufflers stop the active animation
    }
}
