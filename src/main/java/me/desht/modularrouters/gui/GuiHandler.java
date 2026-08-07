package me.desht.modularrouters.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerItemRouter;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.gui.module.GuiModuleActivator;
import me.desht.modularrouters.gui.module.GuiModuleDetector;
import me.desht.modularrouters.gui.module.GuiModuleDistributor;
import me.desht.modularrouters.gui.module.GuiModuleFlinger;
import me.desht.modularrouters.gui.module.GuiModuleFluid;
import me.desht.modularrouters.gui.module.GuiModuleExtruder2;
import me.desht.modularrouters.gui.module.GuiModulePlayer;
import me.desht.modularrouters.gui.module.GuiModuleVacuum;
import me.desht.modularrouters.gui.module.GuiModuleBreaker;
import me.desht.modularrouters.item.module.ActivatorModule;
import me.desht.modularrouters.item.module.DetectorModule;
import me.desht.modularrouters.item.module.DistributorModule;
import me.desht.modularrouters.item.module.FlingerModule;
import me.desht.modularrouters.item.module.FluidModule1;
import me.desht.modularrouters.item.module.FluidModule2;
import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.PlayerModule;
import me.desht.modularrouters.item.module.VacuumModule;
import me.desht.modularrouters.item.module.BreakerModule;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == ModularRouters.GUI_ROUTER) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
            if (router != null && router.isPermitted(player)) {
                return new ContainerItemRouter(player, router);
            }
        } else if (id == ModularRouters.GUI_MODULE_HELD_MAIN || id == ModularRouters.GUI_MODULE_HELD_OFF) {
            ItemStack stack = player.getHeldItem();
            Module module = ItemModule.getModule(stack);
            return module == null ? null : module.createGuiContainer(player, stack, null);
        } else if (id == ModularRouters.GUI_MODULE_INSTALLED) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
            if (router != null && router.isPermitted(player)) {
                int slot = router.getModuleConfigSlot(player);
                if (slot >= 0) {
                    ItemStack stack = router.getModules().getStackInSlot(slot);
                    Module module = ItemModule.getModule(stack);
                    return module == null ? null : module.createGuiContainer(player, stack, router);
                }
            }
        }
        return null;
    }

    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == ModularRouters.GUI_ROUTER) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
            if (router != null && router.isPermitted(player)) {
                return new GuiItemRouter(player, router);
            }
        } else if (id == ModularRouters.GUI_MODULE_HELD_MAIN || id == ModularRouters.GUI_MODULE_HELD_OFF) {
            ItemStack stack = player.getHeldItem();
            Module module = ItemModule.getModule(stack);
            if (module != null) {
                ContainerModule container = module.createGuiContainer(player, stack, null);
                return createClientModuleGui(module, container, player, stack, null);
            }
        } else if (id == ModularRouters.GUI_MODULE_INSTALLED) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, x, y, z);
            if (router != null && router.isPermitted(player)) {
                int slot = router.getModuleConfigSlot(player);
                if (slot >= 0) {
                    ItemStack stack = router.getModules().getStackInSlot(slot);
                    Module module = ItemModule.getModule(stack);
                    if (module != null) {
                        ContainerModule container = module.createGuiContainer(player, stack, router);
                        return createClientModuleGui(module, container, player, stack, router);
                    }
                }
            }
        }
        return null;
    }

    private GuiModule createClientModuleGui(Module module, ContainerModule container, EntityPlayer player,
                                             ItemStack stack, TileEntityItemRouter router) {
        if (module instanceof ActivatorModule) {
            return new GuiModuleActivator(container, player, stack, router);
        }
        if (module instanceof DetectorModule) {
            return new GuiModuleDetector(container, player, stack, router);
        }
        if (module instanceof DistributorModule) {
            return new GuiModuleDistributor(container, player, stack, router);
        }
        if (module instanceof FlingerModule) {
            return new GuiModuleFlinger(container, player, stack, router);
        }
        if (module instanceof FluidModule1 || module instanceof FluidModule2) {
            return new GuiModuleFluid(container, player, stack, router);
        }
        if (module instanceof ExtruderModule2) {
            return new GuiModuleExtruder2(container, player, stack, router);
        }
        if (module instanceof PlayerModule) {
            return new GuiModulePlayer(container, player, stack, router);
        }
        if (module instanceof VacuumModule) {
            return new GuiModuleVacuum(container, player, stack, router);
        }
        if (module instanceof BreakerModule) {
            return new GuiModuleBreaker(container, player, stack, router);
        }
        return new GuiModule(container, player, stack, router);
    }
}
