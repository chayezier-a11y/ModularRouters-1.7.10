package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import me.desht.modularrouters.util.MiscUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.util.List;

public class FlingerModule extends Module {
    public static final float MIN_SPEED = 0.0f;
    public static final float MAX_SPEED = 20.0f;
    public static final float MIN_PITCH = -90.0f;
    public static final float MAX_PITCH = 90.0f;
    public static final float MIN_YAW = -60.0f;
    public static final float MAX_YAW = 60.0f;

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledFlingerModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() { return null; }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        CompiledFlingerModule flinger = new CompiledFlingerModule(null, stack);
        list.add(MiscUtil.translate("itemText.misc.flingerDetails", flinger.getSpeed(),
                flinger.getPitch(), flinger.getYaw()));
    }
}
