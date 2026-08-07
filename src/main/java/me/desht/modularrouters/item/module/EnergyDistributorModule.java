package me.desht.modularrouters.item.module;

import cofh.api.energy.IEnergyHandler;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledEnergyDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EnergyDistributorModule extends TargetedModule implements IRangedModule {
    @Override
    protected int getMaxTargets() {
        return 8;
    }

    @Override
    protected boolean isValidTarget(World world, int x, int y, int z, ForgeDirection face) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof IEnergyHandler && ((IEnergyHandler) tile).canConnectEnergy(face);
    }

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
