package me.desht.modularrouters.block;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockTemplateFrame extends BlockBase {
    public static final String BLOCK_NAME = "templateFrame";

    public BlockTemplateFrame() {
        super(Material.iron, BLOCK_NAME);
        setHardness(2.0f);
        setStepSound(Block.soundTypeMetal);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityTemplateFrame();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        TileEntityTemplateFrame te = TileEntityTemplateFrame.getFrameAt(world, x, y, z);
        if (te != null && stack.hasDisplayName()) {
            te.setInventoryName(stack.getDisplayName());
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side,
                                     float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(ModularRouters.instance, 0, world, x, y, z);
        }
        return true;
    }

    public void registerIcons(IIconRegister iconRegister) {
        blockIcon = iconRegister.registerIcon(ModularRouters.modId + ":templateFrame");
    }
}
