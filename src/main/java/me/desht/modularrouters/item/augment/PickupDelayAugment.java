package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.DropperModule;
import net.minecraft.item.ItemStack;

public class PickupDelayAugment extends ItemAugment {
    public static final int TICKS_PER_AUGMENT = 10;

    public PickupDelayAugment() { super("pickupDelayAugment"); }

    @Override
    public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
        return DropperModule.class.isAssignableFrom(moduleClass) ? 20 : 0;
    }

    @Override
    public String getExtraInfo(int nAugments, ItemStack moduleStack) {
        int pickupDelay = nAugments * TICKS_PER_AUGMENT;
        return " - " + net.minecraft.util.StatCollector.translateToLocalFormatted("modularrouters.itemText.augments.pickupDelay", pickupDelay, pickupDelay / 20.0f);
    }
}
