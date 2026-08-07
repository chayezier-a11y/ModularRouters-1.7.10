package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.*;
import net.minecraft.item.ItemStack;

public class StackAugment extends ItemAugment {
    public StackAugment() { super("stackAugment"); }

    @Override
    public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
        if (moduleClass == DetectorModule.class
                || moduleClass == ExtruderModule1.class || moduleClass == ExtruderModule2.class
                || moduleClass == BreakerModule.class || moduleClass == PlacerModule.class
                || moduleClass == FluidModule1.class || moduleClass == FluidModule2.class) {
            return 0;
        }
        return 6;
    }

    @Override
    public String getExtraInfo(int c, ItemStack stack) {
        return " - " + net.minecraft.util.StatCollector.translateToLocalFormatted("modularrouters.itemText.augments.stackInfo", Math.min(1 << c, 64));
    }
}
