package me.desht.modularrouters.item.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

public class ItemModule extends ItemBase {
    private static final String NBT_CONFIG_SLOT = "ConfigSlot";
    public static final String NBT_OWNER = "Owner";
    public static final String NBT_OWNER_UUID = "OwnerUUID";

    public enum ModuleType {
        BREAKER, DROPPER, PLACER, PULLER1, PULLER2, SENDER1, SENDER2, SENDER3,
        VACUUM, VOID, DETECTOR, FLINGER, PLAYER,
        EXTRUDER1, EXTRUDER2, FLUID1, FLUID2, ACTIVATOR, DISTRIBUTOR,
        CREATIVE, ENERGY_DISTRIBUTOR, ENERGY_OUTPUT;

        public static ModuleType getType(ItemStack stack) {
            return stack.getItem() instanceof ItemModule ? values()[stack.getMetadata()] : null;
        }
    }

    public static final int SUBTYPES = ModuleType.values().length;
    private static final Module[] modules = new Module[SUBTYPES];

    static {
        registerSubItem(ModuleType.BREAKER, new BreakerModule());
        registerSubItem(ModuleType.DROPPER, new DropperModule());
        registerSubItem(ModuleType.PLACER, new PlacerModule());
        registerSubItem(ModuleType.PULLER1, new PullerModule1());
        registerSubItem(ModuleType.PULLER2, new PullerModule2());
        registerSubItem(ModuleType.SENDER1, new SenderModule1());
        registerSubItem(ModuleType.SENDER2, new SenderModule2());
        registerSubItem(ModuleType.SENDER3, new SenderModule3());
        registerSubItem(ModuleType.VACUUM, new VacuumModule());
        registerSubItem(ModuleType.VOID, new VoidModule());
        registerSubItem(ModuleType.DETECTOR, new DetectorModule());
        registerSubItem(ModuleType.FLINGER, new FlingerModule());
        registerSubItem(ModuleType.PLAYER, new PlayerModule());
        registerSubItem(ModuleType.EXTRUDER1, new ExtruderModule1());
        registerSubItem(ModuleType.EXTRUDER2, new ExtruderModule2());
        registerSubItem(ModuleType.FLUID1, new FluidModule1());
        registerSubItem(ModuleType.FLUID2, new FluidModule2());
        registerSubItem(ModuleType.ACTIVATOR, new ActivatorModule());
        registerSubItem(ModuleType.DISTRIBUTOR, new DistributorModule());
        registerSubItem(ModuleType.CREATIVE, new CreativeModule());
        registerSubItem(ModuleType.ENERGY_DISTRIBUTOR, new EnergyDistributorModule());
        registerSubItem(ModuleType.ENERGY_OUTPUT, new EnergyOutputModule());
    }

    public static void registerSubItem(ModuleType type, Module module) {
        modules[type.ordinal()] = module;
    }

    public static Module getModule(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemModule)) return null;
        int meta = stack.getMetadata();
        if (meta < 0 || meta >= modules.length) return null;
        return modules[meta];
    }

    public static Class<? extends Module> getModuleClass(ModuleType type) {
        Module m = modules[type.ordinal()];
        return m != null ? m.getClass() : null;
    }

    public static ItemStack makeItemStack(ModuleType type) {
        return new ItemStack(ModItems.module, 1, type.ordinal());
    }

    public static void setOwner(ItemStack stack, EntityPlayer player) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString(NBT_OWNER, player.getCommandSenderName());
        stack.getTagCompound().setString(NBT_OWNER_UUID, player.getUniqueID().toString());
    }

    public static String getOwnerName(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_OWNER)
                ? stack.getTagCompound().getString(NBT_OWNER) : null;
    }

    public static UUID getOwnerId(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(NBT_OWNER_UUID)) return null;
        try {
            return UUID.fromString(stack.getTagCompound().getString(NBT_OWNER_UUID));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

        @Override
    public String getSubTypeName(int meta) {
        if (meta >= 0 && meta < ModuleType.values().length) {
            return mapIconName(ModuleType.values()[meta]);
        }
        return name;
    }

    private static String mapIconName(ModuleType type) {
        switch (type) {
            case PULLER1: return "pullerModule";
            case EXTRUDER1: return "extruderModule";
            case FLUID1: return "fluidModule";
            default: return type.toString().toLowerCase() + "Module";
        }
    }

    private IIcon[] icons;
    private IIcon iconLayer0;
    private IIcon iconLayer1;

    public ItemModule() {
        super("module");
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        icons = new IIcon[SUBTYPES];
        iconLayer0 = ir.registerIcon(ModularRouters.modId + ":module_layer0");
        iconLayer1 = ir.registerIcon(ModularRouters.modId + ":module_layer1");
        ModuleType[] types = ModuleType.values();
        for (int i = 0; i < SUBTYPES; i++) {
            icons[i] = ir.registerIcon(ModularRouters.modId + ":" + mapIconName(types[i]));
        }
    }

    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public int getRenderPasses(int metadata) {
        return 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        if (pass == 0) return iconLayer0;
        if (pass == 1) return iconLayer1;
        return getIconFromDamage(damage);
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int pass) {
        return pass == 0 ? getModuleTint(stack.getMetadata()) : 0xFFFFFF;
    }

    private static int getModuleTint(int metadata) {
        if (metadata < 0 || metadata >= ModuleType.values().length) return 0xFFFFFF;
        switch (ModuleType.values()[metadata]) {
            case ACTIVATOR:
            case DETECTOR:
                return 0xFFFFC3;
            case BREAKER:
            case PLACER:
                return 0xF0D0D0;
            case CREATIVE:
                return 0xBB26B9;
            case DISTRIBUTOR:
                return 0xF0F03C;
            case DROPPER:
            case FLINGER:
                return 0xE6CCF0;
            case ENERGY_DISTRIBUTOR:
                return 0x36013D;
            case ENERGY_OUTPUT:
                return 0x41044B;
            case EXTRUDER1:
            case EXTRUDER2:
                return 0xE3AE1B;
            case FLUID1:
                return 0x4FBFFF;
            case FLUID2:
                return 0x40E0FF;
            case PLAYER:
                return 0xFFD090;
            case PULLER1:
                return 0xC0C0FF;
            case PULLER2:
                return 0x8080FF;
            case SENDER1:
                return 0xDDFFA3;
            case SENDER2:
                return 0x95FF5D;
            case SENDER3:
                return 0x19FF0B;
            case VACUUM:
                return 0x7830BF;
            case VOID:
                return 0xFF0000;
            default:
                return 0xFFFFFF;
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
        if (dmg < modules.length && modules[dmg] != null) {
            return "item." + mapIconName(ModuleType.values()[dmg]);
        }
        return super.getUnlocalizedName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4) {
        Module module = getModule(itemstack);
        if (module != null) {
            module.addBasicInformation(itemstack, player, list, par4);
            if (GuiScreen.isShiftKeyDown()) {
                module.addExtraInformation(itemstack, player, list, par4);
            } else if (GuiScreen.isCtrlKeyDown()) {
                module.addUsageInformation(itemstack, player, list, par4);
            } else {
                list.add(EnumChatFormatting.GRAY + MiscUtil.translate("itemText.misc.holdShiftCtrl"));
            }
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        Module module = getModule(stack);
        if (module != null) {
            return module.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player.isSneaking()) {
            Module module = getModule(stack);
            if (module != null) {
                return module.onSneakRightClick(stack, world, player);
            }
        } else if (!world.isRemote && getModule(stack) != null) {
            player.openGui(ModularRouters.instance, ModularRouters.GUI_MODULE_HELD_MAIN, world,
                    (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return stack;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        Module module = getModule(stack);
        return module != null && module.onEntitySwing(entityLiving, stack);
    }

    public static ContainerModule createGuiContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        Module m = getModule(moduleStack);
        return m != null ? m.createGuiContainer(player, moduleStack, router) : null;
    }
}
