package me.desht.modularrouters.logic.filter;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

public class OreDictMatcher {
    private final String oreName;

    public OreDictMatcher(String oreName) {
        this.oreName = oreName;
    }

    public boolean match(ItemStack stack) {
        if (stack == null) return false;
        int[] ids = OreDictionary.getOreIDs(stack);
        for (int id : ids) {
            if (OreDictionary.getOreName(id).equals(oreName)) {
                return true;
            }
        }
        return false;
    }

    public List<ItemStack> getMatchingStacks() {
        return OreDictionary.getOres(oreName);
    }
}
