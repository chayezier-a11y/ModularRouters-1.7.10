package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public abstract class SmartFilter {
    public abstract IItemMatcher compile(ItemStack filterStack);

    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack, ModuleTarget target) {
        return compile(filterStack);
    }

    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {}
}
