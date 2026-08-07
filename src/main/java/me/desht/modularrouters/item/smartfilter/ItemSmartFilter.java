package me.desht.modularrouters.item.smartfilter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.ItemBase;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import java.util.List;

public class ItemSmartFilter extends ItemBase {
    public static final int SUBTYPES = 4;
    private static final SmartFilter[] filters = new SmartFilter[SUBTYPES];

    public enum FilterType {
        BULKITEM, MOD, REGEX, INSPECTION;
    public static FilterType getType(ItemStack stack) {
            int meta = stack.getMetadata();
            return stack.getItem() instanceof ItemSmartFilter && meta >= 0 && meta < values().length
                    ? values()[meta] : null;
        }
    }

    static {
        filters[FilterType.BULKITEM.ordinal()] = new BulkItemFilter();
        filters[FilterType.MOD.ordinal()] = new ModFilter();
        filters[FilterType.REGEX.ordinal()] = new RegexFilter();
        filters[FilterType.INSPECTION.ordinal()] = new InspectionFilter();
    }

    private IIcon[] icons;

    public ItemSmartFilter() {
        super("smartFilter");
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        icons = new IIcon[SUBTYPES];
        String[] names = {"bulkitemFilter", "modFilter", "regexFilter", "inspectionFilter"};
        for (int i = 0; i < SUBTYPES; i++) {
            icons[i] = ir.registerIcon(ModularRouters.modId + ":" + names[i]);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        if (damage >= 0 && damage < icons.length) return icons[damage];
        return icons[0];
    }

    public static SmartFilter getFilter(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemSmartFilter)) return null;
        return filters[stack.getMetadata()];
    }

    /** Number of entries represented by a configured smart filter, for module tooltips. */
    public static int getSize(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemSmartFilter) || !stack.hasTagCompound()) return 0;
        NBTTagCompound tag = stack.getTagCompound();
        FilterType type = FilterType.getType(stack);
        if (type == null) return 0;
        switch (type) {
            case BULKITEM:
                return tag.hasKey("Filter") ? tag.getTagList("Filter", 10).tagCount() : 0;
            case MOD:
                return tag.hasKey("ModId") && !tag.getString("ModId").isEmpty() ? 1 : 0;
            case REGEX:
                return tag.hasKey("Pattern") && !tag.getString("Pattern").isEmpty() ? 1 : 0;
            case INSPECTION:
                return tag.getKeySet().size();
            default:
                return 0;
        }
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
        if (dmg < filters.length && filters[dmg] != null) {
            return "item." + FilterType.values()[dmg].toString().toLowerCase() + "Filter";
        }
        return super.getUnlocalizedName(stack);
    }

    public static ItemStack makeItemStack(FilterType type) {
        return new ItemStack(me.desht.modularrouters.item.ModItems.smartFilter, 1, type.ordinal());
    }
}
