package me.desht.modularrouters.container.slot;

import me.desht.modularrouters.container.handler.AugmentHandler;
import net.minecraft.item.ItemStack;

public class AugmentSlot extends BaseModuleSlot {
    private final AugmentHandler handler;

    public AugmentSlot(AugmentHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.handler = handler;
    }

    @Override
    public void putStack(ItemStack stack) {
        super.putStack(stack);
        handler.save();
        onSlotChanged();
    }
}
