package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledPlacerModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class PlacerModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPlacerModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        return null;
    }
}
