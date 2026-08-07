package me.desht.modularrouters.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ModularRoutersTab extends CreativeTabs {

    public static final CreativeTabs creativeTab = new ModularRoutersTab();

    public ModularRoutersTab() {
        super(ModularRouters.modId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        return Item.getItemFromBlock(ModBlocks.itemRouter);
    }
}
