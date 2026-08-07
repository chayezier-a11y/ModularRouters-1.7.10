package me.desht.modularrouters.item.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class PlayerModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPlayerModule(router, stack);
    }

    @Override
    public boolean isDirectional() { return false; }

    @Override
    public ItemStack onSneakRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            ItemModule.setOwner(stack, player);
            MiscUtil.sendStatusMessage(player, "chatText.security.owner", player.getCommandSenderName());
        }
        return stack;
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        CompiledPlayerModule module = new CompiledPlayerModule(null, stack);
        String owner = module.getOwnerName() == null ? "-" : module.getOwnerName();
        list.add(MiscUtil.translate("itemText.security.owner", owner));

        String arrow = module.getOperation() == CompiledPlayerModule.Operation.INSERT ? "\u27f9" : "\u27f8";
        String operation = MiscUtil.translate("guiText.tooltip.player.operation." + module.getOperation());
        String section = MiscUtil.translate("guiText.tooltip.player.section." + module.getSection());
        list.add(EnumChatFormatting.YELLOW + MiscUtil.translate("itemText.misc.operation") + ": "
                + EnumChatFormatting.AQUA + MiscUtil.translate("tile.itemRouter.name") + " "
                + arrow + " " + operation + " (" + section + ")");
    }
}
