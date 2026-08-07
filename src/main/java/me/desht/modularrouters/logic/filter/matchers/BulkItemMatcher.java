package me.desht.modularrouters.logic.filter.matchers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

public class BulkItemMatcher implements IItemMatcher {
    private final List<ItemStack> filterStacks = new ArrayList<ItemStack>();

    public BulkItemMatcher(ItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagList list = stack.getTagCompound().getTagList("Filter", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                filterStacks.add(ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
            }
        }
    }

    @Override
    public boolean match(ItemStack stack) {
        if (stack == null) return false;
        for (ItemStack filter : filterStacks) {
            if (filter.getItem() == stack.getItem()
                    && (filter.getMetadata() == 32767 || filter.getMetadata() == stack.getMetadata())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack getStack() {
        return filterStacks.isEmpty() ? null : filterStacks.get(0);
    }
}
