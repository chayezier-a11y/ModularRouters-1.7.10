package me.desht.modularrouters.item.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class VacuumModule extends Module implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledVacuumModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    public int getBaseRange() { return Config.vacuumBaseRange; }

    @Override
    public int getHardMaxRange() { return Config.vacuumMaxRange; }

    @Override
    public boolean isOmniDirectional() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        CompiledVacuumModule vacuum = new CompiledVacuumModule(null, stack);
        if (vacuum.isXpMode()) {
            list.add(ModuleInfoFormatter.settingLine(
                    MiscUtil.translate("guiText.label.xpVacuum"),
                    MiscUtil.translate("guiText.tooltip.vacuum.xpBottle")));
            if (vacuum.isAutoEjecting()) {
                list.add(EnumChatFormatting.GREEN
                        + MiscUtil.translate("guiText.tooltip.xpVacuum.ejectFluid"));
            }
        }
    }
}
