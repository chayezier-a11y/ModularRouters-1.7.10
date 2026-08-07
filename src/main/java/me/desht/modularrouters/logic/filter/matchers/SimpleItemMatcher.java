package me.desht.modularrouters.logic.filter.matchers;

import net.minecraft.item.ItemStack;

public class SimpleItemMatcher implements IItemMatcher {
    private final ItemStack filterStack;

    public SimpleItemMatcher(ItemStack stack) {
        this.filterStack = stack;
    }

    @Override
    public boolean match(ItemStack stack) {
        if (stack == null) return false;
        return stack.getItem() == filterStack.getItem()
                && (filterStack.getMetadata() == 32767 || stack.getMetadata() == filterStack.getMetadata())
                && ItemStack.areItemStackTagsEqual(stack, filterStack);
    }

    @Override
    public ItemStack getStack() {
        return filterStack;
    }
}
