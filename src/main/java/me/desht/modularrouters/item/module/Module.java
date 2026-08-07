package me.desht.modularrouters.item.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.gui.module.GuiModule;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Module {
    public enum ModuleFlags {
        BLACKLIST(true, 0x1),
        IGNORE_META(false, 0x2),
        IGNORE_NBT(true, 0x4),
        IGNORE_TAGS(true, 0x8),
        MATCH_ALL(false, 0x40),
        TERMINATE(false, 0x80);

        private final boolean defaultValue;
        private final byte mask;

        ModuleFlags(boolean defaultValue, int mask) {
            this.defaultValue = defaultValue;
            this.mask = (byte) mask;
        }
        public boolean getDefaultValue() { return defaultValue; }
        public byte getMask() { return mask; }
    }

    public enum RelativeDirection {
        NONE(0x00),
        DOWN(0x01),
        UP(0x02),
        LEFT(0x04),
        RIGHT(0x08),
        FRONT(0x10),
        BACK(0x20);

        private static final RelativeDirection[] realSides = { FRONT, BACK, UP, DOWN, LEFT, RIGHT };

        private final int mask;

        RelativeDirection(int mask) {
            this.mask = mask;
        }

        public static RelativeDirection[] realSides() {
            return realSides;
        }

        public ForgeDirection toForgeDirection(ForgeDirection current) {
            switch (this) {
                case UP: return ForgeDirection.UP;
                case DOWN: return ForgeDirection.DOWN;
                case FRONT: return current;
                case BACK: return current.getOpposite();
                case LEFT:
                    switch (current) {
                        case NORTH: return ForgeDirection.WEST;
                        case SOUTH: return ForgeDirection.EAST;
                        case WEST: return ForgeDirection.SOUTH;
                        case EAST: return ForgeDirection.NORTH;
                        default: return ForgeDirection.WEST;
                    }
                case RIGHT:
                    switch (current) {
                        case NORTH: return ForgeDirection.EAST;
                        case SOUTH: return ForgeDirection.WEST;
                        case WEST: return ForgeDirection.NORTH;
                        case EAST: return ForgeDirection.SOUTH;
                        default: return ForgeDirection.EAST;
                    }
                default: return current;
            }
        }

        public int getMask() { return mask; }
    }

    public abstract CompiledModule compile(TileEntityItemRouter router, ItemStack stack);

    @SideOnly(Side.CLIENT)
    public void addBasicInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {}

    @SideOnly(Side.CLIENT)
    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        MiscUtil.appendMultiline(list, "itemText.usage." + itemstack.getUnlocalizedName(), getExtraUsageParams());
    }

    @SideOnly(Side.CLIENT)
    protected void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        addSettingsInformation(stack, list);
        addAugmentInformation(stack, list);
    }

    @SideOnly(Side.CLIENT)
    protected void addSettingsInformation(ItemStack stack, List<String> list) {
        if (isDirectional()) {
            RelativeDirection dir = ModuleHelper.getDirectionFromNBT(stack);
            String key = isOmniDirectional() && dir == RelativeDirection.NONE
                    ? "guiText.tooltip.allDirections" : "guiText.tooltip." + dir;
            list.add(ModuleInfoFormatter.settingLine(MiscUtil.translate("guiText.label.direction"),
                    MiscUtil.translate(key)));
        }

        addFilterInformation(stack, list);

        list.add(EnumChatFormatting.YELLOW + MiscUtil.translate("itemText.misc.flags") + ": "
                + ModuleInfoFormatter.formatFlag(MiscUtil.translate("itemText.misc.IGNORE_DAMAGE"),
                ModuleHelper.checkFlag(stack, ModuleFlags.IGNORE_META)) + " | "
                + ModuleInfoFormatter.formatFlag(MiscUtil.translate("itemText.misc.IGNORE_NBT"),
                ModuleHelper.checkFlag(stack, ModuleFlags.IGNORE_NBT)) + " | "
                + ModuleInfoFormatter.formatFlag(MiscUtil.translate("itemText.misc.IGNORE_TAGS"),
                ModuleHelper.checkFlag(stack, ModuleFlags.IGNORE_TAGS)));

        if (this instanceof IRangedModule) {
            IRangedModule ranged = (IRangedModule) this;
            int current = ranged.getCurrentRange(stack);
            String colour = ModuleInfoFormatter.rangeColor(current, ranged.getBaseRange());
            list.add(MiscUtil.translate("itemText.misc.rangeInfo", colour, current,
                    ranged.getBaseRange(), ranged.getHardMaxRange()));
        }

        ModuleHelper.Termination termination = ModuleHelper.getTermination(stack);
        if (termination != ModuleHelper.Termination.NONE) {
            list.add(EnumChatFormatting.YELLOW
                    + MiscUtil.translate("guiText.tooltip.terminate." + termination + ".header"));
        }

        if (this instanceof IPickaxeUser) {
            addPickaxeInformation(stack, list);
        }

        int energy = getEnergyCost(stack);
        if (energy != 0) {
            list.add(MiscUtil.translate("itemText.misc.energyUsage", energy));
        }
    }

    @SideOnly(Side.CLIENT)
    private void addPickaxeInformation(ItemStack stack, List<String> list) {
        ItemStack pick = ((IPickaxeUser) this).getPickaxe(stack);
        if (pick == null) return;

        list.add(EnumChatFormatting.YELLOW + MiscUtil.translate("itemText.misc.breakerPick")
                + EnumChatFormatting.AQUA + pick.getDisplayName());
        for (Object entryObject : EnchantmentHelper.getEnchantments(pick).entrySet()) {
            Map.Entry entry = (Map.Entry) entryObject;
            int id = ((Integer) entry.getKey()).intValue();
            int level = ((Integer) entry.getValue()).intValue();
            Enchantment enchantment = id >= 0 && id < Enchantment.enchantmentsList.length
                    ? Enchantment.enchantmentsList[id] : null;
            if (enchantment != null) {
                list.add(EnumChatFormatting.YELLOW + " \u25b6 " + EnumChatFormatting.AQUA
                        + enchantment.getTranslatedName(level));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void addFilterInformation(ItemStack stack, List<String> list) {
        List<String> entries = new ArrayList<String>();
        ModuleFilterHandler handler = new ModuleFilterHandler(stack);
        for (int i = 0; i < handler.getSizeInventory(); i++) {
            ItemStack filterStack = handler.getStackInSlot(i);
            if (filterStack == null) continue;

            if (filterStack.getItem() instanceof ItemSmartFilter) {
                int size = ItemSmartFilter.getSize(filterStack);
                String suffix = size > 0 ? " [" + size + "]" : "";
                entries.add(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.ITALIC + " \u2022 "
                        + filterStack.getDisplayName() + suffix);
            } else {
                entries.add(EnumChatFormatting.AQUA + " \u2022 " + filterStack.getDisplayName());
            }
        }

        String header = MiscUtil.translate("itemText.misc."
                + (ModuleHelper.checkFlag(stack, ModuleFlags.BLACKLIST) ? "blacklist" : "whitelist"));
        if (entries.size() > 1) {
            header += " (" + MiscUtil.translate("itemText.misc."
                    + (ModuleHelper.checkFlag(stack, ModuleFlags.MATCH_ALL) ? "matchAll" : "matchAny")) + ")";
        }
        if (entries.isEmpty()) {
            list.add(EnumChatFormatting.YELLOW + header + ": " + EnumChatFormatting.AQUA
                    + EnumChatFormatting.ITALIC + MiscUtil.translate("itemText.misc.noItems"));
        } else {
            list.add(EnumChatFormatting.YELLOW + header + ":");
            list.addAll(entries);
        }
    }

    @SideOnly(Side.CLIENT)
    private void addAugmentInformation(ItemStack stack, List<String> list) {
        ItemAugment.AugmentCounter counter = new ItemAugment.AugmentCounter(stack);
        List<String> entries = new ArrayList<String>();
        Collection<ItemAugment> augments = counter.getAugments();
        for (ItemAugment augment : augments) {
            int count = counter.getAugmentCount(augment);
            if (count <= 0) continue;

            String name = new ItemStack(augment, 1, 0).getDisplayName();
            if (count > 1) name = count + " x " + name;
            name += EnumChatFormatting.AQUA + augment.getExtraInfo(count, stack);
            entries.add(" \u2022 " + EnumChatFormatting.DARK_GREEN + name);
        }
        if (!entries.isEmpty()) {
            list.add(EnumChatFormatting.GREEN + MiscUtil.translate("itemText.augments"));
            list.addAll(entries);
        }
    }

    public Object[] getExtraUsageParams() {
        return new Object[0];
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
        if (router != null) {
            if (!player.isSneaking()) {
                player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER, world, x, y, z);
                return true;
            }
        }
        return false;
    }

    public ItemStack onSneakRightClick(ItemStack stack, World world, EntityPlayer player) {
        return stack;
    }

    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return false;
    }

    public ContainerModule createGuiContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }

    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModule.class;
    }

    public boolean isDirectional() {
        return true;
    }

    public boolean isOmniDirectional() {
        return false;
    }

    public abstract IRecipe getRecipe();

    public boolean isFluidModule() {
        return false;
    }

    public boolean canBeRegulated() {
        return true;
    }

    public boolean isItemValidForFilter(ItemStack stack) {
        return true;
    }

    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new SimpleItemMatcher(stack);
    }

    public RelativeDirection getDirection(ItemStack stack, RelativeDirection defaultDir) {
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("Flags")) {
            return RelativeDirection.values()[(stack.getTagCompound().getByte("Flags") & 0x70) >> 4];
        }
        return defaultDir;
    }

    public void setDirection(ItemStack stack, RelativeDirection dir) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        byte flags = stack.getTagCompound().getByte("Flags");
        flags = (byte) ((flags & 0x8F) | (dir.ordinal() << 4));
        stack.getTagCompound().setByte("Flags", flags);
    }

    public int getEnergyCost(ItemStack stack) {
        return 0;
    }
}
