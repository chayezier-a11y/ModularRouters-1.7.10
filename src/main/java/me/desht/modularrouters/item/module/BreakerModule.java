package me.desht.modularrouters.item.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class BreakerModule extends Module implements IPickaxeUser {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledBreakerModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    public ItemStack getPickaxe(ItemStack moduleStack) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        CompiledBreakerModule breaker = new CompiledBreakerModule(null, stack);
        list.add(EnumChatFormatting.YELLOW + MiscUtil.translate(
                "guiText.label.breakMatchType." + breaker.getMatchType()));
    }
}
