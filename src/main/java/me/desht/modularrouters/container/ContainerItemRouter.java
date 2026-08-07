package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.slot.BufferSlot;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerItemRouter extends Container {
    public static final int BUFFER_SLOT = 0;
    public static final int MODULE_SLOT_START = 1;
    public static final int MODULE_SLOT_END = 9;
    public static final int UPGRADE_SLOT_START = 10;
    public static final int UPGRADE_SLOT_END = 14;

    private static final int SLOT_X_SPACING = 18;
    private static final int SLOT_Y_SPACING = 18;
    private static final int BUFFER_XPOS = 8;
    private static final int BUFFER_YPOS = 40;
    private static final int HOTBAR_XPOS = 8;
    private static final int HOTBAR_YPOS = 162;
    private static final int PLAYER_INVENTORY_XPOS = 8;
    private static final int PLAYER_INVENTORY_YPOS = 104;
    public static final int MODULE_XPOS = 8;
    private static final int MODULE_YPOS = 72;
    public static final int UPGRADE_XPOS = 80;
    private static final int UPGRADE_YPOS = 40;
    public static final int TE_FIRST_SLOT = 36;

    private final TileEntityItemRouter router;

    public ContainerItemRouter(EntityPlayer player, TileEntityItemRouter router) {
        this(player.inventory, router);
    }

    public ContainerItemRouter(InventoryPlayer invPlayer, TileEntityItemRouter router) {
        this.router = router;

        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(invPlayer, x, HOTBAR_XPOS + SLOT_X_SPACING * x, HOTBAR_YPOS));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                int slotNumber = 9 + y * 9 + x;
                int xpos = PLAYER_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = PLAYER_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlotToContainer(new Slot(invPlayer, slotNumber, xpos, ypos));
            }
        }

        addSlotToContainer(new BufferSlot(router.getBuffer(), BUFFER_SLOT, BUFFER_XPOS, BUFFER_YPOS));

        for (int slot = 0; slot < TileEntityItemRouter.N_MODULE_SLOTS; slot++) {
            addSlotToContainer(new ModuleSlot(router, slot, MODULE_XPOS + slot * SLOT_X_SPACING, MODULE_YPOS));
        }

        for (int slot = 0; slot < TileEntityItemRouter.N_UPGRADE_SLOTS; slot++) {
            addSlotToContainer(new UpgradeSlot(router, slot, UPGRADE_XPOS + slot * SLOT_X_SPACING, UPGRADE_YPOS));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return router.isUseableByPlayer(player) && router.isPermitted(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int sourceSlotIndex) {
        Slot sourceSlot = (Slot) inventorySlots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.getHasStack()) {
            return null;
        }

        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        int bufferSlot = TE_FIRST_SLOT + BUFFER_SLOT;
        int moduleStart = TE_FIRST_SLOT + MODULE_SLOT_START;
        int moduleEnd = TE_FIRST_SLOT + MODULE_SLOT_END + 1;
        int upgradeStart = TE_FIRST_SLOT + UPGRADE_SLOT_START;
        int upgradeEnd = TE_FIRST_SLOT + UPGRADE_SLOT_END + 1;

        if (sourceSlotIndex < TE_FIRST_SLOT) {
            if (sourceStack.getItem() instanceof ItemModule) {
                if (!mergeItemStack(sourceStack, moduleStart, moduleEnd, false)) {
                    return null;
                }
            } else if (sourceStack.getItem() instanceof ItemUpgrade) {
                if (!mergeItemStack(sourceStack, upgradeStart, upgradeEnd, false)) {
                    return null;
                }
            } else if (!mergeItemStack(sourceStack, bufferSlot, bufferSlot + 1, false)) {
                return null;
            }
        } else if (!mergeItemStack(sourceStack, 0, TE_FIRST_SLOT, false)) {
            return null;
        }

        if (sourceStack.stackSize == 0) {
            sourceSlot.putStack(null);
        } else {
            sourceSlot.onSlotChanged();
        }

        sourceSlot.onPickupFromSlot(player, sourceStack);
        return copyOfSourceStack;
    }

    private static class ModuleSlot extends Slot {
        ModuleSlot(TileEntityItemRouter router, int index, int x, int y) {
            super(router.getModules(), index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack != null && stack.getItem() instanceof ItemModule;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }

    private static class UpgradeSlot extends Slot {
        UpgradeSlot(TileEntityItemRouter router, int index, int x, int y) {
            super(router.getUpgrades(), index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack != null && stack.getItem() instanceof ItemUpgrade;
        }
    }
}
