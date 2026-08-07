package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;

public class FluidUpgrade extends Upgrade {
    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        // Effect handled in compileUpgrades() - increases fluid transfer rate
    }
}
