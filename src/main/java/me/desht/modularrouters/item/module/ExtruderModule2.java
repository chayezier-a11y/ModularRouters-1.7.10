package me.desht.modularrouters.item.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ContainerExtruder2Module;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule2;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class ExtruderModule2 extends Module implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruderModule2(router, stack);
    }

    @Override
    public ContainerModule createGuiContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerExtruder2Module(player, moduleStack, router);
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    public int getBaseRange() { return Config.extruder2BaseRange; }

    @Override
    public int getHardMaxRange() { return Config.extruder2MaxRange; }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        list.add(EnumChatFormatting.YELLOW + MiscUtil.translate("itemText.extruder2.template"));
        int before = list.size();
        ContainerExtruder2Module.TemplateHandler handler =
                new ContainerExtruder2Module.TemplateHandler(stack, null);
        for (int i = 0; i < handler.getSizeInventory(); i++) {
            ItemStack template = handler.getStackInSlot(i);
            if (template != null) {
                list.add(EnumChatFormatting.AQUA + " \u2022 " + template.stackSize + " x "
                        + template.getDisplayName());
            }
        }
        if (list.size() == before) {
            list.add(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.ITALIC
                    + " " + MiscUtil.translate("itemText.misc.noItems"));
        }
    }
}
