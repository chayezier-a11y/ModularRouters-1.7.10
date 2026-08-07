package me.desht.modularrouters.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModularRoutersTab;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.List;

public class ItemBase extends Item {
    protected final String name;

    public ItemBase(String name) {
        this.name = name;
        setUnlocalizedName(name);
        setTextureName(ModularRouters.modId + ":" + name);
        setCreativeTab(ModularRoutersTab.creativeTab);
    }

    public void registerItemModel(int nSubtypes) {
        if (nSubtypes == 0) {
            ModularRouters.proxy.registerItemRenderer(this, 0, name);
        } else {
            for (int i = 0; i < nSubtypes; i++) {
                ModularRouters.proxy.registerItemRenderer(this, i, getSubTypeName(i));
            }
        }
    }

    public String getSubTypeName(int meta) {
        return name;
    }

    @Override
    public ItemBase setCreativeTab(CreativeTabs tab) {
        super.setCreativeTab(tab);
        return this;
    }
}
