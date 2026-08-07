package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.RegexMatcher;
import net.minecraft.item.ItemStack;

public class RegexFilter extends SmartFilter {
    @Override
    public IItemMatcher compile(ItemStack filterStack) {
        return new RegexMatcher(filterStack);
    }
}
