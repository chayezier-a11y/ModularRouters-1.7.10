package me.desht.modularrouters.util;

import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CountedItemStacks {
    private final Map<ItemStack, Integer> stacks = new HashMap<ItemStack, Integer>();

    public void add(ItemStack stack, int count) {
        for (Map.Entry<ItemStack, Integer> entry : stacks.entrySet()) {
            if (ItemStack.areItemStacksEqual(entry.getKey(), stack)) {
                entry.setValue(entry.getValue() + count);
                return;
            }
        }
        stacks.put(stack.copy(), count);
    }

    public int get(ItemStack stack) {
        for (Map.Entry<ItemStack, Integer> entry : stacks.entrySet()) {
            if (ItemStack.areItemStacksEqual(entry.getKey(), stack)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public void clear() {
        stacks.clear();
    }

    public Map<ItemStack, Integer> getMap() {
        return stacks;
    }
}
