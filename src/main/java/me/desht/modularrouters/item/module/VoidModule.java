package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledVoidModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class VoidModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledVoidModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        return null;
    }
}
