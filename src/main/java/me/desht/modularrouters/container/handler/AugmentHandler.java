package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class AugmentHandler extends BaseModuleHandler {
    public static final int SLOTS = 4;
    private final TileEntityItemRouter router;

    public AugmentHandler(ItemStack holderStack, TileEntityItemRouter router) {
        super(holderStack, SLOTS, ModuleHelper.NBT_AUGMENTS);
        this.router = router;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemAugment)) return false;

        ItemAugment augment = (ItemAugment) stack.getItem();
        ItemModule.ModuleType type = ItemModule.ModuleType.getType(getHolderStack());
        if (type == null) return false;

        Class<? extends me.desht.modularrouters.item.module.Module> moduleClass =
            ItemModule.getModuleClass(type);
        if (augment.getMaxAugments(moduleClass) == 0) return false;

        for (int i = 0; i < getSizeInventory(); i++) {
            if (slot != i && getStackInSlot(i) != null && getStackInSlot(i).getItem() == stack.getItem()) return false;
        }

        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }
}

