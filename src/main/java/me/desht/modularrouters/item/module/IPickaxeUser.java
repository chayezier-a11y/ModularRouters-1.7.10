package me.desht.modularrouters.item.module;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IPickaxeUser {
    String NBT_PICKAXE = "Pickaxe";

    default ItemStack getPickaxe(ItemStack moduleStack) {
        if (moduleStack != null && moduleStack.hasTagCompound()
                && moduleStack.getTagCompound().hasKey(NBT_PICKAXE)) {
            ItemStack stored = ItemStack.loadItemStackFromNBT(
                    moduleStack.getTagCompound().getCompoundTag(NBT_PICKAXE));
            if (stored != null) return stored;
        }
        return new ItemStack(Items.iron_pickaxe);
    }

    default ItemStack setPickaxe(ItemStack moduleStack, ItemStack pickaxeStack) {
        if (!moduleStack.hasTagCompound()) moduleStack.setTagCompound(new NBTTagCompound());
        NBTTagCompound pickaxeTag = new NBTTagCompound();
        pickaxeStack.writeToNBT(pickaxeTag);
        moduleStack.getTagCompound().setTag(NBT_PICKAXE, pickaxeTag);
        return moduleStack;
    }
}
