package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CompiledEnergyDistributorModule extends CompiledModule {
    public CompiledEnergyDistributorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        return new ArrayList<ModuleTarget>(TargetedModule.getTargets(stack));
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        // Placeholder - energy transfer to be implemented
        return false;
    }
}
