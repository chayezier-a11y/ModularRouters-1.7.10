package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledEnergyOutputModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class EnergyOutputModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledEnergyOutputModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        return null;
    }
}
