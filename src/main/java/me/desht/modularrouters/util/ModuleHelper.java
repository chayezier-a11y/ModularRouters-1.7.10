package me.desht.modularrouters.util;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.ModuleFlags;
import me.desht.modularrouters.item.module.Module.RelativeDirection;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ModuleHelper {
    public static final String NBT_FILTER = "ModuleFilter";
    public static final String NBT_FLAGS = "Flags";
    public static final String NBT_REGULATOR_AMOUNT = "RegulatorAmount";
    public static final String NBT_AUGMENTS = "Augments";

    public static Module getModule(ItemStack stack) {
        return ItemModule.getModule(stack);
    }

    public static boolean isModule(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemModule;
    }

    public static NBTTagCompound validateNBT(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            stack.setTagCompound(compound = new NBTTagCompound());
        }
        if (!compound.hasKey(NBT_FLAGS)) {
            byte flags = 0x0;
            for (ModuleFlags b : ModuleFlags.values()) {
                if (b.getDefaultValue()) {
                    flags |= b.getMask();
                }
            }
            compound.setByte(NBT_FLAGS, flags);
        }
        if (!compound.hasKey(NBT_FILTER)) {
            compound.setTag(NBT_FILTER, new NBTTagList());
        }
        return compound;
    }

    public static boolean checkFlag(ItemStack stack, ModuleFlags flag) {
        NBTTagCompound compound = validateNBT(stack);
        return (compound.getByte(NBT_FLAGS) & flag.getMask()) != 0;
    }

    public static RelativeDirection getDirectionFromNBT(ItemStack stack) {
        Module module = ItemModule.getModule(stack);
        if (module == null || !module.isDirectional()) {
            return RelativeDirection.NONE;
        }
        NBTTagCompound compound = validateNBT(stack);
        return RelativeDirection.values()[(compound.getByte(NBT_FLAGS) & 0x70) >> 4];
    }

    public static RouterRedstoneBehaviour getRedstoneBehaviour(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        if (compound.hasKey("RedstoneBehaviour")) {
            return RouterRedstoneBehaviour.values()[compound.getByte("RedstoneBehaviour")];
        }
        return RouterRedstoneBehaviour.ALWAYS;
    }

    public static int getRegulatorAmount(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getInteger(NBT_REGULATOR_AMOUNT);
    }

    public enum Termination {
        NONE, RAN, NOT_RAN
    }

    public static Termination getTermination(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.hasKey("Termination") ?
                Termination.values()[compound.getByte("Termination")] : Termination.NONE;
    }

    public static void setTermination(ItemStack stack, Termination t) {
        NBTTagCompound compound = validateNBT(stack);
        compound.setByte("Termination", (byte) t.ordinal());
    }

    public static int getRangeModifier(ItemStack stack) {
        ItemAugment.AugmentCounter counter = new ItemAugment.AugmentCounter(stack);
        return counter.getAugmentCount(ItemAugment.getAugment("rangeUpAugment"))
                - counter.getAugmentCount(ItemAugment.getAugment("rangeDownAugment"));
    }

    public static NBTTagList getFilterItems(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getTagList(NBT_FILTER, 10);
    }
}
