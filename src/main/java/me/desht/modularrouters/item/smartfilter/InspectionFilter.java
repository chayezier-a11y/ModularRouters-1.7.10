package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher;
import net.minecraft.item.ItemStack;

public class InspectionFilter extends SmartFilter {
    @Override
    public IItemMatcher compile(ItemStack filterStack) {
        return new InspectionMatcher(filterStack);
    }
}
