package me.desht.modularrouters.item.upgrade;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ItemBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public abstract class Upgrade {
    public enum UpgradeType {
        SPEED, STACK, RANGE, RANGEDOWN, MUFFLER, BLAST, SECURITY, SYNC, FLUID, CAMOUFLAGE;
        public static final UpgradeType[] VALUES = values();
    }

    public abstract void onCompiled(ItemStack stack, TileEntityItemRouter router);

    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {}

    @SideOnly(Side.CLIENT)
    public void addUsageInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {}
}
