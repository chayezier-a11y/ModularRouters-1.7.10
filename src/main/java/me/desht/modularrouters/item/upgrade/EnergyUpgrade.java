package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class EnergyUpgrade extends Upgrade {
    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        // Energy upgrades are handled in TileEntityItemRouter
    }

    @Override
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        list.add("Increases router energy capacity");
    }
}
