package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule2;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class FluidModule2 extends TargetedModule implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledFluidModule2(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    public boolean isFluidModule() { return true; }

    @Override
    public int getBaseRange() { return Config.fluid2BaseRange; }

    @Override
    public int getHardMaxRange() { return Config.fluid2MaxRange; }
}
