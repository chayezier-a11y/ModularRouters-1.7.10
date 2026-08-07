package me.desht.modularrouters.item.upgrade;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class ItemUpgrade extends ItemBase {

    public enum UpgradeType {
        STACK, SPEED, SECURITY, CAMOUFLAGE, SYNC, FLUID, MUFFLER, BLAST, ENERGY;

        public static UpgradeType getType(ItemStack stack) {
            return stack.getItem() instanceof ItemUpgrade ? values()[stack.getMetadata()] : null;
        }
    }

    public static final int SUBTYPES = UpgradeType.values().length;
    private static final Upgrade[] upgrades = new Upgrade[SUBTYPES];

    static {
        registerUpgrade(UpgradeType.STACK, new StackUpgrade());
        registerUpgrade(UpgradeType.SPEED, new SpeedUpgrade());
        registerUpgrade(UpgradeType.SECURITY, new SecurityUpgrade());
        registerUpgrade(UpgradeType.CAMOUFLAGE, new CamouflageUpgrade());
        registerUpgrade(UpgradeType.SYNC, new SyncUpgrade());
        registerUpgrade(UpgradeType.FLUID, new FluidUpgrade());
        registerUpgrade(UpgradeType.MUFFLER, new MufflerUpgrade());
        registerUpgrade(UpgradeType.BLAST, new BlastUpgrade());
        registerUpgrade(UpgradeType.ENERGY, new EnergyUpgrade());
    }

    public static void registerUpgrade(UpgradeType type, Upgrade upgrade) {
        upgrades[type.ordinal()] = upgrade;
    }

    public static Upgrade getUpgrade(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemUpgrade)) return null;
        int meta = stack.getMetadata();
        if (meta < 0 || meta >= upgrades.length) return null;
        return upgrades[meta];
    }

        @Override
    public String getSubTypeName(int meta) {
        if (meta >= 0 && meta < UpgradeType.values().length) {
            return UpgradeType.values()[meta].toString().toLowerCase() + "Upgrade";
        }
        return name;
    }

    private IIcon[] icons;

    public ItemUpgrade() {
        super("upgrade");
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        icons = new IIcon[SUBTYPES];
        UpgradeType[] types = UpgradeType.values();
        for (int i = 0; i < SUBTYPES; i++) {
            icons[i] = ir.registerIcon(ModularRouters.modId + ":" + types[i].toString().toLowerCase() + "Upgrade");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        if (damage >= 0 && damage < icons.length) return icons[damage];
        return icons[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < SUBTYPES; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int dmg = stack.getMetadata();
        if (dmg < upgrades.length && upgrades[dmg] != null) {
            return "item." + UpgradeType.values()[dmg].toString().toLowerCase() + "Upgrade";
        }
        return super.getUnlocalizedName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4) {
        Upgrade upgrade = getUpgrade(itemstack);
        if (upgrade != null) {
            if (GuiScreen.isShiftKeyDown()) {
                upgrade.addExtraInformation(itemstack, player, list, par4);
            } else if (GuiScreen.isCtrlKeyDown()) {
                upgrade.addUsageInformation(itemstack, player, list, par4);
            } else {
                list.add(EnumChatFormatting.GRAY + MiscUtil.translate("itemText.misc.holdShiftCtrl"));
            }
        }
    }

    public static ItemStack makeItemStack(UpgradeType type, int count) {
        return new ItemStack(ModItems.upgrade, count, type.ordinal());
    }
}
