package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledEnergyDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class EnergyDistributorModule extends TargetedModule implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledEnergyDistributorModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    public int getBaseRange() { return 8; }

    @Override
    public int getHardMaxRange() { return 48; }
}
