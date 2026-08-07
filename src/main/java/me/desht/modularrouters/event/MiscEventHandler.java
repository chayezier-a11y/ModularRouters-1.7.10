package me.desht.modularrouters.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;

public class MiscEventHandler {

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.crafting;
        if (crafted == null) return;

        if (crafted.getItem() instanceof ItemModule) {
            for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
                ItemStack inputStack = event.craftMatrix.getStackInSlot(i);
                if (inputStack != null && inputStack.getItem() == crafted.getItem()) {
                    AugmentHandler h = new AugmentHandler(inputStack, null);
                    for (int j = 0; j < h.getSizeInventory(); j++) {
                        ItemStack augmentStack = h.getStackInSlot(j);
                        if (augmentStack != null) {
                            if (!event.player.inventory.addItemStackToInventory(augmentStack.copy())) {
                                event.player.dropPlayerItemWithRandomChoice(augmentStack.copy(), false);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
}
