package me.desht.modularrouters.item.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule1;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class FluidModule1 extends Module {
    public enum FluidDirection { IN, OUT }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledFluidModule1(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    public boolean isFluidModule() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        CompiledFluidModule1 fluid = new CompiledFluidModule1(null, stack);
        list.add(MiscUtil.translate("itemText.fluid.direction",
                MiscUtil.translate("itemText.fluid.direction." + fluid.getFluidDirection())));
        list.add(MiscUtil.translate("itemText.fluid.maxTransfer", fluid.getMaxTransfer()));
    }
}
