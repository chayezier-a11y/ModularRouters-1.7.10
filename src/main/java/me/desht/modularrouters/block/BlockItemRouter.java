package me.desht.modularrouters.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class BlockItemRouter extends BlockBase {
    public static final String BLOCK_NAME = "itemRouter";

    public static final String NBT_MODULES = "Modules";
    public static final String NBT_UPGRADES = "Upgrades";
    public static final String NBT_MODULE_COUNT = "ModuleCount";
    public static final String NBT_UPGRADE_COUNT = "UpgradeCount";
    public static final String NBT_REDSTONE_BEHAVIOUR = "RedstoneBehaviour";

    private IIcon iconFront;
    private IIcon iconFrontActive;
    private IIcon iconBack;
    private IIcon iconOther;

    public BlockItemRouter() {
        super(Material.iron, BLOCK_NAME);
        setHardness(5.0f);
        setResistance(6000000.0F);
        setStepSound(Block.soundTypeMetal);
        setHarvestLevel("pickaxe", 1);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        iconFront = ir.registerIcon(ModularRouters.modId + ":itemRouterFront");
        iconFrontActive = ir.registerIcon(ModularRouters.modId + ":itemRouterFrontActive");
        iconBack = ir.registerIcon(ModularRouters.modId + ":itemRouterBack");
        iconOther = ir.registerIcon(ModularRouters.modId + ":itemRouterOther");
        blockIcon = iconOther;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        ForgeDirection facing = ForgeDirection.getOrientation(meta);
        if (side == facing.ordinal()) return iconFront;
        if (side == facing.getOpposite().ordinal()) return iconBack;
        return iconOther;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int facing = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int meta;
        switch (facing) {
            case 0: meta = 2; break;
            case 1: meta = 5; break;
            case 2: meta = 3; break;
            case 3: meta = 4; break;
            default: meta = 2;
        }
        world.setBlockMetadataWithNotify(x, y, z, meta, 3);

        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
        NBTTagCompound compound = stack.getTagCompound();
        if (router != null && compound != null) {
            router.getModules().readFromNBT(compound.getCompoundTag(NBT_MODULES));
            router.getUpgrades().readFromNBT(compound.getCompoundTag(NBT_UPGRADES));
            try {
                router.setRedstoneBehaviour(RouterRedstoneBehaviour.valueOf(compound.getString(NBT_REDSTONE_BEHAVIOUR)));
            } catch (IllegalArgumentException e) {
                router.setRedstoneBehaviour(RouterRedstoneBehaviour.ALWAYS);
            }
        }
    }

    @Override
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
        if (axis != ForgeDirection.UP && axis != ForgeDirection.DOWN) {
            int meta = world.getBlockMetadata(x, y, z);
            world.setBlockMetadataWithNotify(x, y, z, axis.ordinal(), 3);
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
            if (router != null) {
                router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityItemRouter();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
        if (router != null) {
            InventoryUtils.dropInventoryItems(world, x, y, z, router.getBuffer());
            world.notifyBlockChange(x, y, z, block);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
        if (router != null) {
            ItemStack stack = router.getBufferItemStack();
            return stack == null ? 0 : MathHelper.floor_float(1 + ((float) stack.stackSize / (float) stack.getMaxStackSize()) * 14);
        }
        return 0;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(world, player, x, y, z, false);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
        super.harvestBlock(world, player, x, y, z, meta);
        world.setBlockToAir(x, y, z);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> l = new ArrayList<ItemStack>();
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
        if (router != null) {
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            if (router.getModuleCount() > 0 || router.getUpgradeCount() > 0) {
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }
                NBTTagCompound compound = stack.getTagCompound();
                compound.setTag(NBT_MODULES, router.getModules().writeToNBT(new NBTTagCompound()));
                compound.setTag(NBT_UPGRADES, router.getUpgrades().writeToNBT(new NBTTagCompound()));
                compound.setString(NBT_REDSTONE_BEHAVIOUR, router.getRedstoneBehaviour().toString());
                compound.setInteger(NBT_MODULE_COUNT, router.getModuleCount());
                for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
                    compound.setInteger(NBT_UPGRADE_COUNT + "." + type, router.getUpgradeCount(type));
                }
            }
            l.add(stack);
        }
        return l;
    }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4) {
        NBTTagCompound compound = itemstack.getTagCompound();
        if (compound != null && compound.hasKey(NBT_MODULE_COUNT)) {
            list.add(MiscUtil.translate("itemText.misc.routerConfigured"));
            int modules = compound.getInteger(NBT_MODULE_COUNT);
            MiscUtil.appendMultiline(list, "itemText.misc.moduleCount", modules);
            for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
                int c = compound.getInteger(NBT_UPGRADE_COUNT + "." + type);
                if (c > 0) {
                    String name = MiscUtil.translate("item." + type.toString().toLowerCase() + "Upgrade.name");
                    list.add(MiscUtil.translate("itemText.misc.upgradeCount", name, c));
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
            if (router != null) {
                if (router.isPermitted(player) && !world.isRemote) {
                    player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER, world, x, y, z);
                } else if (!router.isPermitted(player) && world.isRemote) {
                    player.addChatMessage(new ChatComponentTranslation("chatText.security.accessDenied"));
                }
            }
        }
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
        if (router != null) {
            router.checkForRedstonePulse();
        }
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
        if (router != null && router.getUpgradeCount(ItemUpgrade.UpgradeType.BLAST) > 0) {
            return false;
        }
        return super.canEntityDestroy(world, x, y, z, entity);
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
        if (router != null) {
            int l = router.getRedstoneLevel(side, false);
            return l < 0 ? 0 : l;
        }
        return 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
        if (router != null) {
            int l = router.getRedstoneLevel(side, true);
            return l < 0 ? 0 : l;
        }
        return 0;
    }

}
