package me.desht.modularrouters.util;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Random;

public class InventoryUtils {
    private static final Random rand = new Random();

    public static void dropInventoryItems(World world, int x, int y, int z, IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) {
                spawnItemStack(world, x, y, z, stack);
                inventory.setInventorySlotContents(i, null);
            }
        }
    }

    public static void spawnItemStack(World world, double x, double y, double z, ItemStack stack) {
        float f = rand.nextFloat() * 0.8F + 0.1F;
        float f1 = rand.nextFloat() * 0.8F + 0.1F;
        float f2 = rand.nextFloat() * 0.8F + 0.1F;

        while (stack.stackSize > 0) {
            int j = rand.nextInt(21) + 10;
            if (j > stack.stackSize) j = stack.stackSize;
            stack.stackSize -= j;

            EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2,
                    new ItemStack(stack.getItem(), j, stack.getMetadata()));
            if (stack.hasTagCompound()) {
                entityitem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
            }

            float f3 = 0.05F;
            entityitem.motionX = rand.nextGaussian() * f3;
            entityitem.motionY = rand.nextGaussian() * f3 + 0.2;
            entityitem.motionZ = rand.nextGaussian() * f3;
            world.spawnEntityInWorld(entityitem);
        }
    }
}
