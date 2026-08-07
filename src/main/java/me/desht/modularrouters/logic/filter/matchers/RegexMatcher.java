package me.desht.modularrouters.logic.filter.matchers;

import net.minecraft.item.ItemStack;

import java.util.regex.Pattern;

public class RegexMatcher implements IItemMatcher {
    private final ItemStack filterStack;
    private Pattern pattern;

    public RegexMatcher(ItemStack stack) {
        this.filterStack = stack;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Pattern")) {
            try {
                pattern = Pattern.compile(stack.getTagCompound().getString("Pattern"));
            } catch (Exception e) {
                pattern = null;
            }
        }
    }

    @Override
    public boolean match(ItemStack stack) {
        if (stack == null || pattern == null) return false;
        String name = stack.getDisplayName();
        return pattern.matcher(name).find();
    }

    @Override
    public ItemStack getStack() {
        return filterStack;
    }
}
