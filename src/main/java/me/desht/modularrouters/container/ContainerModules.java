package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ContainerModules {
    public static ContainerModule createBreakerContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }

    public static ContainerModule createActivatorContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }

    public static ContainerModule createDetectorContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }

    public static ContainerModule createDistributorContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }

    public static ContainerModule createExtruder2Container(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }

    public static ContainerModule createFlingerContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }

    public static ContainerModule createFluidContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }

    public static ContainerModule createPlayerContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }

    public static ContainerModule createVacuumContainer(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, moduleStack, router);
    }
}
