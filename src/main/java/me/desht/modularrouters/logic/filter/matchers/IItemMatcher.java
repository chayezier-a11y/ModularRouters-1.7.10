package me.desht.modularrouters.logic.filter.matchers;

import net.minecraft.item.ItemStack;

public interface IItemMatcher {
    boolean match(ItemStack stack);
    ItemStack getStack();
}
