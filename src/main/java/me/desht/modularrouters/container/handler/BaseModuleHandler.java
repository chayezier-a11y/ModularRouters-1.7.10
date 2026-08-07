package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BaseModuleHandler extends GhostItemHandler {
    private final ItemStack holderStack;
    private final String tagName;

    public BaseModuleHandler(ItemStack holderStack, int size, String tagName) {
        super(size);
        this.holderStack = holderStack;
        this.tagName = tagName;
        if (!holderStack.hasTagCompound()) {
            holderStack.setTagCompound(new NBTTagCompound());
        }
        if (holderStack.getTagCompound().hasKey(tagName)) {
            deserializeNBT(holderStack.getTagCompound().getTagList(tagName, 10));
        }
    }

    public ItemStack getHolderStack() {
        return holderStack;
    }

    public void save() {
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].stackSize <= 0) {
                items[i] = null;
            }
        }
        holderStack.getTagCompound().setTag(tagName, serializeNBT());
    }

    public static class ModuleFilterHandler extends BaseModuleHandler {
        public ModuleFilterHandler(ItemStack holderStack) {
            super(holderStack, 9, ModuleHelper.NBT_FILTER);
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            Module module = ItemModule.getModule(getHolderStack());
            if (module != null && module.isItemValidForFilter(stack)) {
                super.setInventorySlotContents(slot, stack);
            }
        }
    }
}
