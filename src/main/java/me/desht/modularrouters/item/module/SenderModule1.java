package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule1;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class SenderModule1 extends Module implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSenderModule1(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    public int getBaseRange() { return Config.sender1BaseRange; }

    @Override
    public int getHardMaxRange() { return Config.sender1MaxRange; }
}
