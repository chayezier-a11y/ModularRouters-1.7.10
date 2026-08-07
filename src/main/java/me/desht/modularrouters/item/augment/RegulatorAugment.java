package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.DetectorModule;
import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;

public class RegulatorAugment extends ItemAugment {
    public RegulatorAugment() { super("regulatorAugment"); }

    @Override
    public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
        return moduleClass == DetectorModule.class || moduleClass == ExtruderModule2.class ? 0 : 1;
    }

    @Override
    public String getExtraInfo(int c, ItemStack moduleStack) {
        int amount = ModuleHelper.getRegulatorAmount(moduleStack);
        return " - " + net.minecraft.util.StatCollector.translateToLocalFormatted("modularrouters.itemText.regulator.amount", amount);
    }
}
