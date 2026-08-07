package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;

public class StackUpgrade extends Upgrade {
    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        // Effect handled in compileUpgrades() - increases itemsPerTick
    }
}
