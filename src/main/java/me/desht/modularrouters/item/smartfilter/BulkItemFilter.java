package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.logic.filter.matchers.BulkItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import net.minecraft.item.ItemStack;

public class BulkItemFilter extends SmartFilter {
    @Override
    public IItemMatcher compile(ItemStack filterStack) {
        return new BulkItemMatcher(filterStack);
    }
}
