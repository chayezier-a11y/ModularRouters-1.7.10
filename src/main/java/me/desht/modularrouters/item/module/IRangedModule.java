package me.desht.modularrouters.item.module;

import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;

public interface IRangedModule {
    int getBaseRange();
    int getHardMaxRange();

    default int getCurrentRange(ItemStack stack) {
        return Math.max(1, Math.min(getHardMaxRange(), getBaseRange() + ModuleHelper.getRangeModifier(stack)));
    }

    default int getCurrentRange(int boost) {
        return Math.max(1, Math.min(getHardMaxRange(), getBaseRange() + boost));
    }
}
