package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;

public class RedstoneAugment extends ItemAugment {
    public RedstoneAugment() { super("redstoneAugment"); }

    @Override
    public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
        return 1;
    }

    @Override
    public String getExtraInfo(int c, ItemStack moduleStack) {
        RouterRedstoneBehaviour rrb = ModuleHelper.getRedstoneBehaviour(moduleStack);
        return " - " + net.minecraft.util.StatCollector.translateToLocal("modularrouters.guiText.tooltip.redstone." + rrb.toString());
    }
}
