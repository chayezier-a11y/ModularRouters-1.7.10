package me.desht.modularrouters.logic.filter.matchers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class InspectionMatcher implements IItemMatcher {
    private final ItemStack filterStack;

    public InspectionMatcher(ItemStack stack) {
        this.filterStack = stack;
    }

    @Override
    public boolean match(ItemStack stack) {
        if (stack == null) return false;
        // Matches items that have the exact NBT data specified
        if (filterStack.hasTagCompound()) {
            NBTTagCompound filterNBT = filterStack.getTagCompound();
            if (stack.hasTagCompound()) {
                NBTTagCompound stackNBT = stack.getTagCompound();
                return matchesNBT(filterNBT, stackNBT);
            }
            return false;
        }
        return true;
    }

    private boolean matchesNBT(NBTTagCompound filter, NBTTagCompound stack) {
        for (Object keyObj : filter.getKeySet()) {
            String key = (String) keyObj;
            if (!stack.hasKey(key)) return false;
            if (!filter.getTag(key).equals(stack.getTag(key))) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack() {
        return filterStack;
    }
}
