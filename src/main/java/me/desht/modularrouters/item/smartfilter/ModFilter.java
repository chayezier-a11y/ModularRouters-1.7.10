package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.ModMatcher;
import net.minecraft.item.ItemStack;

public class ModFilter extends SmartFilter {
    @Override
    public IItemMatcher compile(ItemStack filterStack) {
        return new ModMatcher(filterStack);
    }
}
