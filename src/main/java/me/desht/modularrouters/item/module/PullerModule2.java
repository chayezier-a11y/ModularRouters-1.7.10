package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule2;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class PullerModule2 extends TargetedModule implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPullerModule2(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    public int getBaseRange() { return Config.puller2BaseRange; }

    @Override
    public int getHardMaxRange() { return Config.puller2MaxRange; }
}
