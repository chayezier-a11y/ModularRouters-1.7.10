package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.container.slot.AugmentSlot;
import me.desht.modularrouters.container.slot.ModuleFilterSlot;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerModule extends Container {
    public static final int AUGMENT_START = Filter.FILTER_SIZE;
    private static final int INV_START = AUGMENT_START + AugmentHandler.SLOTS;
    private static final int INV_END = INV_START + 26;
    private static final int HOTBAR_START = INV_END + 1;
    private static final int HOTBAR_END = HOTBAR_START + 8;
    private static final int SLOT_SIZE = 18;
    private static final int PLAYER_INV_Y = 116;
    private static final int PLAYER_INV_X = 16;
    private static final int PLAYER_HOTBAR_Y = PLAYER_INV_Y + 58;

    private final ModuleFilterHandler filterHandler;
    private final AugmentHandler augmentHandler;
    private Runnable onContentsChanged;

    public void setOnFilterChanged(Runnable r) { this.onContentsChanged = r; }

    public ContainerModule(EntityPlayer player, ItemStack moduleStack, TileEntityItemRouter router) {
        this.filterHandler = new ModuleFilterHandler(moduleStack);
        this.augmentHandler = new AugmentHandler(moduleStack, router);

        for (int i = 0; i < Filter.FILTER_SIZE; i++) {
            addSlotToContainer(new ModuleFilterSlot(filterHandler, i,
                    8 + SLOT_SIZE * (i % 3), 17 + SLOT_SIZE * (i / 3)));
        }
        for (int i = 0; i < AugmentHandler.SLOTS; i++) {
            addSlotToContainer(new AugmentSlot(augmentHandler, i,
                    78 + SLOT_SIZE * (i % 2), 75 + SLOT_SIZE * (i / 2)));
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9,
                        PLAYER_INV_X + x * SLOT_SIZE, PLAYER_INV_Y + y * SLOT_SIZE));
            }
        }
        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(player.inventory, x,
                    PLAYER_INV_X + x * SLOT_SIZE, PLAYER_HOTBAR_Y));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) { return true; }

    private void contentsChanged() {
        if (onContentsChanged != null) onContentsChanged.run();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot src = (Slot) inventorySlots.get(index);
        if (src == null || !src.getHasStack()) return null;
        ItemStack source = src.getStack();

        if (index < Filter.FILTER_SIZE) {
            src.putStack(null);
            contentsChanged();
            return null;
        }
        if (index >= AUGMENT_START && index < INV_START) {
            if (!mergeItemStack(source, INV_START, HOTBAR_END + 1, false)) return null;
        } else if (index >= INV_START && index <= HOTBAR_END) {
            if (source.getItem() instanceof ItemAugment) {
                if (!mergeItemStack(source, AUGMENT_START, INV_START, false)) return null;
            } else {
                ItemStack copy = source.copy();
                copy.stackSize = 1;
                int firstFree = -1;
                for (int i = 0; i < Filter.FILTER_SIZE; i++) {
                    ItemStack existing = filterHandler.getStackInSlot(i);
                    if (firstFree < 0 && existing == null && filterHandler.isItemValidForSlot(i, copy)) {
                        firstFree = i;
                    }
                    if (existing != null && existing.isItemEqual(copy)) {
                        firstFree = i;
                        break;
                    }
                }
                if (firstFree < 0) return null;
                ((Slot) inventorySlots.get(firstFree)).putStack(copy);
                contentsChanged();
                return null;
            }
        }

        if (source.stackSize == 0) src.putStack(null);
        else src.onSlotChanged();
        contentsChanged();
        return null;
    }

    @Override
    public ItemStack slotClick(int slot, int button, int flag, EntityPlayer player) {
        if (slot >= 0 && slot < Filter.FILTER_SIZE) {
            Slot s = (Slot) inventorySlots.get(slot);
            ItemStack cursor = player.inventory.getItemStack();
            if (cursor != null) {
                ItemStack copy = cursor.copy();
                copy.stackSize = 1;
                s.putStack(copy);
            } else {
                s.putStack(null);
            }
            contentsChanged();
            return null;
        }
        ItemStack result = super.slotClick(slot, button, flag, player);
        if (slot >= AUGMENT_START && slot < INV_START) contentsChanged();
        return result;
    }
}
