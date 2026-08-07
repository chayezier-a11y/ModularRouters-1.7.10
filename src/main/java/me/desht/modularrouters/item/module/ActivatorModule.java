package me.desht.modularrouters.item.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class ActivatorModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledActivatorModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        CompiledActivatorModule cam = new CompiledActivatorModule(null, stack);
        list.add(ModuleInfoFormatter.settingLine(
                MiscUtil.translate("guiText.tooltip.activator.action"),
                MiscUtil.translate("itemText.activator.action." + cam.getActionType().name())));
        if (cam.getActionType() == CompiledActivatorModule.ActionType.RIGHT_CLICK) {
            list.add(ModuleInfoFormatter.settingLine(
                    MiscUtil.translate("guiText.tooltip.activator.lookDirection"),
                    MiscUtil.translate("itemText.activator.direction." + cam.getLookDirection().name())));
        } else {
            list.add(ModuleInfoFormatter.settingLine(
                    MiscUtil.translate("guiText.tooltip.activator.entityMode"),
                    MiscUtil.translate("itemText.activator.entityMode." + cam.getEntityMode().name())));
        }
        if (cam.isSneaking()) {
            list.add(EnumChatFormatting.YELLOW + MiscUtil.translate("guiText.tooltip.activator.sneak"));
        }
    }
}
