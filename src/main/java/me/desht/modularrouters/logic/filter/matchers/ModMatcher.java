package me.desht.modularrouters.logic.filter.matchers;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.item.ItemStack;

public class ModMatcher implements IItemMatcher {
    private final String modId;

    public ModMatcher(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ModId")) {
            this.modId = stack.getTagCompound().getString("ModId");
        } else {
            this.modId = "";
        }
    }

    @Override
    public boolean match(ItemStack stack) {
        if (stack == null) return false;
        String itemModId = GameData.getItemRegistry().getNameForObject(stack.getItem()).split(":")[0];
        return itemModId.equalsIgnoreCase(modId);
    }

    @Override
    public ItemStack getStack() {
        return null;
    }
}
