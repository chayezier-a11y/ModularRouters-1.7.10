package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule1;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class PullerModule1 extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPullerModule1(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }
}
