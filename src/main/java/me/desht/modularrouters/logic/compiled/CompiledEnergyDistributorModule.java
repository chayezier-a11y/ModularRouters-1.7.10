package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.item.ItemStack;

public class CompiledEnergyDistributorModule extends CompiledModule {
    public CompiledEnergyDistributorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        // Placeholder - energy transfer to be implemented
        return false;
    }
}
