package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledDetectorModule;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class DetectorModule extends Module {

    public enum SignalType {
        NONE, WEAK, STRONG;

        public static SignalType getType(boolean strong) {
            return strong ? STRONG : WEAK;
        }
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledDetectorModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }
}
