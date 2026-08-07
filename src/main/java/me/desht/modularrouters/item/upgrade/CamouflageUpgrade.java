package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CamouflageUpgrade extends Upgrade {
    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("CamoBlock")) {
            Block camo = Block.getBlockById(stack.getTagCompound().getInteger("CamoBlock"));
            int meta = stack.getTagCompound().getInteger("CamoMeta");
            router.setCamouflage(camo, meta);
        }
    }
}
