package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Container for the Mk2 extruder's ordered template buffer. */
public class ContainerExtruder2Module extends ContainerModule {
    public static final int TEMPLATE_SLOTS = 9;
    public static final int TEMPLATE_START = 9 + AugmentHandler.SLOTS + 36;

    private final TemplateHandler templateHandler;

    public ContainerExtruder2Module(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        super(player, moduleStack, router);
        templateHandler = new TemplateHandler(moduleStack, router);
        for (int i = 0; i < TEMPLATE_SLOTS; i++) {
            addSlotToContainer(new TemplateSlot(templateHandler, i,
                    129 + 18 * (i % 3), 17 + 18 * (i / 3)));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index >= TEMPLATE_START && index < TEMPLATE_START + TEMPLATE_SLOTS) {
            Slot slot = (Slot) inventorySlots.get(index);
            if (slot.getHasStack()) slot.putStack(null);
            return null;
        }
        return super.transferStackInSlot(player, index);
    }

    @Override
    public ItemStack slotClick(int slot, int button, int flag, EntityPlayer player) {
        if (slot >= TEMPLATE_START && slot < TEMPLATE_START + TEMPLATE_SLOTS) {
            TemplateSlot templateSlot = (TemplateSlot) inventorySlots.get(slot);
            ItemStack cursor = player.inventory.getItemStack();
            ItemStack existing = templateSlot.getStack();

            if (flag == 1) {
                templateSlot.putStack(null);
            } else if (cursor != null && (existing == null || !existing.isItemEqual(cursor))) {
                ItemStack copy = cursor.copy();
                if (button == 1) copy.stackSize = 1;
                templateSlot.putStack(copy);
            } else if (existing != null) {
                ItemStack copy = existing.copy();
                if (button == 1) {
                    copy.stackSize = Math.min(copy.getMaxStackSize(), copy.stackSize + 1);
                } else if (button == 0) {
                    copy.stackSize--;
                }
                templateSlot.putStack(copy.stackSize > 0 ? copy : null);
            }
            return null;
        }
        return super.slotClick(slot, button, flag, player);
    }

    private static final class TemplateSlot extends Slot {
        private final TemplateHandler handler;

        private TemplateSlot(TemplateHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
            this.handler = handler;
        }

        @Override
        public void putStack(ItemStack stack) {
            if (stack != null && !handler.isItemValidForSlot(getSlotIndex(), stack)) return;
            handler.setInventorySlotContents(getSlotIndex(), stack);
            handler.save();
            onSlotChanged();
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return handler.isItemValidForSlot(getSlotIndex(), stack);
        }

        @Override
        public int getSlotStackLimit() {
            return 64;
        }
    }

    public static class TemplateHandler extends BaseModuleHandler {
        private static final String NBT_TEMPLATE = "Template";

        public TemplateHandler(ItemStack holderStack, TileEntityItemRouter router) {
            super(holderStack, TEMPLATE_SLOTS, NBT_TEMPLATE);
        }

        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return true;
        }
    }
}
